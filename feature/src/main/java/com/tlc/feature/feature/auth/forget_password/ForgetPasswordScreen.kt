package com.tlc.feature.feature.auth.forget_password

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.tlc.feature.R
import com.tlc.feature.feature.auth.forget_password.viewmodel.ForgetPasswordViewModel
import com.tlc.feature.feature.component.LoadingLottie
import com.tlc.feature.feature.component.TextFieldComponent
import com.tlc.feature.feature.component.auth_components.AuthButtonComponent
import com.tlc.feature.navigation.NavigationGraph

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ForgotPasswordScreen(
    navController: NavHostController,
    viewModel: ForgetPasswordViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current


    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }


    LaunchedEffect(uiState.transaction) {
        if (uiState.transaction) {
            Toast.makeText(context, "Email Sent, Check Your Email", Toast.LENGTH_SHORT).show()
            navController.navigate(NavigationGraph.LOGIN.route) {
                popUpTo(NavigationGraph.LOGIN.route) {
                    inclusive = true
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.padding(18.dp)
    ) {
        if (uiState.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                LoadingLottie(resId = R.raw.loading_lottie)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                LoadingLottie(resId = R.raw.loading_lottie, height = 260.dp)

                Spacer(modifier = Modifier.height(50.dp))

                TextFieldComponent(
                    email,
                    onValueChange = { updatedEmail ->
                        email = updatedEmail
                    },
                    label = "Email",
                    painterResource = painterResource(id = R.drawable.mail_icon)
                )

                Spacer(modifier = Modifier.height(30.dp))

                Spacer(modifier = Modifier.height(20.dp))

                AuthButtonComponent(value = "Reset Password", onClick = {
                    viewModel.sendPasswordResetEmail(email)
                })
            }
        }
    }
}