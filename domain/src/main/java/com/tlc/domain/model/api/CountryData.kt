package com.tlc.domain.model.api

data class CountryData(
    val country: String,
    val cities: List<String>
)

data class CountriesResponse(
    val error: Boolean,
    val msg: String,
    val data: List<CountryData>
) 