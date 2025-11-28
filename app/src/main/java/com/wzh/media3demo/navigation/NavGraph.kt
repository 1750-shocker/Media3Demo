package com.wzh.media3demo.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.wzh.media3demo.music.ui.MusicScreen
import com.wzh.media3demo.LocalThemeController

object Routes {
    const val Home = "home"
    const val Music = "music"
    const val VideoPicker = "videoPicker"
    const val VideoPlayer = "videoPlayer"
}

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.Home
    ) {
        composable(Routes.Home) {
            val themeController = LocalThemeController.current
            Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(onClick = { navController.navigate(Routes.Music) }) {
                        Text("音乐播放器")
                    }
                    Button(onClick = { navController.navigate(Routes.VideoPicker) }) {
                        Text("视频播放器")
                    }
                    Button(onClick = { themeController.toggle() }) {
                        Text("切换主题")
                    }
                }
            }
        }
        composable(Routes.Music) {
            MusicScreen()
        }
        composable(Routes.VideoPicker) {
            com.wzh.media3demo.video.ui.VideoPickerScreen(navController)
        }
        composable(Routes.VideoPlayer) {
            com.wzh.media3demo.video.ui.VideoPlayerScreen()
        }
    }
}
