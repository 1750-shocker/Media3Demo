## 数据来源与结构
- 使用 `common` 模块的 `JsonSource` 加载 JSON（网络 URL 或可访问的 Uri），并调用 `MusicSource.load()` 构建基础数据。
- 通过 `BrowseTree` 读取 `UAMP_ALBUMS_ROOT`、具体专辑 ID、`UAMP_RECOMMENDED_ROOT` 等，数据承载为 `MediaBrowser.MediaItem`，元数据从 `description.extras` 读取：`album`、`artist`、`albumArtUri`、`trackNumber`、`mediaUri` 等。
- 注意 `AlbumArtContentProvider` authority 一致性；如果需要，后续将 provider 在 `app` Manifest 中声明使内容 URI可用。

## ViewModel 层
- `AlbumsViewModel`：
  - 负责初始化 `JsonSource(Uri)` 与 `BrowseTree`，在 `load()` 完成后暴露：专辑列表（每个专辑的 `mediaId`、标题、封面 `iconUri`）。
  - 专辑节点来自 `BrowseTree[UAMP_ALBUMS_ROOT]`。
- `AlbumDetailViewModel`：
  - 输入：`albumId`（如 `"album:" + Uri.encode(albumName)`）。
  - 输出：该专辑下的歌曲列表（title、artist、duration、artwork、mediaUri、trackNumber）。
- `PlayerViewModel`：
  - 负责与 `MediaController` 建链（使用已存在的 `AudioService`），将所选歌曲转为 `androidx.media3.common.MediaItem` 加入队列并控制播放（play/pause/seek/next/prev）。
  - 从 `MediaBrowser.MediaItem.description.extras` 取 `mediaUri`、`albumArtUri` 构造 Media3 `MediaItem`。

## UI 层（Compose）
- `AlbumsScreen`：网格/列表展示专辑卡片（封面、标题、副标题），点击进入 `AlbumDetailScreen(albumId)`。
- `AlbumDetailScreen`：列表展示该专辑的曲目项（序号、标题、艺术家、时长），点击进入 `NowPlayingScreen(trackId)` 或直接触发播放并跳转。
- `NowPlayingScreen`：展示封面、标题、艺术家、进度条与播放控制；读取 `PlayerViewModel` 状态（position、duration、isPlaying）。
- Glide 加载封面：在 Compose 中用 `AndroidView` 包装 `ImageView` 或引入适配层显示 `albumArtUri`/`iconUri`。

## 导航与路由
- 在 `NavGraph` 中新增：
  - `Routes.Albums`（默认音乐入口）
  - `Routes.AlbumDetail/{albumId}`
  - `Routes.NowPlaying/{trackId}`（可选；也可仅通过控制器状态显示当前播放）
- 从 `AlbumsScreen` → `AlbumDetailScreen`：携带 `albumId`。
- 从 `AlbumDetailScreen` → `NowPlayingScreen`：携带 `trackId` 或直接开始播放并跳转。

## 播放集成
- 继续使用现有 `AudioService` 与 `MediaSession`，但将播放列表来源改为 `AlbumDetailViewModel` 产出的歌曲；将 `UampRepository` 过渡为使用 `common` 数据源或保留为备用。
- 播放时：根据选中歌曲的 `mediaUri` 构建 `Media3 MediaItem` 并 `setMediaItems`/`addMediaItem`，`prepare()` 后 `play()`。

## 辅助与校验
- 为 `JsonSource` 的 JSON 结构添加说明/示例，确保字段与 `JsonMusic` 匹配（id/title/album/artist/genre/source/image/trackNumber/totalTrackCount/duration）。
- 验证：
  - 在 `AlbumsViewModel` 加载后能得到非空专辑列表；
  - 点击专辑能获取该专辑子列表；
  - 点击歌曲能正常推送到 `AudioService` 播放并在通知/控制器观察到元数据更新。

## 交付内容（代码改动概要）
- 新增 ViewModel：`AlbumsViewModel`、`AlbumDetailViewModel`、`PlayerViewModel`（复用 `MusicViewModel` 的控制器管理逻辑或整合）。
- 新增屏幕：`AlbumsScreen`、`AlbumDetailScreen`、`NowPlayingScreen`。
- 更新导航：新增三条路由并从首页入口跳转到 `AlbumsScreen`。
- 接入 `common` 数据源：在 ViewModel 中使用 `JsonSource` + `BrowseTree`。

## 需要你确认的点
- 使用的 JSON 源地址/Uri（你在 common 写的数据源位置）；若为网络 URL，直接传 `Uri.parse(url)`；若为本地 assets，需要我提供一种 assets → `Uri` 的加载适配。
- `AlbumArtContentProvider` 的 authority 是否需要改为你的应用包名以便显示封面。
- 专辑/曲目 UI 风格是否与截图一致（卡片布局、暗色主题、圆角等），我可以按你的图示实现。
