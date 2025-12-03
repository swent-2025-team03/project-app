package com.android.agrihealth.testutil

import com.android.agrihealth.ui.navigation.NavigationActionsContract
import com.android.agrihealth.ui.navigation.Screen

/** Simple fake NavigationActions for tests. Tracks goBack calls and last destination. */
class FakeNavigationActions : NavigationActionsContract {
  var goBackCalls: Int = 0
  var lastNavigateTo: Screen? = null
  private var currentRouteValue: String = ""

  override fun navigateTo(screen: Screen) {
    lastNavigateTo = screen
    currentRouteValue = screen.route
  }

  override fun goBack() {
    goBackCalls++
    // In a real nav, current route would change to previous. For tests, reset to empty.
    currentRouteValue = ""
  }

  override fun navigateToAuthAndClear() {
    // no-op for tests
    currentRouteValue = "auth"
  }

  override fun currentRoute(): String = currentRouteValue
}
