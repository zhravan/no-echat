package com.zhravan.noechat.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri

object TurnByTurnNavigation {

    fun open(context: Context, latitude: Double, longitude: Double) {
        val mapsIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("google.navigation:q=$latitude,$longitude")
        ).apply { setPackage("com.google.android.apps.maps") }
        try {
            if (mapsIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapsIntent)
                return
            }
        } catch (_: Exception) {
        }
        val geo = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")
        )
        try {
            context.startActivity(geo)
        } catch (_: Exception) {
        }
    }
}
