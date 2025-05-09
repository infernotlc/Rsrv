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
    SAVE_RESERVATION_SCREEN("save_reservation_screen/{placeId}/{tableId}"),
    PROFILE_SCREEN("profile_screen"),
    ADMIN_PROFILE_SCREEN("admin_profile_screen"),
    CUSTOMER_RESERVATIONS_SCREEN("reservations_screen"),
    ADMIN_RESERVATIONS_SCREEN("admin_reservations_screen");

    companion object {
        fun getDesignRoute(place: Place): String {
            val gson = Gson()
            val placeJson = gson.toJson(place)
            val encodedPlaceJson = URLEncoder.encode(placeJson, StandardCharsets.UTF_8.toString())
            return "design_screen/$encodedPlaceJson"
        }

        // For showing/hiding the navigation drawer
        fun shouldShowNavigationDrawer(route: String): Boolean {
            return when {
                route == LOGIN.route -> false
                route == REGISTER.route -> false
                route == FORGOT_PASSWORD.route -> false
                route == IS_LOGGED_IN.route -> false
                route == ADMIN_SCREEN.route -> true
                route == CUSTOMER_SCREEN.route -> true
                route.startsWith("design_screen/") -> true
                route == SAVE_RESERVATION_SCREEN.route -> true
                route == PROFILE_SCREEN.route -> true
                route == ADMIN_PROFILE_SCREEN.route -> true
                route == ADMIN_RESERVATIONS_SCREEN.route -> true
                else -> false
            }
        }

        // For showing/hiding the top bar
        fun shouldShowTopBar(route: String): Boolean {
            return when {
                route == CUSTOMER_SCREEN.route -> true
                route == ADMIN_SCREEN.route -> true
                route == LOGIN.route -> true
                route == REGISTER.route -> true
                route == FORGOT_PASSWORD.route -> true
                route == PROFILE_SCREEN.route -> true
                route == ADMIN_PROFILE_SCREEN.route -> true
                route == ADMIN_RESERVATIONS_SCREEN.route -> true
                route.startsWith("design_screen/") -> true
                route.startsWith("save_reservation_screen/") -> true
                else -> false
            }
        }
    }
}