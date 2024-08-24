package com.tlc.feature.feature.admin.component

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import com.tlc.domain.model.firebase.PlaceData
import com.tlc.feature.feature.component.auth_components.AuthButtonComponent

@Composable
fun AddPlaceDialog(
    onDismiss: () -> Unit,
    onSave: (PlaceData) -> Unit,
    onImagePick: () -> Unit,
    selectedImageUri: Uri?,
    context: Context
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedPlaceData by remember { mutableStateOf<PlaceData?>(null) }
    var customPlaceName by remember { mutableStateOf("") }

    AlertDialog(
        containerColor = Color.White,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Competition",
                style = TextStyle(
                    color = Color.White,
                    fontSize = 25.sp,
                )
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(128.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .clickable { onImagePick() }
                        .align(Alignment.CenterHorizontally)
                        .border(
                            width = 1.dp,
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp)
                        )
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
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(50.dp),
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .border(
                                width = 1.dp,
                                color = Color.White,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                expanded = true
                                customPlaceName = ""
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedPlaceData?.name ?: "Select Competition",
                            modifier = Modifier.weight(1f),
                            color = Color.White
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
                            modifier = Modifier.padding(8.dp),
                            tint = Color.White
                        )
                    }
                    DropdownMenu(
                        modifier = Modifier
                            .background(Color.White)
                            .height(300.dp),
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {

                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        focusedTextColor = Color.White,
                        focusedIndicatorColor = Color.White,
                        unfocusedIndicatorColor = Color.White,
                        unfocusedLabelColor = Color.White,
                        focusedLabelColor = Color.White,
                        cursorColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    value = customPlaceName,
                    onValueChange = {
                        customPlaceName = it
                        selectedPlaceData = null
                    },
                    label = {
                        Text(
                            "Or Enter Custom Competition Name", style = TextStyle(
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            AuthButtonComponent(
                value = "Save", onClick = {
                    if (selectedImageUri == null) {
                        Toast.makeText(context, "Please choose an image.", Toast.LENGTH_SHORT)
                            .show()
                        return@AuthButtonComponent
                    }
                    if (selectedPlaceData == null && customPlaceName.isBlank()) {
                        Toast.makeText(
                            context,
                            "Please choose at least one competition",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@AuthButtonComponent
                    }

                    val placeName =
                        selectedPlaceData?.name ?: customPlaceName
                    if (placeName.isNotBlank()) {
                        if (selectedPlaceData != null) {
                            onSave(selectedPlaceData!!)
                        } else {
                            onSave(
                                PlaceData(
                                    name = placeName,
                                    placeImageUrl = selectedImageUri.toString(),

                                    //                                    competitionFirstImage = 0,
//                                    competitionTeamImage = 0
                                )
                            )
                        }
                        onDismiss()
                    }
                },
                modifier = Modifier.width(70.dp),
                fillMaxWidth = false,
                heightIn = 40.dp,
                firstColor = Color.White,
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
                firstColor = Color.White,
                secondColor = Color.Black
            )
        }
    )
}

