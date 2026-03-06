package me.jakev.nexuscore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import me.jakev.nexuscore.ui.NexusCoreNavHost
import me.jakev.nexuscore.ui.theme.NexusCoreTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NexusCoreTheme {
                NexusCoreNavHost()
            }
        }
    }
}
