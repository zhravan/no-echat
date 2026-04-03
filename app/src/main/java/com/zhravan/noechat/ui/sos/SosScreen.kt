package com.zhravan.noechat.ui.sos

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.zhravan.noechat.ui.common.SimpleScreen

@Composable
fun SosScreen(onBack: () -> Unit) {
    SimpleScreen(title = "SOS", onBack = onBack) {
        Text("Broadcast distress.")
    }
}
