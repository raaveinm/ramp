package com.raaveinm.chirro.ui.veiwmodel

import com.raaveinm.chirro.data.datastore.PlaybackState
import com.raaveinm.chirro.data.datastore.SettingDataStoreRepository
import com.raaveinm.chirro.data.datastore.SettingsList
import com.raaveinm.chirro.data.datastore.UiPreferences
import com.raaveinm.chirro.data.values.EqualizerPreferences
import com.raaveinm.chirro.data.values.OrderMediaQueue
import com.raaveinm.chirro.ui.theme.AppTheme
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var settingsRepository: SettingDataStoreRepository
    private lateinit var viewModel: SettingsViewModel
    private val setEqualizerBand: (Int, Float) -> Unit = mockk(relaxed = true)

    private val settingsFlow = MutableStateFlow(SettingsList())
    private val playbackStateFlow = MutableStateFlow(PlaybackState())
    private val uiSettingsFlow = MutableStateFlow(UiPreferences(backgroundImageOpacity = 30))

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.w(any(), any() as String) } returns 0
        
        settingsRepository = mockk(relaxed = true)

        every { settingsRepository.settingsFlow } returns settingsFlow
        every { settingsRepository.playbackStateFlow } returns playbackStateFlow
        every { settingsRepository.uiSettingsFlow } returns uiSettingsFlow

        viewModel = SettingsViewModel(settingsRepository, setEqualizerBand)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `uiState initial value matches default values`() = runTest {
        advanceUntilIdle()
        val expected = SettingsUiState()
        assertEquals(expected, viewModel.uiState.value)
    }

    @Test
    fun `setShuffleMode updates repository`() = runTest {
        viewModel.setShuffleMode(true)
        advanceUntilIdle()
        coVerify { settingsRepository.setShuffleMode(true) }
    }

    @Test
    fun `setTrackPrimaryOrder updates repository with correct enum`() = runTest {
        viewModel.setTrackPrimaryOrder(2) // TITLE
        advanceUntilIdle()
        coVerify { settingsRepository.updatePrimaryOrder(OrderMediaQueue.TITLE) }
    }

    @Test
    fun `setTrackSecondaryOrder updates repository with correct enum`() = runTest {
        viewModel.setTrackSecondaryOrder(3) // ARTIST
        advanceUntilIdle()
        coVerify { settingsRepository.updateSecondaryOrder(OrderMediaQueue.ARTIST) }
    }

    @Test
    fun `setTrackSortAscending updates repository`() = runTest {
        viewModel.setTrackSortAscending(false)
        advanceUntilIdle()
        coVerify { settingsRepository.updateSortAscending(false) }
    }

    @Test
    fun `setTheme updates repository`() = runTest {
        viewModel.setTheme(AppTheme.PINK)
        advanceUntilIdle()
        coVerify { settingsRepository.updateTheme(AppTheme.PINK) }
    }

    @Test
    fun `setBackgroundDynamicColor updates repository`() = runTest {
        viewModel.setBackgroundDynamicColor(false)
        advanceUntilIdle()
        coVerify { settingsRepository.setBackgroundDynamicColor(false) }
    }

    @Test
    fun `setBackgroundImage updates repository`() = runTest {
        viewModel.setBackgroundImage(true)
        advanceUntilIdle()
        coVerify { settingsRepository.setBackgroundImage(true) }
    }

    @Test
    fun `setSavedState updates repository`() = runTest {
        viewModel.setSavedState(true, null)
        advanceUntilIdle()
        coVerify { settingsRepository.setSavedState(true, null) }
    }

    @Test
    fun `setBackgroundAlpha updates local override and repository after delay`() = runTest {
        viewModel.setBackgroundAlpha(0.5f)
        assertEquals(0.5f, viewModel.alphaState.value.backgroundAlpha)
        
        advanceUntilIdle()
        coVerify { settingsRepository.setBackgroundImgOpacity(50) }
    }

    @Test
    fun `setEqualizer updates local override and repository after delay`() = runTest {
        val prefs = EqualizerPreferences.BASS_BOOST
        viewModel.setEqualizer(prefs)
        
        // Wait for the delay in setEqualizer (200ms)
        advanceUntilIdle()
        coVerify { settingsRepository.updateEqualizer(prefs) }
    }

    @Test
    fun `setEqualizerBand updates local override and repository after delay`() = runTest {
        viewModel.setEqualizerBand(0, 5.0f) // Sub Bass
        
        advanceUntilIdle()
        coVerify { 
            settingsRepository.updateEqualizer(match { it.subBass == 5.0f && it.id == "custom" })
        }
    }
}
