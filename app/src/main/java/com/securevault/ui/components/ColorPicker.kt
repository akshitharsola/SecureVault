// app/src/main/java/com/securevault/ui/components/ColorPicker.kt
package com.securevault.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ColorPicker(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    label: String
) {
    var showDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(selectedColor)
        )
    }

    if (showDialog) {
        ColorPickerDialog(
            initialColor = selectedColor,
            onDismiss = { showDialog = false },
            onColorSelected = {
                onColorSelected(it)
                showDialog = false
            }
        )
    }
}

@Composable
fun ColorPickerDialog(
    initialColor: Color,
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    var currentColor by remember { mutableStateOf(initialColor) }
    var red by remember { mutableStateOf(initialColor.red) }
    var green by remember { mutableStateOf(initialColor.green) }
    var blue by remember { mutableStateOf(initialColor.blue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Color") },
        text = {
            Column {
                // Color preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(currentColor)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // RGB Sliders
                Text("Red: ${(red * 255).toInt()}")
                Slider(
                    value = red,
                    onValueChange = {
                        red = it
                        currentColor = Color(red, green, blue)
                    }
                )

                Text("Green: ${(green * 255).toInt()}")
                Slider(
                    value = green,
                    onValueChange = {
                        green = it
                        currentColor = Color(red, green, blue)
                    }
                )

                Text("Blue: ${(blue * 255).toInt()}")
                Slider(
                    value = blue,
                    onValueChange = {
                        blue = it
                        currentColor = Color(red, green, blue)
                    }
                )

                // Preset colors
                Text("Preset Colors", style = MaterialTheme.typography.titleSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PresetColorButton(Color.White, currentColor, onColorSelected = {
                        currentColor = it
                        red = it.red
                        green = it.green
                        blue = it.blue
                    })
                    PresetColorButton(Color.Black, currentColor, onColorSelected = {
                        currentColor = it
                        red = it.red
                        green = it.green
                        blue = it.blue
                    })
                    PresetColorButton(Color.Blue, currentColor, onColorSelected = {
                        currentColor = it
                        red = it.red
                        green = it.green
                        blue = it.blue
                    })
                    PresetColorButton(Color.Red, currentColor, onColorSelected = {
                        currentColor = it
                        red = it.red
                        green = it.green
                        blue = it.blue
                    })
                    PresetColorButton(Color.Green, currentColor, onColorSelected = {
                        currentColor = it
                        red = it.red
                        green = it.green
                        blue = it.blue
                    })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onColorSelected(currentColor) }) {
                Text("Select")
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
private fun PresetColorButton(
    color: Color,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .clickable { onColorSelected(color) }
            .then(
                if (color == selectedColor) {
                    Modifier.border(2.dp, Color.Gray, CircleShape)
                } else {
                    Modifier
                }
            )
    )
}