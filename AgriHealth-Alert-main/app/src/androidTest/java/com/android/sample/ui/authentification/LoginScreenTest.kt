package com.android.sample.ui.authentification

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import org.junit.Test

class LoginScreenSmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun login_title_and_buttons_are_displayed() {
        composeRule.setContent {
            MaterialTheme {
                LoginScreen()
            }
        }

        composeRule.onNodeWithTag("LoginTitle", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("EmailField", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("PasswordField", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("LogInButton", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("SignUpButton", useUnmergedTree = true).assertIsDisplayed()
    }
}

