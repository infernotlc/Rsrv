package com.tlc.feature.feature.settings

import android.annotation.SuppressLint
import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.tlc.feature.feature.admin.viewmodel.AdminViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun YourPlaceScreen(
    navController: NavHostController,
    adminViewModel: AdminViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val countries by adminViewModel.countriesState.collectAsState()
    val isLoadingCountries by adminViewModel.isLoadingCountries.collectAsState()
    val countriesError by adminViewModel.countriesError.collectAsState()

    var selectedCountry by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf("") }
    var countryExpanded by remember { mutableStateOf(false) }
    var cityExpanded by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    // Load existing user location if available
    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            try {
                val snapshot = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()
                val country = snapshot.getString("country") ?: ""
                val city = snapshot.getString("city") ?: ""
                selectedCountry = country
                selectedCity = city

                // If coming from redirect (no city set), show info toast once
                if (city.isEmpty()) {
                    Toast.makeText(
                        context,
                        "Please select your country and city to show places.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (_: Exception) {
            }
        }
    }

    Scaffold(
        containerColor = Color.Black,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Your Place",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Location",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 8.dp)
                )

                // Country selection
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (!isLoadingCountries && countriesError == null && countries.isNotEmpty()) {
                                    countryExpanded = true
                                } else if (countriesError != null) {
                                    Toast
                                        .makeText(
                                            context,
                                            "Error: $countriesError",
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
                                }
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isLoadingCountries) {
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
                        } else if (countriesError != null) {
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
                        if (!isLoadingCountries && countriesError == null && countries.isNotEmpty()) {
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
                        countries
                            .filter { !it.country.contains("Failed to resolve", ignoreCase = true) }
                            .forEach { countryData ->
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text(countryData.country) },
                                    onClick = {
                                        selectedCountry = countryData.country
                                        selectedCity = ""
                                        countryExpanded = false
                                    }
                                )
                            }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // City selection
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
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
                        countries
                            .find { it.country == selectedCountry }
                            ?.cities
                            ?.forEach { city ->
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

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (selectedCountry.isEmpty()) {
                            Toast
                                .makeText(
                                    context,
                                    "Please select a country.",
                                    Toast.LENGTH_SHORT
                                )
                                .show()
                            return@Button
                        }
                        if (selectedCity.isEmpty()) {
                            Toast
                                .makeText(
                                    context,
                                    "Please select a city.",
                                    Toast.LENGTH_SHORT
                                )
                                .show()
                            return@Button
                        }

                        val currentUser = FirebaseAuth.getInstance().currentUser
                        if (currentUser == null) {
                            Toast
                                .makeText(
                                    context,
                                    "You need to be logged in.",
                                    Toast.LENGTH_SHORT
                                )
                                .show()
                            return@Button
                        }

                        scope.launch {
                            isSaving = true
                            try {
                                FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(currentUser.uid)
                                    .set(
                                        mapOf(
                                            "country" to selectedCountry,
                                            "city" to selectedCity
                                        ),
                                        SetOptions.merge()
                                    )
                                    .await()

                                Toast
                                    .makeText(
                                        context,
                                        "Location saved.",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                                navController.navigateUp()
                            } catch (e: Exception) {
                                Toast
                                    .makeText(
                                        context,
                                        e.message ?: "Failed to save location.",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black,
                        disabledContainerColor = Color.Gray,
                        disabledContentColor = Color.DarkGray
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(text = "Save Location")
                    }
                }
            }
        }
    )
}

