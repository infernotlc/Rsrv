package com.tlc.feature.feature.admin.component

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.tlc.domain.model.api.CountryData
import com.tlc.domain.model.firebase.PlaceData
import com.tlc.domain.use_cases.api.GetCountriesUseCase
import com.tlc.domain.utils.RootResult
import com.tlc.feature.feature.component.auth_components.AuthButtonComponent
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.tlc.feature.feature.admin.viewmodel.AdminViewModel

@SuppressLint("DefaultLocale")
@Composable
fun AddPlaceDialog(
    onDismiss: () -> Unit,
    onSave: (PlaceData,) -> Unit,
    onImagePick: () -> Unit,
    selectedImageUri: Uri?,
    context: Context,
    adminViewModel: AdminViewModel = hiltViewModel()
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedPlaceData by remember { mutableStateOf<PlaceData?>(null) }
    var customPlaceName by remember { mutableStateOf("") }
    var availableTimes by remember { mutableStateOf<List<String>>(emptyList()) }
    var countryExpanded by remember { mutableStateOf(false) }
    var cityExpanded by remember { mutableStateOf(false) }
    var selectedCountry by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf("") }

    val countries by adminViewModel.countriesState.collectAsState()
    val isLoading by adminViewModel.isLoadingCountries.collectAsState()
    val error by adminViewModel.countriesError.collectAsState()

    val timePickerDialog = remember { mutableStateOf(false) }

    AlertDialog(
        containerColor = Color.Black,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Place",
                style = TextStyle(color = Color.White, fontSize = 25.sp)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                // Image Picker
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .height(128.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .clickable { onImagePick() }
                        .align(Alignment.CenterHorizontally)
                        .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                ) {
                    selectedImageUri?.let { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = "Selected Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } ?: run {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Select Image",
                            modifier = Modifier.align(Alignment.Center).size(50.dp),
                            tint = Color.Red
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Place Name Input
                TextField(
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        focusedTextColor = Color.Black,
                        focusedIndicatorColor = Color.Black,
                        unfocusedIndicatorColor = Color.Black,
                        unfocusedLabelColor = Color.Black,
                        focusedLabelColor = Color.Black,
                        cursorColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    value = customPlaceName,
                    onValueChange = {
                        customPlaceName = it
                        selectedPlaceData = null
                    },
                    label = { Text("Enter Place Name", fontSize = 13.sp, color = Color.Black) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Country Selection
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clickable { 
                                if (!isLoading && error == null && countries.isNotEmpty()) {
                                    countryExpanded = true 
                                } else if (error != null) {
                                    Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.Black,
                                    strokeWidth = 2.dp
                                )
                                Text("Loading countries...", color = Color.Gray)
                            }
                        } else if (error != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Error",
                                    tint = Color.Red
                                )
                                Text("Error loading countries", color = Color.Red)
                            }
                        } else if (countries.isEmpty()) {
                            Text("No countries available", color = Color.Gray)
                        } else {
                            Text(
                                text = selectedCountry.ifEmpty { "Select Country" },
                                color = if (selectedCountry.isEmpty()) Color.Gray else Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        if (!isLoading && error == null && countries.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = "Select Country",
                                tint = Color.Black
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = countryExpanded,
                        onDismissRequest = { countryExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        countries.forEach { countryData ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(countryData.country) },
                                onClick = {
                                    selectedCountry = countryData.country
                                    selectedCity = "" // Reset city when country changes
                                    countryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // City Selection
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clickable { if (selectedCountry.isNotEmpty()) cityExpanded = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedCity.ifEmpty { "Select City" },
                            color = if (selectedCity.isEmpty()) Color.Gray else Color.Black
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Select City",
                            tint = Color.Black
                        )
                    }
                    DropdownMenu(
                        expanded = cityExpanded,
                        onDismissRequest = { cityExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        countries.find { it.country == selectedCountry }?.cities?.forEach { city ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(city) },
                                onClick = {
                                    selectedCity = city
                                    cityExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Add Time Button
                Button(
                    onClick = { timePickerDialog.value = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Time", tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Available Time", color = Color.Black)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Display Selected Times
                availableTimes.forEachIndexed { index, time ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = time, color = Color.White, modifier = Modifier.weight(1f))
                        IconButton(onClick = { availableTimes = availableTimes.toMutableList().apply { removeAt(index) } }) {
                            Icon(imageVector = Icons.Filled.Delete, contentDescription = "Remove", tint = Color.Red)
                        }
                    }
                }
            }
        },
        confirmButton = {
            AuthButtonComponent(
                value = "Save",
                onClick = {
                    if (selectedImageUri == null) {
                        Toast.makeText(context, "Please choose an image.", Toast.LENGTH_SHORT).show()
                        return@AuthButtonComponent
                    }
                    if (customPlaceName.isBlank()) {
                        Toast.makeText(context, "Please enter a place name.", Toast.LENGTH_SHORT).show()
                        return@AuthButtonComponent
                    }
                    if (selectedCountry.isEmpty()) {
                        Toast.makeText(context, "Please select a country.", Toast.LENGTH_SHORT).show()
                        return@AuthButtonComponent
                    }
                    if (selectedCity.isEmpty()) {
                        Toast.makeText(context, "Please select a city.", Toast.LENGTH_SHORT).show()
                        return@AuthButtonComponent
                    }
                    if (availableTimes.isEmpty()) {
                        Toast.makeText(context, "Please add at least one available time.", Toast.LENGTH_SHORT).show()
                        return@AuthButtonComponent
                    }

                    val placeData = PlaceData(
                        name = customPlaceName,
                        placeImageUrl = selectedImageUri.toString(),
                        reservationTimes = availableTimes,
                        country = selectedCountry,
                        city = selectedCity
                    )

                    onSave(placeData)
                    onDismiss()
                },
                modifier = Modifier.width(70.dp),
                fillMaxWidth = false,
                heightIn = 40.dp,
                firstColor = Color.Black,
                secondColor = Color.Black
            )
        },
        dismissButton = {
            AuthButtonComponent(
                value = "Cancel",
                onClick = { onDismiss() },
                modifier = Modifier.width(80.dp),
                fillMaxWidth = false,
                heightIn = 40.dp,
                firstColor = Color.Black,
                secondColor = Color.Black
            )
        }
    )

    // Time Picker Dialog
    if (timePickerDialog.value) {
        val timePicker = TimePickerDialog(
            context,
            { _, hour, minute ->
                val selectedTime = String.format("%02d:%02d", hour, minute)
                availableTimes = availableTimes + selectedTime
            },
            12, 0, true
        )
        timePicker.show()
        timePickerDialog.value = false
    }
}
