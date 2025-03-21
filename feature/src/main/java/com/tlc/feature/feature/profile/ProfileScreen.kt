package com.tlc.feature.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.tlc.feature.feature.auth.login.viewmodel.LoginViewModel
import com.tlc.feature.feature.component.auth_components.AuthButtonComponent

@Composable
fun ProfileScreen(
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
        // Profile content here
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Logout button at the bottom
        AuthButtonComponent(
            value = "Logout",
            onClick = { showLogoutDialog = true },
            firstColor = Color.Red,
            secondColor = Color.Black
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            containerColor = Color.White,
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout", color = Color.Black) },
            text = { Text("Are you sure you want to logout?", color = Color.Black) },
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