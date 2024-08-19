package com.tlc.feature.feature.admin

import android.util.Log
import android.widget.Toast
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController

@Composable
fun AdminScreen(
    navController: NavHostController
)
{
Toast.makeText(LocalContext.current, "Admin Screen", Toast.LENGTH_SHORT).show()
}