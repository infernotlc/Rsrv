package com.tlc.feature.feature.reservation.util

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tlc.feature.R

@Composable
fun PetCountDropdown(
    selectedCount: Int?,
    onCountSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val options = (1..10).toList()

    Card(
        modifier = Modifier
            .size(width = 100.dp, height = 100.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .clickable { expanded = true }
        ) {
            Column(
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.surface)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        painterResource(R.drawable.dog),
                        contentDescription = null
                    )
                }
                Text(
                    text = selectedCount?.toString() ?: "Companion Count",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { count ->
                DropdownMenuItem(
                    text = { Text(count.toString()) },
                    onClick = {
                        onCountSelected(count)
                        expanded = false
                    }
                )
            }
        }
    }
}
