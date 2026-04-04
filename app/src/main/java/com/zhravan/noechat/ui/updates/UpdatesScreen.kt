package com.zhravan.noechat.ui.updates

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zhravan.noechat.ui.common.SimpleScreen
import com.zhravan.noechat.ui.copy.UserCopy
import com.zhravan.noechat.ui.rememberAppViewModelFactory

@Composable
fun UpdatesScreen(onBack: () -> Unit) {
    val factory = rememberAppViewModelFactory()
    val vm: PacketsViewModel = viewModel(factory = factory)
    val packets by vm.packets.collectAsStateWithLifecycle()

    SimpleScreen(title = "My alerts & activity", onBack = onBack) {
        Text(
            UserCopy.UPDATES_INTRO,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        if (packets.isEmpty()) {
            Text(
                "Nothing here yet. Sent alerts will show up after you use \"I need help\".",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn {
                items(packets, key = { it.publicId }) { packet ->
                    Text(
                        UserCopy.emergencyStatus(packet.status),
                        style = MaterialTheme.typography.titleSmall
                    )
                    packet.note?.let {
                        Text(it, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        "Reference: ${UserCopy.shortReference(packet.publicId)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${UserCopy.packetOrigin(packet.origin)} · about ${packet.hopCount} step(s)",
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
                        "Still trying to share to other phones: " +
                            if (packet.pendingRelay) "yes" else "no",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Marked as seen on this phone: " + if (packet.acknowledged) "yes" else "no",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}
