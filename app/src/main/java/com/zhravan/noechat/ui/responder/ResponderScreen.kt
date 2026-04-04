package com.zhravan.noechat.ui.responder

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zhravan.noechat.domain.EmergencyStatus
import com.zhravan.noechat.navigation.TurnByTurnNavigation
import com.zhravan.noechat.ui.common.SimpleScreen
import com.zhravan.noechat.ui.rememberAppViewModelFactory

@Composable
fun ResponderScreen(
    onBack: () -> Unit,
    onOpenMap: () -> Unit = {}
) {
    val factory = rememberAppViewModelFactory()
    val vm: ResponderViewModel = viewModel(factory = factory)
    val packets by vm.packets.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var filter by remember { mutableStateOf<EmergencyStatus?>(null) }

    val visible = remember(packets, filter) {
        if (filter == null) packets else packets.filter { it.status == filter }
    }

    SimpleScreen(title = "Responder", onBack = onBack) {
        Button(onClick = onOpenMap, modifier = Modifier.fillMaxWidth()) {
            Text("Open map")
        }
        Spacer(Modifier.height(12.dp))
        Text("Filter")
        Spacer(Modifier.height(8.dp))
        FilterChip(
            selected = filter == null,
            onClick = { filter = null },
            label = { Text("All") }
        )
        Spacer(Modifier.height(8.dp))
        EmergencyStatus.entries.forEach { status ->
            FilterChip(
                selected = filter == status,
                onClick = { filter = status },
                label = { Text(status.name) }
            )
            Spacer(Modifier.height(4.dp))
        }
        Spacer(Modifier.height(12.dp))
        if (visible.isEmpty()) {
            Text("No alerts.")
        } else {
            LazyColumn {
                items(visible, key = { it.publicId }) { packet ->
                    Text(packet.status.name)
                    packet.note?.let { Text(it) }
                    Text("Hops: ${packet.hopCount}  ${packet.origin}")
                    if (packet.latitude != null && packet.longitude != null) {
                        Text("Loc: ${packet.latitude}, ${packet.longitude}")
                        Button(
                            onClick = {
                                TurnByTurnNavigation.open(
                                    context,
                                    packet.latitude!!,
                                    packet.longitude!!
                                )
                            }
                        ) {
                            Text("Navigate")
                        }
                    }
                    Text("Ack: ${packet.acknowledged}")
                    if (!packet.acknowledged) {
                        Button(onClick = { vm.acknowledge(packet.publicId) }) {
                            Text("Acknowledge")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}
