package com.tlc.feature.feature.auth.register

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
import androidx.navigation.NavController
import com.tlc.feature.R
import com.tlc.feature.feature.auth.register.viewmodel.RegisterViewModel
import com.tlc.feature.feature.component.DividerTextComponent
import com.tlc.feature.feature.component.HeadingTextComponent
import com.tlc.feature.feature.component.LoadingLottie
import com.tlc.feature.feature.component.TextFieldComponent
import com.tlc.feature.feature.component.auth_components.AuthButtonComponent
import com.tlc.feature.feature.component.auth_components.ClickableLoginTextComponent
import com.tlc.feature.feature.component.auth_components.PasswordFieldComponent
import com.tlc.feature.navigation.NavigationGraph

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RegisterScreen(
    navController: NavController,
    onLoginClick: () -> Unit = {},
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current


    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(uiState.user) {
        uiState.user?.let {
            navController.navigate(NavigationGraph.LOGIN.route) {
                popUpTo(NavigationGraph.LOGIN.route) {
                    inclusive = true }
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp)
            .background(Color.Black)
    ) {
        if (uiState.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize()
                    .background(Color.Black),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,

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

                Spacer(modifier = Modifier.height(20.dp))
                HeadingTextComponent("Create an Account")
                Spacer(modifier = Modifier.height(20.dp))


                TextFieldComponent(
                    stateValue = email,
                    onValueChange = { updatedEmail ->
                        email = updatedEmail.trim()
                    },
                    label = "Email",
                    painterResource = painterResource(id = R.drawable.mail_icon)
                )

                PasswordFieldComponent(
                    stateValue = password,
                    onValueChange = { updatedPassword ->
                        password = updatedPassword.trim()
                    },
                    label = "Password",
                    painterResource = painterResource(id = R.drawable.ic_lock)
                )

                Spacer(modifier = Modifier.height(70.dp))
                AuthButtonComponent(value = "Register", onClick = {
                    viewModel.signUp(email, password)
                })
                Spacer(modifier = Modifier.height(20.dp))

                DividerTextComponent()

                ClickableLoginTextComponent(tryToLogin = true, onTextSelected = {
                    onLoginClick()
                    navController.navigate(NavigationGraph.LOGIN.route)
                })
            }
        }
    }
}
