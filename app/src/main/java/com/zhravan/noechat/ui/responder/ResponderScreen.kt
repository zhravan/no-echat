package com.zhravan.noechat.ui.responder

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponderScreen(onBack: () -> Unit) {
    val factory = rememberAppViewModelFactory()
    val vm: ResponderViewModel = viewModel(factory = factory)
    val packets by vm.packets.collectAsStateWithLifecycle()
    var filter by remember { mutableStateOf<EmergencyStatus?>(null) }
    var filterMenuExpanded by remember { mutableStateOf(false) }

    val visible = remember(packets, filter) {
        if (filter == null) packets else packets.filter { it.status == filter }
    }

    val filterLabel = filter?.let { UserCopy.emergencyStatus(it) } ?: "All"

    SimpleScreen(title = "Nearby alerts", onBack = onBack, scrollable = false) {
        LazyColumn(Modifier.fillMaxWidth()) {
            item {
                Text(
                    UserCopy.RESPONDER_INTRO,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                ExposedDropdownMenuBox(
                    expanded = filterMenuExpanded,
                    onExpandedChange = { filterMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = filterLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Show") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = filterMenuExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = filterMenuExpanded,
                        onDismissRequest = { filterMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All") },
                            onClick = {
                                filter = null
                                filterMenuExpanded = false
                            }
                        )
                        EmergencyStatus.entries.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(UserCopy.emergencyStatus(status)) },
                                onClick = {
                                    filter = status
                                    filterMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            if (packets.isEmpty()) {
                item {
                    Text(
                        "Nothing here yet — another phone needs to be in Bluetooth range.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (visible.isEmpty()) {
                item {
                    Text(
                        "Nothing matches this filter. Choose \"All\".",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
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
