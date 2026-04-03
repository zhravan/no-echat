package com.zhravan.noechat.domain

data class EmergencyPacket(
    val localId: Long,
    val publicId: String,
    val senderDeviceId: String,
    val signingPublicKeySpkiB64: String,
    val createdAtEpochMs: Long,
    val status: EmergencyStatus,
    val note: String?,
    val latitude: Double?,
    val longitude: Double?,
    val expiresAtEpochMs: Long,
    val hopCount: Int,
    val origin: String,
    val acknowledged: Boolean,
    val pendingRelay: Boolean
)
