package com.android.agrihealth.ui.navigation

import androidx.navigation.NavHostController
import com.android.agrihealth.data.model.location.Location

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

  object Overview : Screen(route = "overview", name = "Overview", isTopLevelDestination = true)

  object AddReport : Screen(route = "add_report", name = "Create a new report")

  //object Map : Screen(route = "map", name = "Map", isTopLevelDestination = true)

  object SignUp : Screen(route = "sign_up", name = "Sign Up")

  data class ViewReport(val reportId: String) :
      Screen(route = "view_report/${reportId}", name = "view_report") {
    companion object {
      const val route = "view_report/{reportId}"
    }
  }

  data class Map(val lat: Double? = null, val lng: Double? = null) : Screen(route = "map?lat=${lat}&lng=${lng}", name = "map", isTopLevelDestination = true) {
    companion object {
      const val route = "map?lat={lat}&lng={lng}"
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
    navController.popBackStack()
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
