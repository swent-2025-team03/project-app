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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.android.agrihealth.ui.authentification.RoleSelectionScreen
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
import com.android.agrihealth.ui.report.AddReportViewModel
import com.android.agrihealth.ui.report.ReportViewModel
import com.android.agrihealth.ui.report.ReportViewScreen
import com.android.agrihealth.ui.theme.SampleAppTheme
import com.android.agrihealth.ui.user.UserViewModel
import com.android.agrihealth.ui.user.defaultUser
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

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

  var reloadReports by remember { mutableStateOf(false) }
  val currentUser by userViewModel.user.collectAsState()
  val currentUserId = currentUser.uid
  val currentUserRole = currentUser.role

  val startDestination =
      if (Firebase.auth.currentUser == null) Screen.Auth.name
      else if (currentUser == defaultUser) Screen.RoleSelection.name else Screen.Overview.name

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
            goToSignUp = { navigationActions.navigateTo(Screen.SignUp) },
            onNewGoogle = { navigationActions.navigateTo(Screen.RoleSelection) })
      }
      composable(Screen.SignUp.route) {
        SignUpScreen(
            userViewModel = userViewModel,
            onBack = { navigationActions.navigateTo(Screen.Auth) },
            onSignedUp = { navigationActions.navigateTo(Screen.EditProfile) })
      }
    }
    navigation(startDestination = Screen.RoleSelection.route, route = Screen.RoleSelection.name) {
      composable(Screen.RoleSelection.route) {
        RoleSelectionScreen(
            credentialManager = credentialManager,
            onBack = { navigationActions.navigateTo(Screen.Auth) },
            onButtonPressed = { navigationActions.navigateTo(Screen.EditProfile) })
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
            userId = currentUserId,
            overviewViewModel = overviewViewModel,
            onAddReport = { navigationActions.navigateTo(Screen.AddReport) },
            onReportClick = { reportId ->
              navigationActions.navigateTo(Screen.ViewReport(reportId))
            },
            navigationActions = navigationActions,
        )
      }
      composable(Screen.AddReport.route) {
        val createReportViewModel = AddReportViewModel(userId = currentUserId)

        AddReportScreen(
            onBack = { navigationActions.goBack() },
            userRole = currentUserRole,
            userId = currentUserId,
            userViewModel = userViewModel,
            onCreateReport = { reloadReports = !reloadReports },
            addReportViewModel = createReportViewModel,
        )
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
    }

    // --- Profile Graph ---
    navigation(
        startDestination = Screen.Profile.route,
        route = Screen.Profile.name,
    ) {
      composable(Screen.Profile.route) {
        val credentialManager = CredentialManager.create(LocalContext.current)
        val overviewViewModel: OverviewViewModel = viewModel()

        ProfileScreen(
            userViewModel = userViewModel,
            onGoBack = { navigationActions.navigateTo(Screen.Overview) },
            onLogout = {
              overviewViewModel.signOut(credentialManager)
              navigationActions.navigateToAuthAndClear()
            },
            onEditProfile = {
              // Navigate to EditProfile normally
              navController.navigate(Screen.EditProfile.route)
            },
            onCodeFarmer = {
              // If farmer clicked "Add new Vet with Code", open EditProfile too
              navController.navigate(Screen.EditProfile.route)
            })
      }
    }

    // --- Edit Profile Graph ---
    navigation(startDestination = Screen.EditProfile.route, route = Screen.EditProfile.name) {
      composable(Screen.EditProfile.route) {
        EditProfileScreen(
            userViewModel = userViewModel,
            onGoBack = { navigationActions.navigateTo(Screen.Profile) },
            onSave = { updatedUser ->
              userViewModel.updateUser(updatedUser)
              navigationActions.navigateTo(Screen.Profile)
            },
            onAddVetCode = { _code ->
              // placeholder for now
            })
      }
    }

    navigation(
        startDestination = Screen.Map.route,
        route = Screen.Map.name,
    ) {
      composable(Screen.Map.route) {
        MapScreen(navigationActions = navigationActions, isViewedFromOverview = true)
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun AgriHealthPreview() {
  SampleAppTheme { AgriHealthApp() }
}
