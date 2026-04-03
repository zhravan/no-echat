package com.zhravan.noechat.ui.readiness

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.zhravan.noechat.ui.common.SimpleScreen

@Composable
fun ReadinessScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val permissions = remember {
        buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_CONNECT)
                add(Manifest.permission.BLUETOOTH_ADVERTISE)
            }
            add(Manifest.permission.ACCESS_FINE_LOCATION)
        }.toTypedArray()
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    SimpleScreen(title = "Readiness", onBack = onBack) {
        Button(onClick = { launcher.launch(permissions) }) {
            Text("Request permissions")
        }
        Spacer(Modifier.height(16.dp))
        permissions.forEach { permission ->
            val granted = ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
            Text("${permission.substringAfterLast('.')}: ${if (granted) "granted" else "denied"}")
            Spacer(Modifier.height(4.dp))
        }
    }
}
