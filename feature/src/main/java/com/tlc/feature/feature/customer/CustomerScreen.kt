package com.tlc.feature.feature.customer

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.tlc.domain.model.firebase.DesignItem
import com.tlc.domain.model.firebase.Place
import com.tlc.domain.utils.RootResult
import com.tlc.feature.feature.customer.viewmodel.CustomerViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CustomerScreen(
    navController: NavHostController,
    viewModel: CustomerViewModel = hiltViewModel(),
) {
    val placesState by viewModel.placeState.collectAsState()
    val designState by viewModel.designState.collectAsState()
    var showDesignPreview by remember { mutableStateOf(false) }

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
                        IconButton(
                            onClick = {
                                Log.d("CustomerScreen", "Back button clicked, closing design preview")
                                showDesignPreview = false
                            },
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.Black
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            when (designState.result) {
                                is RootResult.Loading -> {
                                    Log.d("CustomerScreen", "Loading design preview...")
                                    CircularProgressIndicator(
                                        color = Color.Black,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }

                                is RootResult.Success -> {
                                    val designItems = (designState.result as RootResult.Success<List<DesignItem>>).data ?: emptyList()
                                    Log.d("CustomerScreen", "Rendering design preview with ${designItems.size} items")
                                    DesignPreview(designItems)
                                }

                                is RootResult.Error -> {
                                    val errorMessage = (designState.result as RootResult.Error).message
                                    Log.e("CustomerScreen", "Error loading design: $errorMessage")
                                    Text(
                                        text = errorMessage ?: "Failed to load design",
                                        color = Color.Red,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }

                                else -> Unit
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
                            Log.d("CustomerScreen", "Loading places...")
                            CircularProgressIndicator(
                                color = Color.Black,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }

                        is RootResult.Success -> {
                            val places = (placesState.result as RootResult.Success<List<Place>>).data ?: emptyList()
                            Log.d("CustomerScreen", "Places loaded: ${places.size} places")
                            LazyColumn {
                                items(places) { place ->
                                    PlaceItem(
                                        place = place,
                                        onClick = {
                                            Log.d("CustomerScreen", "Place clicked: ${place.name}, ID: ${place.id}")
                                            viewModel.fetchDesign(place.id)
                                            showDesignPreview = true
                                        }
                                    )
                                }
                            }
                        }

                        is RootResult.Error -> {
                            val errorMessage = (placesState.result as RootResult.Error).message
                            Log.e("CustomerScreen", "Error loading places: $errorMessage")
                            Text(
                                text = errorMessage,
                                color = Color.Red,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }

                        else -> Unit
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
fun DesignPreview(designItems: List<DesignItem>) {
    Log.d("DesignPreview", "Rendering design preview with ${designItems.size} items")
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(600.dp)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        if (designItems.isEmpty()) {
            Log.d("DesignPreview", "No design items available")
            Text(
                text = "No Design Available",
                color = Color.Black
            )
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                designItems.forEach { item ->
                    Log.d("DesignPreview", "Drawing item: $item")
                    when (item.type) {
                        "TABLE" -> {
                            drawCircle(
                                color = Color.Red,
                                radius = 30f,
                                center = Offset(item.xPosition, item.yPosition)
                            )
                        }
                        "CHAIR" -> {
                            drawRect(
                                color = Color.Black,
                                topLeft = Offset(item.xPosition, item.yPosition),
                                size = Size(30f, 30f)
                            )
                        }
                    }
                }
            }
        }
    }
}