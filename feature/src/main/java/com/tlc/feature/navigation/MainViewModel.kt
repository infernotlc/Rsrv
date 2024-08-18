package com.tlc.feature.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlc.feature.navigation.main_datastore.MainDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val myPreferencesDataStore: MainDataStore
): ViewModel() {

    var isLoading by mutableStateOf(true)
        private set

    var startDestination by mutableStateOf("is_logged_in")
        private set

    init {
        viewModelScope.launch {
            myPreferencesDataStore.readAppEntry.collect { appEntry ->
                startDestination = if (appEntry) {
                    NavigationGraph.IS_LOGGED_IN.route
                } else {
                    NavigationGraph.LOGIN.route
                }
                isLoading = false
            }
        }
    }

    fun saveAppEntry() {
        viewModelScope.launch {
            myPreferencesDataStore.saveAppEntry()
        }
    }
}