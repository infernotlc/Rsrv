package com.tlc.feature.feature.reservation

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.tlc.domain.model.firebase.Reservation
import com.tlc.feature.feature.reservation.util.AvailableTimesDropdown
import com.tlc.feature.feature.reservation.util.DatePickerWithDialog
import com.tlc.feature.feature.reservation.util.PetCountDropdown
import com.tlc.feature.feature.reservation.util.PhoneNumber
import com.tlc.feature.feature.reservation.util.RsrvCountDropdown
import com.tlc.feature.feature.reservation.viewmodel.ReservationUiState
import com.tlc.feature.feature.reservation.viewmodel.SaveReservationViewModel
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun SaveReservationScreen(
    navController: NavHostController,
    placeId: String,
    tableId: String
) {
    val viewModel: SaveReservationViewModel = hiltViewModel()
    var customerName by remember { mutableStateOf("") }
    var customerPhoneNo by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf<String?>(null) }
    var selectedCount by remember { mutableStateOf<Int?>(null) }
    var selectedAnimalCount by remember { mutableStateOf<Int?>(null) }
    var selectedTableId by remember { mutableStateOf(tableId) }

    val availableTimes by viewModel.availableTimes.collectAsState(initial = emptyList())
    val currentUser = FirebaseAuth.getInstance().currentUser

    LaunchedEffect(date, selectedTableId) { // Fetch times whenever date or table changes
        if (date.isNotBlank() && selectedTableId.isNotBlank()) {
            viewModel.fetchAvailableTimes(placeId, selectedTableId, date)
        }
    }

    Scaffold(
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    // Header
                    Text(
                        text = "New Reservation",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    // Customer Information Section
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Customer Information",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = customerName,
                            onValueChange = { customerName = it },
                            label = { Text("Reservation Holder Name") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Black,
                                unfocusedBorderColor = Color.Gray,
                                focusedLabelColor = Color.Black,
                                unfocusedLabelColor = Color.Gray
                            )
                        )

                        PhoneNumber(
                            phoneNumber = customerPhoneNo,
                            onPhoneNumberChange = { customerPhoneNo = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Table Selection Section (only show if no tableId provided)
                    if (tableId.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Table Selection",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            Text(
                                text = "Please select a table first to continue with your reservation",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            
                            // Simple table selection - you can enhance this with actual table data
                            Button(
                                onClick = { selectedTableId = "table_1" }, // Placeholder - replace with actual table selection
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                            ) {
                                Text("Select Table 1", color = Color.White)
                            }
                            
                            Button(
                                onClick = { selectedTableId = "table_2" }, // Placeholder - replace with actual table selection
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                            ) {
                                Text("Select Table 2", color = Color.White)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Date and Time Section
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Date & Time",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                DatePickerWithDialog(
                                    modifier = Modifier.weight(1f),
                                    onDateSelected = { selectedDate ->
                                        date = selectedDate
                                    }
                                )

                                AvailableTimesDropdown(
                                    selectedTime = selectedTime,
                                    availableTimes = availableTimes,
                                    onTimeSelected = { selectedTime = it },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Guest Count Section
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Guest Count",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                RsrvCountDropdown(
                                    selectedCount = selectedCount,
                                    onCountSelected = { count -> selectedCount = count },
                                    modifier = Modifier.weight(1f)
                                )

                                PetCountDropdown(
                                    selectedCount = selectedAnimalCount,
                                    onCountSelected = { count -> selectedAnimalCount = count },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Save Button
                    Button(
                        onClick = {
                            if (customerName.isBlank() || customerPhoneNo.isBlank() || date.isBlank() || selectedTime.isNullOrBlank() || selectedCount == null) {
                                viewModel.updateUiState(ReservationUiState.Error("All fields are required"))
                            } else {
                                val userId = currentUser?.uid ?: ""
                                val reservation = Reservation(
                                    id = UUID.randomUUID().toString(),
                                    userId = userId,
                                    placeId = placeId,
                                    placeName = "", // This will be set by the repository
                                    tableId = selectedTableId,
                                    holderName = customerName,
                                    holderPhoneNo = customerPhoneNo,
                                    customerCount = selectedCount!!,
                                    animalCount = selectedAnimalCount ?: 0,
                                    date = date,
                                    time = selectedTime!!,
                                    status = "active",
                                    timestamp = Timestamp.now()
                                )
                                viewModel.saveReservation(placeId, reservation)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Save Reservation",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Status Messages
                when (viewModel.uiState.value) {
                    is ReservationUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color.Black,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    is ReservationUiState.Success -> {
                        val message =
                            (viewModel.uiState.value as ReservationUiState.Success).message
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Text(
                                text = message,
                                color = Color.Green,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(16.dp)
                                    .background(
                                        Color(0xFFE8F5E8),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(
                                            8.dp
                                        )
                                    )
                                    .padding(12.dp)
                            )
                        }
                    }

                    is ReservationUiState.Error -> {
                        val message = (viewModel.uiState.value as ReservationUiState.Error).message
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Text(
                                text = message,
                                color = Color.Red,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(16.dp)
                                    .background(
                                        Color(0xFFFFEBEE),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(
                                            8.dp
                                        )
                                    )
                                    .padding(12.dp)
                            )
                        }
                    }

                    else -> {}
                }
            }
        }
    )
}






