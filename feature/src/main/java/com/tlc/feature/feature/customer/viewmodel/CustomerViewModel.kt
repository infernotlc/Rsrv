package com.tlc.feature.feature.customer.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlc.domain.model.firebase.Reservation
import com.tlc.domain.repository.firebase.ReservationRepository
import com.tlc.domain.use_cases.customer.GetCustomerPlacesUseCase
import com.tlc.domain.use_cases.customer.LoadCustomerDesignUseCase
import com.tlc.domain.utils.RootResult
import com.tlc.feature.feature.customer.state.GetAllState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import com.tlc.domain.utils.RootResult.Loading
import com.tlc.feature.feature.customer.state.DesignState
import kotlinx.coroutines.launch

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val getCustomerPlacesUseCase: GetCustomerPlacesUseCase,
    private val loadCustomerDesignUseCase: LoadCustomerDesignUseCase,
    private val reservationRepository: ReservationRepository
) : ViewModel() {

    private val _placeState = MutableStateFlow(GetAllState())
    val placeState: StateFlow<GetAllState> = _placeState.asStateFlow()

    private val _designState = MutableStateFlow(DesignState())
    val designState: StateFlow<DesignState> = _designState.asStateFlow()

    private val _reservationsState = MutableStateFlow<List<Reservation>>(emptyList())
    val reservationsState: StateFlow<List<Reservation>> = _reservationsState.asStateFlow()

    fun fetchPlaces() {
        Log.d("CustomerViewModel", "Fetching places...")
        _placeState.value = GetAllState(isLoading = true, result = Loading)
        viewModelScope.launch {
            getCustomerPlacesUseCase.getCustomerPlaces().collect { result ->
                Log.d("CustomerViewModel", "Places fetched. Result: $result")
                _placeState.value = GetAllState(isLoading = false, result = result)
            }
        }
    }

    fun fetchDesign(placeId: String) {
        Log.d("CustomerViewModel", "Fetching design for placeId: $placeId")
        _designState.value = DesignState(isLoading = true, result = Loading)
        viewModelScope.launch {
            loadCustomerDesignUseCase.loadCustomerDesign(placeId).collect { result ->
                Log.d("CustomerViewModel", "Design fetched. Result: $result")
                _designState.value = DesignState(isLoading = false, result = result)
            }
        }
    }

    fun fetchReservations(placeId: String) {
        viewModelScope.launch {
            reservationRepository.getReservations(placeId) { updatedReservations ->
                _reservationsState.value = updatedReservations
            }
        }
    }

}


