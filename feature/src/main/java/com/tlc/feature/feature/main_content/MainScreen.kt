package com.tlc.feature.feature.main_content

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.tlc.feature.R
import com.tlc.feature.feature.auth.login.viewmodel.LoginViewModel
import com.tlc.feature.feature.component.LoadingLottie
import com.tlc.feature.feature.component.auth_components.AuthButtonComponent
import com.tlc.feature.feature.main_content.viewmodel.MainContentViewModel
import com.tlc.feature.navigation.NavigationGraph
import com.tlc.feature.navigation.RsrvNavigation

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(
    navController: NavHostController,
    loginViewModel: LoginViewModel = hiltViewModel(),
    mainContentViewModel: MainContentViewModel = hiltViewModel()
) {
    val deleteUserState by mainContentViewModel.deleteUserState.collectAsState()
    val uiState by loginViewModel.uiState.collectAsState()

    var appBarTitle by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var dialogAction by remember { mutableStateOf("") }
    var navigationKey by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    LaunchedEffect(true) {
        loginViewModel.isLoggedIn()
    }

    LaunchedEffect(deleteUserState.transaction) {
        Log.i("MainScreen", "deleteUserState.transaction: ${deleteUserState.transaction}")
        if (deleteUserState.transaction) {
            goToLogin(
                loginViewModel = loginViewModel,
                navHostController = navController
            )
            appBarTitle = null
            navigationKey++
            Toast.makeText(context, "User deleted successfully", Toast.LENGTH_SHORT).show()
        }
    }

    if (showDialog) {
        AlertDialog(
            containerColor = Color.White,
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = if (dialogAction == "Logout") "Logout" else "Delete Account",
                    color = Color.Black
                )
            },
            text = {
                Text(
                    "Are you sure you want to ${dialogAction.lowercase()}?",
                    color = Color.Black
                )
            },
            confirmButton = {
                AuthButtonComponent(
                    value = "Yes",
                    onClick = {
                        showDialog = false
                        when (dialogAction) {
                            "Logout" -> {
                                goToLogin(
                                    loginViewModel = loginViewModel,
                                    navHostController = navController
                                )
                                appBarTitle = null
                                navigationKey++
                            }

                            "Delete Account" -> {
                                mainContentViewModel.deleteUser()
                            }
                        }
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
                    onClick = { showDialog = false },
                    modifier = Modifier.width(60.dp),
                    fillMaxWidth = false,
                    heightIn = 35.dp
                )
            }
        )
    }

    Scaffold( containerColor = Color.Black,
        topBar = {
            appBarTitle?.let { title ->
                Box(modifier = Modifier.background(Color.Red)) {
                    CenterAlignedTopAppBar(colors = TopAppBarColors(
                        actionIconContentColor = Color.White,
                        containerColor = Color.Black,
                        navigationIconContentColor = Color.White,
                        scrolledContainerColor = Color.Red,
                        titleContentColor = Color.White),
                        title = {
                            Text(
                                text = title,
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = 25.sp
                                ),
                            )
                        },
                        navigationIcon = {
                            if (title != "Customer Screen" && title != "Admin Screen" && title !="Login Screen" && title !="Register Screen" && title !="Forget Password Screen") {
                                IconButton(onClick = { navController.navigateUp() }) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_left),
                                        contentDescription = "Back",
                                        tint = Color.White,  // Geri düğmesi ikonu beyaz
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                        },
                        actions = {
                            if (title == "Customer Screen" || title == "Admin Screen") {
                                var expanded by remember { mutableStateOf(false) }
                                IconButton(onClick = { expanded = !expanded }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_vert),
                                        contentDescription = "Settings",
                                        tint = Color.White,  // Üç nokta menü ikonu beyaz
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                                MaterialTheme(
                                    shapes = MaterialTheme.shapes.copy(
                                        extraSmall = RoundedCornerShape(16.dp)
                                    )
                                ) {
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                        Modifier.background(Color.White)  // Dropdown arka planı beyaz
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Delete Account") },
                                            onClick = {
                                                expanded = false
                                                dialogAction = "Delete Account"
                                                showDialog = true
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Logout", color = Color.Black) },
                                            onClick = {
                                                expanded = false
                                                dialogAction = "Logout"
                                                showDialog = true
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }
        },
        content = { innerPadding ->
            if (deleteUserState.isLoading || uiState.isLoading) {
                Column(
                    Modifier.fillMaxSize()
                        .background(Color.Black),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    LoadingLottie(R.raw.loading_lottie)
                }
            } else {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(Color.Black)
                ) {
                    RsrvNavigation(
                        navController = navController,
                        onTitleChange = { newTitle ->
                            appBarTitle = newTitle
                        },
                        key = navigationKey
                    )
                }
            }
        }
    )
}

private fun goToLogin(loginViewModel: LoginViewModel, navHostController: NavHostController) {
    loginViewModel.signOut()
    navHostController.navigate(NavigationGraph.LOGIN.route) {
        popUpTo(NavigationGraph.LOGIN.route) {
            inclusive = true
        }
    }
}