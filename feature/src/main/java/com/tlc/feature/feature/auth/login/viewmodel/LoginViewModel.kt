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
                        val isLoggedIn = user != null && role != null
                        
                        _uiState.value = _uiState.value.copy(
                            user = user,
                            role = role,
                            isLoading = false
                        )
                        _loggingState.value = _loggingState.value.copy(
                            transaction = isLoggedIn,
                            data = role,
                            isLoading = false
                        )
                        _hasCheckedLogin = true
                        Log.d("LoginViewModel", "Sign in successful - User: $user, Role: $role, IsLoggedIn: $isLoggedIn")
                    }

                    is RootResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = result.message,
                            isLoading = false,
                            user = null,
                            role = null
                        )
                        _loggingState.value = _loggingState.value.copy(
                            transaction = false,
                            error = result.message,
                            isLoading = false,
                            data = null
                        )
                        Log.d("LoginViewModel", "Sign in error: ${result.message}")
                    }

                    RootResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                        _loggingState.value = _loggingState.value.copy(isLoading = true)
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

                            Log.d("LoginViewModel", "Sign out successful, navigating to login screen")
                            navController.navigate(NavigationGraph.CUSTOMER_SCREEN.route) {
                                popUpTo(0) { inclusive = true }
                            }
                            delay(100)
                            isNavigating = false
                        }

                        is RootResult.Error -> {
                            _uiState.value = _uiState.value.copy(
                                error = result.message,
                                isLoading = false,
                                user = null,
                                role = null
                            )
                            _loggingState.value = _loggingState.value.copy(
                                transaction = false,
                                error = result.message,
                                isLoading = false,
                                data = null
                            )
                            isNavigating = false
                            Log.d("LoginViewModel", "Sign out error: ${result.message}")
                        }

                        RootResult.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoading = true)
                            _loggingState.value = _loggingState.value.copy(isLoading = true)
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
                            val isLoggedIn = user != null && role != null
                            
                            _loggingState.value = _loggingState.value.copy(
                                transaction = isLoggedIn,
                                isLoading = false,
                                data = role
                            )
                            _uiState.value = _uiState.value.copy(
                                user = user,
                                role = role,
                                isLoading = false
                            )
                            _hasCheckedLogin = true
                            isCheckingLogin = false
                            Log.d("LoginViewModel", "Login check complete - User: $user, Role: $role, IsLoggedIn: $isLoggedIn")
                        }
                        is RootResult.Error -> {
                            _loggingState.value = _loggingState.value.copy(
                                transaction = false,
                                isLoading = false,
                                error = result.message,
                                data = null
                            )
                            _uiState.value = _uiState.value.copy(
                                user = null,
                                role = null,
                                error = result.message,
                                isLoading = false
                            )
                            _hasCheckedLogin = true
                            isCheckingLogin = false
                            Log.d("LoginViewModel", "Login check error: ${result.message}")
                        }
                        RootResult.Loading -> {
                            _loggingState.value = _loggingState.value.copy(isLoading = true)
                            _uiState.value = _uiState.value.copy(isLoading = true)
                        }
                    }
                }
            }
        }

        // Add a function to force update login state
        fun updateLoginState() {
            viewModelScope.launch {
                _hasCheckedLogin = false
                isCheckingLogin = false
                isInitialized = false
                isLoggedIn()
            }
        }
    }