package com.raaveinm.chirro.data.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

// Constants
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingDataStoreRepository(private val dataStore: DataStore<Preferences>) {
    private val TAG: String = "UserPreferencesRepo"

    private object PreferencesKeys {
        val SORT_PRIMARY_ORDER = stringPreferencesKey("sort_order_primary")
        val SORT_SECONDARY_ORDER = stringPreferencesKey("sort_order_secondary")
    }

    ///////////////////////////////////////////////
    // Getting preferences flow
    ///////////////////////////////////////////////
    val settingsPreferencesFlow: Flow<PreferenceList> = dataStore.data
        .catch { exception ->
            Log.e(TAG, "$exception")
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
                preferences[PreferencesKeys.SORT_PRIMARY_ORDER] ?: OrderMediaQueue.DEFAULT.name
            )
        } catch (_: Exception) {
            OrderMediaQueue.ALBUM
        }

        val sortSecondaryOrder = try {
            OrderMediaQueue.valueOf(
                preferences[PreferencesKeys.SORT_SECONDARY_ORDER] ?: OrderMediaQueue.DEFAULT.name
            )
        } catch (_: Exception) {
            OrderMediaQueue.TRACK
        }


        return PreferenceList(
            trackPrimaryOrder = sortPrimaryOrder,
            trackSecondaryOrder = sortSecondaryOrder,
        )
    }

    ///////////////////////////////////////////////
    // Updating preferences
    ///////////////////////////////////////////////
    suspend fun updateOrder(
        primaryOrder: OrderMediaQueue,
        secondaryOrder: OrderMediaQueue
    ) {
        dataStore.edit { preferences ->
            if (primaryOrder == OrderMediaQueue.DEFAULT) {
                preferences[PreferencesKeys.SORT_PRIMARY_ORDER] = OrderMediaQueue.ALBUM.name
                preferences[PreferencesKeys.SORT_SECONDARY_ORDER] = OrderMediaQueue.ID.name
            } else {
                preferences[PreferencesKeys.SORT_PRIMARY_ORDER] = primaryOrder.name
                if (secondaryOrder == OrderMediaQueue.DEFAULT)
                    preferences[PreferencesKeys.SORT_SECONDARY_ORDER] = OrderMediaQueue.ID.name
                else
                    preferences[PreferencesKeys.SORT_SECONDARY_ORDER] = secondaryOrder.name
            }
        }
    }
}