# Chirro Audio Player: Project Overview

## 1. Introduction

**Chirro** (formerly "ramp") is a native Android audio player application designed for devices running Android 12 and above. It aims to provide a clean, modern listening experience leveraging the latest Android development technologies. The application focuses on playing local audio files stored on the user's device, offering playlist management and efficient media handling.

## 2. Target Platform

* **Operating System:** Android 12+
* **Rationale:**
    * **Scoped Media Access:** Utilizes the modern `READ_MEDIA_AUDIO` permission available from Android 13+ (and compatible mechanisms in 12) for accessing audio files without requiring broad storage access, enhancing user privacy and security.
    * **Modern UI/UX:** Leverages Jetpack Compose and Material 3, which integrate seamlessly with the visual style and capabilities introduced in recent Android versions.

## 3. Core Technologies

* **Language:** Kotlin
* **UI Toolkit:** Jetpack Compose
* **Design System:** Material 3
* **Architecture:** Follows Google's recommended Android app architecture guidelines (UI Layer, Domain Layer, Data Layer).
* **Asynchronous Programming:** Likely Kotlin Coroutines (implied by Jetpack Compose and Room KTX usage).

## 4. Architecture Overview

The application is structured into distinct layers for separation of concerns and maintainability:

### 4.1. Main Entry Point

* `MainActivity`: The primary activity hosting the Jetpack Compose UI.

### 4.2. UI Layer

Responsible for displaying application data and handling user interactions.

* **Screens/Composables:**
    * `PlayerScreen.kt`: Displays the currently playing track, playback controls, and track progress.
    * `PlaylistScreen.kt`: Shows lists of tracks, potentially user-created playlists, albums, artists, etc. Allows users to select tracks or playlists to play.
    * `Settings.kt`: Provides options for configuring the application.
* **Components:**
    * Reusable UI elements (e.g., custom buttons, list items, seek bars) used within the screens are located in a `components` sub-package.
* **Theme:**
    * `Color.kt`: Defines the application's color palette.
    * `Theme.kt`: Configures the overall Material 3 theme (colors, typography, shapes).
    * `Type.kt`: Defines typography styles.

### 4.3. Domain Layer

Contains the core business logic, independent of the UI and Data layers.

* `PlayerService`: Likely an Android `Service` (perhaps a `MediaSessionService` or `MediaBrowserService` using Media3) responsible for background audio playback, managing playback state, and handling media controls.
* `CommandResolver`: Interprets user actions or system events into commands for the `PlayerService`.
* `ErrorHandler`: Centralized handling for errors occurring within the application.
* `NotificationChannel`: Manages the notification channel required for playback notifications.
* `PermissionManager`: Handles requesting and checking necessary permissions (e.g., `READ_MEDIA_AUDIO`, notifications).

### 4.4. Data Layer

Handles data sourcing, storage, and retrieval.

* `MediaResolver`: Responsible for querying the Android `MediaStore` to find audio files on the device.
* `TrackDao`: Data Access Object interface for interacting with the Room database (defines CRUD operations for tracks).
* `TrackDatabase`: Room database definition, holding the `trackList` table.
* `TrackInfo`: Entity class representing a track in the database (see schema below).
* `ContentParsing`: Utilities for parsing metadata from retrieved audio files.

## 5. Key Libraries & Rationale

* **Jetpack Compose:** Modern declarative UI toolkit for building native Android UIs with Kotlin. Chosen for its ease of use, integration with Material 3, and improved development workflow.
* **Material 3:** Provides up-to-date Material Design components and styling, ensuring a modern look and feel.
    * **Extended Material Icons:** Used to easily incorporate standard icons, saving design time.
* **Media3 (ExoPlayer):** Google's recommended library for media playback. Chosen for its robustness, flexibility, support for various audio formats, background playback capabilities, and UI components (`media3-ui`).
* **Room:** Persistence library for local database storage. Used to cache resolved media content (avoiding rescans on every launch) and manage user-created playlists. Provides an abstraction layer over SQLite.
* **Navigation Compose:** Simplifies implementing navigation between different screens (Composables) within the app, creating a smooth user experience.

## 6. Database Schema

The application uses a Room database with a single table (`trackList`) to store information about discovered audio tracks.

* **Entity Definition:**

    ```kotlin
    package com.raaveinm.chirro.data

    import androidx.room.Entity
    import androidx.room.PrimaryKey

    @Entity (tableName = "trackList")
    data class TrackInfo (
        @PrimaryKey (autoGenerate = true) val id: Int = 0,
        val title: String,
        val artist: String,
        val album: String,
        val duration: Long, // Duration in milliseconds or seconds
        val uri: String,   // Content URI
        val artUri: String, // URI string for album art
        val included: Boolean // Flag to include/exclude from library
    )
    ```

* **Field Explanations:**
    * `id`: (Integer, Primary Key, Auto-generated) Unique identifier for each track entry in the database.
    * `title`: (String) The title of the track.
    * `artist`: (String) The artist name associated with the track.
    * `album`: (String) The album name associated with the track.
    * `duration`: (Long) The duration of the track, typically in milliseconds.
    * `path`: (String) The URI (`content://...`) or file path pointing to the actual audio file. Used by ExoPlayer to load the media.
    * `artUri`: (String) A URI string pointing to the album artwork associated with the track, if available.
    * `included`: (Boolean) A flag indicating whether the user wants this track to be part of their main library (e.g., to exclude recordings or non-music files found during scanning).

## 7. Future Considerations (Placeholder)

