package com.android.agrihealth.ui.report

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.agrihealth.ui.authentification.SignInScreen
import com.android.agrihealth.ui.authentification.SignInScreenTestTags
import com.android.agrihealth.ui.farmer.AddReportViewModel
import org.junit.Rule
import org.junit.Test

class AddReportScreenTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun displayAllComponents() {
    composeRule.setContent { MaterialTheme { AddReportScreen() } }
    composeRule.onNodeWithTag(AddReportScreenTestTags.TITLE_FIELD).assertIsDisplayed()
    composeRule.onNodeWithTag(AddReportScreenTestTags.DESCRIPTION_FIELD).assertIsDisplayed()
    composeRule.onNodeWithTag(AddReportScreenTestTags.IMAGE_BUTTON).assertIsDisplayed()
    composeRule.onNodeWithTag(AddReportScreenTestTags.VET_DROPDOWN).assertIsDisplayed()
    composeRule.onNodeWithTag(AddReportScreenTestTags.CREATE_BUTTON).assertIsDisplayed()
  }
}