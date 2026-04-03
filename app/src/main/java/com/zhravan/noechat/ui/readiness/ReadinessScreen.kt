package com.zhravan.noechat.ui.readiness

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.zhravan.noechat.ui.common.SimpleScreen

@Composable
fun ReadinessScreen(onBack: () -> Unit) {
    SimpleScreen(title = "Readiness", onBack = onBack) {
        Text("Permissions and radios.")
    }
}
