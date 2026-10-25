# Lark Music Android Player

A high-fidelity, clean-room music player application for Android phones inspired by **Lark Player**, built with **Kotlin**, **Jetpack Compose (Material 3)**, and **Jetpack Media3 (ExoPlayer)**, integrated with a **Reverse Engineer Anything (REA)** inspection pipeline.

---

## 🌟 Hallmark Features

- 🎵 **Jetpack Media3 & ExoPlayer Playback**: Background audio playback via `MediaSessionService`, continuous lock-screen notification controls, audio focus handling, and automatic pause on headphone disconnect (`ACTION_AUDIO_BECOMING_NOISY`).
- 🎤 **Floating Karaoke Lyrics**: Real-time draggable floating lyrics capsule (`SYSTEM_ALERT_WINDOW`) that displays synchronized lyrics on top of other applications.
- 📜 **LRC Lyric Synchronization**: High-precision parser with millisecond timing, auto-scrolling karaoke lyrics view with glowing text animations, and tap-to-seek support.
- 🎛️ **5-Band Equalizer & AudioFX**: Full hardware-accelerated sound enhancement suite including 5-band frequency sliders, custom audio presets (Rock, Pop, Jazz, Classical, Hip Hop, etc.), Bass Boost, and 3D Virtualizer surround sound.
- 🎨 **Modern Sleek UI**: Jetpack Compose Material 3 dark/neon theme, spinning vinyl turntable album artwork, persistent Mini Player bar, and smooth full-screen bottom sheet now-playing experience.
- 📁 **MediaStore Audio Scanner**: Android 10–14 scoped storage compliance for querying local audio tracks, duration filtering, and extracting album artwork.
- 🔍 **REA Reverse-Engineering Blueprint**: Complete mapping and checklist in `rea/` to cross-validate decompiled Lark Player APK internals against this codebase.

---

## 📂 Project Architecture

```
lark-music-android/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/com/larkmusic/player/
│           ├── LarkApplication.kt
│           ├── MainActivity.kt
│           ├── data/
│           │   ├── model/ (Song, LyricLine, EqualizerPreset)
│           │   └── repository/ (MusicRepository, LyricsRepository)
│           ├── playback/
│           │   ├── PlaybackService.kt (MediaSessionService)
│           │   └── EqualizerController.kt (AudioFX Equalizer, BassBoost, Virtualizer)
│           ├── lyrics/
│           │   ├── LrcParser.kt (LRC parser & active index finder)
│           │   └── FloatingLyricsService.kt (System WindowManager floating capsule)
│           └── ui/
│               ├── theme/ (NeonCyan, DarkSurface, Material 3)
│               ├── navigation/ (Screen sealed class)
│               ├── viewmodel/ (MusicViewModel, EqualizerViewModel)
│               └── components/
│                   ├── MainScreen.kt (Scaffold, bottom bar, mini-player anchor)
│                   ├── SongListScreen.kt (Track list, thumbnails, playing indicator)
│                   ├── MiniPlayer.kt (Glassmorphic floating playback bar)
│                   ├── NowPlayingSheet.kt (Vinyl turntable, scrubber slider, controls)
│                   ├── LyricsView.kt (Auto-scrolling synchronized karaoke lyrics)
│                   └── EqualizerScreen.kt (5-band sliders, bass boost, virtualizer)
├── rea/
│   ├── rea_architecture_blueprint.md (Decompilation & feature mapping)
│   ├── rea_inspection_checklist.json (REA milestone tracking)
│   └── rea_apk_ingest_guide.md (Instructions to supply Lark Player APK)
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 🚀 Getting Started

### Opening in Android Studio
1. Open **Android Studio** (Hedgehog 2023.1.1 or newer).
2. Select **Open** and choose:
   ```
   C:\Users\pc\.gemini\antigravity\scratch\lark-music-android
   ```
3. Allow Gradle to sync dependencies.
4. Connect an Android phone (or launch an Android Virtual Device running API 24–34) and click **Run (Shift + F10)**.

### Running with REA Analysis
To inspect an actual Lark Player APK and verify obligations:
1. Place the APK file at `rea/lark_target.apk` (see `rea/rea_apk_ingest_guide.md`).
2. Run the REA Android inspection tools (`inspect_android_package`, `search_android_classes`, `inspect_android_class`).
