package com.example.memoryroutesearch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.example.memoryroutesearch.ui.MemoryRouteGame
import com.example.memoryroutesearch.ui.theme.MemoryRouteSearchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MemoryRouteSearchTheme {
                Surface {
                    MemoryRouteGame()
                }
            }
        }
    }
}
