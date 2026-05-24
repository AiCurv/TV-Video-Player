# TV Video Player

An Android TV video player with an advanced cache manager, built using ExoPlayer (Media3) and inspired by CloudStream's CS3IPlayer architecture.

## Features

- **Advanced Disk Cache** — `SimpleCache` with `LeastRecentlyUsedCacheEvictor` (direct port from CloudStream)
- **Configurable Buffer Settings** — Memory buffer size and duration via `DefaultLoadControl`
- **ExoPlayer Media3** — Full HLS, DASH, and progressive playback support
- **Android TV Leanback** — TV-optimized launcher and UI
- **Settings Screen** — Configure cache size, buffer size, and buffer length
- **Clear Cache** — One-tap cache clearing with usage display

## Architecture

```
com.tvvideoplayer.app/
├── cache/
│   └── VideoCacheManager.kt      # SimpleCache + LRU eviction (CloudStream pattern)
├── player/
│   ├── TVVideoPlayer.kt          # Core ExoPlayer engine with cache integration
│   └── PlayerCacheSettings.kt    # SharedPreferences for cache/buffer config
├── settings/
│   └── SettingsActivity.kt       # Cache & buffer settings UI
└── ui/
    ├── MainActivity.kt           # URL input launcher
    └── TVPlayerActivity.kt       # Full-screen video player
```

## Cache System (from CloudStream)

The cache manager is a direct adaptation of CloudStream's `CS3IPlayer.getCache()`:

```kotlin
// From CloudStream's CS3IPlayer.kt:
SimpleCache(
    File(context.cacheDir, "exoplayer"),
    LeastRecentlyUsedCacheEvictor(cacheSize),
    databaseProvider
)
```

Cache sizes: Disabled, 100MB, 250MB, 500MB, 1GB, 2GB, Unlimited

## Buffer Settings

Based on CloudStream's `DefaultLoadControl.Builder().setBufferDurationsMs()`:

- **Buffer Size**: 16MB–500MB (memory buffer)
- **Buffer Length**: Auto, 15s, 30s, 1min, 2min, 3min, 5min

## Building

```bash
./gradlew assembleDebug
```

## Requirements

- Android SDK 34 (minSdk 21)
- Kotlin 1.9.24
- Media3 1.2.1
- Java 17

## License

GPL-3.0 (same as CloudStream)
