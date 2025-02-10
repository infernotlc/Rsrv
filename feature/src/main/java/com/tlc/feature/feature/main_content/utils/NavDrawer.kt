package com.tlc.feature.feature.main_content.utils

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerDefaults.containerColor
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemColors
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.tlc.feature.feature.auth.login.viewmodel.LoginViewModel
import com.tlc.feature.navigation.NavigationGraph

@Composable
fun NavDrawer(navController: NavHostController, onClose: () -> Unit) {

    val loginViewModel : LoginViewModel = hiltViewModel()
    LaunchedEffect(true) {
        loginViewModel.isLoggedIn()
    }

    ModalDrawerSheet(
        modifier = Modifier.fillMaxHeight(),
        drawerContainerColor = Color.White
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Menu",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)
        )

        HorizontalDivider(color = Color.Black)

        // Navigation Items
        val menuItems = listOf("Home", "Settings", "Logout")
        menuItems.forEach { item ->
            NavigationDrawerItem(
                label = { Text(text = item, color = Color.White) },
                selected = false,
                onClick = {
                    onClose()
                    when (item) {
                        "Home" -> navController.navigate(NavigationGraph.CUSTOMER_SCREEN.route)
                        "Settings" -> navController.navigate(NavigationGraph.SETTINGS_SCREEN.route)
                        "Logout" -> {
                            // Handle logout logic here
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = when (item) {
                            "Home" -> Icons.Default.Home
                            "Settings" -> Icons.Default.Settings
                            "Logout" -> Icons.Default.ExitToApp
                            else -> Icons.Default.Info
                        },
                        contentDescription = item,
                        tint = Color.White
                    )
                },
                modifier = Modifier.padding(8.dp),
                colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = MaterialTheme.colorScheme.onSecondaryContainer),
                shape = MaterialTheme.shapes.medium
            )

        }
    }
}
