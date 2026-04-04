package com.zhravan.noechat.ui.updates

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zhravan.noechat.ui.common.SimpleScreen
import com.zhravan.noechat.ui.rememberAppViewModelFactory

@Composable
fun UpdatesScreen(onBack: () -> Unit) {
    val factory = rememberAppViewModelFactory()
    val vm: PacketsViewModel = viewModel(factory = factory)
    val packets by vm.packets.collectAsStateWithLifecycle()

    SimpleScreen(title = "Updates", onBack = onBack) {
        if (packets.isEmpty()) {
            Text("No packets yet.")
        } else {
            LazyColumn {
                items(packets, key = { it.publicId }) { packet ->
                    Text(packet.status.name)
                    packet.note?.let { Text(it) }
                    Text("Id: ${packet.publicId}")
                    Text("Hops: ${packet.hopCount}  ${packet.origin}")
                    if (packet.latitude != null && packet.longitude != null) {
                        Text("Loc: ${packet.latitude}, ${packet.longitude}")
                    }
                    Text("Relay pending: ${packet.pendingRelay}  Ack: ${packet.acknowledged}")
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}
