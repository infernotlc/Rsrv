package com.tlc.feature.feature.reservation

import android.annotation.SuppressLint
import android.os.Build
import android.util.Base64
import android.widget.DatePicker
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableIntStateOf
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
import com.tlc.feature.feature.reservation.util.DatePickerWithDialog
import com.tlc.feature.feature.reservation.util.PetCountDropdown
import com.tlc.feature.feature.reservation.util.PhoneNumber
import com.tlc.feature.feature.reservation.util.ReservationCountDropdown
import com.tlc.feature.feature.reservation.util.TimePicker
import com.tlc.feature.feature.reservation.viewmodel.ReservationUiState
import com.tlc.feature.feature.reservation.viewmodel.SaveReservationViewModel

@RequiresApi(Build.VERSION_CODES.O)
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
    var customerPhoneNo by remember { mutableStateOf("") } // Change to String
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var selectedCount by remember { mutableStateOf<Int?>(null) }
    var selectedAnimalCount by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Reservation") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Reservation Holder Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Column(
                    modifier =
                    Modifier.align(Alignment.CenterHorizontally)
                ) {
                    PhoneNumber(
                        phoneNumber = customerPhoneNo,
                        onPhoneNumberChange = { customerPhoneNo = it },
                        modifier = Modifier
                            .width(20.dp)
                            .background(Color.White)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                ) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                    ) {
                        DatePickerWithDialog(
                            modifier = Modifier.weight(1f),
                            onDateSelected = { date = it }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        TimePicker(
                            onTimeSelected = { time = it },
                            modifier = Modifier.weight(1f)
                        )

                    }
                }
                Spacer(
                    modifier = Modifier
                        .height(16.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                ) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                    ) {
                        ReservationCountDropdown(
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
                }



                Spacer(modifier = Modifier.height(16.dp))

                // Save Button
                Button(
                    onClick = {
                        // if (customerName.isBlank() || customerPhoneNo.isBlank() || date.isBlank() || time.isBlank() || selectedCount == null) {
                        viewModel.updateUiState(ReservationUiState.Error("All fields are required"))
                        //} else {

                        val reservation = Reservation(
                            chairId = chairId,
                            holderName = customerName,
                            holderPhoneNo = customerPhoneNo,
                            customerCount = selectedCount!!,
                            animalCount = selectedAnimalCount ?: 0,
                            date = date,
                            time = time,
                            isApproved = false,
                            timestamp = Timestamp.now()
                        )
                        viewModel.saveReservation(placeId, listOf(reservation))
                        //    }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(Color.Black)
                ) {
                    Text("Save Reservation")
                }

                when (viewModel.uiState.value) {
                    is ReservationUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }

                    is ReservationUiState.Success -> {
                        val message =
                            (viewModel.uiState.value as ReservationUiState.Success).message
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
