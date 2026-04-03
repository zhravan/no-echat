package com.zhravan.noechat.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onReadiness: () -> Unit,
    onSos: () -> Unit,
    onVolunteer: () -> Unit,
    onUpdates: () -> Unit,
    onResponder: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Mesh", style = MaterialTheme.typography.headlineSmall)
        Text("No peers", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
        Button(onClick = onSos, modifier = Modifier.fillMaxWidth()) {
            Text("SOS")
        }
        OutlinedButton(onClick = onReadiness, modifier = Modifier.fillMaxWidth()) {
            Text("Readiness")
        }
        OutlinedButton(onClick = onVolunteer, modifier = Modifier.fillMaxWidth()) {
            Text("Volunteer")
        }
        OutlinedButton(onClick = onResponder, modifier = Modifier.fillMaxWidth()) {
            Text("Responder")
        }
        OutlinedButton(onClick = onUpdates, modifier = Modifier.fillMaxWidth()) {
            Text("Updates")
        }
    }
}
