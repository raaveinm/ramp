package com.raaveinm.chirro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.raaveinm.chirro.R
import com.raaveinm.chirro.ui.layouts.DropDownSelection
import com.raaveinm.chirro.ui.layouts.SettingCard
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,

) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        val hazeState = remember { HazeState(true) }
        // TODO("fetch order")
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
                        .padding(vertical = dimensionResource(R.dimen.small_padding))
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
                        hazeStateModifier = Modifier.hazeEffect(hazeState)
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
                        hazeStateModifier = Modifier.hazeEffect(hazeState)
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
        ) {
            Column(
                modifier = Modifier.hazeSource(state = hazeState)
            ) {
                val options = stringArrayResource(R.array.track_order_options).toList()
                var selectedOption by remember { mutableStateOf(options[0]) }

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
                        selectedOption = selectedOption,
                        onOptionSelected = { selectedOption = it },
                        hazeStateModifier = Modifier.hazeEffect(hazeState)
                    )
                }
            }
        }
    }
}