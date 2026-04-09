package com.zhravan.noechat.ui.sos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
fun SosScreen(onBack: () -> Unit) {
    val factory = rememberAppViewModelFactory()
    val vm: SosViewModel = viewModel(factory = factory)
    val lastId by vm.lastPublicId.collectAsStateWithLifecycle()

    var status by remember { mutableStateOf(EmergencyStatus.TRAPPED) }
    var note by remember { mutableStateOf("") }
    var includeLocation by remember { mutableStateOf(false) }
    var typeMenuExpanded by remember { mutableStateOf(false) }

    val statusOptions = remember {
        EmergencyStatus.entries.filter { it != EmergencyStatus.SAFE }
    }

    SimpleScreen(title = "I need help", onBack = onBack) {
        Text(
            UserCopy.SOS_INTRO,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        ExposedDropdownMenuBox(
            expanded = typeMenuExpanded,
            onExpandedChange = { typeMenuExpanded = it }
        ) {
            OutlinedTextField(
                value = UserCopy.emergencyStatus(status),
                onValueChange = {},
                readOnly = true,
                label = { Text("Situation") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = typeMenuExpanded,
                onDismissRequest = { typeMenuExpanded = false }
            ) {
                statusOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(UserCopy.emergencyStatus(option)) },
                        onClick = {
                            status = option
                            typeMenuExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text("Approximate location", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Last known fix only, not live tracking.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = includeLocation,
                onCheckedChange = { includeLocation = it }
            )
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Note (optional)") },
            placeholder = { Text("What you need") },
            singleLine = false,
            maxLines = 4
        )
        Spacer(Modifier.height(16.dp))
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
            Text("Send alert")
        }
        lastId?.let { id ->
            Spacer(Modifier.height(12.dp))
            Text(
                "Saved · ${UserCopy.shortReference(id)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
