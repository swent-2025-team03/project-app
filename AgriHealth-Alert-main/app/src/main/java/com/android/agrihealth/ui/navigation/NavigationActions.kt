package com.android.agrihealth.ui.navigation

import androidx.navigation.NavHostController

// file taken from https://github.com/swent-epfl/bootcamp-25-B3-Solution/tree/main

/**
 * Sealed class representing different screens in the app's navigation. Each screen has a route, a
 * display name, and a flag indicating if it's a top-level destination.
 *
 * @property route The unique route string used for navigation within the NavHost.
 * @property name A human-readable label for the screen, typically shown in UI elements like the top
 *   bar.
 * @property isTopLevelDestination Whether this screen is considered a top-level destination (e.g.,
 *   shown in a BottomNavigationBar).
 */
sealed class Screen(
    val route: String,
    val name: String,
    val isTopLevelDestination: Boolean = false
) {
  object Auth : Screen(route = "auth", name = "Authentication")

  object Profile : Screen(route = "profile", name = "Profile", isTopLevelDestination = true)

  object ChangePassword :
      Screen(route = "changePassword", name = "Change Your Password", isTopLevelDestination = true)

  object EditProfile :
      Screen(route = "edit_profile", name = "Edit Profile", isTopLevelDestination = true)

  object Overview : Screen(route = "overview", name = "Overview", isTopLevelDestination = true)

  object AddReport : Screen(route = "add_report", name = "Create a Report")

  object ManageOffice : Screen(route = "manage_office", name = "Manage My Office")

  object CreateOffice : Screen("create_office", "Create Office")

  object SignUp : Screen(route = "sign_up", name = "Sign Up")

  object RoleSelection : Screen(route = "role", name = "Role")

  object LocationPicker : Screen(route = "location_picker", name = "Select a Location")

  data class ViewReport(val reportId: String) :
      Screen(route = "view_report/${reportId}", name = "view_report") {
    companion object {
      const val route = "view_report/{reportId}"
    }
  }

  data class ViewAlert(val alertId: String) :
      Screen(route = "view_alert/${alertId}", name = "view_alert") {
    companion object {
      const val route = "view_alert/{alertId}"
    }
  }

  data class ViewUser(val uid: String) : Screen(route = "view_user/${uid}", name = "View User") {
    companion object {
      const val route = "view_user/{uid}"
    }
  }

  data class ViewOffice(val officeId: String) :
      Screen(route = "view_office/${officeId}", name = "View Office") {
    companion object {
      const val route = "view_office/{officeId}"
    }
  }

  data class Map(val lat: Double? = null, val lng: Double? = null, val reportId: String? = null) :
      Screen(
          route = "map?lat=${lat}&lng=${lng}&reportId=${reportId}",
          name = "map",
          isTopLevelDestination = true) {
    companion object {
      const val route = "map?lat={lat}&lng={lng}&reportId={reportId}"
    }
  }

  data class Planner(val reportId: String? = null) :
      Screen(
          route = "planner?reportId=${reportId}", name = "Planner", isTopLevelDestination = true) {
    companion object {
      const val route = "planner?reportId={reportId}"
    }
  }

  data class ClaimCode(val connectionRepo: String) :
      Screen(route = "claim_code?connectionRepo=${connectionRepo}", name = "Claim a code") {
    companion object {
      const val route = "claim_code?connectionRepo={connectionRepo}"
    }
  }
}

open class NavigationActions(
    private val navController: NavHostController,
) {
  /**
   * Navigate to the specified screen.
   *
   * @param screen The screen to navigate to
   */
  open fun navigateTo(screen: Screen) {
    if (screen.isTopLevelDestination && currentRoute() == screen.route) {
      // If the user is already on the top-level destination, do nothing
      return
    }
    navController.navigate(screen.route) {
      if (screen.isTopLevelDestination) {
        launchSingleTop = true
        popUpTo(screen.route) { inclusive = true }
      }

      if (screen !is Screen.Auth) {
        // Restore state when reselecting a previously selected item
        restoreState = true
      }
    }
  }

  /** Navigate back to the previous screen. */
  open fun goBack() {
    navController.navigateUp()
  }

  /**
   * Get the current route of the navigation controller.
   *
   * @return The current route
   */
  open fun currentRoute(): String {
    return navController.currentDestination?.route ?: ""
  }

  /** Navigate to the sign in screen and clear the backstack. */
  open fun navigateToAuthAndClear() {
    navController.navigate(Screen.Auth.route) { popUpTo(0) }
  }
}
