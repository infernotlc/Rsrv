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

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _showLogoutDialog = MutableStateFlow(false)
    val showLogoutDialog: StateFlow<Boolean> = _showLogoutDialog.asStateFlow()

    init {
        loadUserReservations()
    }

    private fun loadUserReservations() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val userId = auth.currentUser?.uid ?: return@launch

                reservationRepository.getUserReservations(userId)
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

    fun onLogoutClick() {
        _showLogoutDialog.value = true
    }

    fun onLogoutConfirm() {
        auth.signOut()
        _showLogoutDialog.value = false
    }

    fun onLogoutCancel() {
        _showLogoutDialog.value = false
    }
}

data class ProfileUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val reservations: List<Reservation> = emptyList()
)