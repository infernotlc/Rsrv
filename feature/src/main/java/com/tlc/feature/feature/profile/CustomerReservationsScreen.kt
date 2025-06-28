package com.tlc.feature.feature.profile

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.tlc.domain.model.firebase.Reservation
import com.tlc.feature.R
import com.tlc.feature.feature.component.LoadingLottie
import com.tlc.feature.feature.profile.viewmodel.ProfileViewModel

@Composable
fun CustomerReservationsScreen(
    navController: NavHostController,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by profileViewModel.uiState.collectAsState()
    var showCancelDialog by remember { mutableStateOf(false) }
    var reservationToCancel by remember { mutableStateOf<Reservation?>(null) }
    var cancellationNotes by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.width(80.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            
            Text(
                text = "My Reservations",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.width(80.dp))
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoadingLottie(R.raw.loading_lottie)
            }
        } else if (uiState.error != null) {
            Text(
                text = uiState.error!!,
                color = Color.Red,
                modifier = Modifier.padding(16.dp)
            )
        } else if (uiState.reservations.isEmpty()) {
            Text(
                text = "No reservations found",
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.reservations) { reservation ->
                    ReservationCard(
                        reservation = reservation,
                        onCancelClick = {
                            reservationToCancel = reservation
                            cancellationNotes = ""
                            showCancelDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showCancelDialog && reservationToCancel != null) {
        AlertDialog(
            onDismissRequest = { 
                showCancelDialog = false 
                cancellationNotes = ""
            },
            title = {
                Text(
                    text = "Cancel Reservation",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to cancel your reservation for ${reservationToCancel!!.placeName} on ${reservationToCancel!!.date} at ${reservationToCancel!!.time}?",
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = "Please provide a reason for cancellation:",
                        color = Color.Black,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = cancellationNotes,
                        onValueChange = { cancellationNotes = it },
                        placeholder = { Text("Enter cancellation reason...") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Red,
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = Color.Red,
                            unfocusedLabelColor = Color.Gray
                        ),
                        minLines = 3,
                        maxLines = 5
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (cancellationNotes.isNotBlank()) {
                            profileViewModel.cancelReservation(reservationToCancel!!.id, cancellationNotes)
                            showCancelDialog = false
                            reservationToCancel = null
                            cancellationNotes = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    enabled = cancellationNotes.isNotBlank()
                ) {
                    Text("Cancel Reservation", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showCancelDialog = false
                        reservationToCancel = null
                        cancellationNotes = ""
                    }
                ) {
                    Text("Keep Reservation", color = Color.Black)
                }
            }
        )
    }
}

@Composable
private fun ReservationCard(reservation: Reservation, onCancelClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Place: ${reservation.placeName}",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Date: ${reservation.date}",
                color = Color.White,
                fontSize = 16.sp
            )
            Text(
                text = "Time: ${reservation.time}",
                color = Color.White,
                fontSize = 16.sp
            )
            Text(
                text = "Number of People: ${reservation.customerCount}",
                color = Color.White,
                fontSize = 16.sp
            )
            Text(
                text = "Number of Animals: ${reservation.animalCount}",
                color = Color.White,
                fontSize = 16.sp
            )
            Text(
                text = "Phone: ${reservation.holderPhoneNo}",
                color = Color.White,
                fontSize = 16.sp
            )
            Text(
                text = "Status: ${reservation.status.capitalize()}",
                color = if (reservation.status == "cancelled") Color.Red else Color.Green,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            if (reservation.status == "cancelled" && reservation.cancellationNotes.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1B1B))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Cancellation Reason:",
                            color = Color.Red,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = reservation.cancellationNotes,
                            color = Color.White,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            
            if (reservation.status == "active") {
                Button(
                    onClick = onCancelClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(
                        text = "Cancel Reservation",
                        color = Color.White
                    )
                }
            }
        }
    }
} 