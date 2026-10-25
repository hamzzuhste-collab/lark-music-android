# Lark Player: Reverse Engineering & Clean-Room Architecture Blueprint

This document details the reverse-engineering analysis of **Lark Player** (Android) and maps its core architecture, components, and behaviors directly to our modern Android Kotlin + Jetpack Compose + Media3 implementation.

---

## 1. Architectural Component Mapping

| Subsystem | Lark Player Internal (Observed / Expected) | Clean-Room Re-implementation (`com.larkmusic.player`) |
| :--- | :--- | :--- |
| **Media Engine** | Custom ExoPlayer / IjkPlayer wrapper + MediaSessionCompat | Jetpack Media3 `ExoPlayer` + `MediaSessionService` (`playback.PlaybackService`) |
| **Floating Lyrics** | WindowManager `TYPE_APPLICATION_OVERLAY` + drag touch dispatcher | `lyrics.FloatingLyricsService` + System Alert Window permission |
| **LRC Lyric Sync** | Regex timestamp parser (`[mm:ss.xx]`) + binary search | `lyrics.LrcParser` + auto-scrolling Compose `LyricsView` |
| **Sound Effects / DSP** | `android.media.audiofx` (`Equalizer`, `BassBoost`, `Virtualizer`) | `playback.EqualizerController` + Compose `EqualizerScreen` |
| **Media Library Indexing** | `MediaStore.Audio.Media` content resolver projection | `data.repository.MusicRepository` with Android 10–14 scoped storage |
| **UI Framework** | Traditional Android XML Views + Support Library | Kotlin + **Jetpack Compose (BOM 2024)** + Material Design 3 |
| **State Management** | EventBus / Custom Listeners | Kotlin Coroutines + `StateFlow` (`MusicViewModel`, `EqualizerViewModel`) |

---

## 2. Target Reverse-Engineering Breakdown

### A. Manifest & Service Declarations
```xml
<!-- Required foreground service types for Android 14 (API 34) -->
<service
    android:name=".playback.PlaybackService"
    android:foregroundServiceType="mediaPlayback"
    android:exported="true">
    <intent-filter>
        <action android:name="androidx.media3.session.MediaSessionService" />
    </intent-filter>
</service>

<service
    android:name=".lyrics.FloatingLyricsService"
    android:exported="false" />
```

### B. Floating Lyrics Lifecycle
1. User requests floating lyrics toggle.
2. App checks `Settings.canDrawOverlays(context)`. If false, navigates to Android system settings `Settings.ACTION_MANAGE_OVERLAY_PERMISSION`.
3. `FloatingLyricsService` is started:
   - Allocates `WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY` (API 26+).
   - Inflates a glassmorphic rounded capsule with touch drag coordinates (`rawX`, `rawY`).
   - Receives broadcast intents `ACTION_UPDATE_LYRIC` from `MusicViewModel` on playback timestamp tick.

### C. Equalizer & AudioFX Chain
- Attaches to ExoPlayer's `audioSessionId` on `Player.STATE_READY`.
- Manages:
  - 5 center frequency bands: 60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz.
  - Band level range: -1500 to +1500 mB (-15dB to +15dB).
  - Presets: Flat, Rock, Pop, Jazz, Hip Hop, Dance, Classical, Bass Boosted.
  - BassBoost: 0–1000 strength.
  - Virtualizer (3D surround): 0–1000 strength.

---

## 3. REA Verification Pipeline

When an APK target is provided (e.g. `rea/lark_target.apk`):
1. **Package Verification**:
   - `inspect_android_package`: Extract SHA-256 digest, package name, min/target SDK, declared services.
2. **Audio Service Analysis**:
   - `search_android_classes` matching `*Player*`, `*AudioEffect*`, `*Lyric*`.
   - `inspect_android_class` on the playback controller to verify playback callbacks, audio focus handling, and notification layout.
3. **Reconstruction Ledger**:
   - Call `build_reconstruction_obligation_ledger` to verify full coverage of obligations between the original artifact and this codebase.
