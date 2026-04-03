package com.zhravan.noechat.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [EmergencyPacketEntity::class],
    version = 3,
    exportSchema = false
)
abstract class NoEchatDatabase : RoomDatabase() {
    abstract fun emergencyPacketDao(): EmergencyPacketDao

    companion object {
        fun build(context: Context): NoEchatDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                NoEchatDatabase::class.java,
                "noechat.db"
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .build()
    }
}
