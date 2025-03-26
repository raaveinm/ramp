package com.raaveinm.ramp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.raaveinm.ramp.ui.layouts.PlayerScreen
import com.raaveinm.ramp.ui.theme.RampTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RampTheme {
                PlayerScreen()
            }
        }
    }
}