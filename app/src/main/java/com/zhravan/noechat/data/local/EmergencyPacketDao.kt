package com.zhravan.noechat.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyPacketDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: EmergencyPacketEntity)

    @Query("SELECT * FROM emergency_packets ORDER BY createdAtEpochMs DESC")
    fun observeAll(): Flow<List<EmergencyPacketEntity>>

    @Query("DELETE FROM emergency_packets WHERE expiresAtEpochMs < :nowEpochMs")
    suspend fun deleteExpired(nowEpochMs: Long)
}
