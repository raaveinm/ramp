package com.raaveinm.chirro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.raaveinm.chirro.ui.MainScreen
import com.raaveinm.chirro.ui.theme.ChirroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { ChirroTheme { MainScreen() } }
    }
}
