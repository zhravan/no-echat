package com.zhravan.noechat.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zhravan.noechat.NoEchatApplication
import com.zhravan.noechat.ui.home.HomeViewModel
import com.zhravan.noechat.ui.map.MapViewModel
import com.zhravan.noechat.ui.responder.ResponderViewModel
import com.zhravan.noechat.ui.sos.SosViewModel
import com.zhravan.noechat.ui.updates.PacketsViewModel
import com.zhravan.noechat.ui.volunteer.VolunteerViewModel

class AppViewModelFactory(
    private val application: NoEchatApplication
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SosViewModel::class.java) ->
                SosViewModel(application) as T
            modelClass.isAssignableFrom(PacketsViewModel::class.java) ->
                PacketsViewModel(application.packetRepository) as T
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(application) as T
            modelClass.isAssignableFrom(VolunteerViewModel::class.java) ->
                VolunteerViewModel(application) as T
            modelClass.isAssignableFrom(ResponderViewModel::class.java) ->
                ResponderViewModel(application.packetRepository) as T
            modelClass.isAssignableFrom(MapViewModel::class.java) ->
                MapViewModel(application.packetRepository) as T
            else -> error("Unknown ViewModel type: ${modelClass.name}")
        }
    }
}

@Composable
fun rememberAppViewModelFactory(): AppViewModelFactory {
    val app = LocalContext.current.applicationContext as NoEchatApplication
    return remember(app) { AppViewModelFactory(app) }
}
