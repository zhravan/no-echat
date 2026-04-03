package com.zhravan.noechat.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyPacketDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: EmergencyPacketEntity)

    @Query("SELECT * FROM emergency_packets ORDER BY createdAtEpochMs DESC")
    fun observeAll(): Flow<List<EmergencyPacketEntity>>

    @Query(
        "SELECT * FROM emergency_packets WHERE pendingRelay = 1 ORDER BY createdAtEpochMs ASC"
    )
    fun observePendingRelay(): Flow<List<EmergencyPacketEntity>>

    @Query("UPDATE emergency_packets SET pendingRelay = 0 WHERE publicId = :publicId")
    suspend fun clearPendingRelay(publicId: String)

    @Query("UPDATE emergency_packets SET acknowledged = :value WHERE publicId = :publicId")
    suspend fun setAcknowledged(publicId: String, value: Boolean)

    @Query("DELETE FROM emergency_packets WHERE expiresAtEpochMs < :nowEpochMs")
    suspend fun deleteExpired(nowEpochMs: Long)

    @Query("SELECT * FROM emergency_packets WHERE publicId = :publicId LIMIT 1")
    suspend fun getByPublicId(publicId: String): EmergencyPacketEntity?

    @Transaction
    suspend fun insertIfAbsent(entity: EmergencyPacketEntity): Boolean {
        if (getByPublicId(entity.publicId) != null) return false
        return try {
            insert(entity)
            true
        } catch (_: Exception) {
            false
        }
    }
}
