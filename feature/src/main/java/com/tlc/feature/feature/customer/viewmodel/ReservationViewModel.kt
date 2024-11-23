package com.tlc.feature.feature.customer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlc.domain.model.firebase.Reservation
import com.tlc.domain.use_cases.customer.ReservationUseCase
import com.tlc.domain.use_cases.customer.SaveReservationUseCase
import com.tlc.domain.utils.RootResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


class ReservationViewModel @Inject constructor(
    private val reservationUseCase: ReservationUseCase,
    private val saveReservationUseCase: SaveReservationUseCase,
) : ViewModel() {

    private val _reservationsState = MutableStateFlow<RootResult<List<Reservation>>>(RootResult.Loading)
    val reservationsState: StateFlow<RootResult<List<Reservation>>> = _reservationsState

    fun fetchReservations(placeId: String, date: String) {
        viewModelScope. launch {
            reservationUseCase.loadReservations(placeId, date).collect { result ->
                _reservationsState.value = result
            }
        }
    }

    fun saveReservation(placeId: String, reservation: Reservation) {
        viewModelScope.launch {
            saveReservationUseCase.saveReservation(placeId, reservation).collect { result ->
                // Handle result as needed, e.g., updating UI or showing a success message
            }
        }
    }
}
