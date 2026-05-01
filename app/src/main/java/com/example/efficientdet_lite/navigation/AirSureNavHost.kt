package com.example.efficientdet_lite.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.efficientdet_lite.app.TripStorage
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
    val context = LocalContext.current

    var tripDetails by remember {
        mutableStateOf(TripStorage.load(context))
    }


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
                tripDetails = tripDetails,
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
                initialTripDetails = tripDetails,
                onBackClick = {
                    navController.popBackStack()
                },
                onSaveClick = { updatedTrip ->
                    tripDetails = updatedTrip
                    TripStorage.save(context, updatedTrip)
                    navController.popBackStack()
                }
            )
        }
    }
}