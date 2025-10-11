package com.android.agrihealth.ui.authentification

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import org.junit.Test

class SignUpScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun displayAllComponents() {
        composeRule.setContent {
            MaterialTheme { SignUpScreen() }
        }

        composeRule.onNodeWithTag(SignUpScreenTestTags.BACK_BUTTON).assertIsDisplayed()
        composeRule.onNodeWithTag(SignUpScreenTestTags.TITLE).assertIsDisplayed()
        composeRule.onNodeWithTag(SignUpScreenTestTags.NAME_FIELD).assertIsDisplayed()
        composeRule.onNodeWithTag(SignUpScreenTestTags.SURNAME_FIELD).assertIsDisplayed()
        composeRule.onNodeWithTag(SignUpScreenTestTags.EMAIL_FIELD).assertIsDisplayed()
        composeRule.onNodeWithTag(SignUpScreenTestTags.PASSWORD_FIELD).assertIsDisplayed()
        composeRule.onNodeWithTag(SignUpScreenTestTags.CONFIRM_PASSWORD_FIELD).assertIsDisplayed()
        composeRule.onNodeWithTag(SignUpScreenTestTags.FARMER_PILL).assertIsDisplayed()
        composeRule.onNodeWithTag(SignUpScreenTestTags.VET_PILL).assertIsDisplayed()
        composeRule.onNodeWithTag(SignUpScreenTestTags.SAVE_BUTTON).assertIsDisplayed()
    }


}
