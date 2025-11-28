## 问题与结论
- common 的 `AlbumArtContentProvider` 已把网络封面映射为 `content://com.example.android.uamp/...`（common/src/main/java/com/wzh/common/library/AlbumArtContentProvider.kt:26）。
- 但 app 的 Manifest 还未声明该 Provider，UI 也未实际加载封面；播放页没有从 `MediaController` 提取 `artworkUri`。

## 修改方案
### 1. Manifest 声明 Provider
- 在 `app/src/main/AndroidManifest.xml` 添加：
  - `<provider android:name="com.wzh.common.library.AlbumArtContentProvider" android:authorities="com.example.android.uamp" android:exported="false" />`
- 作用：允许 UI 通过 `ImageView.setImageURI(contentUri)` 访问 provider 缓存的封面文件。

### 2. 专辑列表封面显示（AlbumsScreen）
- 使用 `AndroidView(ImageView)` 加载 `album.iconUri`：
  - `imageView.setImageURI(iconUri)`；无需在 app 模块引入 Glide（Provider 已处理网络下载与缓存）。
- 保留 `wzhhh` 日志：进入画面、专辑数量、点击专辑。

### 3. 播放页封面显示（NowPlayingScreen）
- 扩展 `MusicViewModel`：
  - 增加 `artworkUri: StateFlow<Uri?>`，在监听循环 `setupListeners()` 中赋值 `c.mediaMetadata.artworkUri`。
- 在 `NowPlayingScreen` 的 `AndroidView(ImageView)` 中：
  - 若 `artworkUri != null` 则 `setImageURI(artworkUri)`；打印 `wzhhh` 日志（artworkUri）。

### 4. 细节对齐参考项目
- 你提供的 `MyUamp02` 项目使用旧框架，但封面来源同样是 JSON → Provider 映射；本项目沿用该思路，只在 UI 层通过 `content://` 加载即可。
- 如需改 authority 为应用包名，我可同步修改 `AlbumArtContentProvider.mapUri()` 与 Manifest 的 `authorities`。

### 5. 验证
- 打开“音乐播放器”→ 专辑列表应显示封面；日志显示 albums count。
- 点击专辑→ 歌曲列表出现；右侧时长非 00:00。
- 点击歌曲→ 播放页显示封面、标题、艺术家；进度与总时长更新。
- 上一首/下一首有效（队列来自整张专辑）。

## 可选增强
- 在 app 模块也加入 Glide，统一用 Glide 加载 `content://` 与 `http://`，提升过渡动画与错误占位；若你同意，我会加上。
- 播放页样式进一步对齐你截图（返回/更多图标、底部图标按钮、封面阴影与圆角）。

请确认以上方案，我将立即实现并提交改动。