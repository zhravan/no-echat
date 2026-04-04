package com.zhravan.noechat.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhravan.noechat.NoEchatApplication
import com.zhravan.noechat.domain.EmergencyStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(private val application: NoEchatApplication) : ViewModel() {
    val peerCount: StateFlow<Int> = application.meshCoordinator.peerCount
    val relayRunning: StateFlow<Boolean> = application.relayRunning
    val activePublicId: StateFlow<String?> = application.activeAlertStore.activePublicId

    private val _lastSafeId = MutableStateFlow<String?>(null)
    val lastSafeId: StateFlow<String?> = _lastSafeId.asStateFlow()

    fun broadcastSafe() {
        viewModelScope.launch {
            application.activeAlertStore.clear()
            val id = withContext(Dispatchers.IO) {
                application.packetRepository.enqueueLocalSos(
                    status = EmergencyStatus.SAFE,
                    note = null,
                    latitude = null,
                    longitude = null
                )
            }
            _lastSafeId.value = id
        }
    }

    fun stopActiveRelay() {
        viewModelScope.launch {
            val id = application.activeAlertStore.activePublicId.value ?: return@launch
            withContext(Dispatchers.IO) {
                application.packetRepository.markRelayedOut(id)
            }
            application.activeAlertStore.clear()
        }
    }
}
