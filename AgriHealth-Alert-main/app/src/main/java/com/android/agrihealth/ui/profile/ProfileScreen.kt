package com.android.agrihealth.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.agrihealth.data.model.UserRole
import com.android.agrihealth.ui.user.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userViewModel: UserViewModel = viewModel(),
    onGoBack: () -> Unit,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit
) {
    val user = userViewModel.user
    val userRole = userViewModel.userRole

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile - ${if (userRole == UserRole.FARMER) "Farmer" else "Vet"}",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onGoBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            HorizontalDivider(modifier = Modifier.padding(bottom = 24.dp))

            /* Not sure how to handle images for now, so commenting this out
            // Profile Image
            Image(
                painter = painterResource(id = R.drawable.profile_placeholder), // replace with real image loader later
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(12.dp))
            */

            // Name + Edit Icon
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = user?.name ?: "Unknown",
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = onEditProfile) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit profile")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Email
            OutlinedTextField(
                value = user?.email ?: "",
                onValueChange = {},
                label = { Text("Email address") },
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password
            OutlinedTextField(
                value = "********",
                onValueChange = {},
                label = { Text("Password") },
                enabled = false,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Location
            OutlinedTextField(
                value = user?.location ?: "", // Need to merge other PR to do this properly
                onValueChange = {},
                label = { Text("Location") },
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )

            // Default Vet (only for Farmers)
            if (userRole == UserRole.FARMER) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = user?.defaultVet ?: "", // Need to merge other PR to do this properly
                    onValueChange = {},
                    label = { Text("Default Vet") },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
