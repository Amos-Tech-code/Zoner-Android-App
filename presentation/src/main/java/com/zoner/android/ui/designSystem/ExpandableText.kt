package com.zoner.android.ui.designSystem

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

@Composable
fun ExpandableText(
    text: String,
    modifier: Modifier = Modifier,
    collapsedMaxLines: Int = 6,
    animationSpec: FiniteAnimationSpec<IntSize> = tween(300)
) {
    var isExpanded by remember { mutableStateOf(false) }
    var shouldShowSeeMore by remember { mutableStateOf(false) }

    // First, measure full text without maxLines
    SubcomposeLayout(
        modifier = modifier.animateContentSize(animationSpec)
    ) { constraints ->
        val fullTextPlaceable = subcompose("FullText") {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = Int.MAX_VALUE
            )
        }[0].measure(constraints)

        val collapsedTextPlaceable = subcompose("CollapsedText") {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = collapsedMaxLines,
                overflow = TextOverflow.Ellipsis
            )
        }[0].measure(constraints)

        // Decide whether to show the See more link
        shouldShowSeeMore = fullTextPlaceable.height > collapsedTextPlaceable.height

        val finalTextPlaceable = subcompose("FinalText") {
            Column {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = if (isExpanded) Int.MAX_VALUE else collapsedMaxLines,
                    overflow = TextOverflow.Ellipsis
                )

                if (shouldShowSeeMore) {
                    ZonerTextLink(
                        onClick = { isExpanded = !isExpanded },
                        text = if (isExpanded) "Show less" else "Show more",
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }[0].measure(constraints)

        layout(finalTextPlaceable.width, finalTextPlaceable.height) {
            finalTextPlaceable.placeRelative(0, 0)
        }
    }
}