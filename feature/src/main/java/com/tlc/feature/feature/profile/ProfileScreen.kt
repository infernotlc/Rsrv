package com.tlc.feature.feature.profile

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.wear.compose.material.Text

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ProfileScreen(navController: NavHostController, onLogout: () -> Unit) {

    Scaffold(modifier = Modifier.fillMaxWidth(),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()

        ) {
            Button(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally), onClick = {
                    onLogout()
                },
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(Color.Black)
            ) {
                Text("Sign Out")
            }


        }
    }
}

