package com.zhravan.noechat.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE emergency_packets ADD COLUMN acknowledged INTEGER NOT NULL DEFAULT 0"
        )
        db.execSQL(
            "ALTER TABLE emergency_packets ADD COLUMN pendingRelay INTEGER NOT NULL DEFAULT 0"
        )
        db.execSQL(
            "UPDATE emergency_packets SET pendingRelay = 1 WHERE origin = 'LOCAL'"
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE emergency_packets ADD COLUMN signingPublicKeySpkiB64 TEXT NOT NULL DEFAULT ''"
        )
    }
}
