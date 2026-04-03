package com.zhravan.noechat.ui.responder

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.zhravan.noechat.ui.common.SimpleScreen

@Composable
fun ResponderScreen(onBack: () -> Unit) {
    SimpleScreen(title = "Responder", onBack = onBack) {
        Text("Triage incoming alerts.")
    }
}
