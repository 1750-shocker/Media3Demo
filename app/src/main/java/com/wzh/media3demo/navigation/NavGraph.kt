package com.wzh.media3demo.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
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
    const val Albums = "albums"
    const val AlbumDetail = "albumDetail/{albumId}"
    const val NowPlaying = "nowPlaying"
    const val VideoPicker = "videoPicker"
    const val VideoPlayer = "videoPlayer"
}

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.Home
    ) {
        composable(
            route = Routes.Home,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            val themeController = LocalThemeController.current
            Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(onClick = { navController.navigate(Routes.Albums) }) {
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
        composable(
            route = Routes.Albums,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            com.wzh.media3demo.music.ui.AlbumsScreen(navController)
        }
        composable(
            route = Routes.AlbumDetail,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) { backStackEntry ->
            com.wzh.media3demo.music.ui.AlbumDetailScreen(navController, backStackEntry)
        }
        composable(
            route = Routes.NowPlaying,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            com.wzh.media3demo.music.ui.NowPlayingScreen()
        }
        composable(
            route = Routes.VideoPicker,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            com.wzh.media3demo.video.ui.VideoPickerScreen(navController)
        }
        composable(
            route = Routes.VideoPlayer,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            com.wzh.media3demo.video.ui.VideoPlayerScreen()
        }
    }
}
