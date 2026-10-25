package com.larkmusic.player.ui.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.larkmusic.player.data.model.LyricLine
import com.larkmusic.player.data.model.Song
import com.larkmusic.player.data.repository.LyricsRepository
import com.larkmusic.player.data.repository.MusicRepository
import com.larkmusic.player.lyrics.FloatingLyricsService
import com.larkmusic.player.lyrics.LrcParser
import com.larkmusic.player.playback.PlaybackService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val musicRepository = MusicRepository(application)
    private val lyricsRepository = LyricsRepository()

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs = _songs.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs = _durationMs.asStateFlow()

    private val _lyrics = MutableStateFlow<List<LyricLine>>(emptyList())
    val lyrics = _lyrics.asStateFlow()

    private val _activeLyricIndex = MutableStateFlow(0)
    val activeLyricIndex = _activeLyricIndex.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle = _isShuffle.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode = _repeatMode.asStateFlow()

    private val _isNowPlayingExpanded = MutableStateFlow(false)
    val isNowPlayingExpanded = _isNowPlayingExpanded.asStateFlow()

    private val _isFloatingLyricsActive = MutableStateFlow(false)
    val isFloatingLyricsActive = _isFloatingLyricsActive.asStateFlow()

    private var progressTrackingJob: Job? = null

    init {
        initializeMediaController()
    }

    private fun initializeMediaController() {
        val context = getApplication<Application>()
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture?.addListener({
            try {
                mediaController = controllerFuture?.get()
                mediaController?.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(playing: Boolean) {
                        _isPlaying.value = playing
                        if (playing) {
                            startProgressTracking()
                        } else {
                            progressTrackingJob?.cancel()
                        }
                    }

                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        val currentId = mediaItem?.mediaId?.toLongOrNull()
                        val song = _songs.value.find { it.id == currentId }
                        song?.let { setCurrentSongInternal(it) }
                    }

                    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                        _isShuffle.value = shuffleModeEnabled
                    }

                    override fun onRepeatModeChanged(newRepeatMode: Int) {
                        _repeatMode.value = newRepeatMode
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())
    }

    fun loadMusic() {
        viewModelScope.launch {
            val list = musicRepository.loadLocalSongs()
            _songs.value = list
            if (_currentSong.value == null && list.isNotEmpty()) {
                _currentSong.value = list.first()
                _durationMs.value = list.first().duration
                loadLyrics(list.first())
            }
        }
    }

    fun playSong(song: Song) {
        val controller = mediaController ?: return
        setCurrentSongInternal(song)

        // Build MediaItem
        val mediaItem = MediaItem.Builder()
            .setMediaId(song.id.toString())
            .setUri(song.contentUri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setAlbumTitle(song.album)
                    .setArtworkUri(song.albumArtUri)
                    .build()
            )
            .build()

        controller.setMediaItem(mediaItem)
        controller.prepare()
        controller.play()
    }

    private fun setCurrentSongInternal(song: Song) {
        _currentSong.value = song
        _durationMs.value = song.duration
        loadLyrics(song)
    }

    fun togglePlayPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            if (controller.mediaItemCount == 0 && _currentSong.value != null) {
                playSong(_currentSong.value!!)
            } else {
                controller.play()
            }
        }
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
        _currentPositionMs.value = positionMs
        updateActiveLyric(positionMs)
    }

    fun skipNext() {
        val currentList = _songs.value
        val currentIndex = currentList.indexOfFirst { it.id == _currentSong.value?.id }
        if (currentIndex != -1 && currentIndex < currentList.size - 1) {
            playSong(currentList[currentIndex + 1])
        } else if (currentList.isNotEmpty()) {
            playSong(currentList.first())
        }
    }

    fun skipPrevious() {
        val currentList = _songs.value
        val currentIndex = currentList.indexOfFirst { it.id == _currentSong.value?.id }
        if (currentIndex > 0) {
            playSong(currentList[currentIndex - 1])
        } else if (currentList.isNotEmpty()) {
            playSong(currentList.last())
        }
    }

    fun toggleShuffle() {
        val current = _isShuffle.value
        mediaController?.shuffleModeEnabled = !current
        _isShuffle.value = !current
    }

    fun toggleRepeat() {
        val current = _repeatMode.value
        val next = when (current) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        mediaController?.repeatMode = next
        _repeatMode.value = next
    }

    fun setNowPlayingExpanded(expanded: Boolean) {
        _isNowPlayingExpanded.value = expanded
    }

    fun toggleFloatingLyrics() {
        val context = getApplication<Application>()
        if (Settings.canDrawOverlays(context)) {
            val nextState = !_isFloatingLyricsActive.value
            _isFloatingLyricsActive.value = nextState
            val intent = Intent(context, FloatingLyricsService::class.java)
            if (nextState) {
                context.startService(intent)
            } else {
                context.stopService(intent)
            }
        }
    }

    private fun loadLyrics(song: Song) {
        viewModelScope.launch {
            val lines = lyricsRepository.loadLyricsForSong(song)
            _lyrics.value = lines
            updateActiveLyric(_currentPositionMs.value)
        }
    }

    private fun startProgressTracking() {
        progressTrackingJob?.cancel()
        progressTrackingJob = viewModelScope.launch {
            while (isActive) {
                mediaController?.let { controller ->
                    val pos = controller.currentPosition
                    _currentPositionMs.value = pos
                    val dur = controller.duration
                    if (dur > 0) {
                        _durationMs.value = dur
                    }
                    updateActiveLyric(pos)
                }
                delay(300)
            }
        }
    }

    private fun updateActiveLyric(positionMs: Long) {
        val lines = _lyrics.value
        if (lines.isNotEmpty()) {
            val index = LrcParser.findActiveLyricIndex(lines, positionMs)
            if (index != _activeLyricIndex.value) {
                _activeLyricIndex.value = index

                // Broadcast lyric text to floating capsule if active
                if (_isFloatingLyricsActive.value && index in lines.indices) {
                    val context = getApplication<Application>()
                    val updateIntent = Intent(context, FloatingLyricsService::class.java).apply {
                        action = FloatingLyricsService.ACTION_UPDATE_LYRIC
                        putExtra(FloatingLyricsService.EXTRA_LYRIC_TEXT, lines[index].text)
                    }
                    context.startService(updateIntent)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        progressTrackingJob?.cancel()
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
    }
}
