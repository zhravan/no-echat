package com.zhravan.noechat.ui.sos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import com.zhravan.noechat.ui.rememberAppViewModelFactory

@Composable
fun SosScreen(onBack: () -> Unit) {
    val factory = rememberAppViewModelFactory()
    val vm: SosViewModel = viewModel(factory = factory)
    val lastId by vm.lastPublicId.collectAsStateWithLifecycle()

    var status by remember { mutableStateOf(EmergencyStatus.TRAPPED) }
    var note by remember { mutableStateOf("") }
    var includeLocation by remember { mutableStateOf(false) }

    val statusOptions = remember {
        EmergencyStatus.entries.filter { it != EmergencyStatus.SAFE }
    }

    SimpleScreen(title = "SOS", onBack = onBack) {
        Text("Status")
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(statusOptions) { item ->
                FilterChip(
                    selected = status == item,
                    onClick = { status = item },
                    label = { Text(item.name) }
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("Last known location")
        Switch(
            checked = includeLocation,
            onCheckedChange = { includeLocation = it }
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Note") },
            singleLine = false,
            maxLines = 4
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                vm.broadcast(
                    status,
                    note.trim().ifEmpty { null },
                    includeLocation
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Broadcast")
        }
        lastId?.let { id ->
            Spacer(Modifier.height(12.dp))
            Text("Saved: $id")
        }
    }
}
