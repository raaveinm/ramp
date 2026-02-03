package com.raaveinm.chirro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.BrightnessMedium
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raaveinm.chirro.R
import com.raaveinm.chirro.ui.layouts.DropDownSelection
import com.raaveinm.chirro.ui.layouts.SettingCard
import com.raaveinm.chirro.ui.theme.AppTheme
import com.raaveinm.chirro.ui.theme.ThemeOption
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
        val uiState = viewModel.uiState.collectAsState().value

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

                HorizontalDivider(Modifier
                    .padding(horizontal = dimensionResource(R.dimen.medium_padding))
                    .padding(vertical = dimensionResource(R.dimen.small_padding))
                )

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
                .padding(horizontal = dimensionResource(R.dimen.small_padding))
        ) {
            Column(
                modifier = Modifier
                    .hazeSource(state = hazeState)
                    .zIndex(.5f)
            ) {
                val options = stringArrayResource(R.array.track_order_options).toList()
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

                HorizontalDivider(Modifier
                    .padding(horizontal = dimensionResource(R.dimen.medium_padding))
                    .padding(vertical = dimensionResource(R.dimen.small_padding))
                )

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

                HorizontalDivider(Modifier
                    .padding(horizontal = dimensionResource(R.dimen.medium_padding))
                    .padding(vertical = dimensionResource(R.dimen.small_padding))
                )

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensionResource(R.dimen.s_medium_padding))
                        .padding(vertical = dimensionResource(R.dimen.small_padding))
                ) {
                    val checked = uiState.isSavedState

                    Text(
                        text = stringResource(R.string.saved_text),
                        modifier = Modifier,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Switch(
                        checked = checked,
                        onCheckedChange = { viewModel.setSavedState(it)},
                        thumbContent = {
                            Icon(
                                imageVector = if (checked) Icons.Filled.Check
                                else Icons.Filled.Close,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        }
                    )
                }
            }
        }

        ///////////////////////////////////////////////
        // Theme
        ///////////////////////////////////////////////
        SettingCard(
            title = stringResource(R.string.theme),
            modifier = Modifier
                .fillMaxWidth()
                .hazeSource(hazeState)
                .padding(vertical = dimensionResource(R.dimen.medium_padding))
                .padding(horizontal = dimensionResource(R.dimen.small_padding))
        ) {
            Column(modifier = Modifier
                .hazeSource(hazeState)
                .zIndex(.5f)) {
                val themeOptions = listOf(
                    ThemeOption(
                        AppTheme.DARK,
                        stringResource(R.string.dark),
                        Icons.Outlined.DarkMode,
                        Icons.Filled.DarkMode
                    ),
                    ThemeOption(
                        AppTheme.DYNAMIC,
                        stringResource(R.string.dynamic),
                        Icons.Outlined.AutoFixHigh,
                        Icons.Filled.AutoFixHigh
                    ),
                    ThemeOption(
                        AppTheme.LIGHT,
                        stringResource(R.string.light),
                        Icons.Outlined.LightMode,
                        Icons.Filled.LightMode
                    ),
                    ThemeOption(
                        AppTheme.HIGH_CONTRAST,
                        stringResource(R.string.high_contrast),
                        Icons.Outlined.Contrast,
                        Icons.Filled.Contrast
                    ),
                    ThemeOption(
                        AppTheme.MONOCHROME,
                        stringResource(R.string.monochrome),
                        Icons.Outlined.BrightnessMedium,
                        Icons.Filled.BrightnessMedium
                    ),
                    ThemeOption(
                        AppTheme.PINK,
                        stringResource(R.string.pink),
                        Icons.Outlined.Brush,
                        Icons.Filled.Brush
                    )
                )

                themeOptions.forEachIndexed { index, option ->
                    val isSelected = uiState.currentTheme == option.theme

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.surfaceContainerHigh
                                else MaterialTheme.colorScheme.surfaceBright,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .heightIn(min = 56.dp)
                            .clickable { viewModel.setTheme(option.theme) }
                            .padding(vertical = dimensionResource(R.dimen.small_padding))
                    ) {
                        Icon(
                            imageVector = if (isSelected) option.selectedIcon else option.icon,
                            contentDescription = null,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                        Text(
                            text = option.label,
                            modifier = Modifier.padding(end = 16.dp),
                            style = MaterialTheme.typography.titleSmall
                        )
                    }

                    if (index < themeOptions.lastIndex)
                        HorizontalDivider(Modifier
                            .padding(horizontal = dimensionResource(R.dimen.medium_padding))
                            .padding(vertical = dimensionResource(R.dimen.small_padding))
                        )
                }
            }
        }
    }
}