package com.zoner.android.ui.designSystem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ZonerTextField(
    modifier: Modifier = Modifier,
    text: String? = null,
    hint: String,
    isInputSecret: Boolean = false,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource? = null,
    shape: Shape = RoundedCornerShape(10.dp),
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        cursorColor = MaterialTheme.colorScheme.primary,
        focusedBorderColor = MaterialTheme.colorScheme.outline,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(0.4f)
    )
) {

    var isPasswordVisible by rememberSaveable {
        mutableStateOf(false)
    }

    Column(modifier = modifier) {
        text?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = if (label != null)  { {
                Text(text = label)
            }} else null,
            placeholder = {
               Text(text = hint, style = MaterialTheme.typography.bodyLarge)
            },
            visualTransformation = if (isInputSecret && !isPasswordVisible) {
                PasswordVisualTransformation(mask = '*')
            } else VisualTransformation.None,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            minLines = minLines,
            maxLines = maxLines,
            prefix = prefix,
            suffix = suffix,
            enabled = enabled,
            supportingText = supportingText,
            isError = isError,
            readOnly = readOnly,
            textStyle = textStyle.copy(fontWeight = FontWeight.SemiBold),
            interactionSource = interactionSource,
            colors = colors,
            shape = shape,
            singleLine = singleLine,
            leadingIcon = if(leadingIcon != null) {
                {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null
                    )
                }
            } else null,
            trailingIcon = if (isInputSecret) {
                {
                    IconButton(
                        onClick = { isPasswordVisible = !isPasswordVisible }
                    ) {
                        when {
                            isPasswordVisible -> {
                                Icon(
                                    imageVector = Icons.Rounded.VisibilityOff,
                                    contentDescription = "Hide Password"
                                )
                            }

                            !isPasswordVisible -> {
                                Icon(
                                    imageVector = Icons.Rounded.Visibility,
                                    contentDescription = "Show Password"
                                )
                            }
                        }
                    }
                }
            } else trailingIcon
        )
    }

}


@Composable
fun ZonerSelectorTextField(
    modifier: Modifier = Modifier,
    text: String? = null,
    value: String,
    hint: String,
    onClick: () -> Unit,
    trailingIcon: @Composable (() -> Unit)? = {
        Icon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = "Select",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp)
        )
    }
) {
    Column(modifier = modifier) {
        // This makes the field behave like a dropdown
        text?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            enabled = false, // Disable editing
            readOnly = true,
            textStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            placeholder = { Text(hint, style = MaterialTheme.typography.bodyLarge) },
            trailingIcon = trailingIcon,
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}


@Composable
fun ZonerDropdownSelector(
    modifier: Modifier = Modifier,
    label: String? = null,
    selectedOption: String?,
    options: List<String>,
    hint: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        label?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        OutlinedTextField(
            value = selectedOption ?: "",
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            enabled = false,
            readOnly = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            placeholder = { Text(hint, style = MaterialTheme.typography.bodyLarge) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Select",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            },
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 4.dp)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    contentPadding = PaddingValues(horizontal = 32.dp),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}



@Composable
fun TagsField(
    tags: List<String>,
    onTagsChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTag by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier) {
        // Display existing tags as chips
        if (tags.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                tags.forEach { tag ->
                    InputChip(
                        selected = false,
                        onClick = {
                            // Remove tag when clicked
                            onTagsChange(tags.filter { it != tag })
                        },
                        label = { Text("#$tag") },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove tag",
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = InputChipDefaults.inputChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Tag input field
        OutlinedTextField(
            value = currentTag,
            onValueChange = { currentTag = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Add tags") },
            placeholder = { Text("Type and press + to add") },
            trailingIcon = {
                if (currentTag.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            addTag(currentTag, tags, onTagsChange)
                            currentTag = ""
                        }
                    ) {
                        Icon(Icons.Default.Add, "Add tag")
                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    addTag(currentTag, tags, onTagsChange)
                    currentTag = ""
                    focusManager.clearFocus()
                }
            ),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedBorderColor = MaterialTheme.colorScheme.outline,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(0.4f)
            )
        )
    }
}

private fun addTag(tag: String, currentTags: List<String>, onTagsChange: (List<String>) -> Unit) {
    val cleanedTag = tag.trim()
        .replace(Regex("[^a-zA-Z0-9]"), "") // Remove special characters
        .lowercase()

    if (cleanedTag.isNotEmpty() && !currentTags.contains(cleanedTag)) {
        onTagsChange(currentTags + cleanedTag)
    }
}

