package com.android.agrihealth.ui.navigation

interface NavigationActionsContract {
  fun navigateTo(screen: Screen)

  fun goBack()

  fun navigateToAuthAndClear()

  fun currentRoute(): String
}
