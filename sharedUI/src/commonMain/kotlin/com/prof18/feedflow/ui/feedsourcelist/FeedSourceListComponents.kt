package com.prof18.feedflow.ui.feedsourcelist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.prof18.feedflow.MR
import com.prof18.feedflow.core.model.CategoryId
import com.prof18.feedflow.core.model.FeedSource
import com.prof18.feedflow.core.model.FeedSourceState
import com.prof18.feedflow.ui.style.Spacing
import dev.icerock.moko.resources.compose.stringResource

internal expect fun Modifier.feedSourceMenuClickModifier(onLongClick: () -> Unit): Modifier

@Composable
fun FeedSourcesWithCategoryList(
    modifier: Modifier = Modifier,
    feedSourceState: List<FeedSourceState>,
    onExpandClicked: (CategoryId?) -> Unit,
    onDeleteFeedSourceClick: (FeedSource) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(Spacing.regular),
    ) {
        items(feedSourceState) { feedSourceState ->
            Column {
                @Suppress("MagicNumber")
                val degrees by animateFloatAsState(
                    if (feedSourceState.isExpanded) {
                        -90f
                    } else {
                        90f
                    },
                )
                Row(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.medium)
                        .clickable {
                            onExpandClicked(feedSourceState.categoryId)
                        }
                        .fillMaxWidth()
                        .padding(vertical = Spacing.regular),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    val headerText = if (feedSourceState.categoryName != null) {
                        requireNotNull(feedSourceState.categoryName)
                    } else {
                        stringResource(resource = MR.strings.no_category)
                    }

                    Text(
                        text = headerText,
                        style = MaterialTheme.typography.titleMedium,
                    )

                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.rotate(degrees),
                    )
                }

                FeedSourcesList(
                    feedSourceState = feedSourceState,
                    onDeleteFeedSourceClick = onDeleteFeedSourceClick,
                )
            }
        }
    }
}

@Composable
private fun FeedSourcesList(
    feedSourceState: FeedSourceState,
    onDeleteFeedSourceClick: (FeedSource) -> Unit,
) {
    AnimatedVisibility(
        visible = feedSourceState.isExpanded,
        enter = expandVertically(
            spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = IntSize.VisibilityThreshold,
            ),
        ),
        exit = shrinkVertically(),
    ) {
        Column {
            feedSourceState.feedSources.forEachIndexed { index, feedSource ->

                FeedSourceItem(
                    feedSource = feedSource,
                    onDeleteFeedSourceClick = onDeleteFeedSourceClick,
                )

                if (index < feedSourceState.feedSources.size - 1) {
                    Divider(
                        modifier = Modifier,
                        thickness = 0.2.dp,
                        color = Color.Gray,
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedSourceItem(
    feedSource: FeedSource,
    onDeleteFeedSourceClick: (FeedSource) -> Unit,
) {
    var showFeedMenu by remember {
        mutableStateOf(
            false,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .feedSourceMenuClickModifier(
                onLongClick = {
                    showFeedMenu = true
                },
            ),
    ) {
        Text(
            modifier = Modifier
                .padding(top = Spacing.small),
            text = feedSource.title,
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            modifier = Modifier
                .padding(top = Spacing.xsmall)
                .padding(bottom = Spacing.small),
            text = feedSource.url,
            style = MaterialTheme.typography.labelLarge,
        )

        FeedSourceContextMenu(
            showFeedMenu = showFeedMenu,
            hideMenu = {
                showFeedMenu = false
            },
            onDeleteFeedSourceClick = onDeleteFeedSourceClick,
            feedSource = feedSource,
        )
    }
}

@Composable
private fun FeedSourceContextMenu(
    showFeedMenu: Boolean,
    hideMenu: () -> Unit,
    onDeleteFeedSourceClick: (FeedSource) -> Unit,
    feedSource: FeedSource,
) {
    DropdownMenu(
        expanded = showFeedMenu,
        onDismissRequest = hideMenu,
        properties = PopupProperties(),
    ) {
        DropdownMenuItem(
            text = {
                Text(
                    stringResource(resource = MR.strings.delete_feed),
                )
            },
            onClick = {
                onDeleteFeedSourceClick(feedSource)
                hideMenu()
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedSourceNavBar(
    navigateBack: () -> Unit,
    onAddFeedSourceClick: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(stringResource(resource = MR.strings.feeds_title))
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    navigateBack()
                },
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                )
            }
        },
        actions = {
            IconButton(
                onClick = {
                    onAddFeedSourceClick()
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                )
            }
        },
    )
}

@Composable
fun NoFeedSourcesView(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
    ) {
        Text(
            modifier = Modifier
                .padding(Spacing.regular),
            text = stringResource(resource = MR.strings.no_feeds_add_one_message),
        )
    }
}
