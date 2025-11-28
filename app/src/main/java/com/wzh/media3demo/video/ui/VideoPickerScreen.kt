package com.wzh.media3demo.video.ui

import android.net.Uri
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wzh.media3demo.video.VideoViewModel
@Composable
fun VideoPickerScreen(navController: NavController) {
    val context = LocalContext.current
    val vm: VideoViewModel = viewModel()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        val uri: Uri? = data?.data
        Log.d("wzhhh", "onActivityResult uri=" + uri + " flags=" + data?.flags)
        if (uri != null) {
            val flags = data.flags
            val takeFlags = flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            try {
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
                Log.d("wzhhh", "takePersistableUriPermission success read/write")
            } catch (_: SecurityException) {}
            if (context.contentResolver.persistedUriPermissions.none { it.uri == uri }) {
                try {
                    context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    Log.d("wzhhh", "fallback takePersistableUriPermission READ only done")
                } catch (e: SecurityException) {
                    Log.d("wzhhh", "fallback READ persist failed: " + e.message)
                }
            }
            val persisted = context.contentResolver.persistedUriPermissions.joinToString { it.uri.toString() }
            Log.d("wzhhh", "persistedUriPermissions=" + persisted)
            vm.setUri(uri)
            Log.d("wzhhh", "navigate to videoPlayer")
            navController.navigate("videoPlayer")
        }
    }
    Column(modifier = Modifier.fillMaxSize().systemBarsPadding(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "video/*"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }
            Log.d("wzhhh", "launch ACTION_OPEN_DOCUMENT")
            launcher.launch(intent)
        }) { Text("选择视频文件") }
    }
}

