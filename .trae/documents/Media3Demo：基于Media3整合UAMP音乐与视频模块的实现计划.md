# 项目总体方案

## 架构与技术栈

* 语言：Kotlin；UI：Jetpack Compose(Material3)；架构：MVVM + Repository

* 播放内核：`androidx.media3:media3-exoplayer`

* 后台播放与系统集成：`androidx.media3:media3-session`（`MediaSessionService`）

* 视频控件：`androidx.media3:media3-ui`（`PlayerView`/`StyledPlayerView` via `AndroidView`）

* 导航与转场：`androidx.navigation:navigation-compose` + `accompanist-navigation-animation`

* 权限：使用 SAF 选择视频，不依赖存储权限；音频后台播放使用前台服务权限

* 主题：Material3 深色/浅色切换；动态颜色（Android 12+）

## 模块与目录结构

* `app/src/main/java/com/wzh/media3demo/`

  * `main/` 主界面与导航

    * `MainActivity.kt`：`NavHost` + 两个入口按钮 + 动画

    * `navigation/`：路由常量与 `AnimatedNavHost`

  * `music/`

    * `service/AudioService.kt`：`MediaSessionService` + `ExoPlayer` + 通知

    * `data/UampRepository.kt`：解析 UAMP 音乐源（JSON）为 `MediaItem`

    * `ui/MusicScreen.kt`：播放控制（播放/暂停/上一/下一）+ 列表 + 进度条

    * `MusicViewModel.kt`：队列、当前项、进度、命令桥接（到 `MediaController`）

  * `video/`

    * `ui/VideoPickerScreen.kt`：SAF 选择视频（`OpenDocument`，MIME `video/*`）

    * `ui/VideoPlayerScreen.kt`：`ExoPlayer` + `StyledPlayerView` + 全屏/横竖屏

    * `VideoViewModel.kt`：加载 URI、播放器状态、错误提示

  * `common/`：错误展示、Snackbar、格式化工具等

* `app/src/main/assets/uamp_catalog.json`：UAMP 音乐源（示例曲目）

# 依赖与配置变更

## 版本库新增（`gradle/libs.versions.toml`）

* 添加：

  * `media3 = "1.3.1"`（或当前稳定版）

  * `navigationCompose = "2.8.3"`（或项目现有合适版本）

  * `accompanistNavAnim = "0.35.0"`

  * `coroutines = "1.9.0"`

  * `lifecycle = "2.8.6"`（`viewmodel`/`runtime`）

* 库条目：

  * `androidx-media3-exoplayer/ui/session`

  * `androidx-navigation-compose`

  * `accompanist-navigation-animation`

  * `kotlinx-coroutines-android/test`

  * `androidx-lifecycle-viewmodel-ktx`

## app 依赖（`app/build.gradle.kts`）

* `implementation("androidx.media3:media3-exoplayer:")`

* `implementation("androidx.media3:media3-ui:")`

* `implementation("androidx.media3:media3-session:")`

* `implementation("androidx.navigation:navigation-compose:")`

* `implementation("com.google.accompanist:accompanist-navigation-animation:")`

* `implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:")`

* `implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:")`

* 测试：`kotlinx-coroutines-test`、`junit`

## Manifest 更新（`app/src/main/AndroidManifest.xml`）

* 声明服务：`com.wzh.media3demo.music.service.AudioService`（`foregroundServiceType="mediaPlayback"`）

* 权限：

  * `android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK`

  * `android.permission.POST_NOTIFICATIONS`（Android 13+，可选）

* `intent-filter` 无需暴露，`MediaController` 通过 `SessionToken` 连接

# 主界面与导航

## MainActivity 与 NavHost

* 使用 `AnimatedNavHost(startDestination = "home")`，路由：

  * `home`：两个按钮

  * `music`：音乐播放界面

  * `videoPicker`：视频选择界面

  * `videoPlayer/{uri}`：播放页面（支持多 URI，初版先单 URI）

* 转场动画：`accompanist-navigation-animation` 的 `fadeIn/slideIn` 与 `fadeOut/slideOut`

* 主题切换：在 `TopAppBar` 放置开关，写入 `ViewModel`/`DataStore`（初版存内存）

# 音乐播放模块（UAMP 音乐源 + Media3）

## 数据源与解析（UAMP）

* 复制 UAMP 的最小化数据模型：`Album`、`Track` 等，或简化为 `Track(id, title, artist, url, artwork)`

* `assets/uamp_catalog.json`：从 UAMP 示例提取若干曲目（或使用公开 URL）

* `UampRepository`：读取 assets → 解析为 `List<MediaItem>`（设置 `MediaMetadata`）

## 播放服务（后台）

