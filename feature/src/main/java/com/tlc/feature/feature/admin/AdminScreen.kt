package com.tlc.feature.feature.admin

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.tlc.data.mapper.firebase_mapper.toPlace
import com.tlc.domain.model.firebase.PlaceData
import com.tlc.domain.utils.RootResult
import com.tlc.feature.R
import com.tlc.feature.feature.admin.component.AddPlaceDialog
import com.tlc.feature.feature.admin.component.ConfirmationDialog
import com.tlc.feature.feature.admin.component.UpdatePlaceDialog
import com.tlc.feature.feature.admin.viewmodel.AdminViewModel
import com.tlc.feature.feature.component.LoadingLottie
import com.tlc.feature.feature.component.auth_components.AuthButtonComponent
import com.tlc.feature.navigation.NavigationGraph
import com.tlc.feature.navigation.main_datastore.MainDataStore
import com.tlc.feature.navigation.utils.SharedPreferencesHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AdminScreen(
    navController: NavController,
    adminViewModel: AdminViewModel = hiltViewModel()
) {
    val addDeleteState by adminViewModel.addDeleteState.collectAsState()
    val getAllState by adminViewModel.getAllState.collectAsState()
    val openDialog = remember { mutableStateOf(false) }
    val openUpdateDialog = remember { mutableStateOf(false) }
    val selectedPlaceData = remember { mutableStateOf<PlaceData?>(null) }
    var placeDataDatatoUpdate by remember { mutableStateOf<PlaceData?>(null) }

    val context = LocalContext.current
    val selectedImageUri = remember { mutableStateOf<Uri?>(null) }
    val mainDataStore = remember { MainDataStore(context) }
    val galleryPermissionGranted by mainDataStore.readGalleryPermission.collectAsState(initial = false)

     val sharedPreferencesHelper = SharedPreferencesHelper(context)

    BackHandler {
        (context as? Activity)?.finish()
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri.value = uri
    }

    // Initialize permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Save permission granted status in DataStore
            CoroutineScope(Dispatchers.IO).launch {
                mainDataStore.saveGalleryPermission(true)
            }
        } else {
            Toast.makeText(
                context,
                "Gallery permission is required to select an image.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val apiLevel = Build.VERSION.SDK_INT
    LaunchedEffect(Unit) {
        adminViewModel.getAllPlaces()

        if (apiLevel < 33 && !galleryPermissionGranted) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    mainDataStore.saveGalleryPermission(true)
                }
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            if (getAllState.result !is RootResult.Loading) {
                FloatingActionButton(
                    onClick = { openDialog.value = true },
                    containerColor = Color.Black,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Place")
                }
            }
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                if (openDialog.value) {
                    AddPlaceDialog(
                        onDismiss = { openDialog.value = false },
                        onSave = { place ->
                            adminViewModel.uploadImageAndAddPlace(
                                selectedImageUri.value!!,
                                place.name,
                                reservationTimes = place.reservationTimes

                            )
                        },
                        onImagePick = {
                            if (apiLevel >= 33 || galleryPermissionGranted) {
                                imagePickerLauncher.launch("image/*")
                            } else {
                                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                            }
                        },
                        selectedImageUri = selectedImageUri.value,
                        context = context
                    )
                }

                if (openUpdateDialog.value && selectedPlaceData.value != null) {
                    placeDataDatatoUpdate?.let { it1 ->
                        UpdatePlaceDialog(
                            placeData = it1,
                            onDismiss = { openUpdateDialog.value = false },
                            onUpdatePlace = { placeData ->
                                adminViewModel.updatePlace(
                                    placeData.id,
                                    placeData,
                                    selectedImageUri.value
                                )
                                openUpdateDialog.value = false
                            },
                            onImagePick = {
                                if (apiLevel >= 33 || galleryPermissionGranted) {
                                    imagePickerLauncher.launch("image/*")
                                } else {
                                    // Request permission if not granted
                                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                                }
                            },
                            selectedImageUri = selectedImageUri.value
                        )
                    }
                }

                when (val state = getAllState.result) {
                    is RootResult.Loading -> {
                        Column(
                            Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LoadingLottie(R.raw.loading_lottie)
                        }
                    }

                    is RootResult.Success -> {
                        if (getAllState.places.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "You don't have any places yet.",
                                    modifier = Modifier.padding(bottom = 10.dp),

                                    style = TextStyle(
                                        fontSize = 20.sp
                                    )
                                )
                                Text(
                                    text = "Start adding now.",
                                    style = TextStyle(
                                        fontSize = 20.sp
                                    )
                                )
                            }
                        }

                        val places = state.data ?: emptyList()

                        LazyColumn(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .weight(1f)
                        ) {
                            items(places.reversed()) { place ->
                                PlacesCard(
                                    placeData = place,
                                    onClick = {
                                        val placeData = place.toPlace()
                                        sharedPreferencesHelper.placeName = place.name
                                        navController.navigate(
                                            NavigationGraph.getDesignRoute(placeData)
                                        )
                                    },
                                    onDelete = {
                                        Log.d(
                                            "AddPlace",
                                            "Delete button clicked for place: ${place.id}"
                                        )
                                        adminViewModel.deletePlace(place)
                                    },
                                    onUpdate = {
                                        selectedPlaceData.value = place
                                        placeDataDatatoUpdate = place
                                        openUpdateDialog.value = true
                                    }
                                )
                            }
                        }
                    }

                    is RootResult.Error -> {
                        Text(text = state.message)
                    }

                    else -> {}
                }

                when (val addState = addDeleteState.result) {
                    is RootResult.Loading -> {
                        Column(
                            Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LoadingLottie(R.raw.loading_lottie)
                        }
                    }

                    is RootResult.Success -> {
                        Log.d(
                            "ChooseSportScreen",
                            "Place added/updated successfully: ${addState.data}"
                        )
                    }

                    is RootResult.Error -> {
                        Text(text = addState.message)
                    }

                    else -> {}
                }
            }
        }
    )
}


