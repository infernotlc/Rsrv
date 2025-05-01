package com.tlc.domain.use_cases.api

import com.tlc.domain.model.api.CountriesResponse
import com.tlc.domain.repository.api.CountriesRepository
import com.tlc.domain.utils.RootResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCountriesUseCase @Inject constructor(
    private val countriesRepository: CountriesRepository
) {
    suspend operator fun invoke(): Flow<RootResult<CountriesResponse>> = countriesRepository.getCountries()
} 