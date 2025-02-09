package com.tlc.feature.feature.customer

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Timestamp
import com.google.gson.Gson
import com.tlc.domain.model.firebase.DesignItem
import com.tlc.domain.model.firebase.Place
import com.tlc.domain.model.firebase.Reservation
import com.tlc.domain.utils.RootResult
import com.tlc.feature.feature.customer.viewmodel.CustomerViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CustomerScreen(
    navController: NavHostController,
    viewModel: CustomerViewModel = hiltViewModel(),
) {
    val placesState by viewModel.placeState.collectAsState()
    val designState by viewModel.designState.collectAsState()
    val reservationsState by viewModel.reservationsState.collectAsState()
    var showDesignPreview by remember { mutableStateOf(false) }
    var selectedPlaceId by remember { mutableStateOf<String?>(null) }


    LaunchedEffect(Unit) {
        Log.d("CustomerScreen", "LaunchedEffect triggered, fetching places")
        viewModel.fetchPlaces()
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
                        //                        IconButton(
                        //                            onClick = {
                        //                                Log.d(
                        //                                    "CustomerScreen",
                        //                                    "Back button clicked, closing design preview"
                        //                                )
                        //                                showDesignPreview = false
                        //                            },
                        //                            modifier = Modifier.padding(16.dp)
                        //                        ) {
                        //                            Icon(
                        //                                imageVector = Icons.Default.ArrowBack,
                        //                                contentDescription = "Back",
                        //                                tint = Color.Black
                        //                            )
                        //                        }

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
                                    DesignPreview(
                                        designItems,
                                        onTableClick = { selectedTable ->
                                            if (selectedPlaceId != null) {
                                                navController.navigate("save_reservation_screen/${selectedPlaceId}/${selectedTable.designId}")
                                            }
                                        },
                                        reservations = reservationsState

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
                                            viewModel.fetchDesign(place.id)
                                            viewModel.fetchReservations(place.id)
                                            showDesignPreview = true
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
fun DesignPreview(
    designItems: List<DesignItem>,
    reservations: List<DesignItem>,
    onTableClick: (DesignItem) -> Unit = {}
) {
    val density = LocalDensity.current.density

    // Get a list of reserved table IDs from the reservations
    val reservedTableIds = reservations.filter { it.isReserved }.map { it.designId }

// Get a list of available table IDs (not reserved)

// Log reserved and available table IDs
    Log.d("DesignPreview", "Reserved Table IDs: $reservedTableIds")


// Instead of filtering out reserved tables, ensure only available ones are selectable
    // If there are no reservations, show all tables
    val availableDesignItems = designItems.filter { it.type == "TABLE" && it.designId !in reservedTableIds }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(600.dp)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        if (availableDesignItems.isEmpty()) {
            Text(
                text = "No Available Tables",
                color = Color.Black
            )
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                availableDesignItems.forEach { item ->
                    // Show available tables (those that are not reserved)
                    Box(
                        modifier = Modifier
                            .offset(
                                (item.xPosition / density).dp,
                                (item.yPosition / density).dp
                            )
                            .size(30.dp)
                            .background(Color.Red)
                            .clickable {
                                onTableClick(item)
                            }

                    )
                }
            }
        }
    }
}