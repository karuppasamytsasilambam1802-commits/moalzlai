package com.example.myapplication


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.myapplication.navigation.AppNavigation
import com.example.myapplication.ui.theme.VoxBridgeTheme
import com.example.myapplication.viewmodel.VoxBridgeViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: VoxBridgeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Pre-initialize audio & TTS engine
        viewModel.initAudioManagers(this)

        setContent {
            VoxBridgeTheme {
                AppNavigation(viewModel = viewModel)
            }
        }
    }
}
