package com.zhravan.noechat.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

object FreshLocationProvider {

    suspend fun getLatLng(context: Context): Pair<Double, Double>? {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }
        val client = LocationServices.getFusedLocationProviderClient(context)
        return try {
            val cts = CancellationTokenSource()
            val location = client.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cts.token
            ).await()
            location?.let { it.latitude to it.longitude }
                ?: LastKnownLocationProvider.getLatLng(context)
        } catch (_: Exception) {
            LastKnownLocationProvider.getLatLng(context)
        }
    }
}
