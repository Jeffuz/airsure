package com.example.efficientdet_lite.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.efficientdet_lite.announcements.AnnouncementScreen
import com.example.efficientdet_lite.carryon.CarryOnScannerScreen
import com.example.efficientdet_lite.carryon.ItemDetailsScreen
import com.example.efficientdet_lite.carryon.CarryOnRepository
import com.example.efficientdet_lite.ui.HomeScreen
import com.example.efficientdet_lite.ui.SplashScreen
import com.qualcomm.qti.objectdetection.RestrictionManager

@Composable
fun AirSureNavHost() {
    val navController = rememberNavController()
    
    // Observe saved items from repository
    val savedItems by CarryOnRepository.savedItems.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {

        composable(Routes.SPLASH) {
            SplashScreen(
                onFinished = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onScanCarryOnClick = {
                    navController.navigate(Routes.CARRY_ON)
                },
                onAnnouncementsClick = {
                    navController.navigate(Routes.ANNOUNCEMENTS)
                },
                onRecentSubmissionsClick = {
                    navController.navigate(Routes.ITEM_DETAILS)
                }
            )
        }

        composable(Routes.CARRY_ON) {
            CarryOnScannerScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onItemDetected = { results ->
                    // Dynamically add items as they are detected by the tracker
                    CarryOnRepository.addItems(results)
                },
                onSubmitClick = {
                    // Navigate to details screen which already observes the repository
                    navController.navigate(Routes.ITEM_DETAILS) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }

        composable(Routes.ITEM_DETAILS) {
            ItemDetailsScreen(
                items = savedItems,
                onBackClick = {
                    navController.popBackStack()
                },
                onAddAnotherClick = {
                    navController.navigate(Routes.CARRY_ON)
                },
                onHomeClick = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onListenClick = {
                    navController.navigate(Routes.ANNOUNCEMENTS)
                }
            )
        }

        composable(Routes.ANNOUNCEMENTS) {
            AnnouncementScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
