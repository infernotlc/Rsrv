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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
                        .background(Color(0xFFF8F9FA))
                ) {
                    Column {
                        // Enhanced Header with back button and date picker
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { 
                                            showDesignPreview = false
                                            selectedPlaceId = null
                                            selectedPlace = null
                                        },
                                        modifier = Modifier
                                            .background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
                                            .size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowBack,
                                            contentDescription = "Back",
                                            tint = Color(0xFF1976D2)
                                        )
                                    }
                                    
                                    Text(
                                        text = selectedPlace?.name ?: "Select Table",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1A237E)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(40.dp))
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Enhanced Date Selection Section
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Select Date",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF424242),
                                        modifier = Modifier.padding(bottom = 12.dp)
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
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8)),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                        ) {
                                            Text(
                                                text = "Available tables for: $selectedDate",
                                                fontSize = 14.sp,
                                                color = Color(0xFF2E7D32),
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Table Preview Instructions
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Info",
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Click on a green table to make your reservation",
                                    color = Color(0xFF2E7D32),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        // Enhanced Table Preview
                        Card(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                when (designState.result) {
                                    is RootResult.Loading -> {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            CircularProgressIndicator(
                                                color = Color(0xFF1976D2),
                                                modifier = Modifier.size(48.dp)
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = "Loading table layout...",
                                                color = Color(0xFF666666),
                                                fontSize = 16.sp
                                            )
                                        }
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
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Error",
                                                tint = Color(0xFFD32F2F),
                                                modifier = Modifier.size(48.dp)
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = errorMessage ?: "Failed to load design",
                                                color = Color(0xFFD32F2F),
                                                fontSize = 16.sp,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }

                                    else -> {}
                                }
                            }
                        }
                    }
                }
            } else {
                // Enhanced Main Places List
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF1A237E),
                                    Color(0xFF3949AB),
                                    Color(0xFF5C6BC0)
                                )
                            )
                        )
                ) {
                    // Header Section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Restaurant Icon",
                                tint = Color(0xFF1976D2),
                                modifier = Modifier.size(48.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Discover Amazing Places",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A237E),
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Choose your favorite restaurant and make a reservation",
                                fontSize = 16.sp,
                                color = Color(0xFF666666),
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Click on a place to view details or make a reservation",
                                fontSize = 14.sp,
                                color = Color(0xFF888888),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    when (placesState.result) {
                        is RootResult.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Loading amazing places...",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        is RootResult.Success -> {
                            val places =
                                (placesState.result as RootResult.Success<List<Place>>).data
                                    ?: emptyList()
                            LazyColumn(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(places) { place ->
                                    EnhancedPlaceItem(
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
                                        },
                                        navController = navController
                                    )
                                }
                            }
                        }

                        is RootResult.Error -> {
                            val errorMessage = (placesState.result as RootResult.Error).message
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    modifier = Modifier.padding(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Error",
                                            tint = Color(0xFFD32F2F),
                                            modifier = Modifier.size(48.dp)
                                        )
                                        
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        Text(
                                            text = "Oops! Something went wrong",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD32F2F)
                                        )
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        Text(
                                            text = errorMessage ?: "Failed to load places",
                                            color = Color(0xFF666666),
                                            fontSize = 14.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        else -> {}
                    }
                }
            }
        }
    )
}

@Composable
fun EnhancedPlaceItem(place: Place, onClick: () -> Unit, navController: NavHostController) {
    Log.d("PlaceItem", "Displaying place: ${place.name}, ID: ${place.id}")
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = place.name,
                        color = Color(0xFF1A237E),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${place.city}, ${place.country}",
                            color = Color(0xFF666666),
                            fontSize = 16.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Capacity",
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Capacity: ${place.capacity} people",
                            color = Color(0xFF666666),
                            fontSize = 14.sp
                        )
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    // Action Buttons
                    Button(
                        onClick = {
                            navController.navigate(NavigationGraph.getPlaceDetailsRoute(place))
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE3F2FD)
                        ),
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .height(36.dp),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(
                            "View Details", 
                            color = Color(0xFF1976D2), 
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1976D2)
                        ),
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(
                            "Reserve Now", 
                            color = Color.White, 
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
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
                
                // Enhanced Legend
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Table Status Legend",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF1A237E)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(Color.Green, RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Available - Click to reserve",
                                fontSize = 12.sp,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(Color.Yellow, RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Fully Booked for this date",
                                fontSize = 12.sp,
                                color = Color(0xFFF57C00),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(Color.Gray, RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Permanently Reserved",
                                fontSize = 12.sp,
                                color = Color(0xFF666666),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}