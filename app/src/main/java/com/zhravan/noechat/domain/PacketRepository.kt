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
}
