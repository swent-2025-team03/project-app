package com.android.sample.ui.navigation

// file taken from https://github.com/swent-epfl/bootcamp-25-B3-Solution/tree/main
object NavigationTestTags {
  const val BOTTOM_NAVIGATION_MENU = "BottomNavigationMenu"
  const val GO_BACK_BUTTON = "GoBackButton"
  const val TOP_BAR_TITLE = "TopBarTitle"
  const val OVERVIEW_TAB = "OverviewTab"
  const val MAP_TAB = "MapTab"

  fun getTabTestTag(tab: Tab): String =
      when (tab) {
        is Tab.Overview -> OVERVIEW_TAB
        is Tab.Map -> MAP_TAB
      }
}
