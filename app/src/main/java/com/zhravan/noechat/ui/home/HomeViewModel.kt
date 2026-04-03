package com.zhravan.noechat.ui.home

import androidx.lifecycle.ViewModel
import com.zhravan.noechat.NoEchatApplication
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel(application: NoEchatApplication) : ViewModel() {
    val peerCount: StateFlow<Int> = application.meshCoordinator.peerCount
    val relayRunning: StateFlow<Boolean> = application.relayRunning
}
