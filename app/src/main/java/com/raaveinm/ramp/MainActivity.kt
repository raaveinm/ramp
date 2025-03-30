package com.raaveinm.ramp

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_AUDIO
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import com.raaveinm.ramp.ui.layouts.PlayerScreen
import com.raaveinm.ramp.ui.theme.RampTheme



class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        var requirePermission = ""

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requirePermission = READ_MEDIA_AUDIO
        } else {
            requirePermission = READ_EXTERNAL_STORAGE
        }

        setContent {
            RampTheme {
                requestPermission(requirePermission)
                val context = applicationContext

                PlayerScreen()
            }
        }
    }

    fun requestPermission(type: String):Boolean {
        when (type) {
            READ_MEDIA_AUDIO ->{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission( this, READ_MEDIA_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    )
                    return true
                }else{
                    Log.e("MainActivity", "Requested unsupported permission")
                    return false
                }
            }
            READ_EXTERNAL_STORAGE -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission( this, READ_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    )
                    return true
                }else{
                    Log.e("MainActivity", "Requested unsupported permission")
                    return false
                }
            }
        }
        return false
    }
}