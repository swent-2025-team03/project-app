// AuthorNameViewModel.kt
package com.android.agrihealth.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.agrihealth.data.model.user.UserDirectoryDataSource
import com.android.agrihealth.data.model.user.UserDirectoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthorNameViewModel(private val repo: UserDirectoryDataSource = UserDirectoryRepository()) :
    ViewModel() {

  private val _label = MutableStateFlow("…")
  val label: StateFlow<String> = _label

  fun load(uid: String?, deletedText: String, unassignedText: String) {
    viewModelScope.launch {
      if (uid == null) {
        _label.value = unassignedText
        return@launch
      }
      val user = runCatching { repo.getUserSummary(uid) }.getOrNull()
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
      modifier =
          if (onClick != null) {
            Modifier.clickable { onClick() }
          } else {
            Modifier
          })
}
