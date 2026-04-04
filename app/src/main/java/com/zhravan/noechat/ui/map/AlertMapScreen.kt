package com.zhravan.noechat.ui.map

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.zhravan.noechat.BuildConfig
import com.zhravan.noechat.navigation.TurnByTurnNavigation
import com.zhravan.noechat.ui.rememberAppViewModelFactory

@Composable
fun AlertMapScreen(onBack: () -> Unit) {
    val factory = rememberAppViewModelFactory()
    val vm: MapViewModel = viewModel(factory = factory)
    val packets by vm.packetsWithLocation.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(packets, BuildConfig.MAPS_API_KEY) {
        if (BuildConfig.MAPS_API_KEY.isBlank()) return@LaunchedEffect
        val pts = packets.mapNotNull { p ->
            val la = p.latitude ?: return@mapNotNull null
            val lo = p.longitude ?: return@mapNotNull null
            LatLng(la, lo)
        }
        if (pts.isEmpty()) return@LaunchedEffect
        if (pts.size == 1) {
            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(pts.first(), 14f))
        } else {
            val builder = LatLngBounds.builder()
            pts.forEach { builder.include(it) }
            cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(builder.build(), 96))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextButton(onClick = onBack) { Text("Back") }
        Text("Alert map", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        if (BuildConfig.MAPS_API_KEY.isBlank()) {
            Text("Add MAPS_API_KEY to local.properties to load map tiles.")
            Spacer(Modifier.height(12.dp))
        }
        when {
            packets.isEmpty() -> Text("No packet locations yet.")
            BuildConfig.MAPS_API_KEY.isNotBlank() -> {
                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(zoomControlsEnabled = true)
                ) {
                    packets.forEach { p ->
                        val la = p.latitude ?: return@forEach
                        val lo = p.longitude ?: return@forEach
                        key(p.publicId) {
                            Marker(
                                state = rememberMarkerState(position = LatLng(la, lo)),
                                title = p.status.name,
                                snippet = p.note ?: p.publicId
                            )
                        }
                    }
                }
            }
            else -> Spacer(Modifier.weight(1f))
        }
        if (packets.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text("Navigate", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp)
            ) {
                items(packets, key = { it.publicId }) { p ->
                    val la = p.latitude ?: return@items
                    val lo = p.longitude ?: return@items
                    Text("${p.status.name}  $la, $lo")
                    Button(onClick = { TurnByTurnNavigation.open(context, la, lo) }) {
                        Text("Turn-by-turn")
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}
