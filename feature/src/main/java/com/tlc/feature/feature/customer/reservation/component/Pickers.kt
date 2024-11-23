package com.tlc.feature.feature.customer.reservation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun DatePicker(selectedDate: String, onDateSelected: (String) -> Unit) {
    // Example Date Picker using Dialog or any desired method
    TextButton(onClick = { /* Show date picker */ }) {
        Text(selectedDate.ifEmpty { "Select Date" })
    }
}

@Composable
fun TimeSlotPicker(selectedTimeSlot: String, onTimeSlotSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }  // Control dropdown visibility

    Box {
        // Button to open the dropdown
        TextButton(onClick = { expanded = true }) {
            Text(selectedTimeSlot.ifEmpty { "Select Time Slot" })
        }

        // Dropdown menu with time slots
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            listOf("10:00 AM", "12:00 PM", "02:00 PM").forEach { slot ->
                DropdownMenuItem(onClick = {
                    onTimeSlotSelected(slot)
                    expanded = false
                },
                    text = {
                        run { Text(slot) }
                    })
            }
        }
    }
}
