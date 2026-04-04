package com.zhravan.noechat.ui.responder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zhravan.noechat.domain.EmergencyStatus
import com.zhravan.noechat.ui.common.SimpleScreen
import com.zhravan.noechat.ui.copy.UserCopy
import com.zhravan.noechat.ui.rememberAppViewModelFactory

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResponderScreen(onBack: () -> Unit) {
    val factory = rememberAppViewModelFactory()
    val vm: ResponderViewModel = viewModel(factory = factory)
    val packets by vm.packets.collectAsStateWithLifecycle()
    var filter by remember { mutableStateOf<EmergencyStatus?>(null) }

    val visible = remember(packets, filter) {
        if (filter == null) packets else packets.filter { it.status == filter }
    }

    SimpleScreen(title = "Alerts from others nearby", onBack = onBack) {
        Text(
            UserCopy.RESPONDER_INTRO,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        Text("Show", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filter == null,
                onClick = { filter = null },
                label = { Text("All") }
            )
            EmergencyStatus.entries.forEach { status ->
                FilterChip(
                    selected = filter == status,
                    onClick = { filter = status },
                    label = { Text(UserCopy.emergencyStatus(status)) }
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        if (packets.isEmpty()) {
            Text(
                "No alerts heard nearby yet. They appear here when another phone is in Bluetooth range.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else if (visible.isEmpty()) {
            Text(
                "No alerts match this filter. Try \"All\" above.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn {
                items(visible, key = { it.publicId }) { packet ->
                    Text(
                        UserCopy.emergencyStatus(packet.status),
                        style = MaterialTheme.typography.titleSmall
                    )
                    packet.note?.let {
                        Text(it, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        "${UserCopy.packetOrigin(packet.origin)} · about ${packet.hopCount} step(s) from the sender",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (packet.latitude != null && packet.longitude != null) {
                        Text(
                            "Location shared: ${packet.latitude}, ${packet.longitude}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        if (packet.acknowledged) "You marked this as seen."
                        else "Not marked as seen yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!packet.acknowledged) {
                        Button(onClick = { vm.acknowledge(packet.publicId) }) {
                            Text("Mark as seen")
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
