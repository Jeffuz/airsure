package com.example.efficientdet_lite.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.efficientdet_lite.announcements.AnnouncementScreen
import com.example.efficientdet_lite.announcements.CarryOnScannerScreen
import com.example.efficientdet_lite.ui.BoardingPassFormScreen
import com.example.efficientdet_lite.ui.HomeScreen
import com.example.efficientdet_lite.ui.SplashScreen

@Composable
fun AirSureNavHost() {
    val navController = rememberNavController()

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
                onBoardingPassClick = {
                    navController.navigate(Routes.BOARDING_PASS)
                }
            )
        }

        composable(Routes.CARRY_ON) {
            CarryOnScannerScreen(
                onBackClick = {
                    navController.popBackStack()
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

        composable(Routes.BOARDING_PASS) {
            BoardingPassFormScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}