package com.raaveinm.chirro.ui.layouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.raaveinm.chirro.R.dimen.medium_padding
import com.raaveinm.chirro.R.dimen.medium_size
import com.raaveinm.chirro.ui.navigation.NavData

@Composable
fun TopBar(
    modifier: Modifier,
    navController: NavController
) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            modifier = Modifier.padding(horizontal = dimensionResource(medium_padding)),
            onClick = { navController.navigate(NavData.SettingsScreen) }
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "toSettings",
                modifier = Modifier
                    .defaultMinSize(dimensionResource(medium_size)),
            )
        }

        IconButton(
            modifier = Modifier.padding(horizontal = dimensionResource(medium_padding)),
            onClick = { navController.navigate(NavData.PlaylistScreen) }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                contentDescription = "toPlaylist",
                modifier = Modifier
                    .defaultMinSize(dimensionResource(medium_size)),
            )
        }
    }
}

@Preview
@Composable
fun topBarPreview(){
    TopBar(
        modifier = Modifier,
        navController = NavController(LocalContext.current)
    )
}