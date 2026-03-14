package com.tlc.feature.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.tlc.domain.model.firebase.Place
import com.tlc.feature.R
import com.tlc.feature.feature.admin.AdminScreen
import com.tlc.feature.feature.auth.forget_password.ForgotPasswordScreen
import com.tlc.feature.feature.auth.is_logged_in.IsLoggedInScreen
import com.tlc.feature.feature.auth.login.LoginScreen
import com.tlc.feature.feature.auth.register.RegisterScreen
import com.tlc.feature.feature.component.LoadingLottie
import com.tlc.feature.feature.customer.CustomerScreen
import com.tlc.feature.feature.customer.PlaceDetailsScreen
import com.tlc.feature.feature.design.DesignScreen
import com.tlc.feature.feature.profile.AdminProfileScreen
import com.tlc.feature.feature.profile.AdminReservationsScreen
import com.tlc.feature.feature.reservation.SaveReservationScreen
import com.tlc.feature.feature.profile.ProfileScreen
import com.tlc.feature.feature.profile.CustomerReservationsScreen
import com.tlc.feature.feature.settings.SettingsScreen
import com.tlc.feature.feature.settings.YourPlaceScreen
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RsrvNavigation(
    navController: NavHostController,
    mainViewModel: MainViewModel = hiltViewModel(),
    onTitleChange: (String) -> Unit,
    key: Int
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""

    if (mainViewModel.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
        ) {
            LoadingLottie(resId = R.raw.loading_lottie)
        }
    }

    NavHost(
        navController = navController,
        startDestination = mainViewModel.startDestination
    ) {
        composable(NavigationGraph.IS_LOGGED_IN.route) {
            IsLoggedInScreen(navController)
        }
        composable(NavigationGraph.LOGIN.route) {
            LoginScreen(navController)
            onTitleChange("Login Screen")
        }
        composable(NavigationGraph.REGISTER.route) {
            RegisterScreen(navController)
            onTitleChange("Register Screen")
        }
        composable(NavigationGraph.FORGOT_PASSWORD.route) {
            ForgotPasswordScreen(navController)
            onTitleChange("Forget Password Screen")
        }
        composable(NavigationGraph.ADMIN_SCREEN.route) {
            AdminScreen(navController)
            onTitleChange("Admin Screen")
        }
        composable(NavigationGraph.CUSTOMER_SCREEN.route) {
            CustomerScreen(navController)
            onTitleChange("Customer Screen")
        }
        composable(NavigationGraph.SETTINGS_SCREEN.route) {
            SettingsScreen(navController)
            onTitleChange("Settings")
        }
        composable(NavigationGraph.YOUR_PLACE_SCREEN.route) {
            YourPlaceScreen(navController)
            onTitleChange("Your Place")
        }
        composable(
            route = NavigationGraph.PLACE_DETAILS_SCREEN.route,
            arguments = listOf(navArgument("placeData") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val encodedPlaceJson = backStackEntry.arguments?.getString("placeData")
            val placeJson = encodedPlaceJson?.let {
                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            }
            val place = placeJson?.let { Gson().fromJson(it, Place::class.java) }
            if (place != null) {
                PlaceDetailsScreen(
                    navController = navController,
                    place = place
                )
                onTitleChange("${place.name} Details")
            }
        }
//        composable(
//            route = NavigationGraph.CUSTOMER_TABLE_SELECTION_SCREEN.route,
//            arguments = listOf(navArgument("placeData") {
//                type = NavType.StringType
//            })
//        ) { backStackEntry ->
//            val encodedPlaceJson = backStackEntry.arguments?.getString("placeData")
//            val placeJson = encodedPlaceJson?.let {
//                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
//            }
//            val place = placeJson?.let { Gson().fromJson(it, Place::class.java) }
//            if (place != null) {
//                CustomerTableSelectionScreen(
//                    navController = navController,
//                    place = place
//                )
//                onTitleChange("Select Table - ${place.name}")
//            }
//        }

            composable(NavigationGraph.ADMIN_PROFILE_SCREEN.route) {
                AdminProfileScreen(navController = navController)
                onTitleChange("Admin's Profile Screen")
            }
            composable(NavigationGraph.ADMIN_RESERVATIONS_SCREEN.route) {
                AdminReservationsScreen(navController = navController)
                onTitleChange("All Reservations")
            }
            composable(
                route = NavigationGraph.DESIGN_SCREEN.route,
                arguments = listOf(navArgument("placeData") {
                    type = NavType.StringType
                })
            ) { backStackEntry ->
                val encodedPlaceJson = backStackEntry.arguments?.getString("placeData")
                val placeJson = encodedPlaceJson?.let {
                    URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
                }
                val place = placeJson?.let { Gson().fromJson(it, Place::class.java) }
                if (place != null) {
                    DesignScreen(
                        navController = navController, place = place, placeId = place.id
                    )
                    onTitleChange("Design Screen")
                }
            }
            composable(
                route = NavigationGraph.SAVE_RESERVATION_SCREEN.route,
                arguments = listOf(
                    navArgument("placeId") { type = NavType.StringType },
                    navArgument("tableId") { type = NavType.StringType },
                    navArgument("date") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = ""
                    },
                )
            ) { backStackEntry ->
                val placeId = backStackEntry.arguments?.getString("placeId") ?: ""
                val tableId = backStackEntry.arguments?.getString("tableId") ?: ""
                val initialDate = backStackEntry.arguments?.getString("date") ?: ""

                SaveReservationScreen(
                    navController = navController,
                    placeId = placeId,
                    tableId = tableId,
                    initialDate = initialDate
                )
                onTitleChange("Save Your Rsrv")
            }

        composable(
            route = NavigationGraph.SAVE_RESERVATION_SCREEN_WITH_PLACE.route, arguments = listOf(
                navArgument("placeId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getString("placeId") ?: ""

            SaveReservationScreen(
                navController = navController, placeId = placeId, tableId = "")
            onTitleChange("Save Your Rsrv")
        }



//        composable(
//            route = NavigationGraph.SAVE_RESERVATION_SCREEN_WITH_PLACE.route, arguments = listOf(
//                navArgument("placeId") { type = NavType.StringType }
//            )
//        ) { backStackEntry ->
//            val placeId = backStackEntry.arguments?.getString("placeId") ?: ""
//
//            SaveReservationScreen(
//                navController = navController, placeId = placeId, tableId = "")
//            onTitleChange("Save Your Rsrv")
//        }


            composable(NavigationGraph.PROFILE_SCREEN.route) {
                ProfileScreen(navController)
                onTitleChange("Profile")
            }
            composable(NavigationGraph.CUSTOMER_RESERVATIONS_SCREEN.route) {
                CustomerReservationsScreen(navController)
                onTitleChange("My Reservations")
            }
        }
    }
