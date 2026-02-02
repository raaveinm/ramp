package com.raaveinm.chirro.data.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.raaveinm.chirro.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

// Constants
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingDataStoreRepository(private val dataStore: DataStore<Preferences>) {
    private val tag: String = "UserPreferencesRepo"

    private object PreferencesKeys {
        val SORT_PRIMARY_ORDER = stringPreferencesKey("sort_order_primary")
        val SORT_SECONDARY_ORDER = stringPreferencesKey("sort_order_secondary")
        val CURRENT_THEME = stringPreferencesKey("current_theme")
    }

    ///////////////////////////////////////////////
    // Getting preferences flow
    ///////////////////////////////////////////////
    val settingsPreferencesFlow: Flow<PreferenceList> = dataStore.data
        .catch { exception ->
            Log.e(tag, "$exception")
            emit(emptyPreferences())
        }
        .map { preferences ->
            mapOfPreferences(preferences)
        }

    ///////////////////////////////////////////////
    // Fetching preferences list
    ///////////////////////////////////////////////
    private fun mapOfPreferences(preferences: Preferences): PreferenceList {
        val sortPrimaryOrder = try {
            OrderMediaQueue.valueOf(
                preferences[PreferencesKeys.SORT_PRIMARY_ORDER] ?: OrderMediaQueue.ALBUM.name
            )
        } catch (_: Exception) {
            OrderMediaQueue.ALBUM
        }

        val sortSecondaryOrder = try {
            OrderMediaQueue.valueOf(
                preferences[PreferencesKeys.SORT_SECONDARY_ORDER] ?: OrderMediaQueue.TRACK.name
            )
        } catch (_: Exception) {
            OrderMediaQueue.TRACK
        }

        val currentTheme = try {
            AppTheme.valueOf(
                preferences[PreferencesKeys.CURRENT_THEME] ?: AppTheme.DYNAMIC.name
            )
        } catch (_: Exception) {
            AppTheme.DYNAMIC
        }

        return PreferenceList(
            trackPrimaryOrder = sortPrimaryOrder,
            trackSecondaryOrder = sortSecondaryOrder,
            currentTheme = currentTheme
        )
    }

    ///////////////////////////////////////////////
    // Updating preferences
    ///////////////////////////////////////////////
    suspend fun updatePrimaryOrder(order: OrderMediaQueue) {
        dataStore.edit { preferences ->
            if (order == OrderMediaQueue.DEFAULT)
                preferences[PreferencesKeys.SORT_PRIMARY_ORDER] = OrderMediaQueue.ALBUM.name
            else
                preferences[PreferencesKeys.SORT_PRIMARY_ORDER] = order.name
        }
    }

    suspend fun updateSecondaryOrder(order: OrderMediaQueue) {
        dataStore.edit { preferences ->
            if (order == OrderMediaQueue.DEFAULT)
                preferences[PreferencesKeys.SORT_SECONDARY_ORDER] = OrderMediaQueue.TRACK.name
            else
                preferences[PreferencesKeys.SORT_SECONDARY_ORDER] = order.name
        }
    }

    suspend fun updateTheme(theme: AppTheme) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENT_THEME] = theme.name
        }
    }
}