# Chirro
*(ive messed up with naming)*

## Features

* **Local Audio Scanning**: Automatically fetches audio files from the device's external storage.
* **Playback Engine**: Android Media3 (ExoPlayer).
* **Background Playback**: Continues playing music when the app is minimized, complete with a media notification style.
* **Playlist Management**:
    * View all local tracks.
    * Swipe-to-delete functionality to remove tracks from the list/storage.
    * "Jump to current track" auto-scrolling.
* **Favorites System**: Mark tracks as favorites for easy access (Database persistence).
* **Material 3 Design**: deeply integrated with Android design principles.

## Libraries

* **Language**: [Kotlin](https://kotlinlang.org/)
* **UI Toolkit**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
* **Architecture**: MVVM (Model-View-ViewModel)
* **Media**: [Jetpack Media3](https://developer.android.com/jetpack/androidx/releases/media3) (ExoPlayer, MediaSession)
* **Navigation**: [Compose Navigation](https://developer.android.com/guide/navigation/navigation-compose)
* **Asynchronous Programming**: [Coroutines & Flow](https://kotlinlang.org/docs/coroutines-overview.html)
* **Dependency Injection**: Manual DI (via `AppContainer`)
* **Image Loading**: [Coil](https://coil-kt.github.io/coil/compose/)
* **Local Storage**: [Room Database](https://developer.android.com/training/data-storage/room)

## Architecture Overview

The app follows the recommended **App Architecture Guide**:

* **UI Layer**: Composable screens (`PlayerScreen`, `PlaylistScreen`) observe `UiState` from ViewModels.
* **Domain Layer**: `PlaybackService` manages the actual media controller and session, exposing media state to the UI.
* **Data Layer**: `TrackRepository` acts as the single source of truth, mediating between the local database (Room) and media content resolvers.
