package com.tlc.feature.feature.main_content

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.tlc.feature.R
import com.tlc.feature.feature.auth.login.viewmodel.LoginViewModel
import com.tlc.feature.feature.component.LoadingLottie
import com.tlc.feature.feature.component.auth_components.AuthButtonComponent
import com.tlc.feature.feature.main_content.utils.NavDrawer
import com.tlc.feature.feature.main_content.viewmodel.MainContentViewModel
import com.tlc.feature.navigation.NavigationGraph
import com.tlc.feature.navigation.RsrvNavigation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    val loggingState by loginViewModel.loggingState.collectAsState()

    var appBarTitle by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var dialogAction by remember { mutableStateOf("") }
    var navigationKey by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    var currentRoute by remember { mutableStateOf<String?>(null) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

//
//    LaunchedEffect(loggingState) {
//        Log.d("Navigation", "loggingState: $loggingState")
//        Log.d("Navigation", "navController: $navController")
//        Log.d("Navigation", "LOGIN route: ${NavigationGraph.LOGIN.route}")
//        Log.d("Navigation", "PROFILE_SCREEN route: ${NavigationGraph.PROFILE_SCREEN.route}")
//
//        if (!loggingState.isLoading) {  // Ensure loading is finished
//            if (!loggingState.transaction) {
//                Log.d("Navigation", "Navigating to LOGIN screen")
//                val loginRoute = NavigationGraph.LOGIN.route
//                val profileRoute = NavigationGraph.PROFILE_SCREEN.route
//
//                navController.navigate(loginRoute) {
//                    popUpTo(profileRoute) { inclusive = true }
//                }
//            }
//        }
//    }


    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            currentRoute = destination.route
        }
    }



    LaunchedEffect(true) {
        loginViewModel.isLoggedIn()
    }

    LaunchedEffect(deleteUserState.transaction) {
        if (deleteUserState.transaction) {
            goToLogin(
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
                                loginViewModel.signOut(navController)
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
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavDrawer(navController, onClose = {
                scope.launch {
                    drawerState.close()
                }
            }, onLogout = {
                showDialog = true
                dialogAction = "Logout"
            })
        })
    {
        Scaffold(
            containerColor = Color.Black,
            topBar = {
                if (currentRoute != NavigationGraph.DESIGN_SCREEN.route) {
                    appBarTitle?.let { title ->
                        Box(modifier = Modifier.background(Color.Red)) {
                            CenterAlignedTopAppBar(
                                colors = TopAppBarColors(
                                    actionIconContentColor = Color.White,
                                    containerColor = Color.Black,
                                    navigationIconContentColor = Color.White,
                                    scrolledContainerColor = Color.Red,
                                    titleContentColor = Color.White
                                ),
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
                                    if (title != "Customer Screen" && title != "Admin Screen") {
                                        IconButton(onClick = { navController.navigateUp() }) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_left),
                                                contentDescription = "Back",
                                                tint = Color.White,
                                                modifier = Modifier.size(30.dp)
                                            )
                                        }
                                    }
                                },
                                actions = {
                                    if (title == "Customer Screen" || title == "Admin Screen" || title == "Save Your Rsrv") {
                                        IconButton(
                                            onClick = {
                                                  loginViewModel.isLoggedIn()
                                                    if (loginViewModel.loggingState.value.transaction) {
                                                        navController.navigate(NavigationGraph.PROFILE_SCREEN.route)
                                                    } else {
                                                        navController.navigate(NavigationGraph.LOGIN.route)
                                                    }
                                                }
                                        ) {
                                            Icon(
                                                painter = painterResource(
                                                    id = R.drawable.ic_left
                                                ),
                                                contentDescription = "Settings",
                                                tint = Color.White,
                                                modifier = Modifier.size(30.dp)
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            },
            content = { innerPadding ->
                if (deleteUserState.isLoading || uiState.isLoading) {
                    Column(
                        Modifier
                            .fillMaxSize()
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
}

private fun goToLogin( navHostController: NavHostController) {
    navHostController.navigate(NavigationGraph.LOGIN.route) {
        popUpTo(NavigationGraph.CUSTOMER_SCREEN.route) {
            inclusive = true
        }
    }
}
