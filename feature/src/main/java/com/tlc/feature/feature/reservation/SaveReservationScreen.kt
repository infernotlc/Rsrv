package com.tlc.feature.feature.reservation

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.firebase.Timestamp
import com.tlc.domain.model.firebase.Reservation
import com.tlc.feature.feature.reservation.util.AvailableTimesDropdown
import com.tlc.feature.feature.reservation.util.DatePickerWithDialog
import com.tlc.feature.feature.reservation.util.PetCountDropdown
import com.tlc.feature.feature.reservation.util.PhoneNumber
import com.tlc.feature.feature.reservation.util.RsrvCountDropdown
import com.tlc.feature.feature.reservation.viewmodel.ReservationUiState
import com.tlc.feature.feature.reservation.viewmodel.SaveReservationViewModel
import kotlinx.coroutines.flow.forEach

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

    val availableTimes by viewModel.availableTimes.collectAsState(initial = emptyList())

    LaunchedEffect(date) { // Fetch times whenever date changes
        if (date.isNotBlank()) {
            viewModel.fetchAvailableTimes(placeId, tableId, date)
        }
    }

    Scaffold(
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Reservation Holder Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                PhoneNumber(
                    phoneNumber = customerPhoneNo,
                    onPhoneNumberChange = { customerPhoneNo = it },
                    modifier = Modifier
                        .width(200.dp)
                        .background(Color.White)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        DatePickerWithDialog(
                            modifier = Modifier.weight(1f),
                            onDateSelected = { selectedDate ->
                                date = selectedDate
                            }
                        )
                        Spacer(modifier = Modifier.width(16.dp))

                        AvailableTimesDropdown(
                            selectedTime = selectedTime,
                            availableTimes = availableTimes,
                            onTimeSelected = { selectedTime = it },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    RsrvCountDropdown(
                        selectedCount = selectedCount,
                        onCountSelected = { count -> selectedCount = count },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))

                    PetCountDropdown(
                        selectedCount = selectedAnimalCount,
                        onCountSelected = { count -> selectedAnimalCount = count },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (customerName.isBlank() || customerPhoneNo.isBlank() || date.isBlank() || selectedTime.isNullOrBlank() || selectedCount == null) {
                            viewModel.updateUiState(ReservationUiState.Error("All fields are required"))
                        } else {
                            val reservation = Reservation(
                                tableId = tableId,
                                holderName = customerName,
                                holderPhoneNo = customerPhoneNo,
                                customerCount = selectedCount!!,
                                animalCount = selectedAnimalCount ?: 0,
                                date = date,
                                time = selectedTime!!,
                                timestamp = Timestamp.now()
                            )
                            viewModel.saveReservation(placeId, listOf(reservation))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(Color.Black)
                ) {
                    Text("Save Reservation")
                }
            }
        }
    )
    when (viewModel.uiState.value) {
        is ReservationUiState.Loading -> {
            CircularProgressIndicator()
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






