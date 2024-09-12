package com.tlc.feature.feature.reservation

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.tlc.domain.model.firebase.DesignItem
import com.tlc.domain.utils.RootResult
import com.tlc.feature.feature.customer.DesignPreview
import com.tlc.feature.feature.customer.viewmodel.CustomerViewModel
import com.tlc.feature.feature.reservation.viewmodel.MakeAReservationViewModel
import java.sql.Timestamp

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ReservationScreen(
    navController: NavHostController,
    placeId: String,
    reserViewModel: MakeAReservationViewModel = hiltViewModel(),
    customerViewModel: CustomerViewModel = hiltViewModel(),
) {
    val designState by customerViewModel.designState.collectAsState()
    val availabilityState by reserViewModel.availabilityState.collectAsState()

    var startTime by remember { mutableStateOf<Timestamp?>(null) }
    var endTime by remember { mutableStateOf<Timestamp?>(null) }
    var selectedChairId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        customerViewModel.fetchDesign(placeId)
    }

    Scaffold(
        content = {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                DateTimePicker(label = "Start Time") { selectedTime ->
                    startTime = Timestamp(selectedTime.time)
                }
                DateTimePicker(label = "End Time") { selectedTime ->
                    endTime = Timestamp(selectedTime.time)
                }

                when (designState.result) {
                    is RootResult.Success -> {
                        val designItems = (designState.result as RootResult.Success<List<DesignItem>>).data
                        if (designItems != null) {
                            DesignPreview(designItems) { chairId ->
                                selectedChairId = chairId
                            }
                        }
                    }
                    else -> {}
                }

                Button(onClick = {
                    startTime?.let { start ->
                        endTime?.let { end ->
                            selectedChairId?.let { chairId ->
                                reserViewModel.checkAvailability(placeId, chairId, start, end)
                            }
                        }
                    }
                }) {
                    Text("Check Availability")
                }

                when (availabilityState.result) {
                    is RootResult.Success -> {
                        if ((availabilityState.result as RootResult.Success<Boolean>).data == true) {
                            Button(onClick = {
                                selectedChairId?.let { chairId ->
                                    reserViewModel.makeReservation(
                                        designItemId = placeId,
                                        chairId = chairId,
                                        isReserved = true,
                                        reservedBy = "currentUserId",
                                        reservationStartTime = startTime,
                                        reservationEndTime = endTime
                                    )
                                    navController.popBackStack()
                                }
                            }) {
                                Text("Reserve")
                            }
                        } else {
                            Text("Selected time slot is not available.", color = Color.Red)
                        }
                    }
                    is RootResult.Error -> {
                        Text(
                            text = "Error checking availability: ${(availabilityState.result as RootResult.Error).message}",
                            color = Color.Red
                        )
                    }
                    else -> {}
                }
            }
        }
    )
}
@Composable
fun DesignPreview(designItems: List<DesignItem>, onChairSelected: (String) -> Unit) {
    Column {
        designItems.forEach { designItem ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onChairSelected(designItem.designId) }
                    .padding(8.dp)
            ) {
                Text(text = designItem.type)
                if (designItem.isReserved) {
                    Text(text = " (Reserved)", color = Color.Red)
                }
            }
        }
    }
}


