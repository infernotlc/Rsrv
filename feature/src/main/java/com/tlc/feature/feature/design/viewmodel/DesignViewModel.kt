package com.tlc.feature.feature.design.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlc.domain.model.firebase.DesignItem
import com.tlc.domain.use_cases.design.LoadDesignUseCase
import com.tlc.domain.use_cases.design.SaveDesignUseCase
import com.tlc.domain.utils.RootResult
import com.tlc.feature.feature.design.state.DesignState
import com.tlc.feature.feature.design.state.SaveState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DesignViewModel @Inject constructor(
    private val saveDesignUseCase: SaveDesignUseCase,
    private val loadDesignUseCase: LoadDesignUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DesignState())
    val uiState: StateFlow<DesignState> = _uiState.asStateFlow()

    private val _saveState = MutableStateFlow(SaveState())
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    fun loadDesign(placeId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            loadDesignUseCase.loadDesign(placeId).collect { result ->
                when (result) {
                    is RootResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            designItems = result.data ?: emptyList(),
                            isLoading = false,
                            error = null
                        )
                    }
                    is RootResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = result.message ?: "An error occurred"
                        )
                    }
                    RootResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    fun saveDesign(placeId: String, designItems: List<DesignItem>) {
        viewModelScope.launch {
            _saveState.value = SaveState(isSaving = true)
            saveDesignUseCase.saveDesign(placeId, designItems).collect { result ->
                when (result) {
                    is RootResult.Success -> {
                        _saveState.value = SaveState(isSaving = false, isSaved = true)
                    }
                    is RootResult.Error -> {
                        _saveState.value = SaveState(
                            isSaving = false,
                            error = result.message ?: "An error occurred"
                        )
                    }
                    RootResult.Loading -> {
                        _saveState.value = SaveState(isSaving = true)
                    }
                }
            }
        }
    }
}
