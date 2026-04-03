package com.zhravan.noechat.ui.volunteer

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.zhravan.noechat.ui.common.SimpleScreen

@Composable
fun VolunteerScreen(onBack: () -> Unit) {
    SimpleScreen(title = "Volunteer", onBack = onBack) {
        Text("Relay and view nearby alerts.")
    }
}
