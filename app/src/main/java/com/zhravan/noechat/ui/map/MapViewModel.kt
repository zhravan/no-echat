package com.zhravan.noechat.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhravan.noechat.domain.EmergencyPacket
import com.zhravan.noechat.domain.PacketRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MapViewModel(
    repository: PacketRepository
) : ViewModel() {

    val packetsWithLocation: StateFlow<List<EmergencyPacket>> = repository.observePackets()
        .map { list -> list.filter { it.latitude != null && it.longitude != null } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
}
