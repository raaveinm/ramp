package com.raaveinm.chirro.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.raaveinm.chirro.ui.fragments.PlayerScreen

@Composable
fun MainScreen () {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        PlayerScreen(modifier = Modifier.padding(innerPadding))
//        NavHost(
//            navController = navController,
//            startDestination = com.raaveinm.chirro.ui.veiwmodel.NavHost.Player.name,
//            modifier = Modifier.padding(innerPadding)
//        )
    }
}