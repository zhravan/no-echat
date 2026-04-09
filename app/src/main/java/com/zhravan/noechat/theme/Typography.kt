package com.zhravan.noechat.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.unit.sp

/** Slightly tighter line heights for a calmer, more compact body text. */
val NoEchatTypography: Typography = Typography().run {
    copy(
        bodyLarge = bodyLarge.copy(lineHeight = 22.sp),
        bodyMedium = bodyMedium.copy(lineHeight = 20.sp),
        bodySmall = bodySmall.copy(lineHeight = 16.sp)
    )
}
