package com.tlc.feature.feature.auth.is_logged_in

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tlc.domain.utils.RootResult
import com.tlc.feature.R
import com.tlc.feature.feature.auth.login.viewmodel.LoginViewModel
import com.tlc.feature.feature.component.LoadingLottie
import com.tlc.feature.navigation.NavigationGraph

@Composable
fun IsLoggedIn(navController: NavController, viewModel: LoginViewModel = hiltViewModel()) {

    val loggingState by viewModel.loggingState.collectAsState()

    LaunchedEffect(loggingState.transaction) {
        if (!loggingState.isLoading) {
            when (loggingState.data) {
                "admin" -> navController.navigate(NavigationGraph.ADMIN_SCREEN.route) {
                    popUpTo(NavigationGraph.ADMIN_SCREEN.route) {
                        inclusive = true
                    }
                }
                "customer" -> navController.navigate(NavigationGraph.CUSTOMER_SCREEN.route) {
                    popUpTo(NavigationGraph.CUSTOMER_SCREEN.route) {
                        inclusive = true
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
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LoadingLottie(R.raw.loading_lottie)
        }
    }
}