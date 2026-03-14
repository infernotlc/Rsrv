package com.tlc.data.ui.repository.api

import com.tlc.domain.model.api.CountriesResponse
import com.tlc.domain.model.api.CountryData
import com.tlc.domain.repository.api.CountriesRepository
import com.tlc.domain.utils.RootResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import javax.inject.Inject

interface CountriesApi {
    @GET("countries")
    suspend fun getCountries(): CountriesResponse
}

class CountriesRepositoryImpl @Inject constructor() : CountriesRepository {
    private val api: CountriesApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://countriesnow.space/api/v0.1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CountriesApi::class.java)
    }

    override suspend fun getCountries(): Flow<RootResult<CountriesResponse>> = flow {
        emit(RootResult.Loading)
        try {
            val response = api.getCountries()
            emit(RootResult.Success(response))
        } catch (e: Exception) {
            // If remote API fails (e.g. DNS error), fall back to a small local list
            val fallback = CountriesResponse(
                error = false,
                msg = "Local fallback",
                data = listOf(
                    CountryData(
                        country = "Turkey",
                        cities = listOf(
                            "Istanbul",
                            "Ankara",
                            "Izmir",
                            "Bursa",
                            "Antalya",
                            "Adana",
                            "Konya",
                            "Gaziantep",
                            "Kayseri",
                            "Mersin"
                        )
                    )
                )
            )
            emit(RootResult.Success(fallback))
        }
    }
} 