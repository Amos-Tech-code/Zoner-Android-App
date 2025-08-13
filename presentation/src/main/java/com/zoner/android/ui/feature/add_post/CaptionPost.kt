package com.zoner.android.ui.feature.add_post

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.zoner.android.ui.designSystem.TagsField
import com.zoner.android.ui.designSystem.ZonerDropdownSelector
import com.zoner.android.ui.designSystem.ZonerSpacer
import com.zoner.android.ui.designSystem.ZonerTextField
import com.zoner.android.ui.theme.ZonerSuccess


@Composable
fun CaptionPost(
    modifier: Modifier = Modifier,
    description: String,
    category: String,
    location: String,
    price: String?,
    tags: List<String>,
    onCategoryChange: (String) -> Unit,
    onLocationChange: () -> Unit,
    onPriceChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTagsChange: (List<String>) -> Unit,
    onPreviewClick: () -> Unit,
    onPostClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Go back"
                )
            }
            Text("CAPTION POST", style = MaterialTheme.typography.titleLarge)
        }
        ZonerTextField(
            text = "Description",
            value = description,
            onValueChange = onDescriptionChange,
            hint = "Write a brief description of your post...",
            singleLine = false
        )
        ZonerSpacer(16.dp)
        ZonerDropdownSelector(
            label = "Category",
            selectedOption = category,
            options = listOf("Salon", "Services", "Foods", "Fashion"),
            hint = "Select Category",
            onOptionSelected = onCategoryChange
        )
        ZonerSpacer(16.dp)
        Column {
            Text(text = "Posting from", fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = location)
                Spacer(modifier = Modifier.weight(1f))
                AssistChip(
                    onClick = { onLocationChange() },
                    label = { Text("Change") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
        ZonerSpacer(16.dp)
        ZonerTextField(
            text = "Price (Optional)",
            value = price ?: "",
            onValueChange = onPriceChange,
            hint = "Set price for your post item",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )
        )
        ZonerSpacer(16.dp)
        Text(
            text = "Tags ${tags.size}/30",
            fontWeight = FontWeight.SemiBold,
            color = if (tags.size >= 30) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        TagsField(
            tags = tags,
            onTagsChange = onTagsChange,
            modifier = Modifier.fillMaxWidth()
        )
        ZonerSpacer(16.dp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = onPreviewClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                Text(text = "Preview")
            }
            ZonerSpacer(16.dp)
            Button(
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ZonerSuccess
                ),
                contentPadding = PaddingValues(horizontal = 24.dp),
                onClick = onPostClick
            ) {
                Text(text = "Post")
            }
        }
    }
}