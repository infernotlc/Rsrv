package com.tlc.feature.feature.component

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

@Composable
fun UnderLinedTextComponent(value: String, onClick: () -> Unit) {
    Card(colors = CardDefaults.cardColors(
        containerColor = Color.Transparent
    ), onClick = {
        onClick()
    }) {
        Text(
            text = value,
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic
            ),
            color = Color.White,
            textAlign = TextAlign.Center,
            textDecoration = TextDecoration.Underline
        )
    }
}