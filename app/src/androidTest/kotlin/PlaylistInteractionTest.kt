import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import com.raaveinm.chirro.data.values.TrackInfo
import com.raaveinm.chirro.ui.layouts.playlist.PlayerMinimized
import com.raaveinm.chirro.ui.layouts.playlist.PlaylistItemRow
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test

class PlaylistInteractionTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockTrack = TrackInfo(
        id = 1,
        title = "A Moment Apart ",
        artist = "ODESZA",
        album = "A Moment Apart",
        duration = 1000L,
        uri = "",
        cover = "",
        isFavourite = true,
        date = 1000L,
    )

    @Test
    fun click_triggerNavigation() {
        var playCalled = false
        var navigateCalled = false

        composeTestRule.setContent {
            PlaylistItemRow(
                track = mockTrack,
                isExpanded = false,
                isPlaying = false,
                onExpandToggle = {},
                play = { playCalled = true },
                onDeleteSwipe = { false },
                navigateTo = { navigateCalled = true }
            )
        }

        val song = composeTestRule.onNodeWithText("ODESZA", substring = true)
        song.assertIsDisplayed()
        song.performClick()

        assert(playCalled)
        assert(navigateCalled)
    }

    @Test
    fun longClick_expandsTrack_andExpandedPlay_doesNotNavigate() {
        var playCalled = false
        var navigateCalled = false
        var expandToggledId by mutableStateOf<Long?>(null)

        composeTestRule.setContent {
            val isExpanded = expandToggledId == mockTrack.id

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                PlaylistItemRow(
                    track = mockTrack,
                    isExpanded = isExpanded,
                    isPlaying = false,
                    onExpandToggle = { id -> expandToggledId = id },
                    play = { playCalled = true },
                    onDeleteSwipe = { false },
                    navigateTo = { navigateCalled = true }
                )
            }
        }

        val trackNode = composeTestRule.onNode(
            hasText("A Moment Apart", substring = true) and hasClickAction()
        )
        trackNode.performTouchInput { longClick() }
        assertTrue(expandToggledId == mockTrack.id)

        composeTestRule.onNodeWithText("Play track").performClick()
         assertTrue(playCalled)
         assertFalse(navigateCalled)
    }

    @Test
    fun playlist_scrollsToSpecificTrack() {

        val mockPlaylist = List(100) { index ->
            TrackInfo(
                id = index.toLong(),
                title = "Song Number $index",
                artist = "",
                album = "",
                uri = "",
                cover = "",
                duration = 1000L,
                isFavourite = false,
                date = 1000L
            )
        }

        composeTestRule.setContent {
            LazyColumn {
                items(mockPlaylist) { track ->
                    PlaylistItemRow(
                        track = track,
                        isExpanded = false,
                        isPlaying = false,
                        onExpandToggle = {},
                        play = {},
                        onDeleteSwipe = { false },
                        navigateTo = {}
                    )
                }
            }
        }

        composeTestRule.onNode(hasScrollToIndexAction())
            .performScrollToNode(hasText("Song Number 72"))
        composeTestRule.onNodeWithText("Song Number 72")
            .assertIsDisplayed()
    }

    @Test
    fun miniPlayer_coverClick_triggersJumpToCurrent() {
        var jumpToCurrentTriggered = false

        composeTestRule.setContent {
            PlayerMinimized(
                trackInfo = mockTrack,
                onCoverClick = { jumpToCurrentTriggered = true },
                onSurfaceClick = {},
                playPauseIcon = Icons.Default.PlayArrow
            )
        }

        composeTestRule.onNodeWithContentDescription("Album Art").performClick()

        assertTrue(jumpToCurrentTriggered)
    }
}