package com.zhravan.noechat.identity

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ActiveAlertStore(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val _activePublicId = MutableStateFlow(prefs.getString(KEY_ACTIVE_PUBLIC_ID, null))
    val activePublicId: StateFlow<String?> = _activePublicId.asStateFlow()

    fun setActivePublicId(publicId: String) {
        prefs.edit().putString(KEY_ACTIVE_PUBLIC_ID, publicId).apply()
        _activePublicId.value = publicId
    }

    fun clear() {
        prefs.edit().remove(KEY_ACTIVE_PUBLIC_ID).apply()
        _activePublicId.value = null
    }

    private companion object {
        const val PREFS = "noechat_active_alert"
        const val KEY_ACTIVE_PUBLIC_ID = "active_public_id"
    }
}
