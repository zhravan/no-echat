package com.zhravan.noechat.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zhravan.noechat.ui.home.HomeScreen
import com.zhravan.noechat.ui.map.AlertMapScreen
import com.zhravan.noechat.ui.readiness.ReadinessScreen
import com.zhravan.noechat.ui.responder.ResponderScreen
import com.zhravan.noechat.ui.sos.SosScreen
import com.zhravan.noechat.ui.updates.UpdatesScreen
import com.zhravan.noechat.ui.volunteer.VolunteerScreen

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onReadiness = { navController.navigate(Routes.READINESS) },
                onSos = { navController.navigate(Routes.SOS) },
                onVolunteer = { navController.navigate(Routes.VOLUNTEER) },
                onUpdates = { navController.navigate(Routes.UPDATES) },
                onResponder = { navController.navigate(Routes.RESPONDER) },
                onMap = { navController.navigate(Routes.MAP) }
            )
        }
        composable(Routes.READINESS) {
            ReadinessScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SOS) {
            SosScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.VOLUNTEER) {
            VolunteerScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.UPDATES) {
            UpdatesScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.RESPONDER) {
            ResponderScreen(
                onBack = { navController.popBackStack() },
                onOpenMap = { navController.navigate(Routes.MAP) }
            )
        }
        composable(Routes.MAP) {
            AlertMapScreen(onBack = { navController.popBackStack() })
        }
    }
}
