package com.tlc.feature.feature.customer

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

@Composable
fun CustomerScreen(
    navController: NavHostController,
    viewModel: CustomerViewModel = hiltViewModel(),
) {
    val placesState by viewModel.placeState.collectAsState()
    val designState by viewModel.designState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchPlaces()
    }

    Scaffold(
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White)
            ) {
                when (placesState.result) {
                    is RootResult.Loading -> {
                        CircularProgressIndicator(
                            color = Color.Black,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    is RootResult.Success -> {
                        val places = (placesState.result as RootResult.Success<List<Place>>).data ?: emptyList()
                        LazyColumn {
                            items(places) { place ->
                                PlaceItem(
                                    place = place,
                                    onClick = {
                                        viewModel.fetchDesign(place.id)
                                    }
                                )
                            }
                        }
                    }

                    is RootResult.Error -> {
                        Text(
                            text = (placesState.result as RootResult.Error).message,
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    else -> Unit
                }

                when (designState.result) {
                    is RootResult.Loading -> {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    is RootResult.Success -> {
                        val designItems = (designState.result as RootResult.Success<List<DesignItem>>).data
                        if (designItems != null) {
                            DesignPreview(designItems)
                        }
                    }

                    is RootResult.Error -> {
                        Text(
                            text = (designState.result as RootResult.Error).message,
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    else -> Unit
                }
            }
        }
    )
}

@Composable
fun PlaceItem(place: Place, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
    ) {
        Text(
            text = place.name,
            modifier = Modifier.padding(16.dp),
            color = Color.Black // Ensure the text color is visible against the background
        )
    }
}

@Composable
fun DesignPreview(designItems: List<DesignItem>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(8.dp)
            .background(Color.Blue),
        contentAlignment = Alignment.Center
    ) {
        if (designItems.isEmpty()) {
            Text(
                text = "No Design Available",
                color = Color.Black
            )
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                designItems.forEach { item ->
                    when (item.type) {
                        "table" -> {
                            drawCircle(
                                color = Color.Blue,
                                radius = 30f,
                                center = Offset(item.xPosition, item.yPosition)
                            )
                        }
                        "chair" -> {
                            drawRect(
                                color = Color.Green,
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