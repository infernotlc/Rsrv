package com.tlc.feature.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tlc.feature.R
import com.tlc.feature.feature.admin.AdminScreen
import com.tlc.feature.feature.auth.forget_password.ForgotPasswordScreen
import com.tlc.feature.feature.auth.is_logged_in.IsLoggedIn
import com.tlc.feature.feature.auth.login.LoginScreen
import com.tlc.feature.feature.auth.register.RegisterScreen
import com.tlc.feature.feature.component.LoadingLottie
import com.tlc.feature.feature.customer.CustomerScreen

@Composable
fun RsrvNavigation(
    navController: NavHostController,
    mainViewModel: MainViewModel = hiltViewModel(),
    onTitleChange: (String) -> Unit,
    key: Int
) {

        LaunchedEffect(mainViewModel.startDestination) {
            if (!mainViewModel.isLoading) {
                navController.navigate(mainViewModel.startDestination) {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                }
            }
        }

        if (mainViewModel.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoadingLottie(resId = R.raw.loading_lottie)
            }
        } else {
            NavHost(
                navController = navController,
                startDestination = mainViewModel.startDestination
            ) {
                composable(NavigationGraph.IS_LOGGED_IN.route) {
                    IsLoggedIn(navController)
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
            }
        }
    }