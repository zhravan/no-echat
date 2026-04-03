package com.zhravan.noechat.ui.volunteer

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import com.zhravan.noechat.ui.rememberAppViewModelFactory

@Composable
fun VolunteerScreen(onBack: () -> Unit) {
    val factory = rememberAppViewModelFactory()
    val vm: VolunteerViewModel = viewModel(factory = factory)
    val context = LocalContext.current
    val relayOn by vm.relayRunning.collectAsStateWithLifecycle()
    val peers by vm.peerCount.collectAsStateWithLifecycle()

    SimpleScreen(title = "Volunteer", onBack = onBack) {
        Text("Relay packets when the foreground service is on.")
        Spacer(Modifier.height(12.dp))
        Text("Peers: $peers")
        Spacer(Modifier.height(12.dp))
        Switch(
            checked = relayOn,
            onCheckedChange = { vm.setRelay(context, it) }
        )
        Spacer(Modifier.height(8.dp))
        Text(if (relayOn) "Relay on" else "Relay off")
    }
}
