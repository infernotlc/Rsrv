package com.tlc.feature.feature.design

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Color
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
    val saveState by viewModel.saveState.collectAsState()

    // Local state for design items
    val designItems = remember { mutableStateListOf<DesignItem>() }

    // Load design items when the screen is first loaded
    LaunchedEffect(placeId) {
        viewModel.loadDesign(placeId)
    }

    // Update local designItems when UI state changes
    LaunchedEffect(uiState.designItems) {
        if (uiState.designItems != null) {
            designItems.clear()
            designItems.addAll(uiState.designItems)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
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
                .padding(8.dp)
        ) {
            designItems.forEach { item ->
                DraggableItem(
                    item = item,
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
