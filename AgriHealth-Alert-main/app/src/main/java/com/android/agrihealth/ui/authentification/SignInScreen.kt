@file:OptIn(ExperimentalMaterial3Api::class)
package com.android.sample.ui.authentification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class Role { Farmer, Vet }

@Composable
fun SignUpScreen(
    onBack: () -> Unit = {},
    onSave: (name: String, surname: String, email: String, pwd: String, role: Role) -> Unit = { _,_,_,_,_ -> }
) {
    /* To delete after viewModel integration*/
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pwd by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var role by remember { mutableStateOf<Role?>(null) }
    /* End To delete after viewModel integration*/

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                "Create An Account",
                fontSize = 36.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(24.dp))

            Field(name, { name = it }, "Name")
            Field(surname, { surname = it }, "Surname")
            Field(email, { email = it }, "Email")
            Field(pwd, { pwd = it }, "Password")
            Field(confirm, { confirm = it }, "Confirm Password")

            Spacer(Modifier.height(16.dp))
            Text("Are you a vet or a farmer ?", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))

            RoleSelector(
                selected = role,
                onSelected = { role = it }
            )

            Spacer(Modifier.height(28.dp))
            Button(
                onClick = {
                    val r = role ?: Role.Farmer
                    onSave(name, surname, email, pwd, r)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF96B7B1))
            ) {
                Text("Save", fontSize = 24.sp)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RoleSelector(
    selected: Role?,
    onSelected: (Role) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        SelectablePill(
            text = "Farmer",
            selected = selected == Role.Farmer,
            onClick = { onSelected(Role.Farmer) }
        )
        SelectablePill(
            text = "Vet",
            selected = selected == Role.Vet,
            onClick = { onSelected(Role.Vet) }
        )
    }
}

@Composable
private fun SelectablePill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) Color(0xFF96B7B1) else Color(0xFFE5E5E5) // vert / gris
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = bg,
        modifier = Modifier
            .width(140.dp)
            .height(56.dp)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text, style = MaterialTheme.typography.titleMedium, color = Color.Black)
        }
    }
}
@Composable
fun Field(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color(0xFFF0F7F1),
            focusedContainerColor = Color(0xFFF0F7F1),
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Color.Transparent
        )
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SignUpScreenPreview() {
    MaterialTheme { SignUpScreen() }
}

