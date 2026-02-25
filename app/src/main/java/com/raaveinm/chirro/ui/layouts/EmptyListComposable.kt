package com.raaveinm.chirro.ui.layouts

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raaveinm.chirro.R

@Composable
fun EmptyListComposable(text: String? = null, stringRes: Int? = null) {
    val message = text ?: stringResource(stringRes ?: R.string.tracklist_empty)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimensionResource(R.dimen.large_padding))

    ) {
        Text(
            text = message,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = dimensionResource(R.dimen.medium_padding))
                .padding(horizontal = dimensionResource(R.dimen.medium_padding)),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.headlineMedium
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = dimensionResource(R.dimen.medium_padding))
        ) {
            Image(
                painter = painterResource(R.drawable.sad),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.3f)),
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 4.dp, y = 4.dp)
                    .blur(radius = 8.dp)
            )

            Image(
                painter = painterResource(R.drawable.sad),
                contentDescription = "Sad",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview
@Composable
fun EmptyListPreview() {
    EmptyListComposable()
}