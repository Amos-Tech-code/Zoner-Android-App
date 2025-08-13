package com.zoner.android.ui.designSystem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun ZonerButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary
) {

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor
        ),
        contentPadding = PaddingValues(12.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall
        )
    }
}

@Composable
fun ZonerTextLink(
    modifier: Modifier = Modifier,
    text: String,
    textColor : Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {

    Text(
        text = text,
        modifier = modifier
            .clickable { onClick()  },
        style = MaterialTheme.typography.titleSmall,
        color = textColor,
        textAlign = TextAlign.Center
    )
}


@Composable
fun DropdownMenuButton(
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.wrapContentSize(Alignment.TopStart)) {
        OutlinedButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Text(
                selectedOption, maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Dropdown",
                modifier = Modifier.size(16.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.width(IntrinsicSize.Max)
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    onClick = {
                        onOptionSelected(index)
                        expanded = false
                    },
                    text = {
                        Text(
                            text = option,
                            style = if (option == selectedOption) {
                                MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            } else {
                                MaterialTheme.typography.bodySmall
                            }
                        )
                    },
                )
            }
        }
    }
}