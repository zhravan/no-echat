package com.zhravan.noechat.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zhravan.noechat.ui.copy.UserCopy
import com.zhravan.noechat.ui.rememberAppViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onReadiness: () -> Unit,
    onSos: () -> Unit,
    onVolunteer: () -> Unit,
    onUpdates: () -> Unit,
    onResponder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val factory = rememberAppViewModelFactory()
    val vm: HomeViewModel = viewModel(factory = factory)
    val peerCount by vm.peerCount.collectAsStateWithLifecycle()
    val relayOn by vm.relayRunning.collectAsStateWithLifecycle()
    val lastSafeId by vm.lastSafeId.collectAsStateWithLifecycle()
    val activePublicId by vm.activePublicId.collectAsStateWithLifecycle()

    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("NoEchat") },
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                Icons.Filled.MoreVert,
                                contentDescription = "More"
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Setup & permissions") },
                                onClick = {
                                    menuExpanded = false
                                    onReadiness()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Forward alerts for others") },
                                onClick = {
                                    menuExpanded = false
                                    onVolunteer()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Alerts from nearby") },
                                onClick = {
                                    menuExpanded = false
                                    onResponder()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("My alerts") },
                                onClick = {
                                    menuExpanded = false
                                    onUpdates()
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                UserCopy.HOME_TAGLINE,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                UserCopy.homeStatusLine(peerCount, relayOn),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            activePublicId?.let { id ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Passing an alert (${UserCopy.shortReference(id)})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = { vm.stopActiveRelay() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Stop forwarding")
                    }
                }
            }
            Button(onClick = onSos, modifier = Modifier.fillMaxWidth()) {
                Text("I need help")
            }
            OutlinedButton(
                onClick = { vm.broadcastSafe() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("I'm safe")
            }
            lastSafeId?.let { id ->
                Text(
                    "Safe sent · ${UserCopy.shortReference(id)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