* Playlist creation and management features.
* Advanced sorting and filtering options.
* Equalizer settings.
* Metadata editing.
* Cloud sync or integration (if desired).


## Chirro: Project Structure Visualization

This tree represents the proposed package and file structure for the Chirro application, based on the provided description and incorporating common architectural patterns. Items marked with `(ext)` are suggested additions or refinements that might be beneficial.
```
app/
└── src/
└── main/
├── java/
│   └── com/
│       └── raaveinm/
│           └── chirro/
│               ├── MainActivity.kt  # Main entry point hosting Compose UI
│               │
│               ├── ui/              # UI Layer: Composables, ViewModels, Theme
│               │   ├── viewmodel/ (ext) # ViewModels for screens
│               │   │   ├── PlayerViewModel.kt (ext)
│               │   │   ├── PlaylistViewModel.kt (ext)
│               │   │   ├── SettingsViewModel.kt (ext)
│               │   │   └── SearchViewModel.kt (ext)
│               │   │
│               │   ├── screens/     # Main Composable screens
│               │   │   ├── PlayerScreen.kt
│               │   │   ├── PlaylistScreen.kt
│               │   │   ├── SettingsScreen.kt  # Renamed from .kt for clarity
│               │   │   ├── SearchScreen.kt (ext)
│               │   │   ├── AlbumDetailScreen.kt (ext)
│               │   │   └── ArtistDetailScreen.kt (ext)
│               │   │
│               │   ├── components/  # Reusable UI Composables
│               │   │   ├── PlayerControls.kt (ext)
│               │   │   ├── TrackListItem.kt (ext)
│               │   │   ├── PlaylistListItem.kt (ext)
│               │   │   ├── MiniPlayer.kt (ext) # Often shown persistently
│               │   │   └── /* other common UI elements /
│               │   │
│               │   └── theme/       # App theme definitions
│               │       ├── Color.kt
│               │       ├── Theme.kt
│               │       └── Type.kt
│               │
│               ├── domain/          # Domain Layer: Business Logic, Interfaces
│               │   ├── service/     # Background playback service
│               │   │   └── PlayerService.kt # (e.g., MediaSessionService)
│               │   │
│               │   ├── repository/  # Interfaces for data access
│               │   │   └── TrackRepository.kt (ext)
│               │   │
│               │   ├── usecase/ (ext) # Specific business operations
│               │   │   ├── GetTracksUseCase.kt (ext)
│               │   │   ├── GetPlaylistsUseCase.kt (ext)
│               │   │   ├── PlayTrackUseCase.kt (ext)
│               │   │   ├── UpdateTrackUseCase.kt (ext) # e.g., for 'included' flag
│               │   │   └── / other use cases like SavePlaylist /
│               │   │
│               │   ├── model/ (ext) # Domain-specific data models (optional)
│               │   │   ├── DomainTrack.kt (ext)
│               │   │   └── DomainPlaylist.kt (ext)
│               │   │
│               │   └── management/  # Core logic helpers (original grouping)
│               │       ├── CommandResolver.kt
│               │       ├── ErrorHandler.kt
│               │       ├── NotificationChannel.kt # Or NotificationManagerWrapper
│               │       └── PermissionManager.kt
│               │
│               └── data/            # Data Layer: Implementation, Data Sources
│                   ├── local/       # Local data source (Room DB)
│                   │   ├── dao/
│                   │   │   └── TrackDao.kt
│                   │   ├── database/
│                   │   │   └── TrackDatabase.kt
│                   │   ├── entity/
│                   │   │   └── TrackInfo.kt # Room Entity
│                   │   ├── LocalTrackDataSource.kt (ext) # Implements data source interface
│                   │   └── converters/ (ext) # Room TypeConverters if needed
│                   │       └── Converters.kt (ext)
│                   │
│                   ├── remote/ (ext) # Placeholder for potential remote sources
│                   │   └── RemoteTrackDataSource.kt (ext)
│                   │
│                   ├── repository/  # Implementation of domain repository interfaces
│                   │   └── TrackRepositoryImpl.kt (ext)
│                   │
│                   ├── source/      # MediaStore querying and parsing
│                   │   ├── MediaResolver.kt # Queries MediaStore
│                   │   ├── ContentParsing.kt # Parses metadata (might merge into Resolver)
│                   │   └── MediaSourceFactory.kt (ext) # Creates ExoPlayer MediaSources
│                   │
│                   ├── model/ (ext) # Data-layer specific models (e.g., for network responses)
│                   │   └── NetworkTrack.kt (ext)
│                   │   
│                   └── Converter.kt
│
└── res/
└── / Standard Android resource directories (drawable, layout, values, etc.) 
```
**Key:**

* `(ext)`: Marks files or directories that extend the original brainstormed structure, representing common patterns like ViewModels, Repositories, Use Cases, or more specific component breakdowns.

This structure provides a more detailed view, separating concerns further (like adding ViewModels and Repositories) which can be very helpful for larger or more complex applications built with Jetpack Compose and modern architecture patterns.

## MissedParts 

### Core Playback Functionality:

- Queue Management: How will the playback queue work? Can users add tracks, remove tracks, reorder the queue?
- Audio Focus: Your PlayerService needs to correctly request and manage audio focus to pause/duck when other apps need audio. Media3 helps with this.
- Becoming Noisy: Handle headset disconnection (pause playback). Media3 also provides helpers for this (Player.Listener.onAudioBecomingNoisy).

## Potential Future Features (To Keep in Mind):

- Search functionality.
- Equalizer.
- Sleep Timer.
- Editing track tags (metadata).
- Android Auto support (Media3 makes this much easier).
- Widgets.