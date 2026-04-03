package com.zhravan.noechat.ui.responder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhravan.noechat.domain.EmergencyPacket
import com.zhravan.noechat.domain.PacketRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ResponderViewModel(
    private val repository: PacketRepository
) : ViewModel() {

    val packets: StateFlow<List<EmergencyPacket>> = repository.observePackets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun acknowledge(publicId: String) {
        viewModelScope.launch {
            repository.acknowledge(publicId, true)
        }
    }
}
