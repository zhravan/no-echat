package com.zhravan.noechat.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zhravan.noechat.ui.copy.UserCopy
import com.zhravan.noechat.ui.rememberAppViewModelFactory

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Home", style = MaterialTheme.typography.headlineSmall)
        Text(
            UserCopy.HOME_TAGLINE,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Status", style = MaterialTheme.typography.titleMedium)
                Text(
                    UserCopy.nearbyPhonesLine(peerCount),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    UserCopy.relayLine(relayOn),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        activePublicId?.let { id ->
            Text(
                "You are passing along someone else's alert (${UserCopy.shortReference(id)}).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(
                onClick = { vm.stopActiveRelay() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Stop passing this alert")
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            UserCopy.HOME_SOS_HINT,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(onClick = onSos, modifier = Modifier.fillMaxWidth()) {
            Text("I need help (SOS)")
        }
        OutlinedButton(
            onClick = { vm.broadcastSafe() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("I'm safe, tell others")
        }
        lastSafeId?.let { id ->
            Text(
                "Safe message sent. Reference: ${UserCopy.shortReference(id)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        OutlinedButton(onClick = onReadiness, modifier = Modifier.fillMaxWidth()) {
            Text("Setup & permissions")
        }
        OutlinedButton(onClick = onVolunteer, modifier = Modifier.fillMaxWidth()) {
            Text("Help pass alerts for others")
        }
        OutlinedButton(onClick = onResponder, modifier = Modifier.fillMaxWidth()) {
            Text("Alerts from others nearby")
        }
        OutlinedButton(onClick = onUpdates, modifier = Modifier.fillMaxWidth()) {
            Text("My alerts & activity")
        }
    }
}
