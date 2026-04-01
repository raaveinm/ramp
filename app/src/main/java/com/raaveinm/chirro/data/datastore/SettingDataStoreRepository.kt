package com.raaveinm.chirro.data.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.raaveinm.chirro.data.values.OrderMediaQueue
import com.raaveinm.chirro.data.values.TrackInfo
import com.raaveinm.chirro.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
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
        val IS_SHUFFLE_MODE = booleanPreferencesKey("is_shuffle_mode")
        val BACKGROUND_DYNAMIC_COLOR = booleanPreferencesKey("background_dynamic_color")
        val BACKGROUND_IMAGE = booleanPreferencesKey("background_image")
        val BACKGROUND_IMAGE_OPACITY = intPreferencesKey("background_image_opacity")
    }

    ///////////////////////////////////////////////
    // Getting preferences flow
    ///////////////////////////////////////////////
    @Deprecated(message = "preferred separate flows")
    val settingsPreferencesFlow: Flow<PreferenceList> = dataStore.data
        .catch { exception ->
            Log.e(tag, "$exception")
            emit(emptyPreferences())
        }
        .map { preferences -> mapOfPreferences(preferences) }

    val uiSettingsFlow: Flow<UiPreferences> = dataStore.data
        .catch {
            Log.e(tag, "$it")
            emit(emptyPreferences())
        }
        .map { preferences ->
            val currentTheme = try {
                AppTheme.valueOf(
                    preferences[PreferencesKeys.CURRENT_THEME] ?: AppTheme.DYNAMIC.name
                )
            } catch (e: Exception) {
                Log.w(tag, "Failed to read sort order: $e")
                AppTheme.DYNAMIC
            }
            val backgroundDynamicColor = try {
                preferences[PreferencesKeys.BACKGROUND_DYNAMIC_COLOR].toString().toBoolean()
            } catch (e: Exception) {
                Log.w(tag, "Failed to read sort order: $e")
                true
            }
            val backgroundImage = try {
                preferences[PreferencesKeys.BACKGROUND_IMAGE].toString().toBoolean()
            } catch (e: Exception) {
                Log.w(tag, "Failed to read sort order: $e")
                false
            }
            val opacity = try {
                preferences[PreferencesKeys.BACKGROUND_IMAGE_OPACITY]
            } catch (e: Exception) {
                Log.w(tag, "Failed to read sort order: $e")
                30
            }
            UiPreferences(
                currentTheme = currentTheme,
                backgroundDynamicColor = backgroundDynamicColor,
                backgroundImage = backgroundImage,
                backgroundImageOpacity = opacity?: 30
            )
        }

    val playbackStateFlow: Flow<PlaybackState> = dataStore.data
        .catch { exception ->
            Log.e(tag, "$exception")
            emit(emptyPreferences())
        }
        .map { preferences ->
            val isSavedState = try {
                preferences[PreferencesKeys.IS_SAVED_STATE]?.toBoolean() ?: false
            } catch (_: Exception) { false }

            val currentTrack = try {
                val jsonString = preferences[PreferencesKeys.CURRENT_TRACK]
                if (jsonString != null) {
                    Gson().fromJson(jsonString, TrackInfo::class.java)
                } else null
            } catch (e: Exception) {
                Log.e(tag, "Error parsing track: $e")
                null
            }

            PlaybackState(
                currentTrack = currentTrack,
                isSavedState = isSavedState
            )
        }
        .distinctUntilChanged()

    val settingsFlow: Flow<SettingsList> = dataStore.data
        .catch { exception ->
            Log.e(tag, "$exception")
            emit(emptyPreferences())
        }
        .map { preferences ->
            val sortPrimaryOrder = try {
                OrderMediaQueue.valueOf(
                    preferences[PreferencesKeys.SORT_PRIMARY_ORDER] ?: OrderMediaQueue.ALBUM.name
                )
            } catch (e: Exception) {
                Log.w(tag, "Failed to read sort order: $e")
                OrderMediaQueue.ALBUM
            }

            val sortSecondaryOrder = try {
                OrderMediaQueue.valueOf(
                    preferences[PreferencesKeys.SORT_SECONDARY_ORDER] ?: OrderMediaQueue.TRACK.name
                )
            } catch (e: Exception) {
                Log.w(tag, "Failed to read sort order: $e")
                OrderMediaQueue.TRACK
            }
            val isAsc: Boolean = try {
                preferences[PreferencesKeys.SORT_ASCENDING].toString().toBoolean()
            } catch (e: Exception) {
                Log.w(tag, "Failed to read sort order: $e")
                true
            }
            val isShuffleMode = try {
                preferences[PreferencesKeys.IS_SHUFFLE_MODE].toString().toBoolean()
            } catch (e: Exception) {
                Log.w(tag, "Failed to read sort order: $e")
                false
            }

            SettingsList(
                trackPrimaryOrder = sortPrimaryOrder,
                trackSecondaryOrder = sortSecondaryOrder,
                trackSortAscending = isAsc,
                isShuffleMode = isShuffleMode,
            )
        }
        .distinctUntilChanged()

    ///////////////////////////////////////////////
    // #DEPRECATED Fetching preferences list
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

        val isShuffleMode = try {
            preferences[PreferencesKeys.IS_SHUFFLE_MODE].toString().toBoolean()
        } catch (_: Exception) {
            false
        }

        return PreferenceList(
            trackPrimaryOrder = sortPrimaryOrder,
            trackSecondaryOrder = sortSecondaryOrder,
            trackSortAscending = isAsc,
            currentTheme = currentTheme,
            currentTrack = currentTrack,
            isSavedState = isSavedState,
            isShuffleMode = isShuffleMode
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

    suspend fun setSavedState(state: Boolean, trackInfo: TrackInfo?) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_SAVED_STATE] = state.toString()
            if (!state) preferences.remove(PreferencesKeys.CURRENT_TRACK)
            else {
                val jsonString = Gson().toJson(trackInfo)
                preferences[PreferencesKeys.CURRENT_TRACK] = jsonString
            }
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

    suspend fun setShuffleMode(shuffleMode: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_SHUFFLE_MODE] = shuffleMode
        }
    }

    suspend fun setBackgroundDynamicColor(state: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.BACKGROUND_DYNAMIC_COLOR] = state
            if (state) preferences[PreferencesKeys.BACKGROUND_IMAGE] = false
        }
    }

    suspend fun setBackgroundImage(state: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.BACKGROUND_IMAGE] = state
            if (state) preferences[PreferencesKeys.BACKGROUND_DYNAMIC_COLOR] = false
        }
    }

    suspend fun setBackgroundImgOpacity(value: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.BACKGROUND_IMAGE_OPACITY] = value
        }
    }
}