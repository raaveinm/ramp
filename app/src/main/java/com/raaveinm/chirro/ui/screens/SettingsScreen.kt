package com.raaveinm.chirro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raaveinm.chirro.R
import com.raaveinm.chirro.ui.layouts.DropDownSelection
import com.raaveinm.chirro.ui.layouts.SettingCard
import com.raaveinm.chirro.ui.veiwmodel.AppViewModelProvider
import com.raaveinm.chirro.ui.veiwmodel.SettingsViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
    ) {
        val hazeState = remember { HazeState(true) }
        // TODO("Colors and visualization")
        // TODO("set custom unknown cover")
        // TODO("language")

        ///////////////////////////////////////////////
        // Settings sample card
        ///////////////////////////////////////////////
        SettingCard(
            title = "Title",
            modifier = Modifier
                .fillMaxWidth()
                .hazeSource(state = hazeState)
                .padding(horizontal = dimensionResource(R.dimen.small_padding))
                .zIndex(.5f)
        ) {

            Column(
                modifier = Modifier.hazeSource(state = hazeState)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensionResource(R.dimen.s_medium_padding))
                        .padding(vertical = dimensionResource(R.dimen.medium_padding))
                ) {
                    Text(
                        text = "Test",
                        modifier = Modifier,
                        style = MaterialTheme.typography.titleSmall
                    )
                    val options = listOf("Option 1", "Option 2", "Option 3")
                    var selectedOption by remember { mutableStateOf(options[1]) }
                    DropDownSelection(
                        options = options,
                        selectedOption = selectedOption,
                        onOptionSelected = { selectedOption = it },
                        hazeStateModifier = Modifier
                            .hazeEffect(hazeState)
                            .zIndex(1f)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensionResource(R.dimen.s_medium_padding))
                        .padding(bottom = dimensionResource(R.dimen.small_padding))
                ) {
                    Text(
                        text = "Test",
                        modifier = Modifier,
                        style = MaterialTheme.typography.titleSmall
                    )
                    val options = listOf("Option 3", "Option 4", "Option 5")
                    var selectedOption by remember { mutableStateOf(options[1]) }
                    DropDownSelection(
                        options = options,
                        selectedOption = selectedOption,
                        onOptionSelected = { selectedOption = it },
                        hazeStateModifier = Modifier
                            .hazeEffect(hazeState)
                            .zIndex(1f)
                    )
                }
            }
        }

        ///////////////////////////////////////////////
        // Track Order
        ///////////////////////////////////////////////
        SettingCard(
            title = stringResource(R.string.track_order),
            modifier = Modifier
                .fillMaxWidth()
                .hazeSource(state = hazeState)
                .padding(vertical = dimensionResource(R.dimen.medium_padding))
                .padding(horizontal = dimensionResource(R.dimen.s_medium_padding))
        ) {
            Column(
                modifier = Modifier
                    .hazeSource(state = hazeState)
                    .zIndex(.5f)
            ) {
                val options = stringArrayResource(R.array.track_order_options).toList()
                val uiState = viewModel.uiState.collectAsState().value
                val selectedPrimary = options[uiState.trackPrimaryOrder.ordinal]
                val selectedSecondary = options[uiState.trackSecondaryOrder.ordinal]

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensionResource(R.dimen.s_medium_padding))
                        .padding(vertical = dimensionResource(R.dimen.small_padding))
                ) {
                    Text(
                        text = stringResource(R.string.track_primary),
                        modifier = Modifier,
                        style = MaterialTheme.typography.titleSmall
                    )
                    DropDownSelection(
                        options = options,
                        selectedOption = selectedPrimary,
                        onOptionSelected = { viewModel.setTrackPrimaryOrder(it) },
                        hazeStateModifier = Modifier
                            .hazeEffect(hazeState)
                            .zIndex(1f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensionResource(R.dimen.s_medium_padding))
                        .padding(vertical = dimensionResource(R.dimen.small_padding))
                ) {
                    Text(
                        text = stringResource(R.string.track_secondary),
                        modifier = Modifier,
                        style = MaterialTheme.typography.titleSmall
                    )
                    DropDownSelection(
                        options = options,
                        selectedOption = selectedSecondary,
                        onOptionSelected = { viewModel.setTrackSecondaryOrder(it) },
                        hazeStateModifier = Modifier
                            .hazeEffect(hazeState)
                            .zIndex(1f)
                    )
                }
            }
        }

        ///////////////////////////////////////////////
        // Theme
        ///////////////////////////////////////////////

    }
}