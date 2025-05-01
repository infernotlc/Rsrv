package com.tlc.domain.repository.api

import com.tlc.domain.model.api.CountriesResponse
import com.tlc.domain.utils.RootResult
import kotlinx.coroutines.flow.Flow

interface CountriesRepository {
    suspend fun getCountries(): Flow<RootResult<CountriesResponse>>
} 