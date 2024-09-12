package com.tlc.feature.feature.reservation

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DateTimePicker(label: String, onDateSelected: (Date) -> Unit) {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    val context = LocalContext.current

    var selectedDate by remember { mutableStateOf(calendar.time) }

    Button(onClick = {
        val datePickerDialog = DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val timePickerDialog = TimePickerDialog(
                    context,
                    { _, selectedHour, selectedMinute ->
                        calendar.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute)
                        selectedDate = calendar.time
                        onDateSelected(selectedDate)
                    },
                    hour, minute, true
                )
                timePickerDialog.show()
            },
            year, month, day
        )
        datePickerDialog.show()
    }) {
        Text("$label: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(selectedDate)}")
    }
}
