package com.tlc.feature.feature.reservation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlc.domain.model.firebase.DesignItem
import com.tlc.domain.model.firebase.Reservation
import com.tlc.domain.repository.firebase.ReservationRepository
import com.tlc.domain.use_cases.reservation.SaveReservationUseCase
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
    private val saveReservationUseCase: SaveReservationUseCase,
    private val reservationRepository: ReservationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReservationUiState>(ReservationUiState.Idle)
    val uiState: StateFlow<ReservationUiState> = _uiState.asStateFlow()

    private val _availableTimes = MutableStateFlow<List<String>>(emptyList())
    val availableTimes: StateFlow<List<String>> = _availableTimes.asStateFlow()

    private val _designState = MutableStateFlow(RootResult.Success<List<DesignItem>>(emptyList()))
    val designState: StateFlow<RootResult<List<DesignItem>>> = _designState.asStateFlow()

    fun saveReservation(placeId: String, reservations: List<Reservation>) {
        if (reservations.isEmpty()) {
            _uiState.value = ReservationUiState.Error("No reservations to save!")
            return
        }

        _uiState.value = ReservationUiState.Loading
        viewModelScope.launch {
            try {
                val date = reservations.first().date // Extract date from reservations
                saveReservationUseCase(placeId, reservations)
                _uiState.value = ReservationUiState.Success("Reservation saved successfully!")

                val tableId = reservations.first().tableId
                fetchAvailableTimes(placeId, tableId, date)
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

                if (_availableTimes.value.isEmpty()) {
                    markTableAsReserved(placeId, tableId)
                }
            } catch (e: Exception) {
                _uiState.value = ReservationUiState.Error("Failed to fetch available times: ${e.message}")
            }
        }
    }

    private fun markTableAsReserved(placeId: String, tableId: String) {
        _uiState.value = ReservationUiState.Loading
        viewModelScope.launch {
            try {
                reservationRepository.markTableAsReserved(placeId, tableId)
                _uiState.value = ReservationUiState.Success("Table is fully booked for  and now hidden!")
                removeTableFromUI(tableId)
            } catch (e: Exception) {
                _uiState.value = ReservationUiState.Error("Failed to mark table as reserved: ${e.message}")
            }
        }
    }

    private fun removeTableFromUI(tableId: String) {
        val currentDesignItems = (_designState.value as? RootResult.Success<List<DesignItem>>)?.data ?: emptyList()
        _designState.value = RootResult.Success(currentDesignItems.filterNot { it.designId == tableId })
    }
}

sealed class ReservationUiState {
    object Idle : ReservationUiState()
    object Loading : ReservationUiState()
    data class Success(val message: String) : ReservationUiState()
    data class Error(val message: String) : ReservationUiState()
}