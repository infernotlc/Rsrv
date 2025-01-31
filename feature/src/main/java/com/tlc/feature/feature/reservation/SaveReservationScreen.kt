package com.tlc.feature.feature.reservation

import android.annotation.SuppressLint
import android.util.Base64
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.firebase.Timestamp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tlc.domain.model.firebase.Reservation
import com.tlc.feature.feature.reservation.viewmodel.ReservationUiState
import com.tlc.feature.feature.reservation.viewmodel.SaveReservationViewModel

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveReservationScreen(
    navController: NavHostController,
    placeId: String,
    chairId: String
) {
    val viewModel: SaveReservationViewModel = hiltViewModel()
    var customerName by remember { mutableStateOf("") }
    var customerEmail by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Reservation") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
            ) {
                // Input Fields
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Your Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = customerEmail,
                    onValueChange = { customerEmail = it },
                    label = { Text("Your Email") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time (HH:MM)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Save Button
                Button(
                    onClick = {
                        if (customerName.isBlank() || customerEmail.isBlank() || date.isBlank() || time.isBlank()) {
                            viewModel.updateUiState(ReservationUiState.Error("All fields are required"))
                        } else {
                            val reservation = Reservation(
                                chairId = chairId,
                                customerId = customerEmail, // Use email as customer ID
                                date = date,
                                time = time,
                                isApproved = false,
                                timestamp = Timestamp.now()
                            )
                            viewModel.saveReservation(placeId, listOf(reservation))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Reservation")
                }

                // Show loading/error/success states
                when (viewModel.uiState.value) {
                    is ReservationUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    is ReservationUiState.Success -> {
                        val message = (viewModel.uiState.value as ReservationUiState.Success).message
                        Text(text = message, color = Color.Green)
                    }
                    is ReservationUiState.Error -> {
                        val message = (viewModel.uiState.value as ReservationUiState.Error).message
                        Text(text = message, color = Color.Red)
                    }
                    else -> {}
                }
            }
        }
    )
}