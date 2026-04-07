package com.raaveinm.chirro.ui.screens

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raaveinm.chirro.R
import com.raaveinm.chirro.ui.layouts.settings.DropDownSelection
import com.raaveinm.chirro.ui.layouts.settings.EdgeToEdgeRow
import com.raaveinm.chirro.ui.layouts.settings.SettingCard
import com.raaveinm.chirro.ui.layouts.settings.SettingColumn
import com.raaveinm.chirro.ui.theme.AppTheme
import com.raaveinm.chirro.ui.theme.ThemeOption
import com.raaveinm.chirro.ui.theme.languageMap
import com.raaveinm.chirro.ui.veiwmodel.AppViewModelProvider
import com.raaveinm.chirro.ui.veiwmodel.PlayerViewModel
import com.raaveinm.chirro.ui.veiwmodel.SettingsViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.FlowPreview
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    playerViewModel: PlayerViewModel = viewModel(factory = AppViewModelProvider.Factory),
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
    innerPadding: PaddingValues
) {
    val hazeState: HazeState = remember { HazeState(true) }
    Column(
        modifier = modifier
            .hazeSource(state = hazeState)
            .padding(top = innerPadding.calculateTopPadding())
            .nestedScroll(scrollBehavior.nestedScrollConnection)
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
            SettingColumn(
                modifier = Modifier.hazeSource(state = hazeState),
                contentArray = arrayOf({
                    val checked = uiState.isShuffleMode
                    EdgeToEdgeRow(
                        contentArray = arrayOf({
                            Text(
                                text = stringResource(R.string.shuffle_mode),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }, {
                            Switch(
                                checked = checked,
                                onCheckedChange = { viewModel.setShuffleMode(it) },
                                thumbContent = {
                                    Icon(
                                        imageVector = if (checked) Icons.Filled.Check
                                        else Icons.Filled.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                }
                            )
                        })
                    )
                },{
                    val options = stringArrayResource(R.array.track_order_options).toList()
                    val selectedPrimary = options[uiState.trackPrimaryOrder.ordinal]
                    AnimatedVisibility(!uiState.isShuffleMode) {
                        SettingColumn(
                            modifier = Modifier.animateContentSize(),
                            contentArray = arrayOf({
                                EdgeToEdgeRow(
                                    contentArray = arrayOf({
                                        Text(
                                            text = stringResource(R.string.track_primary),
                                            modifier = Modifier.weight(1f),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },{
                                        DropDownSelection(
                                            options = options,
                                            selectedOption = selectedPrimary,
                                            onOptionSelected = {
                                                viewModel.setTrackPrimaryOrder(
                                                    options.indexOf(it)
                                                )
                                            },
                                            hazeStateModifier = Modifier
                                                .hazeEffect(hazeState)
                                                .zIndex(1f)
                                        )
                                    })
                                )
                            },{
                                val options = stringArrayResource(R.array.track_order_options).toList()
                                val selectedSecondary = options[uiState.trackSecondaryOrder.ordinal]
                                EdgeToEdgeRow(
                                    contentArray = arrayOf({
                                        Text(
                                            text = stringResource(R.string.track_secondary),
                                            modifier = Modifier.weight(1f),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },{
                                        DropDownSelection(
                                            options = options,
                                            selectedOption = selectedSecondary,
                                            onOptionSelected = { viewModel.setTrackSecondaryOrder(options.indexOf(it)) },
                                            hazeStateModifier = Modifier
                                                .hazeEffect(hazeState)
                                                .zIndex(1f)
                                        )
                                    })
                                )
                            },{
                                val opt = listOf(
                                    stringResource(R.string.order_asc),
                                    stringResource(R.string.order_desc)
                                )
                                val selectedIndex = if (uiState.trackSortAscending) 0 else 1
                                EdgeToEdgeRow(
                                    contentArray = arrayOf({
                                        Text(
                                            text = stringResource(R.string.order),
                                            modifier = Modifier.wrapContentSize(),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },{
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
                                                )
                                            }
                                        }
                                    })
                                )
                            })
                        )
                    }
                })
            )
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
                                shape = RoundedCornerShape(8.dp)
                            )
                            .heightIn(min = 24.dp)
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
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (index < themeOptions.lastIndex)
                        HorizontalDivider(Modifier
                            .padding(horizontal = dimensionResource(R.dimen.medium_padding))
                            .padding(vertical = dimensionResource(R.dimen.extra_small_padding))
                        )
                }
            }
        }

        ///////////////////////////////////////////////
        // Application behavior
        ///////////////////////////////////////////////
        SettingCard(
            title = stringResource(R.string.app_behaviour),
            modifier = Modifier
                .fillMaxWidth()
                .hazeSource(state = hazeState)
                .padding(vertical = dimensionResource(R.dimen.medium_padding))
                .padding(horizontal = dimensionResource(R.dimen.small_padding))
        ) {
            SettingColumn(
                modifier = Modifier.hazeSource(state = hazeState),
                contentArray = arrayOf({
                    val checked = uiState.isSavedState
                    EdgeToEdgeRow(
                        contentArray = arrayOf({
                            Text(
                                text = stringResource(R.string.saved_text),
                                modifier = Modifier.weight(5f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },{
                            Switch(
                                checked = checked,
                                modifier = Modifier.weight(1f),
                                onCheckedChange = { viewModel.setSavedState(it, playerViewModel.uiState.value.currentTrack) },
                                thumbContent = {
                                    Icon(
                                        imageVector = if (checked) Icons.Filled.Check
                                        else Icons.Filled.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                }
                            )
                        })
                    )
                },{
                    val checked = uiState.backgroundDynamicColor
                    EdgeToEdgeRow(
                        contentArray = arrayOf({
                            Text(
                                text = stringResource(R.string.is_dynamic_color),
                                modifier = Modifier.weight(5f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },{
                            Switch(
                                checked = checked,
                                modifier = Modifier.weight(1f),
                                onCheckedChange = { viewModel.setBackgroundDynamicColor(it) },
                                thumbContent = {
                                    Icon(
                                        imageVector = if (checked) Icons.Filled.Check
                                        else Icons.Filled.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                }
                            )
                        })
                    )
                },{
                    val checked = uiState.backgroundImage
                    Column {
                        EdgeToEdgeRow(
                            contentArray = arrayOf({
                                Text(
                                    text = stringResource(R.string.background_image),
                                    modifier = Modifier.weight(5f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }, {
                                Switch(
                                    checked = checked,
                                    modifier = Modifier.weight(1f),
                                    onCheckedChange = { viewModel.setBackgroundImage(it) },
                                    thumbContent = {
                                        Icon(
                                            imageVector = if (checked) Icons.Filled.Check
                                            else Icons.Filled.Close,
                                            contentDescription = null,
                                            modifier = Modifier.size(SwitchDefaults.IconSize),
                                        )
                                    }
                                )
                            })
                        )
                        AnimatedVisibility(checked) {
                            EdgeToEdgeRow(
                                contentArray = arrayOf(
                                    {
                                        Column {
                                            Text(
                                                text = stringResource(R.string.opacity),
                                                modifier = Modifier.fillMaxWidth().padding(top = dimensionResource(R.dimen.small_padding)),
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Slider(
                                                value = viewModel.alphaState.collectAsState().value.backgroundAlpha,
                                                onValueChange = { newValue ->
                                                    viewModel.setBackgroundAlpha(
                                                        newValue
                                                    )
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                )
                            )
                        }
                    }
                })
            )
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
            SettingColumn(
                modifier = Modifier.hazeSource(state = hazeState),
                contentArray = arrayOf({
                    val currentAppLocales = AppCompatDelegate.getApplicationLocales()
                    val currentLocale =
                        if (!currentAppLocales.isEmpty) currentAppLocales[0]
                        else Locale.getDefault()
                    val languageCode = currentLocale?.language ?: "en"
                    val selectedOption = languageMap[languageCode] ?: "English"
                    val options = languageMap.values.toList()
                    EdgeToEdgeRow(
                        contentArray = arrayOf({
                            Text(
                                text = stringResource(R.string.select_language),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },{
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
                        })
                    )
                })
            )
        }
        Spacer(Modifier.size(size = 48.dp))
    }
}