@Composable
fun PlacesCard(
    placeData: PlaceData,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onUpdate: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var deleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, start = 10.dp, end = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    onClick()
                }
        ) {
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading_lottie))
            var isImageLoading by remember { mutableStateOf(true) }

            SubcomposeAsyncImage(
                model = placeData.placeImageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentScale = ContentScale.Crop,
                loading = {
                    LottieAnimation(
                        composition,
                        modifier = Modifier.size(100.dp),
                        iterations = Int.MAX_VALUE
                    )
                },
                onSuccess = {
                    isImageLoading = false
                },
                onError = {
                    isImageLoading = false
                }
            )
            if (!isImageLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                )

                Box(
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = placeData.name,
                        color = Color.Red,
                        style = TextStyle(
                            fontSize = 35.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            shadow = Shadow(
                                color = Color(0xFF333333),
                                offset = androidx.compose.ui.geometry.Offset(4f, 4f),
                                blurRadius = 10f
                            )
                        )
                    )

                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(24.dp, 24.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        )

                ) {
                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Expand",
                            tint = Color.White,
                        )
                    }
                }
            }
        }

        AnimatedVisibility(visible = expanded) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.Black),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AuthButtonComponent(
                    value = "Delete",
                    onClick = { deleteDialog = true },
                    fillMaxWidth = false,
                    modifier = Modifier.width(80.dp),
                    heightIn = 37.dp,
                    firstColor = Color.White
                )

                AuthButtonComponent(
                    value = "Update",
                    onClick = onUpdate,
                    fillMaxWidth = false,
                    modifier = Modifier.width(80.dp),
                    heightIn = 37.dp
                )
            }
        }
        if (deleteDialog) {
            ConfirmationDialog(
                onDismiss = { deleteDialog = it },
                onConfirm = {
                    onDelete()
                    deleteDialog = false
                }
            )
        }
    }
}