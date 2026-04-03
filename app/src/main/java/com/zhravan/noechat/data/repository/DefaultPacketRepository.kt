package com.zhravan.noechat.data.repository

import android.util.Base64
import com.zhravan.noechat.data.local.EmergencyPacketDao
import com.zhravan.noechat.data.local.EmergencyPacketEntity
import com.zhravan.noechat.data.local.PacketOrigin
import com.zhravan.noechat.domain.EmergencyPacket
import com.zhravan.noechat.domain.EmergencyStatus
import com.zhravan.noechat.domain.IngestResult
import com.zhravan.noechat.domain.PacketRepository
import com.zhravan.noechat.identity.DeviceIdentityStore
import com.zhravan.noechat.mesh.MeshConstants
import com.zhravan.noechat.mesh.crypto.PacketSigner
import com.zhravan.noechat.mesh.wire.WirePacket
import com.zhravan.noechat.mesh.wire.WirePacketCodec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class DefaultPacketRepository(
    private val dao: EmergencyPacketDao,
    private val identity: DeviceIdentityStore,
    private val signer: PacketSigner,
    private val ttlMs: Long = DEFAULT_TTL_MS
) : PacketRepository {

    override suspend fun enqueueLocalSos(
        status: EmergencyStatus,
        note: String?,
        latitude: Double?,
        longitude: Double?
    ): String = withContext(Dispatchers.IO) {
        val senderId = identity.getOrCreateDeviceId()
        val publicId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val expires = now + ttlMs
        val spki = signer.publicKeySpkiB64
        val trimmedNote = note?.take(MAX_NOTE_CHARS)?.trim()?.ifEmpty { null }
        val wireUnsigned = WirePacket(
            version = WIRE_VERSION,
            publicId = publicId,
            senderDeviceId = senderId,
            signingPublicKeySpkiB64 = spki,
            createdAtEpochMs = now,
            status = status.name,
            note = trimmedNote,
            latitude = latitude,
            longitude = longitude,
            expiresAtEpochMs = expires,
            hopCount = 0,
            origin = PacketOrigin.LOCAL,
            signatureB64 = ""
        )
        val signature = signer.sign(wireUnsigned.bytesForSigning())
        val sigB64 = Base64.encodeToString(signature, Base64.NO_WRAP)
        val entity = EmergencyPacketEntity(
            publicId = publicId,
            senderDeviceId = senderId,
            signingPublicKeySpkiB64 = spki,
            createdAtEpochMs = now,
            status = status.name,
            note = trimmedNote,
            latitude = latitude,
            longitude = longitude,
            expiresAtEpochMs = expires,
            hopCount = 0,
            signatureBytes = signature,
            origin = PacketOrigin.LOCAL,
            acknowledged = false,
            pendingRelay = true
        )
        dao.insert(entity)
        dao.deleteExpired(now)
        publicId
    }

    override fun observePackets(): Flow<List<EmergencyPacket>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observePendingRelay(): Flow<List<EmergencyPacket>> =
        dao.observePendingRelay().map { entities -> entities.map { it.toDomain() } }

    override suspend fun acknowledge(publicId: String, acknowledged: Boolean) {
        withContext(Dispatchers.IO) {
            dao.setAcknowledged(publicId, acknowledged)
        }
    }

    override suspend fun markRelayedOut(publicId: String) {
        withContext(Dispatchers.IO) {
            dao.clearPendingRelay(publicId)
        }
    }

    override suspend fun ingestFromWire(bytes: ByteArray): IngestResult = withContext(Dispatchers.IO) {
        val wire = runCatching { WirePacketCodec.decode(bytes) }.getOrElse { return@withContext IngestResult.INVALID }
        val now = System.currentTimeMillis()
        if (now > wire.expiresAtEpochMs) return@withContext IngestResult.EXPIRED
        if (wire.hopCount >= MeshConstants.MAX_HOPS) return@withContext IngestResult.INVALID
        val nextHop = wire.hopCount + 1
        val sigBytes = runCatching { WirePacketCodec.decodeSignature(wire.signatureB64) }
            .getOrElse { return@withContext IngestResult.INVALID }
        if (!signer.verify(wire.bytesForSigning(), sigBytes, wire.signingPublicKeySpkiB64)) {
            return@withContext IngestResult.INVALID
        }
        val entity = EmergencyPacketEntity(
            publicId = wire.publicId,
            senderDeviceId = wire.senderDeviceId,
            signingPublicKeySpkiB64 = wire.signingPublicKeySpkiB64,
            createdAtEpochMs = wire.createdAtEpochMs,
            status = wire.status,
            note = wire.note,
            latitude = wire.latitude,
            longitude = wire.longitude,
            expiresAtEpochMs = wire.expiresAtEpochMs,
            hopCount = nextHop,
            signatureBytes = sigBytes,
            origin = PacketOrigin.RELAY,
            acknowledged = false,
            pendingRelay = nextHop < MeshConstants.MAX_HOPS
        )
        val inserted = dao.insertIfAbsent(entity)
        if (!inserted) IngestResult.DUPLICATE else IngestResult.NEW
    }

    override suspend fun encodeForRelay(publicId: String): ByteArray? = withContext(Dispatchers.IO) {
        val entity = dao.getByPublicId(publicId) ?: return@withContext null
        entityToWireBytes(entity)
    }

    private fun entityToWireBytes(entity: EmergencyPacketEntity): ByteArray? {
        val sig = entity.signatureBytes ?: return null
        if (entity.signingPublicKeySpkiB64.isEmpty()) return null
        val wire = WirePacket(
            version = WIRE_VERSION,
            publicId = entity.publicId,
            senderDeviceId = entity.senderDeviceId,
            signingPublicKeySpkiB64 = entity.signingPublicKeySpkiB64,
            createdAtEpochMs = entity.createdAtEpochMs,
            status = entity.status,
            note = entity.note,
            latitude = entity.latitude,
            longitude = entity.longitude,
            expiresAtEpochMs = entity.expiresAtEpochMs,
            hopCount = entity.hopCount,
            origin = entity.origin,
            signatureB64 = Base64.encodeToString(sig, Base64.NO_WRAP)
        )
        return WirePacketCodec.encode(wire)
    }

    private fun EmergencyPacketEntity.toDomain(): EmergencyPacket = EmergencyPacket(
        localId = localId,
        publicId = publicId,
        senderDeviceId = senderDeviceId,
        signingPublicKeySpkiB64 = signingPublicKeySpkiB64,
        createdAtEpochMs = createdAtEpochMs,
        status = parseStatus(status),
        note = note,
        latitude = latitude,
        longitude = longitude,
        expiresAtEpochMs = expiresAtEpochMs,
        hopCount = hopCount,
        origin = origin,
        acknowledged = acknowledged,
        pendingRelay = pendingRelay
    )

    private companion object {
        const val DEFAULT_TTL_MS = 48L * 60L * 60L * 1000L
        const val MAX_NOTE_CHARS = 280
        const val WIRE_VERSION = 1

        private fun parseStatus(raw: String): EmergencyStatus =
            runCatching { EmergencyStatus.valueOf(raw) }.getOrDefault(EmergencyStatus.OTHER)
    }
}
