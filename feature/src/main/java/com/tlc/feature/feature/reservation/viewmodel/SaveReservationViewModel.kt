package com.tlc.feature.feature.reservation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlc.domain.model.firebase.Reservation
import com.tlc.domain.repository.firebase.ReservationRepository
import com.tlc.domain.use_cases.reservation.SaveReservationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SaveReservationViewModel @Inject constructor(
    private val saveReservationUseCase: SaveReservationUseCase,
    private val reservationRepository: ReservationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReservationUiState>(ReservationUiState.Idle)
    val uiState: StateFlow<ReservationUiState> = _uiState

    private val _availableTimes = MutableStateFlow<List<String>>(emptyList())
    val availableTimes: StateFlow<List<String>> = _availableTimes

    fun saveReservation(placeId: String, reservations: List<Reservation>) {
        _uiState.value = ReservationUiState.Loading
        viewModelScope.launch {
            try {
                saveReservationUseCase(placeId, reservations)
                _uiState.value = ReservationUiState.Success("Reservation saved successfully!")
            } catch (e: Exception) {
                _uiState.value = ReservationUiState.Error(e.message ?: "Failed to save reservation")
            }
        }
    }

    fun updateUiState(state: ReservationUiState) {
        _uiState.value = state
    }

    fun fetchAvailableTimes(placeId: String) {
        viewModelScope.launch {
            reservationRepository.getReservationTimes(placeId)
                .collect { times ->
                    _availableTimes.value = times
                }
        }
    }
}




sealed class ReservationUiState {
    object Idle : ReservationUiState()
    object Loading : ReservationUiState()
    data class Success(val message: String) : ReservationUiState()
    data class Error(val message: String) : ReservationUiState()
}