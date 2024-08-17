package com.tlc.feature.feature.auth.register.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlc.domain.use_cases.firebase_use_cases.auth.RegisterUseCase
import com.tlc.domain.utils.RootResult
import com.tlc.feature.feature.auth.register.state.RegisterUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUIState())
    val uiState: StateFlow<RegisterUIState> = _uiState

    internal fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            registerUseCase(email, password)
                .collect { result ->
                    when (result) {
                        is RootResult.Success -> {
                            result.data?.let { user ->
                                _uiState.value = _uiState.value.copy(
                                    user = user,
                                    isLoading = false
                                )
                            }
                        }

                        is RootResult.Error -> {
                            _uiState.value =
                                _uiState.value.copy(error = result.message, isLoading = false)
                        }

                        is RootResult.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoading = true)
                        }
                    }
                }
        }
    }
}