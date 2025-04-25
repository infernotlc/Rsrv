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
import com.tlc.feature.feature.profile.viewmodel.AdminReservationsViewModel

@Composable
fun AdminReservationsScreen(
    navController: NavHostController,
    viewModel: AdminReservationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                text = "All Reservations",
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
                    ReservationCard(reservation)
                }
            }
        }
    }
}

@Composable
private fun ReservationCard(reservation: Reservation) {
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
                text = "Customer Name: ${reservation.holderName}",
                color = Color.White,
                fontSize = 16.sp
            )
            Text(
                text = "Phone: ${reservation.holderPhoneNo}",
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
        }
    }
} 