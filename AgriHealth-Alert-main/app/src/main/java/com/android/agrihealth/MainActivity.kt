package com.android.agrihealth

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.android.agrihealth.resources.C
import com.android.agrihealth.ui.authentification.SignInScreen
import com.android.agrihealth.ui.authentification.SignUpScreen
import com.android.agrihealth.ui.map.MapScreen
import com.android.agrihealth.ui.navigation.NavigationActions
import com.android.agrihealth.ui.navigation.Screen
import com.android.agrihealth.ui.overview.OverviewScreen
import com.android.agrihealth.ui.overview.OverviewViewModel
import com.android.agrihealth.ui.profile.EditProfileScreen
import com.android.agrihealth.ui.profile.ProfileScreen
import com.android.agrihealth.ui.report.AddReportScreen
import com.android.agrihealth.ui.report.ReportViewModel
import com.android.agrihealth.ui.report.ReportViewScreen
import com.android.agrihealth.ui.theme.SampleAppTheme
import com.android.agrihealth.ui.user.UserViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

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
fun AgriHealthApp(
    context: Context = LocalContext.current,
    credentialManager: CredentialManager = CredentialManager.create(context)
) {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)

  // Shared ViewModel (lives across navigation destinations)
  val userViewModel: UserViewModel = viewModel()
  val currentUser by userViewModel.user.collectAsState()
  val currentUserRole = currentUser.role

  val startDestination =
      if (Firebase.auth.currentUser != null) Screen.Overview.name else Screen.Auth.name

  NavHost(navController = navController, startDestination = startDestination) {
    // --- Auth Graph ---
    navigation(
        startDestination = Screen.Auth.route,
        route = Screen.Auth.name,
    ) {
      composable(Screen.Auth.route) {
        SignInScreen(
            credentialManager = credentialManager,
            onSignedIn = {
              userViewModel.refreshCurrentUser()
              navigationActions.navigateTo(Screen.Overview)
            },
            goToSignUp = { navigationActions.navigateTo(Screen.SignUp) })
      }
      composable(Screen.SignUp.route) {
        SignUpScreen(
            onBack = { navigationActions.navigateTo(Screen.Auth) },
            onSignedUp = {
              userViewModel.refreshCurrentUser()
              navigationActions.navigateTo(Screen.Overview)
            })
      }
    }

    // --- Overview Graph ---
    navigation(
        startDestination = Screen.Overview.route,
        route = Screen.Overview.name,
    ) {
      composable(Screen.Overview.route) {
        val overviewViewModel: OverviewViewModel = viewModel()

        OverviewScreen(
            credentialManager = credentialManager,
            userRole = currentUserRole,
            overviewViewModel = overviewViewModel,
            onAddReport = { navigationActions.navigateTo(Screen.AddReport) },
            // TODO: Pass the selected report to the ViewReportScreen
            onReportClick = { reportId ->
              navigationActions.navigateTo(Screen.ViewReport(reportId))
            },
            navigationActions = navigationActions,
        )
      }
      composable(Screen.AddReport.route) {
        AddReportScreen(
            onCreateReport = { navigationActions.navigateTo(Screen.Overview) },
            onBack = { navigationActions.goBack() })
      }
      composable(
          route = Screen.ViewReport.route,
          arguments = listOf(navArgument("reportId") { type = NavType.StringType })) {
              backStackEntry ->
            val reportId = backStackEntry.arguments?.getString("reportId") ?: ""

            // You might fetch the report by ID here
            val viewModel: ReportViewModel = viewModel()

            ReportViewScreen(
                navController = navController,
                userRole = currentUserRole,
                viewModel = viewModel,
                reportId = reportId)
          }
      composable(Screen.Profile.route) {
        val credentialManager = CredentialManager.create(LocalContext.current)
        val overviewViewModel: OverviewViewModel = viewModel()

        ProfileScreen(
            userViewModel = userViewModel,
            onGoBack = { navigationActions.goBack() },
            onLogout = {
              overviewViewModel.signOut(credentialManager)
              navigationActions.navigateToAuthAndClear()
            },
            onEditProfile = {
              // Navigate to EditProfile normally
              navController.navigate(Screen.EditProfile.route)
            },
            onCode = {
              // If farmer clicked "Add new Vet with Code", open EditProfile and focus code field
              navController.navigate("${Screen.EditProfile.route}?openCode=true")
            })
      }
      composable(
          route = Screen.EditProfile.route + "?openCode={openCode}",
          arguments =
              listOf(
                  navArgument("openCode") {
                    type = NavType.BoolType
                    defaultValue = false
                  })) { backStackEntry ->
            val openCode = backStackEntry.arguments?.getBoolean("openCode") ?: false

            EditProfileScreen(
                userViewModel = userViewModel,
                onGoBack = { navigationActions.goBack() },
                onSave = { updatedUser ->
                  // Update shared ViewModel and go back to Profile
                  userViewModel.user = updatedUser
                  navigationActions.goBack()
                },
                onAddVetCode = { _code ->
                  // Placeholder for now — logic implemented separately
                },
                openCodeField = openCode)
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