* `AudioService : MediaSessionService`

  * `onCreate`：构建 `ExoPlayer`、`MediaSession`、`DefaultMediaNotificationProvider`

  * 加载队列：来自 `UampRepository`，`player.setMediaItems(queue)` + `prepare()`

  * 音频焦点与 `AudioAttributes`：`USAGE_MEDIA`/`CONTENT_TYPE_MUSIC`

  * 前台通知：自动由 `MediaSessionService` 提供（MediaStyle）

  * 错误处理：`Player.Listener.onPlayerError` → 发送 `SessionEvent` 或广播

## 控制与 UI（Compose）

* `MusicViewModel`

  * 通过 `MediaController`（`SessionToken(this, ComponentName(this, AudioService::class.java))`）与服务连接

  * 暴露 `StateFlow`：播放状态、当前曲目、进度、队列

  * 操作：`play/pause/next/previous/seekTo(index/pos)`

* `MusicScreen`

  * 列表：显示队列与选中态

  * 控件：播放/暂停/上一首/下一首、滑块进度条、当前时间/总时长

  * 进度：`LaunchedEffect` + `ticker` 或订阅 `player.position`（通过 ViewModel 封装）

# 视频播放模块（SAF + Media3）

## 视频选择

* `VideoPickerScreen`

  * `rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument)`

  * `type = arrayOf("video/*")`，支持常见格式（MP4、MKV 等）

  * 拿到 `Uri` 后导航至 `videoPlayer/{encodedUri}`

## 视频播放

* `VideoPlayerScreen`

  * `ExoPlayer` 绑定 `StyledPlayerView`（`AndroidView`），设置 `playWhenReady`、`resizeMode`

  * 控制条：播放/暂停，进度滑块，时间显示

  * 全屏：

    * UI 层开关：切换系统栏可见性（`WindowCompat`）

    * 方向：允许横竖屏（`Activity` 配置），按钮触发 `requestedOrientation` 切换

  * 错误处理：`Player.Listener.onPlayerError` → Snackbar/Toast

# 权限与兼容性

* SAF 不需要存储权限；如改用 MediaStore 扫描：Android 13+ 使用 `READ_MEDIA_VIDEO/READ_MEDIA_AUDIO`

* 前台服务：声明 `FOREGROUND_SERVICE_MEDIA_PLAYBACK`；通知权限（Android 13+）用于媒体通知

* `minSdk=29` 已满足 SAF；`targetSdk=36` 与 Compose/Media3 兼容

# 主题与外观

* 保留现有 `Media3DemoTheme`

* 添加深色/浅色切换开关（临时保存在 `ViewModel`，后续可迁移到 `DataStore`）

* 视频全屏时隐藏系统 UI、保持内容沉浸

# 错误处理与健壮性

* 统一错误模型：`UiError(message, cause?)`

* 播放错误：在 `ViewModel` 暴露 `errorFlow`，界面用 `SnackbarHost`

* SAF 选择为空或不支持的 MIME：提示并回退

* 音乐源解析失败：回退到内置示例源或提示

# 单元测试（必要覆盖）

* `UampRepositoryTest`：JSON 解析为 `MediaItem` 列表

* `MusicViewModelTest`：

  * 队列加载逻辑（使用 `fake repository`）

  * 播放状态流的基本变迁（使用 `FakePlayer`/`TestCoroutineDispatcher`）

* `VideoViewModelTest`：`Uri` 加载与错误流

# 交付清单（文件与要点）

* 新增：

  * `music/service/AudioService.kt`

  * `music/data/UampRepository.kt` + `assets/uamp_catalog.json`

  * `music/MusicViewModel.kt`、`music/ui/MusicScreen.kt`

  * `video/VideoViewModel.kt`、`video/ui/VideoPickerScreen.kt`、`video/ui/VideoPlayerScreen.kt`

  * `navigation/NavGraph.kt`（`AnimatedNavHost`）

* 更新：

  * `MainActivity.kt`：接入导航与主题切换

  * `AndroidManifest.xml`：服务与权限

  * `libs.versions.toml`、`app/build.gradle.kts`：依赖添加

* 测试：`app/src/test/...` 下新增对应测试文件

# 实施步骤（按执行顺序）

1. 添加依赖与 `libs.versions.toml` 条目，同步 Gradle
2. 新增 `NavGraph` 与两个入口按钮，接入动画
3. 放置 `uamp_catalog.json`，实现 `UampRepository`
4. 编写 `AudioService`（`MediaSessionService`）并注册 Manifest
5. 编写 `MusicViewModel` 与 `MusicScreen`，验证队列播放与控制
6. 实现 `VideoPickerScreen` 与 `VideoPlayerScreen` 的播放器与全屏
7. 增加主题切换与错误展示
8. 编写并运行单元测试，修正问题

# 验证方式

* 启动应用：

  * 点击“音乐播放器”：看到队列、正常播放、通知显示、后台可控

  * 点击“视频播放器”：选择本地视频，进入播放器，全屏/横竖屏/控制正常

* 切换主题：开关生效

* 测试：单测通过，手动测试无崩溃

