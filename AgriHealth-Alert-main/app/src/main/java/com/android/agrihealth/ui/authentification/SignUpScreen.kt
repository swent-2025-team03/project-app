@file:OptIn(ExperimentalMaterial3Api::class)

package com.android.agrihealth.ui.authentification

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


private val FieldBg  = Color(0xFFF0F6F1)

enum class Role { Farmer, Vet }

// Test tags provisoires (privés)
private val BackButtonTestTag = "BackButton"
private val TitleTestTag = "SignUpTitle"
private val NameFieldTestTag = "NameField"
private val SurnameFieldTestTag = "SurnameField"
private val EmailFieldTestTag = "EmailField"
private val PasswordFieldTestTag = "PasswordField"
private val ConfirmPasswordFieldTestTag = "ConfirmPasswordField"
private val SaveButtonTestTag = "SaveButton"
private val FarmerPillTestTag = "FarmerPill"
private val VetPillTestTag = "VetPill"

@Composable
fun SignUpScreen(
    onBack: () -> Unit = {},
    onSignedUp: () -> Unit = {}
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
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag(BackButtonTestTag)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .background(FieldBg)
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                "Create An Account",
                fontSize = 36.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.testTag(TitleTestTag)
            )
            Spacer(Modifier.height(24.dp))

            Field(name, { name = it }, "Name", modifier = Modifier.testTag(NameFieldTestTag))
            Field(surname, { surname = it }, "Surname", modifier = Modifier.testTag(SurnameFieldTestTag))
            Field(email, { email = it }, "Email", modifier = Modifier.testTag(EmailFieldTestTag))
            Field(pwd, { pwd = it }, "Password", modifier = Modifier.testTag(PasswordFieldTestTag))
            Field(confirm, { confirm = it }, "Confirm Password", modifier = Modifier.testTag(ConfirmPasswordFieldTestTag))

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
                    onSignedUp()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag(SaveButtonTestTag),
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
            onClick = { onSelected(Role.Farmer) },
            modifier = Modifier.testTag(FarmerPillTestTag)
        )
        SelectablePill(
            text = "Vet",
            selected = selected == Role.Vet,
            onClick = { onSelected(Role.Vet) },
            modifier = Modifier.testTag(VetPillTestTag)
        )
    }
}
private val UnselectedColor = Color(0xFFE5E5E5)
private val SelectedColor = Color(0xFF96B7B1) // vert
@Composable
private fun SelectablePill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) SelectedColor else UnselectedColor // vert / gris
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = bg,
        modifier = modifier
            .width(140.dp)
            .height(56.dp)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text, style = MaterialTheme.typography.titleMedium, color = Color.Black)
        }
    }
}

private val unfocusedFieldColor = Color(0xFFF0F7F1)
private val focusedFieldColor = Color(0xFFF0F7F1)
@Composable
private fun Field(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = unfocusedFieldColor,
            focusedContainerColor = focusedFieldColor,
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
