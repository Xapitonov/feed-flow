package com.prof18.feedflow.addfeed

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.prof18.feedflow.desktopViewModel
import com.prof18.feedflow.di.DI
import com.prof18.feedflow.domain.model.FeedAddedState
import com.prof18.feedflow.presentation.AddFeedViewModel
import com.prof18.feedflow.ui.addfeed.AddFeedsContent
import com.prof18.feedflow.ui.style.FeedFlowTheme

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

    AddFeedScreenContent(
        feedUrl = feedUrl,
        isInvalidUrl = showError,
        errorMessage = errorMessage,
        snackbarHostState = snackbarHostState,
        onFeedUrlUpdated = { url ->
            feedUrl = url
            viewModel.updateFeedUrlTextFieldValue(url)
        },
        addFeed = {
            viewModel.addFeed()
        },
    )
}

@Composable
private fun AddFeedScreenContent(
    feedUrl: String,
    isInvalidUrl: Boolean,
    errorMessage: String,
    snackbarHostState: SnackbarHostState,
    onFeedUrlUpdated: (String) -> Unit,
    addFeed: () -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        AddFeedsContent(
            paddingValues = paddingValues,
            feedUrl = feedUrl,
            showError = isInvalidUrl,
            errorMessage = errorMessage,
            onFeedUrlUpdated = onFeedUrlUpdated,
            addFeed = addFeed,
        )
    }
}

@Preview
@Composable
private fun AddScreenContentPreview() {
    FeedFlowTheme {
        AddFeedScreenContent(
            feedUrl = "https://www.ablog.com/feed",
            isInvalidUrl = false,
            errorMessage = "",
            snackbarHostState = SnackbarHostState(),
            onFeedUrlUpdated = {},
            addFeed = { },
        )
    }
}

@Preview
@Composable
private fun AddScreenContentDarkPreview() {
    FeedFlowTheme(
        darkTheme = true,
    ) {
        AddFeedScreenContent(
            feedUrl = "https://www.ablog.com/feed",
            isInvalidUrl = false,
            errorMessage = "",
            snackbarHostState = SnackbarHostState(),
            onFeedUrlUpdated = {},
            addFeed = { },
        )
    }
}

@Preview
@Composable
private fun AddScreenContentInvalidUrlPreview() {
    FeedFlowTheme {
        AddFeedScreenContent(
            feedUrl = "https://www.ablog.com/feed",
            isInvalidUrl = true,
            errorMessage = "The link you provided is not a valid RSS feed",
            snackbarHostState = SnackbarHostState(),
            onFeedUrlUpdated = {},
            addFeed = { },
        )
    }
}
