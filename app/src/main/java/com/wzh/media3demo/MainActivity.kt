package com.wzh.media3demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.wzh.media3demo.navigation.AppNavHost
import com.wzh.media3demo.ui.theme.Media3DemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var darkTheme by remember { mutableStateOf(false) }
            Media3DemoTheme(darkTheme = darkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    CompositionLocalProvider(LocalThemeController provides ThemeController(darkTheme) { darkTheme = !darkTheme }) {
                        AppNavHost(navController)
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {}

data class ThemeController(val dark: Boolean, val toggle: () -> Unit)
val LocalThemeController = compositionLocalOf { ThemeController(false) {} }

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Media3DemoTheme {
        Greeting("Android")
    }
}
