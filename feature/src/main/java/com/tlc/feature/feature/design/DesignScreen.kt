package com.tlc.feature.feature.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tlc.data.remote.dto.firebase_dto.Place
import com.tlc.domain.model.firebase.DesignItem
import com.tlc.feature.feature.design.viewmodel.DesignViewModel


@Composable
fun DesignScreen(
    navController: NavController,
    viewModel: DesignViewModel = hiltViewModel(),
    place: Place,
    placeId: String
) {
    val uiState by viewModel.uiState.collectAsState()

   
    val designItems = remember { mutableStateListOf<DesignItem>() }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val density = LocalDensity.current
        val screenWidthPx = with(density) { maxWidth.toPx() }
        val screenHeightPx = with(density) { maxHeight.toPx() }

     
        val boundary = remember {
            Rect(
                left = 0f,
                top = 10f,
                right = screenWidthPx,
                bottom = screenHeightPx - 150f
            )
        }

       
        LaunchedEffect(placeId) {
            viewModel.loadDesign(placeId)
        }

        
        LaunchedEffect(uiState.designItems) {
            if (uiState.designItems != null) {
                designItems.clear()
                designItems.addAll(uiState.designItems)
            }
        }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = {
                    // Add a new table only if it doesn't already exist
                    val newTable = DesignItem(
                        type = "TABLE",
                        xPosition = 50f,
                        yPosition = 50f
                    )
                    if (designItems.none { it.type == "TABLE" && it.xPosition == 50f && it.yPosition == 50f }) {
                        designItems.add(newTable)
                    }
                }) {
                    Text("Add Table")
                }
                Button(
                    onClick = {
                        viewModel.saveDesign(placeId, designItems)
                    },
                ) {
                    Text("Save Design")
                }

                Button(onClick = {
                    val newChair = DesignItem(
                        type = "CHAIR",
                        xPosition = 50f,
                        yPosition = 50f
                    )
                    if (designItems.none { it.type == "CHAIR" && it.xPosition == 50f && it.yPosition == 50f }) {
                        designItems.add(newChair)
                    }
                }) {
                    Text("Add Chair")
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray)
            ) {
                designItems.forEach { item ->
                    DraggableItem(
                        item = item,
                        boundary = boundary,
                        onPositionChange = { updatedPosition ->
                            item.xPosition = updatedPosition.x
                            item.yPosition = updatedPosition.y
                        }
                    )
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator()
            }
            
            uiState.error?.let {
                Text("Error: $it", color = Color.Red)
            }
        }
    }
}