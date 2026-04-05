package com.zhravan.noechat.ui.readiness

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.zhravan.noechat.ui.common.SimpleScreen
import com.zhravan.noechat.ui.copy.UserCopy

@Composable
fun ReadinessScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    val corePermissions = remember {
        buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_CONNECT)
                add(Manifest.permission.BLUETOOTH_ADVERTISE)
            } else {
                add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }.toTypedArray()
    }

    val locationOptional = remember {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    val coreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    val showOptionalLocation = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    SimpleScreen(title = "Setup & permissions", onBack = onBack) {
        Text(
            UserCopy.READINESS_INTRO,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Text("Step 1: required", style = MaterialTheme.typography.titleMedium)
        Text(
            "Bluetooth lets this app reach nearby phones. Notifications tell you when the app is helping in the background.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = { coreLauncher.launch(corePermissions) }) {
            Text(UserCopy.READINESS_CORE_BUTTON)
        }
        if (showOptionalLocation) {
            Spacer(Modifier.height(20.dp))
            Text("Step 2: optional", style = MaterialTheme.typography.titleMedium)
            Text(
                UserCopy.READINESS_LOCATION_INTRO,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = { locationLauncher.launch(locationOptional) }) {
                Text(UserCopy.READINESS_LOCATION_BUTTON)
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("What's allowed now", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        val allToShow = remember {
            buildList {
                addAll(corePermissions.toList())
                if (showOptionalLocation) add(Manifest.permission.ACCESS_FINE_LOCATION)
            }.distinct()
        }
        allToShow.forEach { permission ->
            val granted = ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
            Text(
                "${UserCopy.permissionShortLabel(permission)}: ${UserCopy.yesNo(granted)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(4.dp))
        }
    }
}
