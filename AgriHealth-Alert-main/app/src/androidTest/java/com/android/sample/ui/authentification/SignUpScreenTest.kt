package com.android.sample.ui.authentification

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import org.junit.Test

class SignUpScreenSmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun signup_fields_and_actions_are_displayed() {
        composeRule.setContent {
            MaterialTheme {
                SignUpScreen()
            }
        }

        composeRule.onNodeWithTag("SignUpTitle", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("NameField", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("SurnameField", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("EmailField", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("PasswordField", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("ConfirmPasswordField", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("SaveButton", useUnmergedTree = true).assertIsDisplayed()
    }
}

