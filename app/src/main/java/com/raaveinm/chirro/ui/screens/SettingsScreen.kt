package com.raaveinm.chirro.ui.screens

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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.raaveinm.chirro.R

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    Box(
        modifier = modifier,
        content = {
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(top = dimensionResource(R.dimen.large_padding))

            ) {
                Text(
                    text = "Developer is now drinking coffee, " +
                            "so he is not in condition to finish settings",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = dimensionResource(R.dimen.medium_padding))
                        .padding(horizontal = dimensionResource(R.dimen.medium_padding)),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge
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
    )
}