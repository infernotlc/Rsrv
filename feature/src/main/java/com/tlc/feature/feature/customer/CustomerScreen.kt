package com.tlc.feature.feature.customer

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Timestamp
import com.google.gson.Gson
import com.tlc.domain.model.firebase.DesignItem
import com.tlc.domain.model.firebase.Place
import com.tlc.domain.model.firebase.Reservation
import com.tlc.domain.utils.RootResult
import com.tlc.feature.feature.auth.login.viewmodel.LoginViewModel
import com.tlc.feature.feature.customer.viewmodel.CustomerViewModel
import com.tlc.feature.navigation.NavigationGraph
import com.tlc.feature.feature.reservation.util.DatePickerWithDialog
import com.tlc.feature.navigation.main_datastore.MainDataStore
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("StateFlowValueCalledInComposition", "UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CustomerScreen(
    navController: NavHostController,
    viewModel: CustomerViewModel = hiltViewModel(),
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    val placesState by viewModel.placesState.collectAsState()
    val designState by viewModel.designState.collectAsState()
    val reservationsState by viewModel.reservationsState.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val role by viewModel.role.collectAsState()
    val fullyBookedTables by viewModel.fullyBookedTables.collectAsState()
    
    var selectedPlaceId by remember { mutableStateOf<String?>(null) }
    var selectedPlace by remember { mutableStateOf<Place?>(null) }
    var showDesignPreview by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf("") }

    // Get login state from login view model
    val loginState = loginViewModel.loggingState.collectAsState()
    val isUserLoggedIn = loginState.value.transaction
    val userRole = loginState.value.data ?: ""

    // Initialize date picker with current date
    LaunchedEffect(Unit) {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        selectedDate = currentDate
        // Update view model with login state
        viewModel.updateLoginState(isUserLoggedIn, userRole)
    }

    LaunchedEffect(Unit) {
        Log.d("CustomerScreen", "LaunchedEffect triggered, fetching places")
        viewModel.fetchPlaces()
    }

    LaunchedEffect(designState.result, selectedDate, selectedPlaceId) {
        if (designState.result is RootResult.Success && selectedDate.isNotEmpty() && selectedPlaceId != null) {
            val designItems = (designState.result as RootResult.Success<List<DesignItem>>).data ?: emptyList()
            viewModel.fetchFullyBookedTables(selectedPlaceId!!, selectedDate, designItems)
        }
    }

    Scaffold(
        content = {
            if (showDesignPreview) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                ) {
                    Column {
                        // Header with back button and date picker
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { 
                                    showDesignPreview = false
                                    selectedPlaceId = null
                                    selectedPlace = null
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.Black
                                )
                            }
                            
                            Text(
                                text = selectedPlace?.name ?: "Select Table",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            
                            Spacer(modifier = Modifier.width(80.dp))
                        }
                        
                        // Date Selection Section
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Select Date",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            DatePickerWithDialog(
                                modifier = Modifier.fillMaxWidth(),
                                onDateSelected = { date ->
                                    selectedDate = date
                                    // Fetch reservations for the new date
                                    if (selectedPlaceId != null) {
                                        viewModel.fetchReservations(selectedPlaceId!!, date)
                                    }
                                }
                            )
                            
                            if (selectedDate.isNotEmpty()) {
                                Text(
                                    text = "Available tables for: $selectedDate",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }

                        // Table Preview
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            when (designState.result) {
                                is RootResult.Loading -> {
                                    CircularProgressIndicator(
                                        color = Color.Black,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }

                                is RootResult.Success -> {
                                    val designItems =
                                        (designState.result as RootResult.Success<List<DesignItem>>).data
                                            ?: emptyList()
                                    DateSpecificDesignPreview(
                                        designItems = designItems,
                                        reservations = reservationsState,
                                        selectedDate = selectedDate,
                                        place = selectedPlace,
                                        fullyBookedTables = fullyBookedTables,
                                        onTableClick = { selectedTable ->
                                            if (selectedPlaceId != null) {
                                                navController.navigate("save_reservation_screen/${selectedPlaceId}/${selectedTable.designId}")
                                            }
                                        }
                                    )
                                }

                                is RootResult.Error -> {
                                    val errorMessage =
                                        (designState.result as RootResult.Error).message
                                    Text(
                                        text = errorMessage ?: "Failed to load design",
                                        color = Color.Red,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }

                                else -> {}
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    when (placesState.result) {
                        is RootResult.Loading -> {
                            CircularProgressIndicator(
                                color = Color.Black,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }

                        is RootResult.Success -> {
                            val places =
                                (placesState.result as RootResult.Success<List<Place>>).data
                                    ?: emptyList()
                            LazyColumn {
                                items(places) { place ->
                                    PlaceItem(
                                        place = place,
                                        onClick = {
                                            selectedPlaceId = place.id
                                            selectedPlace = place
                                            
                                            if (isUserLoggedIn) {
                                                // If logged in as admin, navigate to admin screen
                                                if (userRole == "admin") {
                                                    navController.navigate(NavigationGraph.ADMIN_SCREEN.route)
                                                } else {
                                                    // If logged in as customer, show design
                                                    viewModel.fetchDesign(place.id)
                                                    // Fetch reservations for the selected date
                                                    viewModel.fetchReservations(place.id, selectedDate)
                                                    showDesignPreview = true
                                                }
                                            } else {
                                                // If not logged in, navigate to login
                                                navController.navigate(NavigationGraph.LOGIN.route)
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        is RootResult.Error -> {
                            val errorMessage = (placesState.result as RootResult.Error).message
                            Text(
                                text = errorMessage,
                                color = Color.Red,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }

                        else -> {}
                    }
                }
            }
        }
    )
}


@Composable
fun PlaceItem(place: Place, onClick: () -> Unit) {
    Log.d("PlaceItem", "Displaying place: ${place.name}, ID: ${place.id}")
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
    ) {
        Text(
            text = place.name,
            modifier = Modifier.padding(16.dp),
            color = Color.Black
        )
    }
}


@Composable
fun DateSpecificDesignPreview(
    designItems: List<DesignItem>,
    reservations: List<Reservation>,
    selectedDate: String,
    place: Place?,
    fullyBookedTables: Set<String>,
    onTableClick: (DesignItem) -> Unit = {}
) {
    val density = LocalDensity.current.density

    // Filter out tables that are globally reserved or fully booked for the selected date
    val availableDesignItems = designItems.filter { item ->
        item.type == "TABLE" && 
        !item.isReserved && 
        item.designId !in fullyBookedTables
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        if (availableDesignItems.isEmpty()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No Available Tables",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "for $selectedDate",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                // Show all tables with different colors based on availability
                designItems.filter { it.type == "TABLE" }.forEach { item ->
                    val isAvailable = item.designId in availableDesignItems.map { it.designId }
                    val isFullyBooked = item.designId in fullyBookedTables
                    val isGloballyReserved = item.isReserved
                    
                    val tableColor = when {
                        isGloballyReserved -> Color.Gray // Globally reserved
                        isFullyBooked -> Color.Yellow // Fully booked for this date
                        isAvailable -> Color.Green // Available
                        else -> Color.Red // Default
                    }
                    
                    val tableText = when {
                        isGloballyReserved -> "R"
                        isFullyBooked -> "F"
                        isAvailable -> "A"
                        else -> "X"
                    }
                    
                    Box(
                        modifier = Modifier
                            .offset(
                                (item.xPosition / density).dp,
                                (item.yPosition / density).dp
                            )
                            .size(40.dp)
                            .background(tableColor)
                            .clickable(enabled = isAvailable) {
                                if (isAvailable) {
                                    onTableClick(item)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tableText,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Legend
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .background(Color.White.copy(alpha = 0.9f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Legend:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                    Text(
                        text = "🟢 Available",
                        fontSize = 10.sp,
                        color = Color.Black
                    )
                    Text(
                        text = "🟡 Fully Booked",
                        fontSize = 10.sp,
                        color = Color.Black
                    )
                    Text(
                        text = "⚫ Reserved",
                        fontSize = 10.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}