package com.raaveinm.chirro.data.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.raaveinm.chirro.data.database.TrackInfo
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
        val SORT_ASCENDING = booleanPreferencesKey("sort_ascending")
        val CURRENT_THEME = stringPreferencesKey("current_theme")
        val CURRENT_TRACK = stringPreferencesKey("current_track")
        val IS_SAVED_STATE = stringPreferencesKey("is_saved_state")
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

        val isAsc: Boolean = try {
            preferences[PreferencesKeys.SORT_ASCENDING].toString().toBoolean()
        } catch (_: Exception) {
            true
        }

        val currentTheme = try {
            AppTheme.valueOf(
                preferences[PreferencesKeys.CURRENT_THEME] ?: AppTheme.DYNAMIC.name
            )
        } catch (_: Exception) {
            AppTheme.DYNAMIC
        }

        val currentTrack = try {
            val jsonString = preferences[PreferencesKeys.CURRENT_TRACK]
            if (jsonString != null) {
                Gson().fromJson(jsonString, TrackInfo::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.v(tag, "Error parsing track: $e")
            null
        }

        val isSavedState = try {
            preferences[PreferencesKeys.IS_SAVED_STATE].toBoolean()
        } catch (_: Exception) {
            false
        }

        return PreferenceList(
            trackPrimaryOrder = sortPrimaryOrder,
            trackSecondaryOrder = sortSecondaryOrder,
            trackSortAscending = isAsc,
            currentTheme = currentTheme,
            currentTrack = currentTrack,
            isSavedState = isSavedState
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

    suspend fun updateSortAscending(ascending: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_ASCENDING] = ascending
        }
    }

    suspend fun updateTheme(theme: AppTheme) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENT_THEME] = theme.name
        }
    }

    suspend fun setSavedState(state: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_SAVED_STATE] = state.toString()
        }
    }

    suspend fun updateCurrentTrack(trackInfo: TrackInfo?) {
        dataStore.edit { preferences ->
            if (trackInfo == null) {
                preferences.remove(PreferencesKeys.CURRENT_TRACK)
            } else {
                val jsonString = Gson().toJson(trackInfo)
                preferences[PreferencesKeys.CURRENT_TRACK] = jsonString
            }
        }
    }
}