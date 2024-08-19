package com.tlc.feature.feature.customer

import android.widget.Toast
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController

@Composable
fun CustomerScreen(
    navController: NavController
) {
    Toast.makeText(LocalContext.current, "Customer Screen", Toast.LENGTH_SHORT).show()
}