package com.raaveinm.chirro.ui.layouts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.zIndex
import com.raaveinm.chirro.R
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
fun SettingColumn(
    modifier: Modifier = Modifier,
    contentArray: Array<@Composable () -> Unit> = emptyArray(),
) {
    Column(
        modifier = modifier.zIndex(.5f),
        content = {
            contentArray.forEach {
                it()
                if (contentArray.last() != it)
                    HorizontalDivider(
                        Modifier
                            .padding(horizontal = dimensionResource(R.dimen.medium_padding))
                            .padding(vertical = dimensionResource(R.dimen.extra_small_padding))
                    )
            }
        }
    )
}