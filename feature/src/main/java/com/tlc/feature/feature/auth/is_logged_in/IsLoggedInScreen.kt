package com.tlc.feature.feature.auth.is_logged_in

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tlc.feature.R
import com.tlc.feature.feature.auth.login.state.IsLoggedInState
import com.tlc.feature.feature.auth.login.viewmodel.LoginViewModel
import com.tlc.feature.feature.component.LoadingLottie
import com.tlc.feature.navigation.NavigationGraph
import kotlinx.coroutines.tasks.await


@Composable
fun IsLoggedInScreen(navController: NavController, viewModel: LoginViewModel = hiltViewModel()) {
    val loggingState by viewModel.loggingState.collectAsState()
    var retryCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(loggingState, retryCount) {
        Log.d(
            "IsLoggedIn",
            "Logging state: ${loggingState.transaction}, Role: ${loggingState.data}, Loading: ${loggingState.isLoading}, Retry: $retryCount"
        )
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
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val hasLocation = currentUser?.let { user ->
                        try {
                            val snapshot = FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(user.uid)
                                .get()
                                .await()
                            val country = snapshot.getString("country")
                            val city = snapshot.getString("city")
                            !country.isNullOrBlank() && !city.isNullOrBlank()
                        } catch (_: Exception) {
                            false
                        }
                    } ?: false

                    val targetRoute = if (hasLocation) {
                        NavigationGraph.CUSTOMER_SCREEN.route
                    } else {
                        NavigationGraph.YOUR_PLACE_SCREEN.route
                    }

                    navController.navigate(targetRoute) {
                        popUpTo(targetRoute) {
                            inclusive = true
                        }
                    }
                }
                FirebaseAuth.getInstance().currentUser != null && retryCount < 1 -> {
                    retryCount += 1
                    viewModel.updateLoginState()
                }

                else -> {
                    navController.navigate(NavigationGraph.CUSTOMER_SCREEN.route) {
                        popUpTo(NavigationGraph.CUSTOMER_SCREEN.route) {
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
