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
import com.google.firebase.auth.FirebaseAuth
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
    var lastRoute by remember { mutableStateOf<String?>(null) }
    var currentRoute by remember { mutableStateOf<String?>(null) }
    var isNavigating by remember { mutableStateOf(false) }
    var lastNavigationTime by remember { mutableStateOf(0L) }
    var isInitialNavigation by remember { mutableStateOf(true) }
    val context = LocalContext.current

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(navController) {
        // Initial navigation based on user role
        if (isInitialNavigation) {
            isInitialNavigation = false
            try {
                // First check login status
                loginViewModel.isLoggedIn()

                // Wait until login check is complete
                while (loginViewModel.loggingState.value.isLoading || !loginViewModel.hasCheckedLogin) {
                    delay(100)
                }

                val role = loginViewModel.loggingState.value.data
                val isLoggedIn = loginViewModel.loggingState.value.transaction

                Log.d("MainScreen", "Initial navigation - Role: $role, IsLoggedIn: $isLoggedIn")

                // Only navigate to admin screen if user is logged in as admin
                if (isLoggedIn && role == "admin") {
                    Log.d("MainScreen", "User is admin, navigating to admin screen")
                    navController.navigate(NavigationGraph.ADMIN_SCREEN.route) {
                        popUpTo(0) { inclusive = true }
                    }
                } else {
                    // For all other cases (not logged in, or logged in as customer), show customer screen
                    Log.d("MainScreen", "Showing customer screen")
                    navController.navigate(NavigationGraph.CUSTOMER_SCREEN.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } catch (e: Exception) {
                Log.e("MainScreen", "Error during initial navigation", e)
                // Fallback to customer screen in case of error
                navController.navigate(NavigationGraph.CUSTOMER_SCREEN.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val currentTime = System.currentTimeMillis()
            if (destination.route != lastRoute && !isNavigating && (currentTime - lastNavigationTime) > 500) {
                Log.d("MainScreen", "Destination changed to: ${destination.route}")
                lastRoute = destination.route
                currentRoute = destination.route
                lastNavigationTime = currentTime
            }
        }
    }

    // Handle logout navigation
    LaunchedEffect(loggingState.transaction) {
        if (!loggingState.transaction && !isNavigating && currentRoute != NavigationGraph.LOGIN.route) {
            // When logged out, navigate to login screen
            Log.d("MainScreen", "User logged out, navigating to login screen")
            isNavigating = true
            loginViewModel.updateLoginState()
            navController.navigate(NavigationGraph.CUSTOMER_SCREEN.route) {
                popUpTo(0) { inclusive = true }
            }
            delay(500)
            isNavigating = false
        }
    }

    // Handle profile button navigation
    LaunchedEffect(uiState.user, loggingState.transaction) {
        if (!loggingState.isLoading) {
            if (uiState.user == null && currentRoute == NavigationGraph.PROFILE_SCREEN.route) {
                Log.d("MainScreen", "User logged out while on profile, navigating to login screen")
                isNavigating = true
                loginViewModel.updateLoginState()
                navController.navigate(NavigationGraph.CUSTOMER_SCREEN.route) {
                    popUpTo(0) { inclusive = true }
                }
                delay(500)
                isNavigating = false
            }
        }
    }

    LaunchedEffect(deleteUserState.transaction) {
        if (deleteUserState.transaction && !isNavigating) {
            Log.d("MainScreen", "User deleted, navigating to login")
            isNavigating = true
            goToLogin(navHostController = navController)
            appBarTitle = null
            navigationKey++
            Toast.makeText(context, "User deleted successfully", Toast.LENGTH_SHORT).show()
            scope.launch {
                delay(500)
                isNavigating = false
            }
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
                                if (!isNavigating) {
                                    Log.d("MainScreen", "Logging out user")
                                    isNavigating = true
                                    loginViewModel.signOut(navController)
                                    appBarTitle = null
                                    navigationKey++
                                    scope.launch {
                                        delay(500)
                                        isNavigating = false
                                    }
                                }
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

    val shouldShowNavDrawer = remember(currentRoute) {
        val show = NavigationGraph.shouldShowNavigationDrawer(currentRoute ?: "")
        Log.d("MainScreen", "Should show nav drawer: $show for route: $currentRoute")
        show
    }

    val shouldShowTopBar = remember(currentRoute) {
        val show = NavigationGraph.shouldShowTopBar(currentRoute ?: "")
        Log.d("MainScreen", "Should show top bar: $show for route: $currentRoute")
        show
    }

    if (shouldShowNavDrawer) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                NavDrawer(navController, onClose = {
                    scope.launch {
                        drawerState.close()
                    }
                })
            }
        ) {
            Scaffold(
                containerColor = Color.Black,
                topBar = {
                    if (shouldShowTopBar) {
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
                                        IconButton(onClick = {
                                            scope.launch {
                                                drawerState.open()
                                            }
                                        }) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_left),
                                                contentDescription = "Menu",
                                                tint = Color.White,
                                                modifier = Modifier.size(30.dp)
                                            )
                                        }
                                    },
                                    actions = {
                                        IconButton(
                                            onClick = {
                                                Log.d("MainScreen", "Profile button clicked, logged in: ${loggingState.transaction}, user: ${uiState.user}")
                                                val currentUser = FirebaseAuth.getInstance().currentUser
                                                if (currentUser != null) {
                                                    // User is actually logged in, navigate to profile
                                                    navController.navigate(NavigationGraph.PROFILE_SCREEN.route) {
                                                        popUpTo(NavigationGraph.CUSTOMER_SCREEN.route)
                                                    }
                                                } else {
                                                    // No user in Firebase, force update login state and navigate to login
                                                    loginViewModel.updateLoginState()
                                                    navController.navigate(NavigationGraph.LOGIN.route) {
                                                        popUpTo(NavigationGraph.CUSTOMER_SCREEN.route)
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_left),
                                                contentDescription = "Profile",
                                                tint = Color.White,
                                                modifier = Modifier.size(30.dp)
                                            )
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
                                onTitleChange = { title ->
                                    appBarTitle = title
                                },
                                key = navigationKey
                            )
                        }
                    }
                }
            )
        }
    } else {
        Scaffold(
            containerColor = Color.Black,
            topBar = {
                if (shouldShowTopBar) {
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
                                    IconButton(onClick = {
                                        navController.popBackStack()
                                    }) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_left),
                                            contentDescription = "Back",
                                            tint = Color.White,
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                },
                                actions = {
                                    IconButton(
                                        onClick = {
                                            Log.d("MainScreen", "Profile button clicked, logged in: ${loggingState.transaction}, user: ${uiState.user}")
                                            val currentUser = FirebaseAuth.getInstance().currentUser
                                            if (currentUser != null) {
                                                // User is actually logged in, navigate to profile
                                                navController.navigate(NavigationGraph.PROFILE_SCREEN.route) {
                                                    popUpTo(NavigationGraph.CUSTOMER_SCREEN.route)
                                                }
                                            } else {
                                                // No user in Firebase, force update login state and navigate to login
                                                loginViewModel.updateLoginState()
                                                navController.navigate(NavigationGraph.LOGIN.route) {
                                                    popUpTo(NavigationGraph.CUSTOMER_SCREEN.route)
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_left),
                                            contentDescription = "Profile",
                                            tint = Color.White,
                                            modifier = Modifier.size(30.dp)
                                        )
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
                            onTitleChange = { title ->
                                appBarTitle = title
                            },
                            key = navigationKey
                        )
                    }
                }
            }
        )
    }
}

private fun goToLogin(navHostController: NavHostController) {
    Log.d("MainScreen", "Navigating to login screen")
    navHostController.navigate(NavigationGraph.LOGIN.route) {
        popUpTo(NavigationGraph.IS_LOGGED_IN.route) {
            inclusive = true
        }
    }
}