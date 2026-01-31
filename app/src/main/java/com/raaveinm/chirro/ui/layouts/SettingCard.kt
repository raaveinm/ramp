package com.raaveinm.chirro.ui.layouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raaveinm.chirro.R

@Composable
fun SettingCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            modifier = Modifier
                .padding(horizontal = dimensionResource(R.dimen.medium_padding))
                .padding(bottom = dimensionResource(R.dimen.small_padding)),
            style = MaterialTheme.typography.titleSmall
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(R.dimen.small_padding))
                .shadow(
                    elevation = 5.dp,
                    shape = RoundedCornerShape(10.dp)
                ),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background
            ),
            content = content
        )
    }
}

@Preview
@Composable
fun OrderPreview() {
    SettingCard(
        title = "Title",
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
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
                var selectedOption = options[1]
                DropDownSelection(
                    options = options,
                    selectedOption = selectedOption,
                    onOptionSelected = { selectedOption = it },
                    hazeStateModifier = Modifier
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
                var selectedOption = options[1]
                DropDownSelection(
                    options = options,
                    selectedOption = selectedOption,
                    onOptionSelected = { selectedOption = it },
                    hazeStateModifier = Modifier
                )
            }
        }
    }
}