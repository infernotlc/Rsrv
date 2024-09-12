package com.tlc.feature.feature.reservation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.tlc.domain.use_cases.reservation.MakeAReservationUseCase
import com.tlc.domain.utils.RootResult
import com.tlc.feature.feature.reservation.state.AvailabilityState
import com.tlc.feature.feature.reservation.state.ReservationState
import com.tlc.feature.navigation.NavigationGraph
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.sql.Timestamp
import javax.inject.Inject

@HiltViewModel
class MakeAReservationViewModel @Inject constructor(
    private val makeReservationUseCase: MakeAReservationUseCase
) : ViewModel() {

    private val _availabilityState = MutableStateFlow(AvailabilityState())
    val availabilityState: StateFlow<AvailabilityState> = _availabilityState.asStateFlow()

    private val _reservationState = MutableStateFlow(ReservationState())
    val reservationState: StateFlow<ReservationState> = _reservationState.asStateFlow()

    fun checkAvailability(
        designItemId: String,
        chairId: String, // New parameter
        reservationStartTime: Timestamp,
        reservationEndTime: Timestamp
    ) {
        _availabilityState.value = AvailabilityState(isLoading = true, result = RootResult.Loading)
        viewModelScope.launch {
            makeReservationUseCase.checkAvailability(
                designItemId,
                chairId,
                reservationStartTime,
                reservationEndTime
            ).collect { result ->
                _availabilityState.value = AvailabilityState(isLoading = false, result = result)
            }
        }
    }

    fun makeReservation(
        designItemId: String,
        chairId: String, // New parameter
        isReserved: Boolean,
        reservedBy: String?,
        reservationStartTime: Timestamp?,
        reservationEndTime: Timestamp?
    ) {
        _reservationState.value = ReservationState(isLoading = true, result = RootResult.Loading)
        viewModelScope.launch {
            makeReservationUseCase.makeReservation(
                designItemId,
                isReserved,
                chairId,
                reservedBy,
                reservationStartTime,
                reservationEndTime
            ).collect { result ->
                _reservationState.value = ReservationState(isLoading = false, result = result)
            }
        }
    }
}