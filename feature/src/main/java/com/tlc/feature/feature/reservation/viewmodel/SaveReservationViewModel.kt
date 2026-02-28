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
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
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

    private val _availabilityMessage = MutableStateFlow<String?>(null)
    val availabilityMessage: StateFlow<String?> = _availabilityMessage.asStateFlow()

    fun saveReservation(placeId: String, reservation: Reservation) {
        _uiState.value = ReservationUiState.Loading
        viewModelScope.launch {
            try {
                // Prevent double booking for the same time slot
                val reservedTimes = reservationRepository.getReservedTimesFromFirestore(
                    placeId = placeId,
                    tableId = reservation.tableId,
                    date = reservation.date
                )
                if (reservation.time in reservedTimes) {
                    _uiState.value = ReservationUiState.Error(
                        "This time slot is no longer available. Please choose another time."
                    )
                    return@launch
                }

                val result = reservationRepository.saveReservation(placeId, reservation)
                if (result.isSuccess) {
                    _uiState.value = ReservationUiState.Success("Reservation saved successfully!")
                    _availabilityMessage.value = null
                } else {
                    _uiState.value = ReservationUiState.Error(
                        result.exceptionOrNull()?.message ?: "Failed to save reservation"
                    )
                }
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

                var times = savedTimes.filterNot { it in reservedTimes }

                // If selected date is today, hide times that are already in the past
                if (isSelectedDateToday(selectedDate)) {
                    times = times.filter { isTimeAfterNow(it) }
                }

                _availableTimes.value = times

                // Informational message for fully booked table without overriding reservation status
                _availabilityMessage.value = if (_availableTimes.value.isEmpty()) {
                    "Table is fully booked for $selectedDate. Please select a different date or table."
                } else {
                    null
                }
            } catch (e: Exception) {
                _uiState.value = ReservationUiState.Error("Failed to fetch available times: ${e.message}")
            }
        }
    }

    /**
     * Returns true if [selectedDate] is the current day.
     * Supports "yyyy-MM-dd" (e.g. from nav) and "dd MMM yyyy" (e.g. from DatePicker).
     */
    private fun isSelectedDateToday(selectedDate: String): Boolean {
        if (selectedDate.isBlank()) return false
        return try {
            val today = LocalDate.now()
            val parsed = when {
                selectedDate.contains("-") && selectedDate.length == 10 ->
                    LocalDate.parse(selectedDate)
                else ->
                    LocalDate.parse(selectedDate, DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault()))
            }
            parsed == today
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Returns true if [timeString] (e.g. "14:00" or "9:00") is after the current time.
     */
    private fun isTimeAfterNow(timeString: String): Boolean {
        if (timeString.isBlank()) return false
        return try {
            val formatter = DateTimeFormatter.ofPattern("H:mm", Locale.getDefault())
            val time = LocalTime.parse(timeString.trim(), formatter)
            time.isAfter(LocalTime.now())
        } catch (_: Exception) {
            true
        }
    }
}

sealed class ReservationUiState {
    object Idle : ReservationUiState()
    object Loading : ReservationUiState()
    data class Success(val message: String) : ReservationUiState()
    data class Error(val message: String) : ReservationUiState()
}