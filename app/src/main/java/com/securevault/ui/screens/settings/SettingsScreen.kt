// app/src/main/java/com/securevault/ui/screens/settings/SettingsScreen.kt
package com.securevault.ui.screens.settings

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.securevault.di.AppModule
import com.securevault.ui.components.BackupDialog
import com.securevault.ui.components.ColorPicker
import com.securevault.ui.components.PinSetupDialog
import com.securevault.ui.components.RestoreDialog
import com.securevault.utils.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val viewModel: SettingsViewModel = viewModel()
    val context = LocalContext.current
    val biometricHelper = AppModule.provideBiometricHelper(context)

    // State variables
    val themeConfig by viewModel.themeConfig.collectAsState()
    val tempThemeConfig by viewModel.tempThemeConfig.collectAsState()
    val hasUnsavedChanges by viewModel.hasUnsavedChanges.collectAsState()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val isBackupLoading by viewModel.isBackupLoading.collectAsState()
    val isRestoreLoading by viewModel.isRestoreLoading.collectAsState()
    val selectedBackupUri by viewModel.selectedBackupUri.collectAsState()
    val selectedBackupFileName by viewModel.selectedBackupFileName.collectAsState()

    // Expandable sections state
    var isBasicThemeExpanded by remember { mutableStateOf(false) }
    var isAdvancedThemeExpanded by remember { mutableStateOf(false) }
    var isSecurityExpanded by remember { mutableStateOf(false) }
    var isBackupExpanded by remember { mutableStateOf(false) }

    // Dialog states
    var showPinSetupDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showBiometricDialog by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    // File picker launcher
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            // Extract filename from URI (simplified)
            val fileName = it.path?.substringAfterLast("/") ?: "backup.backup"
            viewModel.selectBackupFile(it, fileName)
        }
    }

    // Check if biometric is available
    val canUseBiometric = remember {
        val biometricManager = BiometricManager.from(context)
        val result = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        result == BiometricManager.BIOMETRIC_SUCCESS
    }

    // Handle messages
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    LaunchedEffect(successMessage) {
        successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearSuccessMessage()
        }
    }

    // Handle back button when there are unsaved changes
    val onBackPressed: () -> Unit = {
        if (hasUnsavedChanges) {
            showDiscardDialog = true
        } else {
            navController.popBackStack()
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
                            Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Settings")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Show unsaved changes indicator
            if (hasUnsavedChanges) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "You have unsaved theme changes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Basic Theme Section
            SettingsCard(
                title = "Basic Theme",
                icon = Icons.Default.Palette,
                isExpanded = isBasicThemeExpanded,
                onToggleExpand = { isBasicThemeExpanded = !isBasicThemeExpanded }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (tempThemeConfig.isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Dark Theme")
                    }
                    Switch(
                        checked = tempThemeConfig.isDark,
                        onCheckedChange = {
                            viewModel.toggleDarkMode()
                            // For basic theme, apply immediately
                            viewModel.applyThemeChanges(context)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Advanced Theme Section
            SettingsCard(
                title = "Advanced Theme",
                icon = Icons.Default.ColorLens,
                isExpanded = isAdvancedThemeExpanded,
                onToggleExpand = { isAdvancedThemeExpanded = !isAdvancedThemeExpanded }
            ) {
                // Color pickers now use tempThemeConfig and don't apply immediately
                ColorPicker(
                    selectedColor = tempThemeConfig.backgroundColor,
                    onColorSelected = { color ->
                        viewModel.updateBackgroundColor(color)
                    },
                    label = "Background Color"
                )

                ColorPicker(
                    selectedColor = tempThemeConfig.surfaceColor,
                    onColorSelected = { color ->
                        viewModel.updateSurfaceColor(color)
                    },
                    label = "Box/Card Color"
                )

                ColorPicker(
                    selectedColor = tempThemeConfig.primaryColor,
                    onColorSelected = { color ->
                        viewModel.updatePrimaryColor(color)
                    },
                    label = "Accent Color"
                )

                ColorPicker(
                    selectedColor = tempThemeConfig.textColor,
                    onColorSelected = { color ->
                        viewModel.updateTextColor(color)
                    },
                    label = "Text Color"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Reset Button
                    OutlinedButton(
                        onClick = { viewModel.resetTheme() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.RestartAlt,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset")
                    }

                    // Discard Changes Button
                    if (hasUnsavedChanges) {
                        OutlinedButton(
                            onClick = { viewModel.discardChanges() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                Icons.Default.Cancel,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Discard")
                        }
                    }
                }

                // Save & Apply Button
                if (hasUnsavedChanges) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            viewModel.saveAndApplyThemeChanges(context)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Save & Apply Changes")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Security Section
            SettingsCard(
                title = "Security",
                icon = Icons.Default.Security,
                isExpanded = isSecurityExpanded,
                onToggleExpand = { isSecurityExpanded = !isSecurityExpanded }
            ) {
                // Biometric toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Fingerprint,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Use Biometric Authentication")
                        }
                        if (!canUseBiometric) {
                            Text(
                                "Not available on this device",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    Switch(
                        checked = isBiometricEnabled,
                        onCheckedChange = { enabled ->
                            if (canUseBiometric) {
                                viewModel.setBiometricEnabled(enabled)
                                if (enabled) {
                                    showBiometricDialog = true
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Biometric authentication not available",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        enabled = canUseBiometric
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // PIN setup
                ListItem(
                    headlineContent = { Text("Set Fallback PIN") },
                    supportingContent = {
                        Text(if (biometricHelper.isFallbackPinSet()) "PIN is set" else "No PIN set")
                    },
                    leadingContent = {
                        Icon(Icons.Default.Pin, contentDescription = null)
                    },
                    modifier = Modifier.clickable { showPinSetupDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Backup & Restore Section
            SettingsCard(
                title = "Backup & Restore",
                icon = Icons.Default.CloudSync,
                isExpanded = isBackupExpanded,
                onToggleExpand = { isBackupExpanded = !isBackupExpanded }
            ) {
                // Create Backup
                ListItem(
                    headlineContent = { Text("Create Backup") },
                    supportingContent = { Text("Create encrypted backup of all passwords") },
                    leadingContent = {
                        if (isBackupLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.CloudUpload, contentDescription = null)
                        }
                    },
                    modifier = Modifier.clickable(enabled = !isBackupLoading && !isRestoreLoading) {
                        showBackupDialog = true
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Restore from Backup
                ListItem(
                    headlineContent = { Text("Restore from Backup") },
                    supportingContent = {
                        Text(
                            selectedBackupFileName?.let { "Selected: $it" }
                                ?: "Select backup file to restore passwords"
                        )
                    },
                    leadingContent = {
                        if (isRestoreLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.CloudDownload, contentDescription = null)
                        }
                    },
                    modifier = Modifier.clickable(enabled = !isBackupLoading && !isRestoreLoading) {
                        showRestoreDialog = true
                    }
                )

                // File selection info
                if (selectedBackupFileName != null) {
                    Spacer(modifier = Modifier.height(8.dp))
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
                            Text(
                                text = "Selected: $selectedBackupFileName",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.clearSelectedBackupFile() },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear selection",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Unsaved Changes") },
            text = { Text("You have unsaved theme changes. Do you want to discard them?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.discardChanges()
                        showDiscardDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showBiometricDialog) {
        AlertDialog(
            onDismissRequest = { showBiometricDialog = false },
            icon = {
                Icon(
                    Icons.Default.Fingerprint,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Biometric Authentication Enabled") },
            text = { Text("You will now be required to authenticate using your biometric data to access the app and view passwords.") },
            confirmButton = {
                TextButton(onClick = { showBiometricDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showPinSetupDialog) {
        PinSetupDialog(
            onDismiss = { showPinSetupDialog = false },
            onPinSet = { pin ->
                biometricHelper.setFallbackPin(pin)
                Toast.makeText(context, "Fallback PIN set successfully", Toast.LENGTH_SHORT).show()
                showPinSetupDialog = false
            }
        )
    }

    if (showBackupDialog) {
        BackupDialog(
            onDismiss = { showBackupDialog = false },
            onBackup = { password, deleteAfterBackup ->
                viewModel.createBackup(password, deleteAfterBackup)
                showBackupDialog = false
            },
            isLoading = isBackupLoading,
            backupLocationInfo = viewModel.getBackupLocationDescription()
        )
    }

    if (showRestoreDialog) {
        RestoreDialog(
            onDismiss = { showRestoreDialog = false },
            onSelectFile = {
                // Launch document picker for backup files
                documentPickerLauncher.launch(arrayOf("*/*"))
            },
            onRestore = { password, replaceAll ->
                viewModel.restoreBackup(password, replaceAll)
                showRestoreDialog = false
            },
            selectedFileName = selectedBackupFileName,
            isLoading = isRestoreLoading
        )
    }
}

@Composable
fun SettingsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onToggleExpand,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    content()
                }
            }
        }
    }
}