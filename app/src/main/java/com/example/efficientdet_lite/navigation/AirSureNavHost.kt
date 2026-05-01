package com.example.efficientdet_lite.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.efficientdet_lite.announcements.AnnouncementScreen
import com.example.efficientdet_lite.app.TripStorage
import com.example.efficientdet_lite.carryon.CarryOnRepository
import com.example.efficientdet_lite.carryon.CarryOnScannerScreen
import com.example.efficientdet_lite.carryon.ItemDetailsScreen
import com.example.efficientdet_lite.ui.BoardingPassFormScreen
import com.example.efficientdet_lite.ui.HomeScreen
import com.example.efficientdet_lite.ui.SplashScreen

@Composable
fun AirSureNavHost() {
    val context = LocalContext.current
    val navController = rememberNavController()

    var tripDetails by remember { mutableStateOf(TripStorage.load(context)) }
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
                tripDetails = tripDetails,
                onScanCarryOnClick = {
                    navController.navigate(Routes.CARRY_ON)
                },
                onAnnouncementsClick = {
                    navController.navigate(Routes.ANNOUNCEMENTS)
                },
                onRecentSubmissionsClick = {
                    navController.navigate(Routes.ITEM_DETAILS)
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
                },
                onHomeClick = {
                    navController.navigate(Routes.HOME)
                },
                onListenClick = {
                    navController.navigate(Routes.ANNOUNCEMENTS)
                },
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
                },
                tripDetails = tripDetails
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