// AuthorNameViewModel.kt
package com.android.agrihealth.ui.common.resolver

import androidx.compose.foundation.clickable
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthorNameViewModel(
    private val repo: UserDirectoryDataSource = UserDirectoryRepositoryProvider.repository
) : ViewModel() {

  private val _label = MutableStateFlow("…")
  val label: StateFlow<String> = _label

  fun load(uid: String?, deletedText: String, unassignedText: String) {
    viewModelScope.launch {
      if (uid == null) {
        _label.value = unassignedText
        return@launch
      }
      val user = repo.getUserSummary(uid)
      _label.value =
          when {
            user == null -> deletedText
            else -> buildString { append(user.firstname).append(' ').append(user.lastname) }
          }
    }
  }
}

@Composable
fun AuthorName(
    uid: String?,
    deletedText: String = "Deleted user",
    unassignedText: String = "Unassigned",
    viewModel: AuthorNameViewModel = viewModel(key = uid),
    onClick: (() -> Unit)? = null
) {
  val label by viewModel.label.collectAsState()

  LaunchedEffect(uid, deletedText, unassignedText) {
    viewModel.load(uid, deletedText, unassignedText)
  }

  Text(
      text = label,
      color = if (onClick != null) MaterialTheme.colorScheme.primary else LocalContentColor.current,
      textDecoration = if (onClick != null) TextDecoration.Underline else TextDecoration.None,
      modifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier)
}

/**
 * String composable in case you need to display the User Name in a context that requires a String
 */
@Composable
fun rememberUserName(userId: String?): String {
  val vm: AuthorNameViewModel = viewModel(key = userId)
  val name by vm.label.collectAsState()

  LaunchedEffect(userId) {
    vm.load(uid = userId, deletedText = "Deleted user", unassignedText = "Unassigned")
  }

  return name
}
