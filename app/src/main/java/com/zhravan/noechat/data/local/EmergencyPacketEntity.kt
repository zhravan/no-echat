package com.zhravan.noechat.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "emergency_packets",
    indices = [
        Index(value = ["publicId"], unique = true),
        Index(value = ["createdAtEpochMs"])
    ]
)
data class EmergencyPacketEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val publicId: String,
    val senderDeviceId: String,
    val createdAtEpochMs: Long,
    val status: String,
    val note: String?,
    val latitude: Double?,
    val longitude: Double?,
    val expiresAtEpochMs: Long,
    val hopCount: Int,
    val signatureBytes: ByteArray?,
    val origin: String
)
