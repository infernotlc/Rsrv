package com.tlc.feature.feature.auth.login.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
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
import com.tlc.feature.navigation.NavigationGraph
import com.tlc.feature.navigation.main_datastore.MainDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val isLoggedInUseCase: IsLoggedInUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    private val _loggingState = MutableStateFlow(IsLoggedInState())
    val loggingState: StateFlow<IsLoggedInState> = _loggingState

    private var _hasCheckedLogin = false
    val hasCheckedLogin get() = _hasCheckedLogin
    private var isCheckingLogin = false
    private var isInitialized = false
    private var isNavigating = false

    init {
        Log.d("LoginViewModel", "Initializing LoginViewModel")
        viewModelScope.launch {
            if (!isInitialized && !_hasCheckedLogin && !isCheckingLogin) {
                isInitialized = true
                delay(100) // Add a small delay to prevent race conditions
                isLoggedIn()
            }
        }
    }

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
                        _hasCheckedLogin = true
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

    internal fun signOut(navController: NavHostController) {
        if (isNavigating) {
            Log.d("LoginViewModel", "Navigation already in progress, skipping")
            return
        }

        viewModelScope.launch {
            isNavigating = true
            _uiState.value = _uiState.value.copy(isLoading = true)
            signOutUseCase().collect { result ->
                when (result) {
                    is RootResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            user = null,
                            role = null,
                            isLoading = false
                        )
                        _loggingState.value = _loggingState.value.copy(
                            transaction = false,
                            isLoading = false,
                            data = null
                        )
                        _hasCheckedLogin = false
                        isCheckingLogin = false
                        isInitialized = false

                        Log.d("LoginViewModel", "Signing out, navigating to customer screen")
                        navController.navigate(NavigationGraph.CUSTOMER_SCREEN.route) {
                            popUpTo(0) { inclusive = true }
                        }
                        delay(100) // Add a small delay before allowing navigation again
                        isNavigating = false
                    }

                    is RootResult.Error -> {
                        _uiState.value = _uiState.value.copy(error = result.message, isLoading = false)
                        isNavigating = false
                    }

                    RootResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    fun isLoggedIn() {
        if (_loggingState.value.isLoading || isCheckingLogin || _hasCheckedLogin) {
            Log.d("LoginViewModel", "Already checked login status or checking in progress, skipping")
            return
        }
        
        Log.d("LoginViewModel", "Starting login check")
        isCheckingLogin = true
        _loggingState.value = _loggingState.value.copy(isLoading = true)

        viewModelScope.launch {
            isLoggedInUseCase().collect { result ->
                when (result) {
                    is RootResult.Success -> {
                        val user = FirebaseAuth.getInstance().currentUser
                        val role = result.data
                        if (user != null && role != null) {
                            Log.d("LoginViewModel", "User is logged in with role: $role")
                            _loggingState.value = _loggingState.value.copy(
                                isLoading = false,
                                transaction = true,
                                data = role
                            )
                            _uiState.value = _uiState.value.copy(
                                user = user,
                                role = role
                            )
                        } else {
                            Log.d("LoginViewModel", "No user found or no role")
                            _loggingState.value = _loggingState.value.copy(
                                isLoading = false,
                                transaction = false,
                                data = null
                            )
                        }
                        isCheckingLogin = false
                        _hasCheckedLogin = true
                    }

                    is RootResult.Error -> {
                        Log.e("LoginViewModel", "Error checking login status: ${result.message}")
                        _loggingState.value = _loggingState.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                        isCheckingLogin = false
                    }

                    RootResult.Loading -> {
                        _loggingState.value = _loggingState.value.copy(isLoading = true)
                    }
                }
            }
        }
    }
}