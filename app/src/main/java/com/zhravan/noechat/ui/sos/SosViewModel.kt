package com.zhravan.noechat.ui.sos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhravan.noechat.NoEchatApplication
import com.zhravan.noechat.domain.EmergencyStatus
import com.zhravan.noechat.location.FreshLocationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SosViewModel(
    private val application: NoEchatApplication
) : ViewModel() {

    private val repository = application.packetRepository
    private val activeAlertStore = application.activeAlertStore

    private val _lastPublicId = MutableStateFlow<String?>(null)
    val lastPublicId: StateFlow<String?> = _lastPublicId.asStateFlow()

    fun broadcast(status: EmergencyStatus, note: String?, includeLocation: Boolean) {
        viewModelScope.launch {
            val loc = withContext(Dispatchers.IO) {
                if (includeLocation) {
                    FreshLocationProvider.getLatLng(application)
                } else {
                    null
                }
            }
            val id = withContext(Dispatchers.IO) {
                repository.enqueueLocalSos(
                    status = status,
                    note = note,
                    latitude = loc?.first,
                    longitude = loc?.second
                )
            }
            activeAlertStore.setActivePublicId(id)
            _lastPublicId.value = id
        }
    }
}
