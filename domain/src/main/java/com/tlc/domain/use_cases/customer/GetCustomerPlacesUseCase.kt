package com.tlc.domain.use_cases.customer

import com.tlc.domain.repository.firebase.CustomerRepository
import javax.inject.Inject

class GetCustomerPlacesUseCase @Inject constructor(
    private val customerRepository: CustomerRepository
)
{
    suspend fun getCustomerPlaces() =
        customerRepository.getCustomerPlaces()
}