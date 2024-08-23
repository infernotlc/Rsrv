package com.tlc.feature.navigation.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesHelper(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)

    var placeName: String?
        get() = sharedPreferences.getString("placeName", null)
        set(value) = sharedPreferences.edit().putString("placeName", value).apply()
}
