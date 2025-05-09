package com.tlc.feature.feature.reservation.util


import android.os.Build
import android.widget.DatePicker
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerWithDialog(
    modifier: Modifier = Modifier,
    onDateSelected: (String) -> Unit,
) {
    val dateState = rememberDatePickerState()
    val calendar = Calendar.getInstance()
    val todayMillis = calendar.timeInMillis

    val millisToLocalDate = dateState.selectedDateMillis?.let {
        DateUtils().convertMillisToLocalDate(it)
    }

    LaunchedEffect(true) {
        dateState.selectedDateMillis = null
    }

    val dateToString = millisToLocalDate?.let {
        DateUtils().dateToString(it)
    } ?: "Choose Date"

    var showDialog by remember { mutableStateOf(false) }

    // Handle date selection validation
    LaunchedEffect(dateState.selectedDateMillis) {
        if (dateState.selectedDateMillis != null && dateState.selectedDateMillis!! < todayMillis) {
            // Reset the date if it's in the past
            dateState.selectedDateMillis = todayMillis
        }
    }

    Card(
        modifier = Modifier
            .size(width = 100.dp, height = 100.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .clickable {
                    showDialog = true
                }
        ) {
            Column(
                modifier = modifier
                    .background(color = MaterialTheme.colorScheme.surface)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = { showDialog = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                }
                Text(
                    text = dateToString,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (showDialog) {
                    DatePickerDialog(
                        onDismissRequest = { showDialog = false },
                        confirmButton = {
                            TextButton(onClick = {
                                showDialog = false
                                millisToLocalDate?.let {
                                    val formattedDate = DateUtils().dateToString(it)
                                    onDateSelected(formattedDate) // Pass the formatted date
                                }
                            }) { Text("OK", color = MaterialTheme.colorScheme.primary) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDialog = false }) {
                                Text("Cancel", color = MaterialTheme.colorScheme.secondary)
                            }
                        },
                        colors = DatePickerDefaults.colors(
                            containerColor = Color.White
                        )
                    ) {
                        DatePicker(
                            state = dateState,
                            colors = DatePickerDefaults.colors(
                                todayDateBorderColor = Color.Black,
                                todayContentColor = Color.Black,
                                selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                                selectedDayContentColor = Color.White,
                            ),
                            showModeToggle = true
                        )
                    }
                }
            }
        }
    }
}

class DateUtils {
    @RequiresApi(Build.VERSION_CODES.O)
    fun convertMillisToLocalDate(millis: Long): LocalDate {
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun dateToString(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
        return date.format(formatter)
    }
}
