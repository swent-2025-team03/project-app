package com.android.agrihealth

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.android.agrihealth.core.design.theme.AgriHealthAppTheme
import com.android.agrihealth.data.model.connection.ConnectionRepositoryProvider
import com.android.agrihealth.data.model.connection.FirestoreSchema.Collections.FARMER_TO_OFFICE
import com.android.agrihealth.data.model.connection.FirestoreSchema.Collections.VET_TO_OFFICE
import com.android.agrihealth.data.model.device.location.LocationPermissionsRequester
import com.android.agrihealth.data.model.device.location.LocationRepository
import com.android.agrihealth.data.model.device.location.LocationRepositoryProvider
import com.android.agrihealth.data.model.device.location.LocationViewModel
import com.android.agrihealth.data.model.device.notifications.NotificationHandlerProvider
import com.android.agrihealth.data.model.device.notifications.NotificationsPermissionsRequester
import com.android.agrihealth.data.model.images.ImageViewModel
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.location.LocationPicker
import com.android.agrihealth.data.model.office.OfficeRepositoryFirestore
import com.android.agrihealth.data.model.user.copyCommon
import com.android.agrihealth.resources.C
import com.android.agrihealth.ui.alert.AlertViewModel
import com.android.agrihealth.ui.alert.AlertViewScreen
import com.android.agrihealth.ui.authentification.ResetPasswordScreen
import com.android.agrihealth.ui.authentification.ResetPasswordViewModel
import com.android.agrihealth.ui.authentification.RoleSelectionScreen
import com.android.agrihealth.ui.authentification.SignInScreen
import com.android.agrihealth.ui.authentification.SignUpScreen
import com.android.agrihealth.ui.authentification.VerifyEmailScreen
import com.android.agrihealth.ui.map.MapScreen
import com.android.agrihealth.ui.map.MapViewModel
import com.android.agrihealth.ui.navigation.NavigationActions
import com.android.agrihealth.ui.navigation.Screen
import com.android.agrihealth.ui.office.CreateOfficeScreen
import com.android.agrihealth.ui.office.ManageOfficeScreen
import com.android.agrihealth.ui.office.ManageOfficeViewModel
import com.android.agrihealth.ui.office.ViewOfficeScreen
import com.android.agrihealth.ui.office.ViewOfficeViewModel
import com.android.agrihealth.ui.overview.OverviewScreen
import com.android.agrihealth.ui.overview.OverviewViewModel
import com.android.agrihealth.ui.planner.PlannerScreen
import com.android.agrihealth.ui.profile.ChangePasswordScreen
import com.android.agrihealth.ui.profile.ChangePasswordViewModel
import com.android.agrihealth.ui.profile.ClaimCodeScreen
import com.android.agrihealth.ui.profile.CodesViewModel
import com.android.agrihealth.ui.profile.EditProfileScreen
import com.android.agrihealth.ui.profile.ProfileScreen
import com.android.agrihealth.ui.report.AddReportScreen
import com.android.agrihealth.ui.report.AddReportViewModel
import com.android.agrihealth.ui.report.ReportViewScreen
import com.android.agrihealth.ui.report.ReportViewViewModel
import com.android.agrihealth.ui.user.UserViewModel
import com.android.agrihealth.ui.user.ViewUserScreen
import com.android.agrihealth.ui.user.ViewUserViewModel
import com.android.agrihealth.ui.user.defaultUser
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      AgriHealthAppTheme {
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

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AgriHealthApp(
    context: Context = LocalContext.current,
    credentialManager: CredentialManager = CredentialManager.create(context)
) {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)

  // Shared ViewModel (lives across navigation destinations)
  val userViewModel: UserViewModel = viewModel()

  val overviewViewModel: OverviewViewModel = viewModel()

  // Location services: Use the ViewModel and not the repository
  LocationRepositoryProvider.repository = LocationRepository(context)
  val locationViewModel: LocationViewModel = viewModel()

  var reloadReports by remember { mutableStateOf(false) }
  val currentUser = userViewModel.uiState.collectAsState().value.user
  val currentUserId = currentUser.uid
  val currentUserRole = currentUser.role
  val currentUserEmail = currentUser.email

  // Notification handling, setup device
  val notificationHandler = NotificationHandlerProvider.handler
  var canSendNotificationToken by remember { mutableStateOf(false) }
  NotificationsPermissionsRequester(
      onGranted = {
        // Fires on each recomposition of Overview, but it's a set so it's fine
        if (canSendNotificationToken) {
          notificationHandler.getToken { token ->
            if (token == null) return@getToken

            val newUser =
                currentUser.copyCommon(deviceTokensFCM = currentUser.deviceTokensFCM + token)
            userViewModel.updateUser(newUser)
          }
        }
      })

  var pickedLat = remember { 0.0 }
  var pickedLng = remember { 0.0 }

  val pickedLocation = remember { mutableStateOf(currentUser.address) }

  LaunchedEffect(currentUser.address) { pickedLocation.value = currentUser.address }

  val startDestination = remember {
    when {
      Firebase.auth.currentUser == null -> Screen.Auth.name
      !(Firebase.auth.currentUser?.isEmailVerified ?: false) -> Screen.EmailVerify.name
      currentUser == defaultUser -> Screen.RoleSelection.name
      else -> Screen.Overview.name
    }
  }

  NavHost(navController = navController, startDestination = startDestination) {
    // --- Auth Graph ---
    navigation(
        startDestination = Screen.Auth.route,
        route = Screen.Auth.name,
    ) {
      composable(Screen.Auth.route) {
        SignInScreen(
            credentialManager = credentialManager,
            onForgotPasswordClick = { navigationActions.navigateTo(Screen.ResetPassword) },
            onSignedIn = {
              userViewModel.refreshCurrentUser()
              navigationActions.navigateTo(Screen.Overview)
            },
            goToSignUp = { navigationActions.navigateTo(Screen.SignUp) },
            onNewGoogle = { navigationActions.navigateTo(Screen.RoleSelection) },
            onNotVerified = {
              userViewModel.refreshCurrentUser()
              navigationActions.navigateTo(Screen.EmailVerify)
            })
      }
      composable(Screen.SignUp.route) {
        SignUpScreen(
            userViewModel = userViewModel,
            onBack = { navigationActions.navigateTo(Screen.Auth) },
            onSignedUp = { navigationActions.navigateTo(Screen.EmailVerify) })
      }

      composable(Screen.ResetPassword.route) {
        val vm: ResetPasswordViewModel = viewModel()
        ResetPasswordScreen(onBack = navigationActions::goBack, vm)
      }
    }
    navigation(startDestination = Screen.RoleSelection.route, route = Screen.RoleSelection.name) {
      composable(Screen.RoleSelection.route) {
        RoleSelectionScreen(
            credentialManager = credentialManager,
            onBack = { navigationActions.navigateTo(Screen.Auth) },
            onButtonPressed = { navigationActions.navigateTo(Screen.EditProfile) },
            userViewModel = userViewModel)
      }
    }
    navigation(startDestination = Screen.EmailVerify.route, route = Screen.EmailVerify.name) {
      composable(Screen.EmailVerify.route) {
        VerifyEmailScreen(
            onBack = { navigationActions.navigateToAuthAndClear() },
            onVerified = { navigationActions.navigateTo(Screen.EditProfile) })
      }
    }

    // --- Overview Graph ---
    navigation(
        startDestination = Screen.Overview.route,
        route = Screen.Overview.name,
    ) {
      composable(Screen.Overview.route) {
        OverviewScreen(
            credentialManager = credentialManager,
            userRole = currentUserRole,
            user = currentUser,
            overviewViewModel = overviewViewModel,
            onAddReport = { navigationActions.navigateTo(Screen.AddReport) },
            onReportClick = { reportId ->
              navigationActions.navigateTo(Screen.ViewReport(reportId))
            },
            onAlertClick = { alertId -> navigationActions.navigateTo(Screen.ViewAlert(alertId)) },
            onLogin = { canSendNotificationToken = true },
            navigationActions = navigationActions,
        )
      }
      composable(Screen.AddReport.route) {
        val imageVM = viewModel<ImageViewModel>()
        val createReportViewModel =
            object : androidx.lifecycle.ViewModelProvider.Factory {
              override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AddReportViewModel(userId = currentUserId, imageViewModel = imageVM) as T
              }
            }
        val addReportViewModel: AddReportViewModel = viewModel(factory = createReportViewModel)

        AddReportScreen(
            onBack = {
              pickedLocation.value = currentUser.address
              navigationActions.goBack()
            },
            userViewModel = userViewModel,
            onCreateReport = { reloadReports = !reloadReports },
            pickedLocation = pickedLocation.value,
            onChangeLocation = { navigationActions.navigateTo(Screen.LocationPicker) },
            addReportViewModel = addReportViewModel,
        )
      }
      composable(route = Screen.LocationPicker.route) {
        val createMapViewModel =
            object : androidx.lifecycle.ViewModelProvider.Factory {
              override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MapViewModel(locationViewModel = locationViewModel, userId = currentUserId)
                    as T
              }
            }
        val mapViewModel: MapViewModel =
            viewModel(factory = createMapViewModel, key = pickedLocation.value.toString())

        LocationPermissionsRequester(
            onComplete = { mapViewModel.setStartingLocation(pickedLocation.value) })

        LocationPicker(
            navigationActions = navigationActions,
            mapViewModel = mapViewModel,
            onLatLng = { lat, lng ->
              pickedLat = lat
              pickedLng = lng
            },
            onAddress = { address ->
              if (address != null) {
                pickedLocation.value = Location(pickedLat, pickedLng, address)
                navigationActions.goBack()
              }
            })
      }
      composable(
          route = Screen.ViewReport.route,
          arguments = listOf(navArgument("reportId") { type = NavType.StringType })) {
              backStackEntry ->
            val reportId = backStackEntry.arguments?.getString("reportId") ?: ""

            // You might fetch the report by ID here
            val viewModel: ReportViewViewModel = viewModel()

            ReportViewScreen(
                navigationActions = navigationActions,
                userRole = currentUserRole,
                viewModel = viewModel,
                reportId = reportId,
                user = currentUser)
          }
      composable(
          route = Screen.ViewAlert.route,
          arguments = listOf(navArgument("alertId") { type = NavType.StringType })) { backStackEntry
            ->
            val alertId = backStackEntry.arguments?.getString("alertId") ?: ""

            val overviewUiState by overviewViewModel.uiState.collectAsState()
            val sortedAlerts = overviewUiState.sortedAlerts
            val alertViewModel = AlertViewModel(sortedAlerts, alertId)

            AlertViewScreen(navigationActions = navigationActions, viewModel = alertViewModel)
          }
      composable(
          route = Screen.ViewUser.route,
          arguments = listOf(navArgument("uid") { type = NavType.StringType })) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("uid") ?: ""
            val viewModel: ViewUserViewModel =
                viewModel(factory = ViewUserViewModel.provideFactory(targetUserId = userId))

            ViewUserScreen(viewModel = viewModel, onBack = { navigationActions.goBack() })
          }
      composable(
          route = Screen.ViewOffice.route,
          arguments = listOf(navArgument("officeId") { type = NavType.StringType })) {
              backStackEntry ->
            val officeId = backStackEntry.arguments?.getString("officeId") ?: ""
            val viewModel: ViewOfficeViewModel =
                viewModel(factory = ViewOfficeViewModel.provideFactory(officeId))

            ViewOfficeScreen(
                viewModel = viewModel,
                onBack = { navController.navigateUp() },
                onOpenUser = { uid -> navigationActions.navigateTo(Screen.ViewUser(uid)) })
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
            onEditProfile = { navigationActions.navigateTo(Screen.EditProfile) },
            onCodeFarmer = { navigationActions.navigateTo(Screen.ClaimCode(FARMER_TO_OFFICE)) },
            onManageOffice = { navigationActions.navigateTo(Screen.ManageOffice) })
      }
      composable(Screen.ManageOffice.route) {
        ManageOfficeScreen(
            navigationActions = navigationActions,
            userViewModel = userViewModel,
            onGoBack = { navigationActions.goBack() },
            onCreateOffice = { navigationActions.navigateTo(Screen.CreateOffice) },
            onJoinOffice = { navigationActions.navigateTo(Screen.ClaimCode(VET_TO_OFFICE)) },
            manageOfficeVmFactory = {
              object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                  return ManageOfficeViewModel(userViewModel, OfficeRepositoryFirestore()) as T
                }
              }
            },
            codesVmFactory = {
              object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                  return CodesViewModel(
                      userViewModel, ConnectionRepositoryProvider.vetToOfficeRepository)
                      as T
                }
              }
            })
      }
      composable(Screen.CreateOffice.route) {
        CreateOfficeScreen(
            userViewModel = userViewModel,
            onGoBack = { navigationActions.goBack() },
            onCreated = { navigationActions.goBack() })
      }
    }

    // --- Edit Profile Graph ---
    navigation(startDestination = Screen.EditProfile.route, route = Screen.EditProfile.name) {
      composable(route = Screen.EditProfile.route) {
        EditProfileScreen(
            userViewModel = userViewModel,
            onGoBack = {
              pickedLocation.value = currentUser.address
              navigationActions.navigateTo(Screen.Profile)
            },
            onSave = { updatedUser ->
              userViewModel.updateUser(updatedUser)
              navigationActions.navigateTo(Screen.Profile)
            },
            pickedLocation = pickedLocation.value,
            onChangeLocation = { navigationActions.navigateTo(Screen.LocationPicker) },
            onPasswordChange = { navigationActions.navigateTo(Screen.ChangePassword) })
      }
    }

    // --- Change Password Graph ---
    navigation(startDestination = Screen.ChangePassword.route, route = Screen.ChangePassword.name) {
      composable(Screen.ChangePassword.route) {
        val changePasswordViewModel: ChangePasswordViewModel = viewModel()
        ChangePasswordScreen(
            onBack = { navigationActions.goBack() },
            onUpdatePassword = { navigationActions.navigateTo(Screen.EditProfile) },
            userEmail = currentUserEmail,
            changePasswordViewModel = changePasswordViewModel)
      }
    }

    composable(
        route = Screen.Map.route,
        arguments =
            listOf(
                navArgument("lat") {
                  type = NavType.StringType
                  nullable = true
                  defaultValue = null
                },
                navArgument("lng") {
                  type = NavType.StringType
                  nullable = true
                  defaultValue = null
                },
                navArgument("reportId") {
                  type = NavType.StringType
                  nullable = true
                  defaultValue = null
                })) { backStackEntry ->
          val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull()
          val lng = backStackEntry.arguments?.getString("lng")?.toDoubleOrNull()
          val sourceReport = backStackEntry.arguments?.getString("reportId")

          val location = if (lat != null && lng != null) Location(lat, lng) else null
          val mapViewModel =
              MapViewModel(
                  locationViewModel = locationViewModel,
                  selectedReportId = sourceReport,
                  startingPosition = location,
                  userId = currentUserId)
          MapScreen(
              mapViewModel = mapViewModel,
              navigationActions = navigationActions,
              isViewedFromOverview = (sourceReport == null),
              forceStartingPosition = (location != null))
        }

    composable(
        route = Screen.Planner.route,
        arguments =
            listOf(
                navArgument("reportId") {
                  type = NavType.StringType
                  nullable = true
                  defaultValue = null
                })) { backStackEntry ->
          val reportId = backStackEntry.arguments?.getString("reportId")
          PlannerScreen(
              user = currentUser,
              reportId = reportId,
              goBack = navigationActions::goBack,
              tabClicked = navigationActions::navigateTo,
              reportClicked = { it -> navigationActions.navigateTo(Screen.ViewReport(it)) })
        }
    composable(
        route = Screen.ClaimCode.route,
        arguments =
            listOf(
                navArgument("connectionRepo") {
                  type = NavType.StringType
                  nullable = false
                  defaultValue = FARMER_TO_OFFICE
                })) { backStackEntry ->
          val connectionRepository =
              when (backStackEntry.arguments?.getString("connectionRepo")) {
                VET_TO_OFFICE -> ConnectionRepositoryProvider.vetToOfficeRepository
                FARMER_TO_OFFICE -> ConnectionRepositoryProvider.farmerToOfficeRepository
                else -> {
                  Log.e(
                      "ClaimCode controller",
                      "Unknown collection path : ${backStackEntry.arguments?.getString("connectionRepo")}")
                  ConnectionRepositoryProvider.farmerToOfficeRepository
                }
              }
          val profileFactory =
              object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                  return CodesViewModel(userViewModel, connectionRepository) as T
                }
              }
          ClaimCodeScreen(
              codesViewModel =
                  viewModel(
                      factory = profileFactory,
                      key = backStackEntry.arguments?.getString("connectionRepo")),
              { navigationActions.goBack() })
        }
  }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Preview(showBackground = true)
@Composable
fun AgriHealthPreview() {
  AgriHealthAppTheme { AgriHealthApp() }
}
