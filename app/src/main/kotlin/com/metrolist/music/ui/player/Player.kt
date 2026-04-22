/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.player

import androidx.activity.compose.BackHandler
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.Player.STATE_ENDED
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.navigation.NavController
import androidx.palette.graphics.Palette
import coil3.compose.AsyncImage
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import com.metrolist.music.LocalDatabase
import com.metrolist.music.LocalDownloadUtil
import com.metrolist.music.LocalListenTogetherManager
import com.metrolist.music.LocalPlayerConnection
import com.metrolist.music.R
import com.metrolist.music.constants.CropAlbumArtKey
import com.metrolist.music.constants.DarkModeKey
import com.metrolist.music.constants.HidePlayerThumbnailKey
import com.metrolist.music.constants.HideStatusBarOnFullscreenKey
import com.metrolist.music.constants.KeepScreenOn
import com.metrolist.music.constants.PlayerBackgroundStyle
import com.metrolist.music.constants.PlayerBackgroundStyleKey
import com.metrolist.music.constants.PlayerButtonsStyle
import com.metrolist.music.constants.PlayerButtonsStyleKey
import com.metrolist.music.constants.PlayerHorizontalPadding
import com.metrolist.music.constants.QueuePeekHeight
import com.metrolist.music.constants.SleepTimerDefaultKey
import com.metrolist.music.constants.SleepTimerFadeOutKey
import com.metrolist.music.constants.SleepTimerStopAfterCurrentSongKey
import com.metrolist.music.constants.SliderStyle
import com.metrolist.music.constants.SliderStyleKey
import com.metrolist.music.constants.SquigglySliderKey
import com.metrolist.music.constants.ThumbnailCornerRadius
import com.metrolist.music.db.entities.LyricsEntity
import com.metrolist.music.extensions.metadata
import com.metrolist.music.extensions.togglePlayPause
import com.metrolist.music.extensions.toggleRepeatMode
import com.metrolist.music.listentogether.RoomRole
import com.metrolist.music.models.MediaMetadata
import com.metrolist.music.playback.ExoDownloadService
import com.metrolist.music.ui.component.BottomSheet
import com.metrolist.music.ui.component.BottomSheetState
import com.metrolist.music.ui.component.LocalBottomSheetPageState
import com.metrolist.music.ui.component.LocalMenuState
import com.metrolist.music.ui.component.Lyrics
import com.metrolist.music.ui.component.PlayerSliderTrack
import com.metrolist.music.ui.component.SquigglySlider
import com.metrolist.music.ui.component.WavySlider
import com.metrolist.music.ui.component.rememberBottomSheetState
import com.metrolist.music.ui.menu.PlayerMenu
import com.metrolist.music.ui.screens.settings.DarkMode
import com.metrolist.music.ui.theme.PlayerColorExtractor
import com.metrolist.music.ui.theme.PlayerSliderColors
import com.metrolist.music.ui.utils.ShowMediaInfo
import com.metrolist.music.ui.utils.ShowOffsetDialog
import com.metrolist.music.utils.dataStore
import com.metrolist.music.utils.makeTimeString
import com.metrolist.music.utils.rememberEnumPreference
import com.metrolist.music.utils.rememberPreference
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.roundToInt
import com.metrolist.music.ui.component.Icon as MIcon

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BottomSheetPlayer(
    state: BottomSheetState,
    navController: NavController,
    modifier: Modifier = Modifier,
    pureBlack: Boolean,
) {
    val context = LocalContext.current
    val database = LocalDatabase.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val menuState = LocalMenuState.current
    val sleepTimerDefaultSetTemplate = stringResource(R.string.sleep_timer_default_set)
    val copiedTitleStr = stringResource(R.string.copied_title)
    val copiedArtistStr = stringResource(R.string.copied_artist)
    val bottomSheetPageState = LocalBottomSheetPageState.current
    val playerConnection = LocalPlayerConnection.current ?: return

    val (hidePlayerThumbnail) = rememberPreference(HidePlayerThumbnailKey, false)
    val (hideStatusBarOnFullscreen) = rememberPreference(HideStatusBarOnFullscreenKey, false)
    val cropAlbumArt by rememberPreference(CropAlbumArtKey, false)

    var showInlineLyrics by rememberSaveable { mutableStateOf(false) }
    var isFullScreen by rememberSaveable { mutableStateOf(false) }

    val playerBackground by rememberEnumPreference(
        key = PlayerBackgroundStyleKey,
        defaultValue = PlayerBackgroundStyle.DEFAULT,
    )
    val playerButtonsStyle by rememberEnumPreference(
        key = PlayerButtonsStyleKey,
        defaultValue = PlayerButtonsStyle.DEFAULT,
    )

    val isSystemInDarkTheme = isSystemInDarkTheme()
    val darkTheme by rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
    val useDarkTheme = remember(darkTheme, isSystemInDarkTheme) {
        if (darkTheme == DarkMode.AUTO) isSystemInDarkTheme else darkTheme == DarkMode.ON
    }

    val isPlaying by playerConnection.isPlaying.collectAsStateWithLifecycle()
    val isKeepScreenOn by rememberPreference(KeepScreenOn, false)
    val keepScreenOn = isPlaying && isKeepScreenOn

    DisposableEffect(playerBackground, state.isExpanded, useDarkTheme, keepScreenOn, isFullScreen, hideStatusBarOnFullscreen) {
        val window = (context as? android.app.Activity)?.window
        if (window != null && state.isExpanded) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            when (playerBackground) {
                PlayerBackgroundStyle.BLUR, PlayerBackgroundStyle.GRADIENT ->
                    insetsController.isAppearanceLightStatusBars = false
                PlayerBackgroundStyle.DEFAULT ->
                    insetsController.isAppearanceLightStatusBars = !useDarkTheme
            }
            if (isFullScreen && hideStatusBarOnFullscreen) {
                insetsController.hide(WindowInsetsCompat.Type.statusBars())
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                insetsController.show(WindowInsetsCompat.Type.statusBars())
            }
            if (keepScreenOn && state.isExpanded) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
        onDispose {
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.isAppearanceLightStatusBars = !useDarkTheme
                insetsController.show(WindowInsetsCompat.Type.statusBars())
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    BackHandler(enabled = state.isExpanded) {
        state.collapseSoft()
    }

    val useBlackBackground = remember(isSystemInDarkTheme, darkTheme, pureBlack) {
        val dark = if (darkTheme == DarkMode.AUTO) isSystemInDarkTheme else darkTheme == DarkMode.ON
        dark && pureBlack
    }

    val onBackgroundColor = when (playerBackground) {
        PlayerBackgroundStyle.DEFAULT -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.onSurface
    }

    val playbackState by playerConnection.playbackState.collectAsStateWithLifecycle()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsStateWithLifecycle()
    val currentSong by playerConnection.currentSong.collectAsStateWithLifecycle(initialValue = null)
    val automix by playerConnection.service.automixItems.collectAsStateWithLifecycle()
    val repeatMode by playerConnection.repeatMode.collectAsStateWithLifecycle()
    val canSkipPrevious by playerConnection.canSkipPrevious.collectAsStateWithLifecycle()
    val canSkipNext by playerConnection.canSkipNext.collectAsStateWithLifecycle()
    val isMuted by playerConnection.isMuted.collectAsStateWithLifecycle()
    val shuffleModeEnabled by playerConnection.shuffleModeEnabled.collectAsStateWithLifecycle()

    val sliderStyle by rememberEnumPreference(SliderStyleKey, SliderStyle.DEFAULT)
    val squigglySlider by rememberPreference(SquigglySliderKey, defaultValue = false)

    val listenTogetherManager = LocalListenTogetherManager.current
    val listenTogetherRoleState = listenTogetherManager?.role?.collectAsStateWithLifecycle(initialValue = RoomRole.NONE)
    val isListenTogetherGuest = listenTogetherRoleState?.value == RoomRole.GUEST

    val castHandler = remember(playerConnection) {
        try { playerConnection.service.castConnectionHandler } catch (e: Exception) { null }
    }
    val isCasting by castHandler?.isCasting?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(false) }
    val castPosition by castHandler?.castPosition?.collectAsStateWithLifecycle() ?: remember { mutableLongStateOf(0L) }
    val castDuration by castHandler?.castDuration?.collectAsStateWithLifecycle() ?: remember { mutableLongStateOf(0L) }
    val castIsPlaying by castHandler?.castIsPlaying?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(false) }

    val effectiveIsPlaying = if (isCasting) castIsPlaying else isPlaying

    val positionState = remember { mutableLongStateOf(0L) }
    val durationState = remember { mutableLongStateOf(0L) }
    var position by positionState
    var duration by durationState

    val effectivePosition by remember {
        derivedStateOf { if (isCasting) castPosition else position }
    }

    var sliderPosition by remember { mutableStateOf<Long?>(null) }
    var lastManualSeekTime by remember { mutableLongStateOf(0L) }

    var gradientColors by remember { mutableStateOf<List<Color>>(emptyList()) }
    val gradientColorsCache = remember { mutableMapOf<String, List<Color>>() }

    if (!canSkipNext && automix.isNotEmpty()) {
        playerConnection.service.addToQueueAutomix(automix[0], 0)
    }

    val fallbackColor = MaterialTheme.colorScheme.surface.toArgb()

    LaunchedEffect(mediaMetadata?.id, playerBackground) {
        if (playerBackground == PlayerBackgroundStyle.GRADIENT) {
            val currentMetadata = mediaMetadata
            if (currentMetadata != null && currentMetadata.thumbnailUrl != null) {
                gradientColorsCache[currentMetadata.id]?.let {
                    gradientColors = it
                    return@LaunchedEffect
                }
                withContext(Dispatchers.IO) {
                    val request = ImageRequest.Builder(context)
                        .data(currentMetadata.thumbnailUrl)
                        .size(100, 100)
                        .allowHardware(false)
                        .memoryCacheKey("gradient_${currentMetadata.id}")
                        .build()
                    val result = runCatching { context.imageLoader.execute(request) }.getOrNull()
                    if (result != null) {
                        val bitmap = result.image?.toBitmap()
                        if (bitmap != null) {
                            val palette = withContext(Dispatchers.Default) {
                                Palette.from(bitmap).maximumColorCount(8).resizeBitmapArea(100 * 100).generate()
                            }
                            val extracted = PlayerColorExtractor.extractGradientColors(
                                palette = palette,
                                fallbackColor = fallbackColor,
                            )
                            gradientColorsCache[currentMetadata.id] = extracted
                            withContext(Dispatchers.Main) { gradientColors = extracted }
                        }
                    }
                }
            }
        } else {
            gradientColors = emptyList()
        }
    }

    val TextBackgroundColor by animateColorAsState(
        targetValue = when (playerBackground) {
            PlayerBackgroundStyle.DEFAULT -> MaterialTheme.colorScheme.onBackground
            PlayerBackgroundStyle.BLUR -> Color.White
            PlayerBackgroundStyle.GRADIENT -> Color.White
        },
        label = "TextBackgroundColor",
    )

    val icBackgroundColor by animateColorAsState(
        targetValue = when (playerBackground) {
            PlayerBackgroundStyle.DEFAULT -> MaterialTheme.colorScheme.surface
            PlayerBackgroundStyle.BLUR -> Color.Black
            PlayerBackgroundStyle.GRADIENT -> Color.Black
        },
        label = "icBackgroundColor",
    )

    // V4-style button colours: filled surface card approach
    val (textButtonColor, iconButtonColor) = when {
        playerBackground == PlayerBackgroundStyle.BLUR || playerBackground == PlayerBackgroundStyle.GRADIENT -> {
            when (playerButtonsStyle) {
                PlayerButtonsStyle.DEFAULT -> Pair(Color.White, Color.Black)
                PlayerButtonsStyle.PRIMARY -> Pair(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.onPrimary,
                )
                PlayerButtonsStyle.TERTIARY -> Pair(
                    MaterialTheme.colorScheme.tertiary,
                    MaterialTheme.colorScheme.onTertiary,
                )
            }
        }
        else -> {
            when (playerButtonsStyle) {
                PlayerButtonsStyle.DEFAULT -> if (useDarkTheme) Pair(Color.White, Color.Black) else Pair(Color.Black, Color.White)
                PlayerButtonsStyle.PRIMARY -> Pair(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.onPrimary,
                )
                PlayerButtonsStyle.TERTIARY -> Pair(
                    MaterialTheme.colorScheme.tertiary,
                    MaterialTheme.colorScheme.onTertiary,
                )
            }
        }
    }

    val download by LocalDownloadUtil.current
        .getDownload(mediaMetadata?.id ?: "")
        .collectAsStateWithLifecycle(initialValue = null)

    val sleepTimerEnabled = remember(
        playerConnection.service.sleepTimer.triggerTime,
        playerConnection.service.sleepTimer.pauseWhenSongEnd,
    ) { playerConnection.service.sleepTimer.isActive }

    var sleepTimerTimeLeft by remember { mutableLongStateOf(0L) }
    LaunchedEffect(sleepTimerEnabled) {
        if (sleepTimerEnabled) {
            while (isActive) {
                sleepTimerTimeLeft = if (playerConnection.service.sleepTimer.pauseWhenSongEnd) {
                    playerConnection.player.duration - playerConnection.player.currentPosition
                } else {
                    playerConnection.service.sleepTimer.triggerTime - System.currentTimeMillis()
                }
                delay(1000L)
            }
        }
    }

    val scope = rememberCoroutineScope()
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    val sleepTimerDefault by rememberPreference(SleepTimerDefaultKey, 30f)
    var sleepTimerValue by remember { mutableFloatStateOf(sleepTimerDefault) }
    val isAtDefault by remember { derivedStateOf { sleepTimerValue.roundToInt() == sleepTimerDefault.roundToInt() } }
    val sleepTimerStopAfterCurrentSong by rememberPreference(SleepTimerStopAfterCurrentSongKey, false)
    val sleepTimerFadeOut by rememberPreference(SleepTimerFadeOutKey, false)

    if (showSleepTimerDialog) {
        AlertDialog(
            properties = DialogProperties(usePlatformDefaultWidth = false),
            onDismissRequest = { showSleepTimerDialog = false },
            icon = {
                Icon(painter = painterResource(R.drawable.bedtime), contentDescription = null)
            },
            title = { Text(stringResource(R.string.sleep_timer)) },
            confirmButton = {
                TextButton(onClick = {
                    showSleepTimerDialog = false
                    playerConnection.service.sleepTimer.start(
                        minute = sleepTimerValue.roundToInt(),
                        stopAfterCurrentSong = sleepTimerStopAfterCurrentSong,
                        fadeOut = sleepTimerFadeOut,
                    )
                }) { Text(stringResource(android.R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showSleepTimerDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.minute,
                            sleepTimerValue.roundToInt(),
                            sleepTimerValue.roundToInt(),
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Slider(
                        value = sleepTimerValue,
                        onValueChange = { sleepTimerValue = it },
                        valueRange = 5f..120f,
                        steps = (120 - 5) / 5 - 1,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    context.dataStore.edit { it[SleepTimerDefaultKey] = sleepTimerValue }
                                }
                                Toast.makeText(
                                    context,
                                    String.format(sleepTimerDefaultSetTemplate, sleepTimerValue.roundToInt()),
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        ) { Text(stringResource(R.string.set_as_default)) }

                        TextButton(onClick = {
                            showSleepTimerDialog = false
                            playerConnection.service.sleepTimer.start(minute = -1)
                        }) { Text(stringResource(R.string.end_of_song)) }
                    }
                }
            },
        )
    }

    // Position update loop
    LaunchedEffect(isPlaying, isCasting) {
        if (!isCasting && isPlaying) {
            while (isActive) {
                delay(100)
                if (sliderPosition == null) {
                    position = playerConnection.player.currentPosition
                    duration = playerConnection.player.duration
                }
            }
        }
    }

    LaunchedEffect(playbackState, mediaMetadata?.id) {
        if (!isCasting) {
            position = playerConnection.player.currentPosition
            duration = playerConnection.player.duration
        }
    }

    LaunchedEffect(isCasting, castPosition, castDuration) {
        if (isCasting && sliderPosition == null) {
            val timeSinceManualSeek = System.currentTimeMillis() - lastManualSeekTime
            if (timeSinceManualSeek > 1500) {
                position = castPosition
                if (castDuration > 0) duration = castDuration
            }
        }
    }

    val dismissedBound = QueuePeekHeight + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
    val queueSheetState = rememberBottomSheetState(
        dismissedBound = dismissedBound,
        expandedBound = state.expandedBound,
        collapsedBound = dismissedBound + 1.dp,
        initialAnchor = 1,
    )

    val bottomSheetBackgroundColor = when (playerBackground) {
        PlayerBackgroundStyle.BLUR, PlayerBackgroundStyle.GRADIENT ->
            MaterialTheme.colorScheme.surfaceContainer
        else ->
            if (useBlackBackground) Color.Black else MaterialTheme.colorScheme.surfaceContainer
    }

    val backgroundAlpha = state.progress.coerceIn(0f, 1f)

    BottomSheet(
        state = state,
        modifier = modifier,
        background = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bottomSheetBackgroundColor),
            ) {
                when (playerBackground) {
                    PlayerBackgroundStyle.BLUR -> {
                        AnimatedContent(
                            targetState = mediaMetadata?.thumbnailUrl,
                            transitionSpec = { fadeIn(tween(800)).togetherWith(fadeOut(tween(800))) },
                            label = "blurBackground",
                        ) { thumbnailUrl ->
                            if (thumbnailUrl != null) {
                                Box(modifier = Modifier.alpha(backgroundAlpha)) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(thumbnailUrl)
                                            .size(100, 100)
                                            .allowHardware(false)
                                            .build(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .blur(if (useDarkTheme) 150.dp else 100.dp),
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.3f)),
                                    )
                                }
                            }
                        }
                    }
                    PlayerBackgroundStyle.GRADIENT -> {
                        AnimatedContent(
                            targetState = gradientColors,
                            transitionSpec = { fadeIn(tween(800)).togetherWith(fadeOut(tween(800))) },
                            label = "gradientBackground",
                        ) { colors ->
                            if (colors.isNotEmpty()) {
                                val colorStops = if (colors.size >= 3) {
                                    arrayOf(0.0f to colors[0], 0.5f to colors[1], 1.0f to colors[2])
                                } else {
                                    arrayOf(0.0f to colors[0], 0.6f to colors[0].copy(alpha = 0.7f), 1.0f to Color.Black)
                                }
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .alpha(backgroundAlpha)
                                        .background(Brush.verticalGradient(colorStops = colorStops))
                                        .background(Color.Black.copy(alpha = 0.2f)),
                                )
                            }
                        }
                    }
                    else -> {}
                }
            }
        },
        onDismiss = if (!isListenTogetherGuest) {
            {
                playerConnection.service.clearAutomix()
                playerConnection.player.stop()
                playerConnection.player.clearMediaItems()
            }
        } else null,
        collapsedContent = {
            MiniPlayer(positionState = positionState, durationState = durationState)
        },
    ) {
        val controlsContent: @Composable ColumnScope.(MediaMetadata) -> Unit = { mediaMetadata ->
            // ── Title + artist row with download & favourite ──────────────────
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PlayerHorizontalPadding),
            ) {
                // Thumbnail shown only when inline lyrics are active
                AnimatedContent(targetState = showInlineLyrics, label = "ThumbnailInline") { showLyrics ->
                    if (showLyrics) {
                        Row {
                            if (hidePlayerThumbnail) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(ThumbnailCornerRadius))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.small_icon),
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        tint = textButtonColor.copy(alpha = 0.7f),
                                    )
                                }
                            } else {
                                AsyncImage(
                                    model = mediaMetadata.thumbnailUrl,
                                    contentDescription = null,
                                    contentScale = if (cropAlbumArt) ContentScale.Crop else ContentScale.Fit,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(ThumbnailCornerRadius)),
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                    } else {
                        Spacer(modifier = Modifier.width(0.dp))
                    }
                }

                // Title + artists
                Column(modifier = Modifier.weight(1f)) {
                    AnimatedContent(
                        targetState = mediaMetadata.title,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "Title",
                    ) { title ->
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = TextBackgroundColor,
                            modifier = Modifier
                                .basicMarquee(iterations = 1, initialDelayMillis = 3000, velocity = 30.dp)
                                .combinedClickable(
                                    enabled = true,
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = {
                                        val albumId = mediaMetadata.album?.id
                                            ?: currentSong?.album?.id
                                            ?: currentSong?.song?.albumId
                                        if (albumId != null) {
                                            navController.navigate("album/$albumId")
                                            state.collapseSoft()
                                        }
                                    },
                                    onLongClick = {
                                        val clip = ClipData.newPlainText(copiedTitleStr, title)
                                        clipboardManager.setPrimaryClip(clip)
                                        Toast.makeText(context, copiedTitleStr, Toast.LENGTH_SHORT).show()
                                    },
                                ),
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (mediaMetadata.explicit) MIcon.Explicit()

                        if (mediaMetadata.artists.any { it.name.isNotBlank() }) {
                            val annotatedString = buildAnnotatedString {
                                mediaMetadata.artists.forEachIndexed { index, artist ->
                                    val tag = "artist_${artist.id.orEmpty()}"
                                    pushStringAnnotation(tag = tag, annotation = artist.id.orEmpty())
                                    withStyle(SpanStyle(color = TextBackgroundColor, fontSize = 16.sp)) {
                                        append(artist.name)
                                    }
                                    pop()
                                    if (index != mediaMetadata.artists.lastIndex) append(", ")
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .basicMarquee(iterations = 1, initialDelayMillis = 3000, velocity = 30.dp)
                                    .padding(end = 12.dp),
                            ) {
                                var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
                                var clickOffset by remember { mutableStateOf<Offset?>(null) }
                                Text(
                                    text = annotatedString,
                                    style = MaterialTheme.typography.titleMedium.copy(color = TextBackgroundColor),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    onTextLayout = { layoutResult = it },
                                    modifier = Modifier
                                        .pointerInput(Unit) {
                                            awaitPointerEventScope {
                                                while (true) {
                                                    val event = awaitPointerEvent()
                                                    val tapPosition = event.changes.firstOrNull()?.position
                                                    if (tapPosition != null) clickOffset = tapPosition
                                                }
                                            }
                                        }
                                        .combinedClickable(
                                            enabled = true,
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() },
                                            onClick = {
                                                val tapPosition = clickOffset
                                                val layout = layoutResult
                                                if (tapPosition != null && layout != null) {
                                                    val offset = layout.getOffsetForPosition(tapPosition)
                                                    annotatedString.getStringAnnotations(offset, offset)
                                                        .firstOrNull()
                                                        ?.let { ann ->
                                                            if (ann.item.isNotBlank()) {
                                                                navController.navigate("artist/${ann.item}")
                                                                state.collapseSoft()
                                                            }
                                                        }
                                                }
                                            },
                                            onLongClick = {
                                                val clip = ClipData.newPlainText(copiedArtistStr, annotatedString)
                                                clipboardManager.setPrimaryClip(clip)
                                                Toast.makeText(context, copiedArtistStr, Toast.LENGTH_SHORT).show()
                                            },
                                        ),
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Download + Favourite — always two buttons, V4 glass-card style
                AnimatedContent(targetState = showInlineLyrics, label = "ActionButtons") { showLyrics ->
                    if (showLyrics) {
                        // In lyrics mode: fullscreen toggle + lyrics menu
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Surface(
                                onClick = { isFullScreen = !isFullScreen },
                                shape = RoundedCornerShape(14.dp),
                                color = textButtonColor.copy(alpha = 0.12f),
                                modifier = Modifier.size(44.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        painter = painterResource(R.drawable.fullscreen),
                                        contentDescription = null,
                                        tint = textButtonColor,
                                        modifier = Modifier.size(22.dp),
                                    )
                                }
                            }

                            val currentLyrics by playerConnection.currentLyrics.collectAsStateWithLifecycle(initialValue = null)
                            Surface(
                                onClick = {
                                    menuState.show {
                                        com.metrolist.music.ui.menu.LyricsMenu(
                                            lyricsProvider = { currentLyrics },
                                            songProvider = { currentSong?.song },
                                            mediaMetadataProvider = { mediaMetadata },
                                            onDismiss = menuState::dismiss,
                                            onShowOffsetDialog = {
                                                bottomSheetPageState.show {
                                                    ShowOffsetDialog(songProvider = { currentSong?.song })
                                                }
                                            },
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                color = textButtonColor.copy(alpha = 0.12f),
                                modifier = Modifier.size(44.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        painter = painterResource(R.drawable.more_horiz),
                                        contentDescription = null,
                                        tint = textButtonColor,
                                        modifier = Modifier.size(22.dp),
                                    )
                                }
                            }
                        }
                    } else {
                        // Normal mode: download + favourite
                        val isEpisode = currentSong?.song?.isEpisode == true
                        val isFavorite = if (isEpisode) currentSong?.song?.inLibrary != null else currentSong?.song?.liked == true

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Download button
                            Surface(
                                onClick = {
                                    mediaMetadata?.let { meta ->
                                        when (download?.state) {
                                            Download.STATE_COMPLETED,
                                            Download.STATE_QUEUED,
                                            Download.STATE_DOWNLOADING -> {
                                                DownloadService.sendRemoveDownload(
                                                    context,
                                                    ExoDownloadService::class.java,
                                                    meta.id,
                                                    false,
                                                )
                                            }
                                            else -> {
                                                database.transaction { insert(meta) }
                                                val downloadRequest = DownloadRequest.Builder(meta.id, meta.id.toUri())
                                                    .setCustomCacheKey(meta.id)
                                                    .setData(meta.title.toByteArray())
                                                    .build()
                                                DownloadService.sendAddDownload(
                                                    context,
                                                    ExoDownloadService::class.java,
                                                    downloadRequest,
                                                    false,
                                                )
                                            }
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                color = textButtonColor.copy(alpha = 0.12f),
                                modifier = Modifier.size(44.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    when (download?.state) {
                                        Download.STATE_COMPLETED -> Icon(
                                            painter = painterResource(R.drawable.offline),
                                            contentDescription = null,
                                            tint = textButtonColor,
                                            modifier = Modifier.size(22.dp),
                                        )
                                        Download.STATE_QUEUED,
                                        Download.STATE_DOWNLOADING -> CircularProgressIndicator(
                                            modifier = Modifier.size(22.dp),
                                            strokeWidth = 2.dp,
                                            color = textButtonColor,
                                        )
                                        else -> Icon(
                                            painter = painterResource(R.drawable.download),
                                            contentDescription = null,
                                            tint = textButtonColor,
                                            modifier = Modifier.size(22.dp),
                                        )
                                    }
                                }
                            }

                            // Favourite button
                            Surface(
                                onClick = playerConnection::toggleLike,
                                shape = RoundedCornerShape(14.dp),
                                color = if (isFavorite)
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.25f)
                                else
                                    textButtonColor.copy(alpha = 0.12f),
                                modifier = Modifier.size(44.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        painter = painterResource(
                                            if (isFavorite) R.drawable.favorite else R.drawable.favorite_border
                                        ),
                                        contentDescription = null,
                                        tint = if (isFavorite) MaterialTheme.colorScheme.error else textButtonColor,
                                        modifier = Modifier.size(22.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Slider ────────────────────────────────────────────────────────
            when (sliderStyle) {
                SliderStyle.DEFAULT -> Slider(
                    value = (sliderPosition ?: effectivePosition).toFloat(),
                    valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                    onValueChange = { if (!isListenTogetherGuest) sliderPosition = it.toLong() },
                    onValueChangeFinished = {
                        if (!isListenTogetherGuest) {
                            sliderPosition?.let {
                                if (isCasting) {
                                    castHandler?.seekTo(it)
                                    lastManualSeekTime = System.currentTimeMillis()
                                } else {
                                    playerConnection.player.seekTo(it)
                                }
                                position = it
                            }
                            sliderPosition = null
                        }
                    },
                    enabled = !isListenTogetherGuest,
                    colors = PlayerSliderColors.getSliderColors(textButtonColor, playerBackground, useDarkTheme),
                    modifier = Modifier.padding(horizontal = PlayerHorizontalPadding),
                )
                SliderStyle.WAVY -> {
                    if (squigglySlider) {
                        SquigglySlider(
                            value = (sliderPosition ?: effectivePosition).toFloat(),
                            valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                            onValueChange = { sliderPosition = it.toLong() },
                            onValueChangeFinished = {
                                sliderPosition?.let {
                                    if (isCasting) {
                                        castHandler?.seekTo(it)
                                        lastManualSeekTime = System.currentTimeMillis()
                                    } else {
                                        playerConnection.player.seekTo(it)
                                    }
                                    position = it
                                }
                                sliderPosition = null
                            },
                            modifier = Modifier.padding(horizontal = PlayerHorizontalPadding),
                            colors = PlayerSliderColors.getSliderColors(textButtonColor, playerBackground, useDarkTheme),
                            isPlaying = effectiveIsPlaying,
                        )
                    } else {
                        WavySlider(
                            value = (sliderPosition ?: effectivePosition).toFloat(),
                            valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                            onValueChange = { sliderPosition = it.toLong() },
                            onValueChangeFinished = {
                                sliderPosition?.let {
                                    if (isCasting) {
                                        castHandler?.seekTo(it)
                                        lastManualSeekTime = System.currentTimeMillis()
                                    } else {
                                        playerConnection.player.seekTo(it)
                                    }
                                    position = it
                                }
                                sliderPosition = null
                            },
                            colors = PlayerSliderColors.getSliderColors(textButtonColor, playerBackground, useDarkTheme),
                            modifier = Modifier.padding(horizontal = PlayerHorizontalPadding),
                            isPlaying = effectiveIsPlaying,
                        )
                    }
                }
                SliderStyle.SLIM -> Slider(
                    value = (sliderPosition ?: effectivePosition).toFloat(),
                    valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                    onValueChange = { if (!isListenTogetherGuest) sliderPosition = it.toLong() },
                    onValueChangeFinished = {
                        if (!isListenTogetherGuest) {
                            sliderPosition?.let {
                                if (isCasting) {
                                    castHandler?.seekTo(it)
                                    lastManualSeekTime = System.currentTimeMillis()
                                } else {
                                    playerConnection.player.seekTo(it)
                                }
                                position = it
                            }
                            sliderPosition = null
                        }
                    },
                    enabled = !isListenTogetherGuest,
                    thumb = { Spacer(modifier = Modifier.size(0.dp)) },
                    track = { sliderState ->
                        PlayerSliderTrack(
                            sliderState = sliderState,
                            colors = PlayerSliderColors.getSliderColors(textButtonColor, playerBackground, useDarkTheme),
                        )
                    },
                    modifier = Modifier.padding(horizontal = PlayerHorizontalPadding),
                )
            }

            Spacer(Modifier.height(4.dp))

            // ── Time labels ───────────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PlayerHorizontalPadding + 4.dp),
            ) {
                Text(
                    text = makeTimeString(sliderPosition ?: effectivePosition),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextBackgroundColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (duration != C.TIME_UNSET) makeTimeString(duration) else "",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextBackgroundColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Playback controls (V4 layout) ─────────────────────────────────
            AnimatedVisibility(
                visible = !isFullScreen,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Top) + slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PlayerHorizontalPadding),
                ) {
                    val baseLarge = 56.dp
                    val baseSmall = 46.dp
                    val baseGap = 12.dp
                    val baseLargeIcon = 28.dp
                    val baseSmallIcon = 22.dp
                    val baseLargeRadius = 18.dp
                    val baseSmallRadius = 16.dp
                    val centerSize = 88.dp
                    val centerPadding = 40.dp
                    val sideTotal = (maxWidth - centerSize - centerPadding) / 2f
                    val scale = ((sideTotal - baseGap) / (baseLarge + baseSmall))
                        .coerceAtMost(1f).coerceAtLeast(0.6f)
                    val large = baseLarge * scale
                    val small = baseSmall * scale
                    val gap = baseGap * scale
                    val largeIcon = baseLargeIcon * scale
                    val smallIcon = baseSmallIcon * scale
                    val largeRadius = baseLargeRadius * scale
                    val smallRadius = baseSmallRadius * scale

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Left side: shuffle + previous
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Surface(
                                onClick = {
                                    if (!isListenTogetherGuest)
                                        playerConnection.player.shuffleModeEnabled = !shuffleModeEnabled
                                },
                                shape = RoundedCornerShape(smallRadius),
                                color = TextBackgroundColor.copy(alpha = if (shuffleModeEnabled) 0.2f else 0.08f),
                                modifier = Modifier
                                    .size(small)
                                    .alpha(if (isListenTogetherGuest) 0.5f else 1f),
                            ) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(
                                        painter = painterResource(R.drawable.shuffle),
                                        contentDescription = null,
                                        tint = TextBackgroundColor.copy(alpha = if (shuffleModeEnabled) 1f else 0.6f),
                                        modifier = Modifier.size(smallIcon),
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(gap))

                            Surface(
                                onClick = playerConnection::seekToPrevious,
                                enabled = canSkipPrevious && !isListenTogetherGuest,
                                shape = RoundedCornerShape(largeRadius),
                                color = TextBackgroundColor.copy(alpha = 0.15f),
                                modifier = Modifier.size(large),
                            ) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(
                                        painter = painterResource(R.drawable.skip_previous),
                                        contentDescription = null,
                                        tint = TextBackgroundColor.copy(alpha = if (canSkipPrevious && !isListenTogetherGuest) 1f else 0.4f),
                                        modifier = Modifier.size(largeIcon),
                                    )
                                }
                            }
                        }

                        // Centre: play/pause
                        Surface(
                            onClick = {
                                if (isListenTogetherGuest) {
                                    playerConnection.toggleMute()
                                    return@Surface
                                }
                                if (isCasting) {
                                    if (castIsPlaying) castHandler?.pause() else castHandler?.play()
                                } else if (playbackState == STATE_ENDED) {
                                    playerConnection.player.seekTo(0, 0)
                                    playerConnection.player.playWhenReady = true
                                } else {
                                    playerConnection.togglePlayPause()
                                }
                            },
                            shape = RoundedCornerShape(28.dp),
                            color = textButtonColor,
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                                .size(centerSize),
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(
                                        if (isListenTogetherGuest) {
                                            if (isMuted) R.drawable.volume_off else R.drawable.volume_up
                                        } else {
                                            when {
                                                playbackState == STATE_ENDED -> R.drawable.replay
                                                effectiveIsPlaying -> R.drawable.pause
                                                else -> R.drawable.play
                                            }
                                        }
                                    ),
                                    contentDescription = null,
                                    tint = iconButtonColor,
                                    modifier = Modifier.size(44.dp),
                                )
                            }
                        }

                        // Right side: next + repeat
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Surface(
                                onClick = playerConnection::seekToNext,
                                enabled = canSkipNext && !isListenTogetherGuest,
                                shape = RoundedCornerShape(largeRadius),
                                color = TextBackgroundColor.copy(alpha = 0.15f),
                                modifier = Modifier.size(large),
                            ) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(
                                        painter = painterResource(R.drawable.skip_next),
                                        contentDescription = null,
                                        tint = TextBackgroundColor.copy(alpha = if (canSkipNext && !isListenTogetherGuest) 1f else 0.4f),
                                        modifier = Modifier.size(largeIcon),
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(gap))

                            Surface(
                                onClick = {
                                    if (!isListenTogetherGuest)
                                        playerConnection.player.toggleRepeatMode()
                                },
                                shape = RoundedCornerShape(smallRadius),
                                color = TextBackgroundColor.copy(
                                    alpha = if (repeatMode != Player.REPEAT_MODE_OFF) 0.2f else 0.08f
                                ),
                                modifier = Modifier
                                    .size(small)
                                    .alpha(if (isListenTogetherGuest) 0.5f else 1f),
                            ) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(
                                        painter = painterResource(
                                            when (repeatMode) {
                                                Player.REPEAT_MODE_ONE -> R.drawable.repeat_one
                                                else -> R.drawable.repeat
                                            }
                                        ),
                                        contentDescription = null,
                                        tint = TextBackgroundColor.copy(
                                            alpha = if (repeatMode == Player.REPEAT_MODE_OFF) 0.6f else 1f
                                        ),
                                        modifier = Modifier.size(smallIcon),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Layout: landscape vs portrait ─────────────────────────────────────
        when (LocalConfiguration.current.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                val density = LocalDensity.current
                val verticalPadding = max(
                    WindowInsets.systemBars.getTop(density),
                    WindowInsets.systemBars.getBottom(density),
                )
                val verticalPaddingDp = with(density) { verticalPadding.toDp() }
                val verticalWindowInsets = WindowInsets(
                    left = 0.dp, top = verticalPaddingDp, right = 0.dp, bottom = verticalPaddingDp,
                )

                Row(
                    modifier = Modifier
                        .windowInsetsPadding(
                            WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)
                                .add(verticalWindowInsets)
                        )
                        .padding(bottom = 24.dp)
                        .fillMaxSize(),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .nestedScroll(state.preUpPostDownNestedScrollConnection),
                    ) {
                        val currentSliderPosition by rememberUpdatedState(sliderPosition)
                        val sliderPositionProvider = remember { { currentSliderPosition } }
                        val isExpandedProvider = remember(state) { { state.isExpanded } }
                        AnimatedContent(
                            targetState = showInlineLyrics,
                            label = "Lyrics",
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                        ) { showLyrics ->
                            if (showLyrics) {
                                InlineLyricsView(
                                    mediaMetadata = mediaMetadata,
                                    showLyrics = showLyrics,
                                    positionProvider = { effectivePosition },
                                )
                            } else {
                                Thumbnail(
                                    sliderPositionProvider = sliderPositionProvider,
                                    modifier = Modifier.animateContentSize(),
                                    isPlayerExpanded = isExpandedProvider,
                                    isLandscape = true,
                                    isListenTogetherGuest = isListenTogetherGuest,
                                )
                            }
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(if (showInlineLyrics) 0.65f else 1f, false)
                            .animateContentSize()
                            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top)),
                    ) {
                        Spacer(Modifier.weight(1f))
                        mediaMetadata?.let { controlsContent(it) }
                        Spacer(Modifier.weight(1f))
                    }
                }
            }

            else -> {
                val bottomPadding by animateDpAsState(
                    targetValue = if (isFullScreen) 0.dp else queueSheetState.collapsedBound,
                    label = "bottomPadding",
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
                        .padding(bottom = bottomPadding)
                        .animateContentSize(),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.weight(1f),
                    ) {
                        val currentSliderPosition by rememberUpdatedState(sliderPosition)
                        val sliderPositionProvider = remember { { currentSliderPosition } }
                        val isExpandedProvider = remember(state) { { state.isExpanded } }
                        AnimatedContent(
                            targetState = showInlineLyrics,
                            label = "Lyrics",
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                        ) { showLyrics ->
                            if (showLyrics) {
                                InlineLyricsView(
                                    mediaMetadata = mediaMetadata,
                                    showLyrics = showLyrics,
                                    positionProvider = { effectivePosition },
                                )
                            } else {
                                Thumbnail(
                                    sliderPositionProvider = sliderPositionProvider,
                                    modifier = Modifier.nestedScroll(state.preUpPostDownNestedScrollConnection),
                                    isPlayerExpanded = isExpandedProvider,
                                    isListenTogetherGuest = isListenTogetherGuest,
                                )
                            }
                        }
                    }

                    mediaMetadata?.let { controlsContent(it) }
                    Spacer(Modifier.height(30.dp))
                }
            }
        }

        // ── Queue sheet ───────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = !isFullScreen,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        ) {
            Queue(
                state = queueSheetState,
                playerBottomSheetState = state,
                navController = navController,
                background = if (useBlackBackground) Color.Black else MaterialTheme.colorScheme.surfaceContainer,
                onBackgroundColor = onBackgroundColor,
                TextBackgroundColor = TextBackgroundColor,
                textButtonColor = textButtonColor,
                iconButtonColor = iconButtonColor,
                pureBlack = pureBlack,
                showInlineLyrics = showInlineLyrics,
                playerBackground = playerBackground,
                onToggleLyrics = { showInlineLyrics = !showInlineLyrics },
            )
        }
    }
}

