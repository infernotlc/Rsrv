package com.tlc.feature.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.tlc.feature.feature.auth.login.viewmodel.LoginViewModel
import com.tlc.feature.feature.component.auth_components.AuthButtonComponent
import com.tlc.feature.navigation.NavigationGraph

@Composable
fun AdminProfileScreen(
    navController: NavHostController,
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Spacer(modifier = Modifier.height(32.dp))

        AuthButtonComponent(
            value = "Manage Places",
            onClick = {
                navController.navigate(NavigationGraph.ADMIN_SCREEN.route)
            }
        )

        AuthButtonComponent(
            value = "View All Reservations",
            onClick = {
                navController.navigate(NavigationGraph.ADMIN_RESERVATIONS_SCREEN.route)
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        AuthButtonComponent(
            value = "Logout",
            onClick = {
                showLogoutDialog = true
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Logout",
                    color = Color.Black
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to logout?",
                    color = Color.Black
                )
            },
            confirmButton = {
                AuthButtonComponent(
                    value = "Yes",
                    onClick = {
                        showLogoutDialog = false
                        loginViewModel.signOut(navController)
                    },
                    modifier = Modifier.width(60.dp),
                    fillMaxWidth = false,
                    heightIn = 35.dp,
                    firstColor = Color.Red,
                    secondColor = Color.Black
                )
            },
            dismissButton = {
                AuthButtonComponent(
                    value = "No",
                    onClick = { showLogoutDialog = false },
                    modifier = Modifier.width(60.dp),
                    fillMaxWidth = false,
                    heightIn = 35.dp
                )
            }
        )
    }
} 