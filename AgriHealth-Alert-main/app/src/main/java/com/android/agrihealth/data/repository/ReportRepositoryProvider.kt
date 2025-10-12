package com.android.agrihealth.data.repository

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

object ReportRepositoryProvider {

  /**
   * Provides a single instance of the repository in the app. `repository` is mutable for testing
   * purposes.
   */
  private val _repository: ReportRepository by lazy { ReportRepositoryLocal() }

  var repository: ReportRepository = _repository
}
