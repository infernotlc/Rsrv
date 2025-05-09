package com.tlc.feature.feature.auth.forget_password.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlc.domain.use_cases.firebase_use_cases.auth.ResetPasswordUseCase
import com.tlc.domain.utils.RootResult
import com.tlc.feature.feature.auth.forget_password.state.ForgotPasswordState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgetPasswordViewModel @Inject constructor(
    private val resetPasswordUseCase: ResetPasswordUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ForgotPasswordState())
    val uiState: StateFlow<ForgotPasswordState> = _uiState

    fun sendPasswordResetEmail(email: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            resetPasswordUseCase(email).collect {
                    result ->
                when(result){
                    is RootResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                    is RootResult.Success -> {
                        _uiState.value = _uiState.value.copy(transaction = true, isLoading = false)
                    }
                    is RootResult.Error -> {
                        _uiState.value = _uiState.value.copy(error = result.message, isLoading = false, transaction = false)
                    }
                }
            }
        }
    }
}