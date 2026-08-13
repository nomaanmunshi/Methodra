package io.methodra.app.onboarding

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.methodra.app.design.MethodraTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class StoneOnboardingTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun openingOffersAccessibleSkipPath() {
        var completed = false
        composeRule.setContent {
            MethodraTheme {
                StoneOnboarding(reduceMotion = true, hapticsEnabled = false, onComplete = { completed = true })
            }
        }

        composeRule.onNodeWithText("Good advice is everywhere.\nA method is what turns it into action.").assertIsDisplayed()
        composeRule.onNodeWithText("Skip").assertHasClickAction().performClick()
        composeRule.runOnIdle { assertTrue(completed) }
    }
}
