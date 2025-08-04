package com.tlc.feature.feature.customer.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlc.domain.model.firebase.DesignItem
import com.tlc.domain.model.firebase.Reservation
import com.tlc.domain.repository.firebase.ReservationRepository
import com.tlc.domain.use_cases.customer.GetCustomerPlacesUseCase
import com.tlc.domain.use_cases.customer.LoadCustomerDesignUseCase
import com.tlc.domain.utils.RootResult
import com.tlc.feature.feature.customer.state.GetAllState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import com.tlc.domain.utils.RootResult.Loading
import com.tlc.feature.feature.customer.state.DesignState
import kotlinx.coroutines.launch

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val getCustomerPlacesUseCase: GetCustomerPlacesUseCase,
    private val loadCustomerDesignUseCase: LoadCustomerDesignUseCase,
    private val reservationRepository: ReservationRepository
) : ViewModel() {

    private val _placesState = MutableStateFlow(GetAllState())
    val placesState: StateFlow<GetAllState> = _placesState.asStateFlow()

    private val _designState = MutableStateFlow(DesignState())
    val designState: StateFlow<DesignState> = _designState.asStateFlow()

    private val _reservationsState = MutableStateFlow<List<Reservation>>(emptyList())
    val reservationsState: StateFlow<List<Reservation>> = _reservationsState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _role = MutableStateFlow("")
    val role: StateFlow<String> = _role.asStateFlow()

    private val _fullyBookedTables = MutableStateFlow<Set<String>>(emptySet())
    val fullyBookedTables: StateFlow<Set<String>> = _fullyBookedTables.asStateFlow()

    fun fetchPlaces() {
        Log.d("CustomerViewModel", "Fetching places...")
        _placesState.value = GetAllState(isLoading = true, result = Loading)
        viewModelScope.launch {
            getCustomerPlacesUseCase.getCustomerPlaces().collect { result ->
                Log.d("CustomerViewModel", "Places fetched. Result: $result")
                _placesState.value = GetAllState(isLoading = false, result = result)
            }
        }
    }

    fun fetchDesign(placeId: String) {
        Log.d("CustomerViewModel", "Fetching design for placeId: $placeId")
        _designState.value = DesignState(isLoading = true, result = Loading)
        viewModelScope.launch {
            loadCustomerDesignUseCase.loadCustomerDesign(placeId).collect { result ->
                Log.d("CustomerViewModel", "Design fetched. Result: $result")
                _designState.value = DesignState(isLoading = false, result = result)
            }
        }
    }

    fun fetchReservations(placeId: String, date: String) {
        viewModelScope.launch {
            reservationRepository.getReservations(placeId, date).collect { reservations ->
                _reservationsState.value = reservations
            }
        }
    }

    fun fetchFullyBookedTables(placeId: String, date: String, designItems: List<DesignItem>) {
        viewModelScope.launch {
            val fullyBookedTableIds = mutableSetOf<String>()
            
            // Check each table to see if it's fully booked
            designItems.filter { it.type == "TABLE" }.forEach { table ->
                val isFullyBooked = reservationRepository.getTableFullyBookedStatus(placeId, table.designId, date)
                if (isFullyBooked) {
                    fullyBookedTableIds.add(table.designId)
                    Log.d("CustomerViewModel", "Table ${table.designId} is fully booked for $date")
                }
            }
            
            _fullyBookedTables.value = fullyBookedTableIds
        }
    }

    fun updateLoginState(isLoggedIn: Boolean, role: String) {
        _isLoggedIn.value = isLoggedIn
        _role.value = role
    }

    /**
     * Determines if a table is fully booked for a specific date
     * @param tableId The ID of the table to check
     * @param date The date to check
     * @param availableTimeSlots The total number of available time slots for this place
     * @return true if the table is fully booked, false otherwise
     */
    fun isTableFullyBooked(tableId: String, date: String, availableTimeSlots: Int): Boolean {
        val tableReservations = _reservationsState.value
            .filter { it.tableId == tableId && it.date == date && it.status == "active" }
        
        val reservedTimeSlots = tableReservations.map { it.time }.distinct()
        
        Log.d("CustomerViewModel", "Table $tableId: ${reservedTimeSlots.size} reserved slots out of $availableTimeSlots total slots")
        Log.d("CustomerViewModel", "Reserved times: $reservedTimeSlots")
        
        return reservedTimeSlots.size >= availableTimeSlots
    }

}


