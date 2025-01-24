package com.tlc.feature.feature.reservation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tlc.feature.feature.reservation.util.DatePickerWithDialog
import com.tlc.feature.feature.reservation.util.TimePicker
import com.tlc.feature.feature.reservation.viewmodel.ReservationViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReservationScreen(
    placeId: String,
    chairId: String,
    navController: NavController,
    viewModel: ReservationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()


    LaunchedEffect(Unit) {
        viewModel.loadReservations(placeId)
    }

    LaunchedEffect(chairId) {
        viewModel.toggleChairSelection(chairId)
    }

    val backStackEntry = navController.currentBackStackEntry
    val placeId = backStackEntry?.arguments?.getString("placeId")
    val chairId = backStackEntry?.arguments?.getString("chairId")


    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Reservation Screen", style = MaterialTheme.typography.labelMedium)

        // Date and Time Pickers
        DatePickerWithDialog(onDateSelected = { viewModel.updateDate(it) },)
        TimePicker(onTimeSelected = { viewModel.updateTime(it) })

        // Chairs List
        LazyColumn {
            items(state.availableChairs) { chair ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (chairId != null) {
                                viewModel.toggleChairSelection(chairId)
                            }
                        }
                        .background(if (chair.isReserved) Color.Gray else Color.White)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Chair: $chairId")
                }
            }
        }

        Button(
            onClick = {
                Log.d("ReservationScreen", "Save button clicked")
                if (placeId != null) {
                    viewModel.saveReservation(placeId)
                }
            },
            modifier = Modifier.align(Alignment.End).padding(top = 16.dp)
        ) {
            Text("Save Reservation")
        }

        Log.d("ReservationScreen", "Selected Chairs: ${state.availableChairs.filter { it.isReserved }}")

        // Loading and Error Handling
        if (state.isLoading) {
            CircularProgressIndicator()
        }
        state.errorMessage?.let {
            Text("Error: $it", color = Color.Red)
        }
    }
}

