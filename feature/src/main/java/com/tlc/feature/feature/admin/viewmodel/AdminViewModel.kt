package com.tlc.feature.feature.admin.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlc.domain.model.api.CountryData
import com.tlc.domain.model.firebase.PlaceData
import com.tlc.domain.use_cases.api.GetCountriesUseCase
import com.tlc.domain.use_cases.firebase.place.AddPlaceUseCase
import com.tlc.domain.use_cases.firebase.place.DeletePlaceUseCase
import com.tlc.domain.use_cases.firebase.place.GetAllPlacesUseCase
import com.tlc.domain.use_cases.firebase.place.UpdatePlaceUseCase
import com.tlc.domain.use_cases.firebase.place.UploadImageUseCase
import com.tlc.domain.utils.RootResult
import com.tlc.domain.utils.RootResult.Loading
import com.tlc.domain.utils.RootResult.Success
import com.tlc.domain.utils.RootResult.Error
import com.tlc.feature.feature.admin.state.AddDeleteState
import com.tlc.feature.feature.admin.state.GetAllState
import com.tlc.feature.feature.admin.state.PlaceImageUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val addPlaceUseCase: AddPlaceUseCase,
    private val deletePlaceUseCase: DeletePlaceUseCase,
    private val getAllPlacesUseCase: GetAllPlacesUseCase,
    private val uploadImageUseCase: UploadImageUseCase,
    private val updatePlaceUseCase: UpdatePlaceUseCase,
    private val getCountriesUseCase: GetCountriesUseCase
) : ViewModel() {

    private val _addDeleteState = MutableStateFlow(AddDeleteState())
    val addDeleteState: StateFlow<AddDeleteState> = _addDeleteState

    private val _getAllState = MutableStateFlow(GetAllState())
    val getAllState: StateFlow<GetAllState> = _getAllState

    private val _placeImageUIState = MutableStateFlow(PlaceImageUIState())
    val placeImageUIState: StateFlow<PlaceImageUIState> get() = _placeImageUIState

    private val _countriesState = MutableStateFlow<List<CountryData>>(emptyList())
    val countriesState: StateFlow<List<CountryData>> = _countriesState

    private val _isLoadingCountries = MutableStateFlow(true)
    val isLoadingCountries: StateFlow<Boolean> = _isLoadingCountries

    private val _countriesError = MutableStateFlow<String?>(null)
    val countriesError: StateFlow<String?> = _countriesError

    init {
        loadCountries()

    }

    private fun loadCountries() {
        viewModelScope.launch {
            getCountriesUseCase().collect { result ->
                when (result) {
                    is Loading -> {
                        _isLoadingCountries.value = true
                        _countriesError.value = null
                        Log.d("AdminViewModel", "Loading countries...")
                    }
                    is Success -> {
                        _isLoadingCountries.value = false
                        _countriesError.value = null
                        result.data?.let { response ->
                            _countriesState.value = response.data
                            Log.d("AdminViewModel", "Countries loaded successfully: ${response.data.size} countries")
                        } ?: run {
                            Log.e("AdminViewModel", "Success but no data received")
                            _countriesError.value = "No data received"
                        }
                    }
                    is Error -> {
                        _isLoadingCountries.value = false
                        _countriesError.value = result.message
                        Log.e("AdminViewModel", "Error loading countries: ${result.message}")
                    }
                }
            }
        }
    }

    private fun addPlace(placeData: PlaceData) {
        _addDeleteState.value = AddDeleteState(isLoading = true, result = Loading)
        viewModelScope.launch {
            addPlaceUseCase(placeData).collect { result ->
                _addDeleteState.value = AddDeleteState(
                    isLoading = result is Loading,
                    result = result,
                    error = if (result is Error) result.message else null
                )
                if (result is RootResult.Success) {
                    getAllPlaces()
                }
            }
        }
    }

    internal fun deletePlace(placeData: PlaceData) {
        _addDeleteState.value = AddDeleteState(isLoading = true, result = RootResult.Loading)
        viewModelScope.launch {
            deletePlaceUseCase(placeData).collect { result ->
                _addDeleteState.value = AddDeleteState(
                    isLoading = result is Loading,
                    result = result,
                    error = if (result is Error) result.message else null
                )
                if (result is Success) {
                    getAllPlaces()
                }
            }
        }
    }

    internal fun getAllPlaces() {
        _getAllState.value = GetAllState(isLoading = true, result = Loading)
        viewModelScope.launch {
            getAllPlacesUseCase().collect { result ->
                _getAllState.value = GetAllState(
                    isLoading = result is Loading,
                    places = if (result is Success) result.data
                        ?: emptyList() else emptyList(),
                    result = result,
                    error = if (result is Error) result.message else null
                )
            }
        }
    }

    internal fun uploadImageAndAddPlace(
        uri: Uri, 
        placeName: String, 
        reservationTimes: List<String>,
        country: String,
        city: String
    ) {
        _placeImageUIState.value = _placeImageUIState.value.copy(isLoading = true)
        viewModelScope.launch {
            uploadImageUseCase(uri, "places").collect { result ->
                when (result) {
                    is Success -> {
                        val imageUrl = result.data
                        _placeImageUIState.value = _placeImageUIState.value.copy(
                            imageUri = imageUrl,
                            isLoading = false,
                            error = null
                        )
                        val place = PlaceData(
                            name = placeName,
                            placeImageUrl = imageUrl ?: "",
                            reservationTimes = reservationTimes,
                            country = country,
                            city = city
                        )
                        addPlace(place)
                    }

                    is Error -> {
                        _placeImageUIState.value = _placeImageUIState.value.copy(
                            imageUri = null,
                            isLoading = false,
                            error = result.message
                        )
                    }

                    is Loading -> {
                        _placeImageUIState.value = _placeImageUIState.value.copy(
                            isLoading = true,
                            error = null,
                            imageUri = null
                        )
                    }
                }
            }
        }
    }

    internal fun updatePlace(
        placeId: String,
        placeData: PlaceData,
        imageUri: Uri?
    ) {
        _addDeleteState.value = AddDeleteState(isLoading = true, result = Loading)
        viewModelScope.launch {
            var updatedPlaceData = placeData
            imageUri?.let { uri ->
                uploadImageUseCase(uri, placeData.name).collect { result ->
                    when (result) {
                        is Success -> {
                            val imageUrl = result.data ?: ""
                            updatedPlaceData =
                                updatedPlaceData.copy(placeImageUrl = imageUrl)
                        }

                        is Error -> {
                            _addDeleteState.value = AddDeleteState(
                                isLoading = false,
                                result = result,
                                error = result.message
                            )
                        }

                        is Loading -> {
                            _addDeleteState.value = AddDeleteState(
                                isLoading = true,
                                result = Loading,
                                error = null
                            )
                        }
                    }
                }
            }
            updatePlaceUseCase(placeId, updatedPlaceData).collect { result ->
                _addDeleteState.value = AddDeleteState(
                    isLoading = result is Loading,
                    result = result,
                    error = if (result is Error) result.message else null
                )
                if (result is Success) {
                    getAllPlaces()
                }
            }
        }
    }
}
