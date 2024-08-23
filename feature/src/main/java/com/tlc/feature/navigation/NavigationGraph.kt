package com.tlc.feature.navigation

import com.google.gson.Gson
import com.tlc.data.remote.dto.firebase_dto.PlaceDataDto

enum class NavigationGraph(val route: String) {
    IS_LOGGED_IN("is_logged_in"),
    LOGIN("login"),
    REGISTER("register"),
    FORGOT_PASSWORD("forgot_password"),
    ADMIN_SCREEN("admin_screen"),
    DESIGN_SCREEN("design_screen/{placeJson}"),
    CUSTOMER_SCREEN("customer_screen");


    companion object {
        fun getDesignRoute(place: PlaceDataDto): String {
            val gson = Gson()
            val placeJson = gson.toJson(place)
            return "design_screen/$placeJson"
        }
    }
}