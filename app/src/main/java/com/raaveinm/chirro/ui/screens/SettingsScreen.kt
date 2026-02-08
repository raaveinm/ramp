package com.raaveinm.chirro.ui.screens

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raaveinm.chirro.R
import com.raaveinm.chirro.ui.layouts.DropDownSelection
import com.raaveinm.chirro.ui.layouts.SettingCard
import com.raaveinm.chirro.ui.theme.AppTheme
import com.raaveinm.chirro.ui.theme.ThemeOption
import com.raaveinm.chirro.ui.theme.languageMap
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
    val hazeState: HazeState = remember { HazeState(true) }
    Column(
        modifier = modifier
            .hazeSource(state = hazeState)
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
    ) {
        val uiState = viewModel.uiState.collectAsState().value

        // TODO("set custom unknown cover")

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
                    .animateContentSize()
            ) {
                val options = stringArrayResource(R.array.track_order_options).toList()
                val selectedPrimary = options[uiState.trackPrimaryOrder.ordinal]
                val selectedSecondary = options[uiState.trackSecondaryOrder.ordinal]
                val isShuffleMode = uiState.isShuffleMode

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensionResource(R.dimen.s_medium_padding))
                        .padding(vertical = dimensionResource(R.dimen.small_padding))
                ) {
                    val opt = listOf(
                        stringResource(R.string.shuffle_on),
                        stringResource(R.string.shuffle_off)
                    )
                    val selectedIndex = if (uiState.isShuffleMode) 0 else 1

                    Text(
                        text = stringResource(R.string.shuffle_mode),
                        modifier = Modifier.wrapContentSize(),
                        style = MaterialTheme.typography.titleSmall
                    )

                    SingleChoiceSegmentedButtonRow {
                        opt.forEachIndexed { index, label ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = opt.size
                                ),
                                onClick = { viewModel.setShuffleMode(!uiState.isShuffleMode) },
                                selected = index == selectedIndex,
                                label = { Text(label) },
//                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = !isShuffleMode,
                    modifier = Modifier.padding(vertical = dimensionResource(R.dimen.small_padding))
                ) {
                    Column {

                        HorizontalDivider(
                            Modifier
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
                                text = stringResource(R.string.track_primary),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleSmall
                            )
                            DropDownSelection(
                                options = options,
                                selectedOption = selectedPrimary,
                                onOptionSelected = {
                                    viewModel.setTrackPrimaryOrder(
                                        options.indexOf(
                                            it
                                        )
                                    )
                                },
                                hazeStateModifier = Modifier
                                    .hazeEffect(hazeState)
                                    .zIndex(1f)
                            )
                        }

                        HorizontalDivider(
                            Modifier
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
                                modifier = Modifier.weight(2f),
                                style = MaterialTheme.typography.titleSmall
                            )
                            DropDownSelection(
                                options = options,
                                selectedOption = selectedSecondary,
                                onOptionSelected = {
                                    viewModel.setTrackSecondaryOrder(
                                        options.indexOf(
                                            it
                                        )
                                    )
                                },
                                hazeStateModifier = Modifier
                                    .hazeEffect(hazeState)
                                    .zIndex(1f)
                            )
                        }

                        HorizontalDivider(
                            Modifier
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
                            val opt = listOf(
                                stringResource(R.string.order_asc),
                                stringResource(R.string.order_desc)
                            )
                            val selectedIndex = if (uiState.trackSortAscending) 0 else 1

                            Text(
                                text = stringResource(R.string.order),
                                modifier = Modifier.wrapContentSize(),
                                style = MaterialTheme.typography.titleSmall
                            )

                            SingleChoiceSegmentedButtonRow {
                                opt.forEachIndexed { index, label ->
                                    SegmentedButton(
                                        shape = SegmentedButtonDefaults.itemShape(
                                            index = index,
                                            count = opt.size
                                        ),
                                        onClick = { viewModel.setTrackSortAscending(!uiState.trackSortAscending) },
                                        selected = index == selectedIndex,
                                        label = { Text(label) },
//                                modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                        }
                    }
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

        ///////////////////////////////////////////////
        // Application behaviour
        ///////////////////////////////////////////////
        SettingCard(
            title = stringResource(R.string.app_behaviour),
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
                        modifier = Modifier.weight(5f),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Switch(
                        checked = checked,
                        modifier = Modifier.weight(1f),
                        onCheckedChange = { viewModel.setSavedState(it) },
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
        // Language
        ///////////////////////////////////////////////
        SettingCard(
            title = stringResource(R.string.language),
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
                val currentAppLocales = AppCompatDelegate.getApplicationLocales()
                val currentLocale = if (!currentAppLocales.isEmpty) currentAppLocales[0]
                else java.util.Locale.getDefault()
                val languageCode = currentLocale?.language ?: "en"
                val selectedOption = languageMap[languageCode] ?: "English"
                val options = languageMap.values.toList()

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensionResource(R.dimen.s_medium_padding))
                        .padding(vertical = dimensionResource(R.dimen.small_padding))
                ) {
                    Text(
                        text = stringResource(R.string.select_language),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall
                    )

                    DropDownSelection(
                        options = options,
                        selectedOption = selectedOption,
                        onOptionSelected = { displayName ->
                            val selectedCode = languageMap.entries.firstOrNull {
                                it.value == displayName
                            }?.key ?: "en"
                            val appLocale = LocaleListCompat.forLanguageTags(selectedCode)
                            AppCompatDelegate.setApplicationLocales(appLocale)
                        },
                        hazeStateModifier = Modifier
                            .hazeEffect(hazeState)
                            .zIndex(1f)
                    )
                }
            }
        }
    }
}