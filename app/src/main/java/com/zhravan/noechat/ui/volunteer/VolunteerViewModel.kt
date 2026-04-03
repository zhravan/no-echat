package com.zhravan.noechat.ui.volunteer

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.zhravan.noechat.NoEchatApplication
import com.zhravan.noechat.mesh.RelayForegroundService
import kotlinx.coroutines.flow.StateFlow

class VolunteerViewModel(application: NoEchatApplication) : ViewModel() {
    val peerCount: StateFlow<Int> = application.meshCoordinator.peerCount
    val relayRunning: StateFlow<Boolean> = application.relayRunning

    fun setRelay(context: Context, enabled: Boolean) {
        val appContext = context.applicationContext
        val intent = Intent(appContext, RelayForegroundService::class.java)
        if (enabled) {
            ContextCompat.startForegroundService(appContext, intent)
        } else {
            appContext.stopService(intent)
        }
    }
}
