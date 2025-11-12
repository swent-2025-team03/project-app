// AuthorNameViewModel.kt
package com.android.agrihealth.ui.common

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.agrihealth.data.model.user.UserDirectoryDataSource
import com.android.agrihealth.data.model.user.UserDirectoryRepository
import com.android.agrihealth.data.model.user.displayString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthorNameViewModel(private val repo: UserDirectoryDataSource = UserDirectoryRepository()) :
    ViewModel() {

  private val _label = MutableStateFlow("…")
  val label: StateFlow<String> = _label

  fun load(uid: String?, showRole: Boolean, deletedText: String, unassignedText: String) {
    viewModelScope.launch {
      if (uid == null) {
        _label.value = unassignedText
        return@launch
      }
      val user = runCatching { repo.getUserSummary(uid) }.getOrNull()
      _label.value =
          when {
            user == null -> deletedText
            else ->
                buildString {
                  append(user.firstname).append(' ').append(user.lastname)
                  if (showRole && user.role != null) {
                    append(" (").append(user.role.displayString()).append(')')
                  }
                }
          }
    }
  }
}

@Composable
fun AuthorName(
    uid: String?,
    showRole: Boolean = false,
    deletedText: String = "Deleted user",
    unassignedText: String = "Unassigned",
    viewModel: AuthorNameViewModel = viewModel(key = uid)
) {
  val label by viewModel.label.collectAsState()

  LaunchedEffect(uid, showRole, deletedText, unassignedText) {
    viewModel.load(uid, showRole, deletedText, unassignedText)
  }
  Text(text = label)
}
