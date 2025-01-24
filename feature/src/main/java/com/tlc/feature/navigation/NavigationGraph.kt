package com.tlc.feature.navigation

import com.google.gson.Gson
import com.tlc.domain.model.firebase.Place
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

enum class NavigationGraph(val route: String) {
    IS_LOGGED_IN("is_logged_in"),
    LOGIN("login"),
    REGISTER("register"),
    FORGOT_PASSWORD("forgot_password"),
    ADMIN_SCREEN("admin_screen"),
    DESIGN_SCREEN("design_screen/{placeData}"),
    CUSTOMER_SCREEN("customer_screen"),
    RESERVATION_SCREEN("reservation_screen");

    companion object {
        fun getDesignRoute(place: Place): String {
            val gson = Gson()
            val placeJson = gson.toJson(place)
            val encodedPlaceJson = URLEncoder.encode(placeJson, StandardCharsets.UTF_8.toString())
            return "design_screen/$encodedPlaceJson"
        }
    }
}