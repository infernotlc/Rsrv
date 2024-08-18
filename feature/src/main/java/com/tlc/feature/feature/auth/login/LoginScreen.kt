package com.tlc.feature.feature.auth.login

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
import com.tlc.feature.feature.auth.login.viewmodel.LoginViewModel
import com.tlc.feature.feature.component.LoadingLottie
import com.tlc.feature.feature.component.TextFieldComponent
import com.tlc.feature.feature.component.UnderLinedTextComponent
import com.tlc.feature.feature.component.auth_components.AuthButtonComponent
import com.tlc.feature.feature.component.auth_components.ClickableLoginTextComponent
import com.tlc.feature.feature.component.auth_components.PasswordFieldComponent
import com.tlc.feature.navigation.MainViewModel
import com.tlc.feature.navigation.NavigationGraph

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LoginScreen(
    navController: NavHostController,
    onSignUpClick: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val loginResult by viewModel.loggingState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(uiState.user, uiState.role) {
        mainViewModel.saveAppEntry()
        if (uiState.user != null && uiState.role != null) {
            when (uiState.role) {
                "admin" -> {
                    navController.navigate(NavigationGraph.ADMIN_SCREEN.route) {
                        popUpTo(NavigationGraph.ADMIN_SCREEN.route) {
                            inclusive = true

                        }
                    }
                }
                "customer" -> {
                    navController.navigate(NavigationGraph.CUSTOMER_SCREEN.route) {
                        popUpTo(NavigationGraph.CUSTOMER_SCREEN.route) {
                            inclusive = true

                        }
                    }
                }
                else -> {
                    Toast.makeText(context, "Unknown role", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


            if (uiState.isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    LoadingLottie(resId = R.raw.loading_lottie)
                }
            } else {
                Scaffold(modifier = Modifier.padding(18.dp).background(Color.Black) ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        LoadingLottie(resId = R.raw.loading_lottie, height = 275.dp)

                        Spacer(modifier = Modifier.height(10.dp))

                        TextFieldComponent(
                            email,
                            onValueChange = { updatedEmail -> email = updatedEmail.trim() },
                            label = "Email",
                            painterResource = painterResource(id = R.drawable.mail_icon)
                        )
                        PasswordFieldComponent(
                            password,
                            label = "Password",
                            onValueChange = { updatedPassword ->
                                password = updatedPassword.trim()
                            },
                            painterResource(id = R.drawable.ic_lock)
                        )
                        UnderLinedTextComponent(value = "Forgot your password?", onClick = {
                            navController.navigate(NavigationGraph.FORGOT_PASSWORD.route)
                        })
                        Spacer(modifier = Modifier.height(10.dp))
                        AuthButtonComponent(value = "Login", onClick = {
                            viewModel.signIn(email, password)
                        })
                        Spacer(modifier = Modifier.height(10.dp))

                        Spacer(modifier = Modifier.height(15.dp))

                        ClickableLoginTextComponent(tryToLogin = false, onTextSelected = {
                            onSignUpClick()
                            navController.navigate(NavigationGraph.REGISTER.route)
                        })
                    }
                }
            }
        }

