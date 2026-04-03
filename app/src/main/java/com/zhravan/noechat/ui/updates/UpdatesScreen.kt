package com.zhravan.noechat.ui.updates

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.zhravan.noechat.ui.common.SimpleScreen

@Composable
fun UpdatesScreen(onBack: () -> Unit) {
    SimpleScreen(title = "Updates", onBack = onBack) {
        Text("Local activity and delivery state.")
    }
}
