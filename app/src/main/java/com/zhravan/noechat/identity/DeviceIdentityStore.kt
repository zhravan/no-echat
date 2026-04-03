package com.zhravan.noechat.identity

import android.content.Context
import java.util.UUID

class DeviceIdentityStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getOrCreateDeviceId(): String {
        val existing = prefs.getString(KEY_DEVICE_ID, null)
        if (existing != null) return existing
        val created = UUID.randomUUID().toString()
        prefs.edit().putString(KEY_DEVICE_ID, created).apply()
        return created
    }

    private companion object {
        const val PREFS = "noechat_identity"
        const val KEY_DEVICE_ID = "device_id"
    }
}
