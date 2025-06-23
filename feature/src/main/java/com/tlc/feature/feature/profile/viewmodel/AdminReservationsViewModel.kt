package com.tlc.feature.feature.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.tlc.domain.model.firebase.Reservation
import com.tlc.domain.repository.firebase.ReservationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminReservationsUiState(
    val reservations: List<Reservation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AdminReservationsViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminReservationsUiState())
    val uiState: StateFlow<AdminReservationsUiState> = _uiState.asStateFlow()

    init {
        loadAllReservations()
    }

    private fun loadAllReservations() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val adminUserId = auth.currentUser?.uid ?: return@launch

                reservationRepository.getAllAdminReservations(adminUserId)
                    .collect { reservations ->
                        _uiState.value = _uiState.value.copy(
                            reservations = reservations,
                            isLoading = false
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load reservations",
                    isLoading = false
                )
            }
        }
    }

    fun cancelReservation(reservationId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val adminUserId = auth.currentUser?.uid ?: return@launch

                // For admin, we need to get the customer's userId from the reservation
                // First, find the reservation in the current list
                val reservation = _uiState.value.reservations.find { it.id == reservationId }
                if (reservation != null) {
                    reservationRepository.cancelReservation(reservationId, reservation.userId)
                        .onSuccess {
                            // Reload reservations after cancellation
                            loadAllReservations()
                        }
                        .onFailure { exception ->
                            _uiState.value = _uiState.value.copy(
                                error = exception.message ?: "Failed to cancel reservation",
                                isLoading = false
                            )
                        }
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Reservation not found",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to cancel reservation",
                    isLoading = false
                )
            }
        }
    }
} 