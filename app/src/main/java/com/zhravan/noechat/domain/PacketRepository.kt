package com.zhravan.noechat.domain

import kotlinx.coroutines.flow.Flow

interface PacketRepository {
    suspend fun enqueueLocalSos(
        status: EmergencyStatus,
        note: String?,
        latitude: Double?,
        longitude: Double?
    ): String

    fun observePackets(): Flow<List<EmergencyPacket>>

    fun observePendingRelay(): Flow<List<EmergencyPacket>>

    suspend fun acknowledge(publicId: String, acknowledged: Boolean)

    suspend fun markRelayedOut(publicId: String)

    suspend fun ingestFromWire(bytes: ByteArray): IngestResult

    suspend fun encodeForRelay(publicId: String): ByteArray?

    suspend fun purgeExpired()
}
