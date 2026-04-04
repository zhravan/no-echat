package com.zhravan.noechat.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat

object LastKnownLocationProvider {

    @SuppressLint("MissingPermission")
    fun getLatLng(context: Context): Pair<Double, Double>? {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val candidates = listOfNotNull(
            runCatching { lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) }.getOrNull(),
            runCatching { lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) }.getOrNull(),
            runCatching { lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER) }.getOrNull()
        )
        val best = candidates.maxByOrNull { it.time } ?: return null
        return best.latitude to best.longitude
    }
}
