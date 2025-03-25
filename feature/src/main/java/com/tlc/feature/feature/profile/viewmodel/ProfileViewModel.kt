package com.tlc.feature.feature.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.tlc.domain.model.firebase.Reservation
import com.tlc.domain.use_cases.reservation.GetUserReservationsUseCase
import com.tlc.domain.utils.RootResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserReservationsUseCase: GetUserReservationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserReservations()
    }

    private fun loadUserReservations() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch

                when (val result = getUserReservationsUseCase(userId)) {
                    is RootResult.Success -> {
                        _uiState.value = result.data?.let {
                            _uiState.value.copy(
                                reservations = it,
                                isLoading = false
                            )
                        }!!
                    }
                    is RootResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = result.message,
                            isLoading = false
                        )
                    }

                    RootResult.Loading -> TODO()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load reservations",
                    isLoading = false
                )
            }
        }
    }
}

data class ProfileUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val reservations: List<Reservation> = emptyList()
) 