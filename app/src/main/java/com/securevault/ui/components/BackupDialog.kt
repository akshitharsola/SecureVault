// app/src/main/java/com/securevault/ui/components/BackupDialog.kt
package com.securevault.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Dialog for creating encrypted backups with sharing options
 */
@Composable
fun BackupDialog(
    onDismiss: () -> Unit,
    onBackup: (password: String, deleteAfterBackup: Boolean) -> Unit,
    isLoading: Boolean = false,
    backupLocationInfo: String? = null
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var deleteAfterBackup by remember { mutableStateOf(false) }
    var showAdvancedOptions by remember { mutableStateOf(false) }

    val passwordsMatch = password == confirmPassword
    val passwordValid = password.length >= 6
    val canProceed = passwordValid && passwordsMatch && password.isNotBlank() && !isLoading

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = {
            Text(
                text = "Create Backup",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Create an encrypted backup of all your passwords. The backup will be saved to Downloads/SecureVault_Backups where you can access it through your file manager.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Password input
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Backup Password") },
                    placeholder = { Text("Enter backup password") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    isError = password.isNotEmpty() && !passwordValid,
                    supportingText = {
                        if (password.isNotEmpty() && !passwordValid) {
                            Text(
                                text = "Password must be at least 6 characters",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                // Confirm password input
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    placeholder = { Text("Confirm backup password") },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    isError = confirmPassword.isNotEmpty() && !passwordsMatch,
                    supportingText = {
                        if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                            Text(
                                text = "Passwords do not match",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                // Advanced options toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showAdvancedOptions = !showAdvancedOptions },
                        enabled = !isLoading
                    ) {
                        Text(if (showAdvancedOptions) "▲ Advanced Options" else "▼ Advanced Options")
                    }
                }

                // Advanced options
                if (showAdvancedOptions) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Delete after backup option
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = deleteAfterBackup,
                                    onCheckedChange = { deleteAfterBackup = it },
                                    enabled = !isLoading
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Delete all passwords after backup",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            if (deleteAfterBackup) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "⚠️ Warning: This will permanently delete all passwords from this device after creating the backup.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            // Backup location info
                            if (backupLocationInfo != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Backup Location:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = backupLocationInfo,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (isLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Creating backup...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onBackup(password, deleteAfterBackup) },
                enabled = canProceed
            ) {
                Text("Create Backup")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Dialog for restoring from encrypted backups
 */
@Composable
fun RestoreDialog(
    onDismiss: () -> Unit,
    onSelectFile: () -> Unit,
    onRestore: (password: String, replaceAll: Boolean) -> Unit,
    selectedFileName: String? = null,
    isLoading: Boolean = false
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var replaceAll by remember { mutableStateOf(true) }

    val canProceed = password.isNotBlank() && selectedFileName != null && !isLoading

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = {
            Text(
                text = "Restore from Backup",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Select a backup file from Downloads/SecureVault_Backups or browse for other backup files, then enter the password used to encrypt it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // File selection
                OutlinedButton(
                    onClick = onSelectFile,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = selectedFileName ?: "📁 Select Backup File",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (selectedFileName != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Selected File:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = selectedFileName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            TextButton(
                                onClick = onSelectFile,
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Text("Change", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                // Password input
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Backup Password") },
                    placeholder = { Text("Enter backup password") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                // Replace all option
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = replaceAll,
                        onCheckedChange = { replaceAll = it },
                        enabled = !isLoading
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Replace all existing passwords",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = if (replaceAll) "All current passwords will be deleted" else "Backup passwords will be merged with existing ones",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (replaceAll) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚠️ Warning: This will permanently delete all current passwords and replace them with the backup.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                if (isLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Restoring backup...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onRestore(password, replaceAll) },
                enabled = canProceed
            ) {
                Text("🔄 Restore")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        }
    )
}