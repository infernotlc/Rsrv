package com.tlc.feature.feature.main_content.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlc.domain.use_cases.firebase_use_cases.auth.DeleteUserUseCase
import com.tlc.domain.utils.RootResult
import com.tlc.feature.feature.main_content.state.DeleteUserState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainContentViewModel @Inject constructor(
    private val deleteUserUseCase: DeleteUserUseCase
): ViewModel() {

    private val _deleteUserState = MutableStateFlow(DeleteUserState())
    val deleteUserState: StateFlow<DeleteUserState> = _deleteUserState


    fun deleteUser(){
        viewModelScope.launch {
            _deleteUserState.value = _deleteUserState.value.copy(isLoading = true)
            deleteUserUseCase().collect{ result ->
                when(result){
                    is RootResult.Success -> {
                        Log.d("MainContentViewModel", result.data.toString())
                        _deleteUserState.value = _deleteUserState.value.copy(
                            isLoading = false,
                            transaction = true,
                            error = null
                        )
                    }
                    is RootResult.Error -> {
                        _deleteUserState.value = _deleteUserState.value.copy(
                            isLoading = false,
                            error = result.message,
                            transaction = false
                        )
                    }
                    RootResult.Loading -> {
                        _deleteUserState.value = _deleteUserState.value.copy(isLoading = true)
                    }
                }
            }
        }
    }
}