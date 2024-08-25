package com.tlc.domain.use_cases.customer

import com.tlc.domain.repository.firebase.CustomerRepository
import javax.inject.Inject

class LoadCustomerDesignUseCase @Inject constructor(
    private val customerRepository: CustomerRepository

) {
    suspend fun loadCustomerDesign(placeId: String)  =
        customerRepository.getCustomerDesign(placeId)
}