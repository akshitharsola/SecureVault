// app/src/main/java/com/securevault/ui/screens/form/FormScreen.kt
package com.securevault.ui.screens.form

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.securevault.ui.viewmodels.PasswordViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(
    passwordId: String?,
    navController: NavController
) {
    val context = LocalContext.current

    val viewModel: FormViewModel = viewModel(
        factory = PasswordViewModelFactory(
            application = context.applicationContext as Application,
            passwordId = passwordId
        )
    )

    val title by viewModel.title.collectAsState()
    val username by viewModel.username.collectAsState()
    val password by viewModel.password.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val isPasswordVisible by viewModel.isPasswordVisible.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isEditMode by viewModel.isEditMode.collectAsState()
    val error by viewModel.error.collectAsState()

    val titleError by viewModel.titleError.collectAsState()
    val usernameError by viewModel.usernameError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()

    // Handle errors
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isEditMode) Icons.Default.EditNote else Icons.Default.AddCircle,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isEditMode) "Edit Password" else "Add Password")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.savePassword {
                        // Navigate back on success
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .imePadding() // Handle keyboard overlap
                    .navigationBarsPadding(), // Handle navigation bar
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Save",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        // Remove contentWindowInsets to prevent double insets
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->

        // Scrollable content with proper keyboard handling
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()) // Make content scrollable
                .padding(16.dp)
                .imePadding(), // Add padding when keyboard appears
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Title field with enhanced icon
            OutlinedTextField(
                value = title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("Title") },
                placeholder = { Text("e.g., Gmail, Bank Account, etc.") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Title,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                isError = titleError != null,
                supportingText = { titleError?.let { Text(it) } },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )

            // Username field with enhanced icon
            OutlinedTextField(
                value = username,
                onValueChange = { viewModel.updateUsername(it) },
                label = { Text("Username") },
                placeholder = { Text("Enter username or email") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                isError = usernameError != null,
                supportingText = { usernameError?.let { Text(it) } },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )

            // Password field with enhanced icons
            OutlinedTextField(
                value = password,
                onValueChange = { viewModel.updatePassword(it) },
                label = { Text("Password") },
                placeholder = { Text("Enter secure password") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Key,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                        Icon(
                            if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.RemoveRedEye,
                            contentDescription = if (isPasswordVisible) "Hide Password" else "Show Password",
                            tint = if (isPasswordVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                isError = passwordError != null,
                supportingText = { passwordError?.let { Text(it) } },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )

            // Password strength indicator (visual enhancement)
            if (password.isNotEmpty()) {
                val strength = calculatePasswordStrength(password)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (strength) {
                            "Weak" -> MaterialTheme.colorScheme.errorContainer
                            "Medium" -> MaterialTheme.colorScheme.tertiaryContainer
                            "Strong" -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            when (strength) {
                                "Weak" -> Icons.Default.Warning
                                "Medium" -> Icons.Default.Info
                                "Strong" -> Icons.Default.Shield
                                else -> Icons.Default.QuestionMark
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = when (strength) {
                                "Weak" -> MaterialTheme.colorScheme.error
                                "Medium" -> MaterialTheme.colorScheme.tertiary
                                "Strong" -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Password Strength: $strength",
                            style = MaterialTheme.typography.bodySmall,
                            color = when (strength) {
                                "Weak" -> MaterialTheme.colorScheme.onErrorContainer
                                "Medium" -> MaterialTheme.colorScheme.onTertiaryContainer
                                "Strong" -> MaterialTheme.colorScheme.onPrimaryContainer
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }

            // Notes field with enhanced icon - Fixed height, scrollable
            OutlinedTextField(
                value = notes,
                onValueChange = { viewModel.updateNotes(it) },
                label = { Text("Notes (Optional)") },
                placeholder = { Text("Add any additional notes here...") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp), // Fixed height instead of weight(1f)
                maxLines = 4, // Limit lines to prevent taking too much space
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )

            // Form tips card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Lightbulb,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Security Tips",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Use unique passwords for each account\n• Include uppercase, lowercase, numbers, and symbols\n• Avoid personal information in passwords",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Add some bottom padding for better spacing with FAB
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// Simple password strength calculator
private fun calculatePasswordStrength(password: String): String {
    var score = 0

    // Length check
    when {
        password.length >= 12 -> score += 2
        password.length >= 8 -> score += 1
    }

    // Character variety checks
    if (password.any { it.isUpperCase() }) score += 1
    if (password.any { it.isLowerCase() }) score += 1
    if (password.any { it.isDigit() }) score += 1
    if (password.any { !it.isLetterOrDigit() }) score += 1

    return when {
        score >= 5 -> "Strong"
        score >= 3 -> "Medium"
        score >= 1 -> "Weak"
        else -> "Very Weak"
    }
}