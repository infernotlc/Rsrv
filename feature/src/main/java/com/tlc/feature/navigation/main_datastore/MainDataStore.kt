package com.tlc.feature.navigation.main_datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.myPreferencesDataStore: DataStore<Preferences> by preferencesDataStore("settings")

@Singleton
class MainDataStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val myPreferencesDataStore = context.myPreferencesDataStore

    private object PreferencesKeys {
        val APP_ENTRY_KEY = booleanPreferencesKey("app_entry")
        val GALLERY_PERMISSION_KEY = booleanPreferencesKey("gallery_permission")
    }

    val readGalleryPermission = myPreferencesDataStore.data.catch { exception ->
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map { preferences ->
        preferences[PreferencesKeys.GALLERY_PERMISSION_KEY] ?: false
    }

    suspend fun saveGalleryPermission(granted: Boolean) {
        myPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.GALLERY_PERMISSION_KEY] = granted
        }
    }
    val readAppEntry = myPreferencesDataStore.data.catch { exception ->
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map { preferences ->
        preferences[PreferencesKeys.APP_ENTRY_KEY] ?: true
    }

    suspend fun saveAppEntry(loggedIn: Boolean) {
        myPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_ENTRY_KEY] = loggedIn
        }
    }
}