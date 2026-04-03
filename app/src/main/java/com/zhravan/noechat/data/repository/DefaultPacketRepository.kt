package com.zhravan.noechat.data.repository

import com.zhravan.noechat.data.local.EmergencyPacketDao
import com.zhravan.noechat.data.local.EmergencyPacketEntity
import com.zhravan.noechat.data.local.PacketOrigin
import com.zhravan.noechat.domain.EmergencyPacket
import com.zhravan.noechat.domain.EmergencyStatus
import com.zhravan.noechat.domain.PacketRepository
import com.zhravan.noechat.identity.DeviceIdentityStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class DefaultPacketRepository(
    private val dao: EmergencyPacketDao,
    private val identity: DeviceIdentityStore,
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
        val entity = EmergencyPacketEntity(
            publicId = publicId,
            senderDeviceId = senderId,
            createdAtEpochMs = now,
            status = status.name,
            note = note?.take(MAX_NOTE_CHARS)?.trim()?.ifEmpty { null },
            latitude = latitude,
            longitude = longitude,
            expiresAtEpochMs = now + ttlMs,
            hopCount = 0,
            signatureBytes = null,
            origin = PacketOrigin.LOCAL
        )
        dao.insert(entity)
        dao.deleteExpired(now)
        publicId
    }

    override fun observePackets(): Flow<List<EmergencyPacket>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    private fun EmergencyPacketEntity.toDomain(): EmergencyPacket = EmergencyPacket(
        localId = localId,
        publicId = publicId,
        senderDeviceId = senderDeviceId,
        createdAtEpochMs = createdAtEpochMs,
        status = parseStatus(status),
        note = note,
        latitude = latitude,
        longitude = longitude,
        expiresAtEpochMs = expiresAtEpochMs,
        hopCount = hopCount,
        origin = origin
    )

    private companion object {
        const val DEFAULT_TTL_MS = 48L * 60L * 60L * 1000L
        const val MAX_NOTE_CHARS = 280

        private fun parseStatus(raw: String): EmergencyStatus =
            runCatching { EmergencyStatus.valueOf(raw) }.getOrDefault(EmergencyStatus.OTHER)
    }
}
