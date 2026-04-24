import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.raaveinm.chirro.ui.layouts.player.PlayerControlButtons
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test

class PlayerControlsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testButtonsCallback() {
        var playPauseClicked = false
        var nextClicked = false
        var previousClicked = false

        composeTestRule.setContent {
            PlayerControlButtons(
                modifier = Modifier,
                isPlaying = playPauseClicked,
                onPlayPauseClick = { playPauseClicked = !playPauseClicked },
                onPreviousClick = { previousClicked = true },
                onNextClick = { nextClicked = true },
                onSeek = {},
                extendedMenu = {},
                currentDuration = 12L,
                isFavourite = false,
                onShareClick = {},
                trackLength = 1200L,
                onDismissRequest = {},
            )
        }

        val playPauseButton = composeTestRule.onNodeWithContentDescription("play/pause")
        playPauseButton.assertIsDisplayed()
        playPauseButton.performClick()
        assertTrue(playPauseClicked)

        val nextButton = composeTestRule.onNodeWithContentDescription("next")
        nextButton.assertIsDisplayed()
        nextButton.performClick()
        assertTrue(nextClicked)

        val previousButton = composeTestRule.onNodeWithContentDescription("previous")
        previousButton.assertIsDisplayed()
        previousButton.performClick()
        assertTrue(previousClicked)
    }
}