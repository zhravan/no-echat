package com.zhravan.noechat.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhravan.noechat.NoEchatApplication
import com.zhravan.noechat.domain.EmergencyStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val application: NoEchatApplication) : ViewModel() {
    val peerCount: StateFlow<Int> = application.meshCoordinator.peerCount
    val relayRunning: StateFlow<Boolean> = application.relayRunning

    private val _lastSafeId = MutableStateFlow<String?>(null)
    val lastSafeId: StateFlow<String?> = _lastSafeId.asStateFlow()

    fun broadcastSafe() {
        viewModelScope.launch {
            val id = application.packetRepository.enqueueLocalSos(
                status = EmergencyStatus.SAFE,
                note = null,
                latitude = null,
                longitude = null
            )
            _lastSafeId.value = id
        }
    }
}
