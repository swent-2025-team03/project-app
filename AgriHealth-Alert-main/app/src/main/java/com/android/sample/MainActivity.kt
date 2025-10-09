package com.android.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.android.sample.resources.C
import com.android.sample.ui.authentification.SignInScreen
import com.android.sample.ui.authentification.SignUpScreen
import com.android.sample.ui.farmer.AddReportScreen
import com.android.sample.ui.farmer.MapScreen
import com.android.sample.ui.overview.OverviewScreen
import com.android.sample.ui.navigation.NavigationActions
import com.android.sample.ui.navigation.Screen
import com.android.sample.ui.theme.SampleAppTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.data.model.UserRole
import com.android.sample.ui.overview.OverviewViewModel


class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      SampleAppTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize().semantics { testTag = C.Tag.main_screen_container },
            color = MaterialTheme.colorScheme.background) {
              AgriHealthApp()
            }
      }
    }
  }
}

@Composable
fun AgriHealthApp() {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)
  val startDestination =
      // TODO replace by real authentication check
      if (false
      // needs to be replaced with the condition of a user being already signed in
      ) Screen.Auth.name
      else Screen.Overview.route

  NavHost(navController = navController, startDestination = startDestination) {
    navigation(
        startDestination = Screen.Auth.route,
        route = Screen.Auth.name,
    ) {
      composable(Screen.Auth.route) {
        SignInScreen(
            onSignedIn = { navigationActions.navigateTo(Screen.Overview) },
            goToSignUp = { navigationActions.navigateTo(Screen.SignUp) })
      }
      composable(Screen.SignUp.route) {
        SignUpScreen(onSignedUp = { navigationActions.navigateTo(Screen.Overview) })
      }
    }

    navigation(
        startDestination = Screen.Overview.route,
        route = Screen.Overview.name,
    ) {
      composable(Screen.Overview.route) {
          val overviewViewModel: OverviewViewModel = viewModel()

          //TODO: Get the information from logged in user
          val currentUserRole = UserRole.FARMER
          val currentUserId = "FARMER_001"

          val reportsForUser = overviewViewModel.getReportsForUser(currentUserRole, currentUserId)

        OverviewScreen(
            userRole = currentUserRole,
            reports = reportsForUser,
            onAddReport = { navigationActions.navigateTo(Screen.AddReport) },
            onSignedOut = { navigationActions.navigateTo(Screen.Auth) },
            // Temporarily commented out because the ViewReport screen has not been merged yet.
            //onReportClick = {navigationActions.navigateTo(Screen,ViewReport)},
            navigationActions = navigationActions,
        )
      }
      composable(Screen.AddReport.route) {
        AddReportScreen(
            onDone = { navigationActions.navigateTo(Screen.Overview) },
            onGoBack = { navigationActions.goBack() })
      }
    }

    navigation(
        startDestination = Screen.Map.route,
        route = Screen.Map.name,
    ) {
      composable(Screen.Map.route) { MapScreen(navigationActions) }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun AgriHealthPreview() {
  SampleAppTheme { AgriHealthApp() }
}
