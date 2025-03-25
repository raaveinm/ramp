package com.raaveinm.ramp.ui.layouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raaveinm.ramp.ui.theme.RampTheme

@Composable
fun PlayerLayout(
    modifier: Modifier,

){ RampTheme {
        Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
            Buttons(
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaffoldExample() {
    var presses by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("Top app bar")
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = "Bottom app bar",
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { presses++ }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                modifier = Modifier.padding(8.dp),
                text =
                    """
                    This is an example of a scaffold. It uses the Scaffold composable's parameters to create a screen with a simple top app bar, bottom app bar, and floating action button.

                    It also contains some basic inner content, such as this text.

                    You have pressed the floating action button $presses times.
                """.trimIndent(),
            )
        }
    }
}

@Composable
fun Buttons(
    modifier: Modifier = Modifier.padding(20.dp, 0.dp, 20.dp, 0.dp),
) {
    var isAdded by rememberSaveable { mutableStateOf(false) }
    var isPlaying by rememberSaveable { mutableStateOf(false) }
    var isShuffled by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = modifier.padding(10.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = { isAdded=!isAdded },
            modifier = Modifier
        ) {
            Icon(
                imageVector = if (isAdded) Icons.Default.Check else Icons.Default.Add,
                tint = MaterialTheme.colorScheme.onPrimary,
                contentDescription = if (isAdded) "Added" else "Add",
                modifier = Modifier.fillMaxSize(),
            )
        }

        IconButton(
            onClick = {},
            modifier = Modifier
        ) {
            Icon(
                imageVector = Icons.Default.FastRewind,
                tint = MaterialTheme.colorScheme.onPrimary,
                contentDescription = "Rewind",
                modifier = Modifier.fillMaxSize()
            )
        }

        IconButton(
            onClick = { isPlaying=!isPlaying },
            modifier = Modifier
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                tint = MaterialTheme.colorScheme.onPrimary,
                contentDescription = if (isPlaying) "Stop" else "Play",
                modifier = Modifier.fillMaxSize()
            )
        }

        IconButton(
            onClick = {},
            modifier = Modifier
        ) {
            Icon(
                imageVector = Icons.Default.FastForward,
                tint = MaterialTheme.colorScheme.onPrimary,
                contentDescription = "Fast Forward",
                modifier = Modifier.fillMaxSize()
            )
        }

        IconButton(
            onClick = { isShuffled=!isShuffled },
            modifier = Modifier
        ) {
            Icon(
                imageVector =  if (isShuffled) Icons.Default.AllInclusive else Icons.Default.Shuffle,
                tint = MaterialTheme.colorScheme.onPrimary,
                contentDescription =  if (isShuffled) "Shuffled" else "Shuffle",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}


@Preview
@Composable
fun PlayerLayoutPreview() {
    PlayerLayout(
        modifier = Modifier,
    )
}

@Preview
@Composable
fun ButtonsPreview() {
    RampTheme {
        Buttons()
    }
}

@Preview
@Composable
fun ScaffoldExamplePreview() {
    ScaffoldExample()
}