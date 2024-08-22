package com.tlc.feature.feature.auth.is_logged_in

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.tlc.feature.R
import com.tlc.feature.feature.auth.login.state.IsLoggedInState
import com.tlc.feature.feature.auth.login.viewmodel.LoginViewModel
import com.tlc.feature.feature.component.LoadingLottie
import com.tlc.feature.navigation.NavigationGraph


@Composable
fun IsLoggedInScreen(navController: NavController, viewModel: LoginViewModel = hiltViewModel()) {
    val loggingState by viewModel.loggingState.collectAsState()

    LaunchedEffect(loggingState) {
        Log.d("IsLoggedIn", "Logging state: ${loggingState.transaction}, Role: ${loggingState.data}, Loading: ${loggingState.isLoading}")

        if (!loggingState.isLoading) {
            when {
                loggingState.transaction && loggingState.data == "admin" -> {
                    navController.navigate(NavigationGraph.ADMIN_SCREEN.route) {
                        popUpTo(NavigationGraph.ADMIN_SCREEN.route) {
                            inclusive = true
                        }
                    }
                }
                loggingState.transaction && loggingState.data == "customer" -> {
                    navController.navigate(NavigationGraph.CUSTOMER_SCREEN.route) {
                        popUpTo(NavigationGraph.CUSTOMER_SCREEN.route) {
                            inclusive = true
                        }
                    }
                }
                else -> {
                    navController.navigate(NavigationGraph.LOGIN.route) {
                        popUpTo(NavigationGraph.LOGIN.route) {
                            inclusive = true
                        }
                    }
                }
            }
        }
    }

    if (loggingState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LoadingLottie(resId = R.raw.loading_lottie)
        }
    }
}
