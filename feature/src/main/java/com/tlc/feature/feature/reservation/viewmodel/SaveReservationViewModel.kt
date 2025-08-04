package com.tlc.feature.feature.reservation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlc.domain.model.firebase.DesignItem
import com.tlc.domain.model.firebase.Reservation
import com.tlc.domain.repository.firebase.ReservationRepository
import com.tlc.domain.utils.RootResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SaveReservationViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReservationUiState>(ReservationUiState.Idle)
    val uiState: StateFlow<ReservationUiState> = _uiState.asStateFlow()

    private val _availableTimes = MutableStateFlow<List<String>>(emptyList())
    val availableTimes: StateFlow<List<String>> = _availableTimes.asStateFlow()

    private val _designState = MutableStateFlow(RootResult.Success<List<DesignItem>>(emptyList()))
    val designState: StateFlow<RootResult<List<DesignItem>>> = _designState.asStateFlow()

    fun saveReservation(placeId: String, reservation: Reservation) {
        _uiState.value = ReservationUiState.Loading
        viewModelScope.launch {
            try {
                reservationRepository.saveReservation(placeId, reservation)
                _uiState.value = ReservationUiState.Success("Reservation saved successfully!")

                // After saving, refresh available times for the specific date
                fetchAvailableTimes(placeId, reservation.tableId, reservation.date)
            } catch (e: Exception) {
                _uiState.value = ReservationUiState.Error(e.message ?: "Failed to save reservation")
            }
        }
    }

    fun updateUiState(state: ReservationUiState) {
        _uiState.value = state
    }

    fun fetchAvailableTimes(placeId: String, tableId: String, selectedDate: String) {
        viewModelScope.launch {
            try {
                val savedTimes = reservationRepository.getSavedReservationTimes(placeId).first()
                val reservedTimes = reservationRepository.getReservedTimesFromFirestore(placeId, tableId, selectedDate)

                _availableTimes.value = savedTimes.filterNot { it in reservedTimes }

                // Check if table is fully booked for this specific date
                if (_availableTimes.value.isEmpty()) {
                    _uiState.value = ReservationUiState.Success("Table is fully booked for $selectedDate. Please select a different date or table.")
                }
            } catch (e: Exception) {
                _uiState.value = ReservationUiState.Error("Failed to fetch available times: ${e.message}")
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