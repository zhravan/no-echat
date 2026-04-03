package com.zhravan.noechat.ui.sos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhravan.noechat.domain.EmergencyStatus
import com.zhravan.noechat.domain.PacketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SosViewModel(
    private val repository: PacketRepository
) : ViewModel() {

    private val _lastPublicId = MutableStateFlow<String?>(null)
    val lastPublicId: StateFlow<String?> = _lastPublicId.asStateFlow()

    fun broadcast(status: EmergencyStatus, note: String?) {
        viewModelScope.launch {
            val id = repository.enqueueLocalSos(
                status = status,
                note = note,
                latitude = null,
                longitude = null
            )
            _lastPublicId.value = id
        }
    }
}
