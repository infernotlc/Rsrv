package com.tlc.feature.feature.reservation.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.tlc.domain.repository.firebase.ReservationRepository
import com.tlc.feature.feature.reservation.state.ReservationState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReservationViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _customerId = mutableStateOf<String?>(null)
    private val customerId: State<String?> = _customerId

    private val _state = MutableStateFlow(ReservationState())
    val state: StateFlow<ReservationState> = _state.asStateFlow()


    fun updateDate(date: String) {
        _state.update { it.copy(date = date) }
    }

    fun updateTime(time: String) {
        _state.update { it.copy(time = time) }
    }

    init {
        loadCustomerId()
    }
    private fun loadCustomerId() {
        val currentUser = auth.currentUser
        _customerId.value = currentUser?.uid
    }

    fun toggleChairSelection(chairId: String) {
        _state.value = _state.value.copy(
            availableChairs = _state.value.availableChairs.map {
                if (it.id == chairId) it.copy(isReserved = !it.isReserved)
                else it
            }
        )
    }


    fun saveReservation(placeId: String) {
        viewModelScope.launch {
            try {
                val selectedChairs = _state.value.availableChairs.filter { it.isReserved }
                if (selectedChairs.isEmpty()) {
                    Log.w("ReservationViewModel", "No chairs selected for reservation")
                    return@launch
                }

                if (_state.value.date.isBlank() || _state.value.time.isBlank()) {
                    Log.w("ReservationViewModel", "Date or time is blank")
                    _state.update { it.copy(errorMessage = "Date and time must be selected") }
                    return@launch
                }


                selectedChairs.forEach { chair ->
                    reservationRepository.saveReservation(
                        chairId = chair.id,
                        tableId = chair.tableId,
                        customerId = customerId.toString(),
                        date = _state.value.date,
                        time = _state.value.time,
                        placeId = placeId,
                        isApproved = false,
                        timestamp = Timestamp.now(),

                    )
                    Log.d("ReservationViewModel", "Saved reservation for chairId: ${chair.id}")
                }

                _state.update { it.copy(errorMessage = null) }
            } catch (e: Exception) {
                Log.e("ReservationViewModel", "Error saving reservation: ${e.message}", e)
                _state.update { it.copy(errorMessage = e.message) }
            }
        }
    }


    fun loadReservations(placeId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val reservations = reservationRepository.fetchReservationsForPlace(placeId)
                val updatedChairs = _state.value.availableChairs.map { chair ->
                    chair.copy(
                        isReserved = reservations.any { it.chairId == chair.id }
                    )
                }
                _state.update { it.copy(availableChairs = updatedChairs, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = e.message, isLoading = false) }
            }
        }
    }
}
