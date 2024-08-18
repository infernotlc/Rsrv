package com.tlc.feature.feature.auth.login.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.tlc.data.ui.repository.firebase.AuthRepositoryImpl
import com.tlc.domain.repository.firebase.AuthRepository
import com.tlc.domain.use_cases.firebase_use_cases.auth.IsLoggedInUseCase
import com.tlc.domain.use_cases.firebase_use_cases.auth.SignInUseCase
import com.tlc.domain.use_cases.firebase_use_cases.auth.SignOutUseCase
import com.tlc.domain.utils.RootResult
import com.tlc.feature.feature.auth.login.state.IsLoggedInState
import com.tlc.feature.feature.auth.login.state.LoginUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val isLoggedInUseCase: IsLoggedInUseCase,
    private val authRepository: AuthRepositoryImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    private val _loggingState = MutableStateFlow(IsLoggedInState())
    val loggingState: StateFlow<IsLoggedInState> = _loggingState


    internal fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            signInUseCase(email, password).collect { result ->
                when (result) {
                    is RootResult.Success -> {
                        val user = result.data
                        val role = user?.let { authRepository.getUserRole(it.uid) }
                        _uiState.value = _uiState.value.copy(
                            user = user,
                            role = role,
                            isLoading = false
                        )
                    }
                    is RootResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = result.message,
                            isLoading = false
                        )
                    }
                    RootResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    internal fun signOut() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            signOutUseCase().collect { result ->
                when (result) {
                    is RootResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            user = null,
                            isLoading = false
                        )
                    }

                    is RootResult.Error -> {
                        _uiState.value =
                            _uiState.value.copy(error = result.message, isLoading = false)
                    }

                    RootResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    fun isLoggedIn() {
        _loggingState.value = _loggingState.value.copy(isLoading = true)
        viewModelScope.launch {
            isLoggedInUseCase().collect { isLoggedIn ->
                when (isLoggedIn) {
                    is RootResult.Success -> {
                        val role =
                            authRepository.getUserRole(FirebaseAuth.getInstance().currentUser!!.uid)

                        _loggingState.value = _loggingState.value.copy(
                            isLoading = false,
                            transaction = isLoggedIn.data ?: false,
                            data = role
                        )
                        Log.d("LoginViewModel", "User Role: $role")
                    }

                    is RootResult.Error -> {
                        _loggingState.value = _loggingState.value.copy(
                            isLoading = false,
                            error = isLoggedIn.message
                        )
                    }

                    RootResult.Loading -> {
                        _loggingState.value = _loggingState.value.copy(isLoading = true)
                    }
                }
            }
        }
    }
}
