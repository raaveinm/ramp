package com.raaveinm.chirro.ui.layouts.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.raaveinm.chirro.R

@Composable
fun EdgeToEdgeRow(
    modifier: Modifier = Modifier,
    contentArray: Array<@Composable () -> Unit> = emptyArray()
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.s_medium_padding))
            .padding(vertical = dimensionResource(R.dimen.extra_small_padding)),
        content = {
            contentArray.forEach { it() }
        }
    )
}