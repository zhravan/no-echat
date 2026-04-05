package com.zhravan.noechat.ui.volunteer

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zhravan.noechat.ui.common.SimpleScreen
import com.zhravan.noechat.ui.copy.UserCopy
import com.zhravan.noechat.ui.rememberAppViewModelFactory

@Composable
fun VolunteerScreen(onBack: () -> Unit) {
    val factory = rememberAppViewModelFactory()
    val vm: VolunteerViewModel = viewModel(factory = factory)
    val context = LocalContext.current
    val relayOn by vm.relayRunning.collectAsStateWithLifecycle()
    val peers by vm.peerCount.collectAsStateWithLifecycle()
    val stats by vm.relayStats.collectAsStateWithLifecycle()

    SimpleScreen(title = "Help pass alerts", onBack = onBack) {
        Text(
            UserCopy.VOLUNTEER_INTRO,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Text(UserCopy.nearbyPhonesLine(peers), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            if (stats.timestampEpochMs == 0L) {
                "No recent attempts yet. Turn the switch on when you want to help."
            } else {
                "Last attempt: reached ${stats.successes} of ${stats.peersTried} nearby phones."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Text("Pass alerts for others", style = MaterialTheme.typography.titleMedium)
        Switch(
            checked = relayOn,
            onCheckedChange = { vm.setRelay(context, it) }
        )
        Text(
            if (relayOn) "On: you may see a small ongoing notification while this runs."
            else "Off: your phone will not forward alerts for others.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