// ── Inline lyrics view ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun InlineLyricsView(
    mediaMetadata: MediaMetadata?,
    showLyrics: Boolean,
    positionProvider: () -> Long,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val currentLyrics by playerConnection.currentLyrics.collectAsStateWithLifecycle(initialValue = null)
    val queueWindows by playerConnection.queueWindows.collectAsStateWithLifecycle(initialValue = emptyList())
    val currentWindowIndex by playerConnection.currentWindowIndex.collectAsStateWithLifecycle(initialValue = -1)
    val lyrics = remember(currentLyrics) { currentLyrics?.lyrics?.trim() }
    val context = LocalContext.current
    val database = LocalDatabase.current
    val coroutineScope = rememberCoroutineScope()

    var appInForeground by remember {
        mutableStateOf(
            ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        )
    }
    DisposableEffect(Unit) {
        val lifecycle = ProcessLifecycleOwner.get().lifecycle
        val observer = LifecycleEventObserver { _, _ ->
            appInForeground = lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    val nextMetadata = remember(queueWindows, currentWindowIndex) {
        if (currentWindowIndex >= 0 && currentWindowIndex + 1 < queueWindows.size)
            queueWindows[currentWindowIndex + 1].mediaItem.metadata
        else null
    }

    LaunchedEffect(mediaMetadata?.id, currentLyrics) {
        if (mediaMetadata != null && currentLyrics == null) {
            delay(500)
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val entryPoint = EntryPointAccessors.fromApplication(
                        context.applicationContext,
                        com.metrolist.music.di.LyricsHelperEntryPoint::class.java,
                    )
                    val fetched = entryPoint.lyricsHelper().getLyrics(mediaMetadata)
                    database.query {
                        upsert(LyricsEntity(mediaMetadata.id, fetched.lyrics, fetched.provider))
                    }
                } catch (_: Exception) {}
            }
        }
    }

    LaunchedEffect(nextMetadata?.id, showLyrics, appInForeground, mediaMetadata?.id, currentLyrics) {
        if (!showLyrics || !appInForeground || nextMetadata == null) return@LaunchedEffect
        val loadedForCurrent = currentLyrics?.let { l ->
            mediaMetadata == null || l.id == mediaMetadata.id
        } == true
        if (mediaMetadata != null && !loadedForCurrent) return@LaunchedEffect
        delay(400)
        if (!showLyrics || !appInForeground || !isActive) return@LaunchedEffect
        withContext(Dispatchers.IO) {
            try {
                val existing = database.lyrics(nextMetadata.id).first()
                if (existing != null) return@withContext
                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    com.metrolist.music.di.LyricsHelperEntryPoint::class.java,
                )
                val fetched = entryPoint.lyricsHelper().getLyrics(nextMetadata)
                database.query {
                    upsert(LyricsEntity(nextMetadata.id, fetched.lyrics, fetched.provider))
                }
            } catch (_: Exception) {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        when {
            lyrics == null -> {
                androidx.compose.material3.ContainedLoadingIndicator()
            }
            lyrics == LyricsEntity.LYRICS_NOT_FOUND -> {
                Text(
                    text = stringResource(R.string.lyrics_not_found),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            }
            else -> {
                androidx.compose.material3.ProvideTextStyle(
                    value = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                ) {
                    Lyrics(
                        sliderPositionProvider = positionProvider,
                        modifier = Modifier.padding(horizontal = 24.dp),
                        showLyrics = showLyrics,
                    )
                }
            }
        }
    }
}
