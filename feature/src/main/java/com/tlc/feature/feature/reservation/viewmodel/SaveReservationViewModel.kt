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

    private val _designState = MutableStateFlow(RootResult.Success<List<DesignItem>>(emptyList()))
    val designState: StateFlow<RootResult<List<DesignItem>>> = _designState.asStateFlow()


    fun saveReservation(placeId: String, reservations: List<Reservation>) {
        _uiState.value = ReservationUiState.Loading
        viewModelScope.launch {
            try {
                saveReservationUseCase(placeId, reservations)
                _uiState.value = ReservationUiState.Success("Reservation saved successfully!")
                val tableId = reservations.firstOrNull()?.tableId
                if (tableId != null) {
                    fetchAvailableTimes(placeId, tableId)
                } else {
                    _uiState.value = ReservationUiState.Error("Table ID is missing!")
                }

            } catch (e: Exception) {
                _uiState.value = ReservationUiState.Error(e.message ?: "Failed to save reservation")
            }
        }
    }

    fun updateUiState(state: ReservationUiState) {
        _uiState.value = state
    }

    fun fetchAvailableTimes(placeId: String, tableId: String) {
        viewModelScope.launch {
            reservationRepository.getSavedReservationTimes(placeId)
                .collect { times ->
                    val reservedTimes =
                        reservationRepository.getReservedTimesFromFirestore(placeId, tableId)
                    val filteredTimes = times.filterNot { it in reservedTimes }

                    _availableTimes.value = filteredTimes

                    if (filteredTimes.isEmpty()) {
                        markTableAsReserved(placeId, tableId)
                    }
                }
        }
    }

    private fun markTableAsReserved(placeId: String, tableId: String) {
        viewModelScope.launch {
            reservationRepository.markTableAsReserved(placeId, tableId)
            removeTableFromUI(tableId) // Remove table if fully booked
        }
    }

    private fun removeTableFromUI(tableId: String) {
        _uiState.value = ReservationUiState.Success("Table $tableId fully booked and now hidden!")

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