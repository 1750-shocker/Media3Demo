## 问题分析
- 播放失败：`MusicViewModel.playSingle` 在 `controller == null` 时直接 return；当前从专辑详情直接调用播放，但 `NowPlayingScreen` 未建立 MediaController 连接。
- 时长为 00:00：远端流媒体的 duration 需要在准备后异步获得；且 common 的 `JsonSource` 将 `duration` 原样写入 extras，未做“秒→毫秒”转换，专辑详情的显示采用毫秒制，导致显示异常。
- 播放页样式：现有 `NowPlayingScreen` 为占位 UI，与目标设计相差较大（顶部标题、封面卡片、两端时间、底部控制栏等）。

## 改动计划
### 1. 建立控制器连接
- 在 `NowPlayingScreen` 的 `LaunchedEffect(Unit)` 中调用 `vm.connect()`，并打印 `wzhhh` 日志（进入播放页、连接完成）。
- 在 `AlbumDetailScreen` 点击歌曲前，若 `controller == null`，先调用 `vm.connect()` 并等待连接后再 `playSingle`（打印“controller null，尝试连接”日志）。
- 在 `MusicViewModel.playSingle` 中补充日志：`controller is null` 时打印并返回，便于定位。

### 2. 修正歌曲时长
- 在 `common` 的 `JsonSource`：将 `json.duration` 从秒转换为毫秒：`TimeUnit.SECONDS.toMillis(song.duration)`；extras 中写入毫秒制 `duration`。
- 在 `AlbumDetailViewModel`：读取 `extras["duration"]` 直接作为毫秒值；格式化函数按毫秒制显示。
- 在 `MusicViewModel` 的循环中保留对 `c.duration` 的采集，并在准备完成后显示正确时长。

### 3. 播放页样式还原
- 重做 `NowPlayingScreen`：
  - 顶部栏：返回箭头 + 标题“正在播放”+ 右上角菜单 icon；
  - 中部：圆角大封面；标题、艺术家；
  - 进度条：两端显示当前/总时长（毫秒格式化）；
  - 底部控制栏：随机、上一首、播放/暂停、下一首、分享/收藏 图标（使用 Material icons）；
  - 打印关键交互日志：play/pause/prev/next、seek。

### 4. 专辑详情样式与信息
- 列表项左侧显示序号（`trackNumber`），右侧显示时长（毫秒格式化）；中间标题/艺术家；
- 点击日志已存在，保留并补充“即将播放”日志；
- 若列表为空，显示占位文案并打印日志。

### 5. 可选增强
- 在 `AlbumsScreen` 用 Glide 加载封面（你已添加 Glide 到 common；如你同意，我在 app 模块也加 Glide 并实现 ImageView + Glide）。
- 在 Manifest 声明 `AlbumArtContentProvider` authority，以支持 content:// 映射的封面（若你希望展示 provider 映射封面而非 http URL）。

## 交付与验证
- 代码变更：`NowPlayingScreen`、`AlbumDetailScreen`、`MusicViewModel`、`common/JsonSource`。
- 验证路径：
  - 进入“音乐播放器”→ 专辑列表（日志 albums count）；
  - 点击专辑→ 专辑详情（日志 tracks count）；
  - 点击歌曲→ 进入播放页（连接日志、元数据刷新、时长显示、控件生效）。
- 如网络不稳定导致时长仍为 0，保留日志便于复核；支持本地 JSON 兜底可在后续增加。