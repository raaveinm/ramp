package com.raaveinm.chirro.ui.layouts

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect

@Composable
fun DropDownSelection(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    @SuppressLint("ModifierParameter") hazeStateModifier: Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(12.dp)
    val glassBackground = MaterialTheme.colorScheme.surface.copy(alpha = 0.05f)
    val glassBorder = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier
            .wrapContentSize()
            .wrapContentSize(Alignment.TopCenter)
            .defaultMinSize(minWidth = 96.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = hazeStateModifier
                .clip(shape)
                .background(glassBackground)
                .border(1.dp, glassBorder, shape)
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .defaultMinSize(minWidth = 96.dp)
        ) {
            Text(
                text = selectedOption,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }

        MaterialTheme(
            shapes = MaterialTheme.shapes.copy(extraSmall = shape)
        ) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                offset = DpOffset(x = 0.dp, y = 0.dp),
                shadowElevation = 0.dp,
                tonalElevation = 0.dp,
                containerColor = Color.Transparent,
                modifier = hazeStateModifier
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.1f), shape)
                    .border(1.dp, glassBorder, shape)
                    .defaultMinSize(96.dp)
            ) {
                options.forEach { option ->
                    val isSelected = option == selectedOption

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.defaultMinSize(64.dp)
                            )
                        },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        modifier = Modifier
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                            )
                            .defaultMinSize(128.dp)

                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun DropDownPreview() {
    val options = listOf("Option 1", "Option 2", "Option 3")
    var selectedOption by remember { mutableStateOf(options[1]) }

    androidx.compose.material3.Surface(
        color = Color(0xFFEFEFEF),
        modifier = Modifier.padding(50.dp)
    ) {
        val hazeState = remember { HazeState() }
        DropDownSelection(
            options = options,
            selectedOption = selectedOption,
            onOptionSelected = { },
            hazeStateModifier = Modifier.hazeEffect(hazeState)
        )
    }
}