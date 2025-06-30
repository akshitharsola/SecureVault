// app/src/main/java/com/securevault/ui/components/PinDialog.kt
package com.securevault.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun PinSetupDialog(
    onDismiss: () -> Unit,
    onPinSet: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Fallback PIN") },
        text = {
            Column {
                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        if (it.length <= 6) pin = it
                        error = null
                    },
                    label = { Text("Enter PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = {
                        if (it.length <= 6) confirmPin = it
                        error = null
                    },
                    label = { Text("Confirm PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )

                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when {
                        pin.length < 4 -> error = "PIN must be at least 4 digits"
                        pin != confirmPin -> error = "PINs do not match"
                        else -> onPinSet(pin)
                    }
                }
            ) {
                Text("Set PIN")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PinEntryDialog(
    onDismiss: () -> Unit,
    onPinEntered: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter PIN") },
        text = {
            Column {
                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        if (it.length <= 6) pin = it
                        error = null
                    },
                    label = { Text("Enter your PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )

                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (pin.length < 4) {
                        error = "PIN must be at least 4 digits"
                    } else {
                        onPinEntered(pin)
                    }
                }
            ) {
                Text("Verify")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}