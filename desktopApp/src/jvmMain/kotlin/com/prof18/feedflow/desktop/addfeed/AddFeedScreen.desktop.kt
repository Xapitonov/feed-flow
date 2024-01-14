package com.prof18.feedflow.desktop.addfeed

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.prof18.feedflow.desktop.desktopViewModel
import com.prof18.feedflow.desktop.di.DI
import com.prof18.feedflow.shared.domain.model.FeedAddedState
import com.prof18.feedflow.shared.presentation.AddFeedViewModel
import com.prof18.feedflow.shared.presentation.preview.categoriesExpandedState
import com.prof18.feedflow.shared.ui.addfeed.AddFeedContent
import com.prof18.feedflow.shared.ui.theme.FeedFlowTheme

@Composable
fun AddFeedScreen(
    onFeedAdded: () -> Unit,
) {
    var feedUrl by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val viewModel = desktopViewModel { DI.koin.get<AddFeedViewModel>() }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.feedAddedState.collect { feedAddedState ->
            when (feedAddedState) {
                is FeedAddedState.Error -> {
                    showError = true
                    errorMessage = feedAddedState.errorMessage.localized()
                }

                is FeedAddedState.FeedAdded -> {
                    val message = feedAddedState.message.localized()

                    snackbarHostState.showSnackbar(
                        message,
                        duration = SnackbarDuration.Short,
                    )

                    feedUrl = ""
                    onFeedAdded()
                }

                FeedAddedState.FeedNotAdded -> {
                    showError = false
                    errorMessage = ""
                }
            }
        }
    }

    val categoriesState by viewModel.categoriesState.collectAsState()

    AddFeedContent(
        feedUrl = feedUrl,
        showError = showError,
        errorMessage = errorMessage,
        onFeedUrlUpdated = { url ->
            feedUrl = url
            viewModel.updateFeedUrlTextFieldValue(url)
        },
        addFeed = {
            viewModel.addFeed()
        },
        categoriesState = categoriesState,
        onExpandClick = {
            viewModel.onExpandCategoryClick()
        },
        onAddCategoryClick = { categoryName ->
            viewModel.addNewCategory(categoryName)
        },
        onDeleteCategoryClick = { categoryId ->
            viewModel.deleteCategory(categoryId.value)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    )
}

@Preview
@Composable
private fun AddScreenContentPreview() {
    FeedFlowTheme {
        AddFeedContent(
            feedUrl = "https://www.ablog.com/feed",
            showError = false,
            errorMessage = "",
            snackbarHost = { SnackbarHost(SnackbarHostState()) },
            categoriesState = categoriesExpandedState,
            onFeedUrlUpdated = {},
            addFeed = { },
            onExpandClick = {},
            onAddCategoryClick = {},
            onDeleteCategoryClick = {},
        )
    }
}
