package com.raaveinm.chirro.ui.layouts.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.raaveinm.chirro.R
import com.raaveinm.chirro.data.values.EqualizerPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerPreset(
    modifier: Modifier = Modifier,
    presets: List<EqualizerPreferences> = listOf(
        EqualizerPreferences.NORMAL,
        EqualizerPreferences.ROCK,
        EqualizerPreferences.JAZZ,
        EqualizerPreferences.CLASSICAL,
        EqualizerPreferences.BASS_BOOST,
        EqualizerPreferences.TREBLE_BOOST,
        EqualizerPreferences.CUSTOM
    ),
    selectedIndex: Int = 0,
    onSelectionChange: (Int) -> Unit
) {
    val labels = listOf(
        stringResource(R.string.eq_normal),
        stringResource(R.string.eq_rock),
        stringResource(R.string.eq_jazz),
        stringResource(R.string.eq_classical),
        stringResource(R.string.eq_bass_boost),
        stringResource(R.string.eq_treble_boost),
        stringResource(R.string.eq_custom)
    )

    Row(modifier = modifier.horizontalScroll(rememberScrollState())) {
        SingleChoiceSegmentedButtonRow {
            presets.forEachIndexed { index, _ ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = presets.size
                    ),
                    onClick = { onSelectionChange(index) },
                    selected = index == selectedIndex,
                    label = { Text(labels[index]) }
                )
            }
        }
    }
}


@Preview
@Composable
fun EqualizerPreview() {
    var selectedPresetIndex by remember { mutableIntStateOf(0) }

    EqualizerPreset(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.inverseOnSurface),
        selectedIndex = selectedPresetIndex,
        onSelectionChange = { selectedPresetIndex = it }
    )
}
