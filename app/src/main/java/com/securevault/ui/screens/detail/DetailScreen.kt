// app/src/main/java/com/securevault/ui/screens/detail/DetailScreen.kt
package com.securevault.ui.screens.detail

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.securevault.di.AppModule
import com.securevault.ui.navigation.Screen
import com.securevault.ui.viewmodels.PasswordViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    passwordId: String,
    navController: NavController
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    val viewModel: DetailViewModel = viewModel(
        factory = PasswordViewModelFactory(
            application = context.applicationContext as Application,
            passwordId = passwordId
        )
    )

    val password by viewModel.password.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isPasswordVisible by viewModel.isPasswordVisible.collectAsState()
    val error by viewModel.error.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }

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
                title = { Text(password?.title ?: "Password Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        password?.id?.let { id ->
                            navController.navigate(Screen.Form.createRoute(id))
                        }
                    }) {
                        Icon(Icons.Default.EditNote, contentDescription = "Edit") // More distinctive edit icon
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.DeleteForever, contentDescription = "Delete") // More distinctive delete icon
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                password?.let { pwd ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Username section with enhanced icon
                        DetailsSection(
                            title = "Username",
                            content = pwd.username,
                            onCopyClick = {
                                viewModel.copyToClipboard("Username", pwd.username)
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                        // Password section with biometric authentication
                        PasswordSection(
                            password = pwd.password,
                            isVisible = isPasswordVisible,
                            onToggleVisibility = {
                                if (!isPasswordVisible) {
                                    // Always authenticate when trying to show password
                                    activity?.let { fragmentActivity ->
                                        viewModel.authenticateAndShowPassword(
                                            activity = fragmentActivity,
                                            onError = { errorMsg ->
                                                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                } else {
                                    // Just hide the password without authentication
                                    viewModel.hidePassword()
                                }
                            },
                            onCopyClick = {
                                viewModel.copyToClipboard("Password", pwd.password)
                            }
                        )

                        if (pwd.notes.isNotEmpty()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                            // Notes section with enhanced icon
                            NotesSection(
                                notes = pwd.notes
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Last updated info with enhanced icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Last updated: ${formatDate(pwd.updatedAt)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }

        // Delete confirmation dialog with enhanced styling
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                icon = {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = { Text("Delete Password") },
                text = { Text("Are you sure you want to delete this password? This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteDialog = false
                            // Use biometric authentication for password deletion
                            activity?.let { fragmentActivity ->
                                viewModel.authenticateAndDeletePassword(
                                    activity = fragmentActivity,
                                    onSuccess = {
                                        navController.popBackStack()
                                    },
                                    onError = { errorMsg ->
                                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            } ?: run {
                                // Fallback if activity isn't available
                                viewModel.deletePassword {
                                    navController.popBackStack()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun DetailsSection(
    title: String,
    content: String,
    onCopyClick: () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onCopyClick) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Username")
                }
            }
        }
    }
}

@Composable
fun PasswordSection(
    password: String,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit,
    onCopyClick: () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Key,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Password",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isVisible)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else
                    MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = if (isVisible) password else "••••••••••••",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                    fontFamily = if (isVisible) androidx.compose.ui.text.font.FontFamily.Monospace else androidx.compose.ui.text.font.FontFamily.Default
                )
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        if (isVisible) Icons.Default.VisibilityOff else Icons.Default.RemoveRedEye,
                        contentDescription = if (isVisible) "Hide Password" else "Show Password",
                        tint = if (isVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                IconButton(onClick = onCopyClick) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Password")
                }
            }
        }
    }
}

@Composable
fun NotesSection(notes: String) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Notes",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Text(
                text = notes,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

// Utility function to format date
private fun formatDate(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val format = java.text.SimpleDateFormat("MMM dd, yyyy hh:mm a", java.util.Locale.getDefault())
    return format.format(date)
}