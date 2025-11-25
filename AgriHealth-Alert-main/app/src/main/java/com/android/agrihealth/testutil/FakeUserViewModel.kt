package com.android.agrihealth.testutil

import com.android.agrihealth.data.model.user.User
import com.android.agrihealth.ui.user.UserViewModel

class FakeUserViewModel(initialUser: User) : UserViewModel(initialUser = initialUser)
