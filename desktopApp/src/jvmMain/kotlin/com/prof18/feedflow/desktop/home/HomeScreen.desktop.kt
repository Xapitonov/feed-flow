package com.prof18.feedflow.desktop.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.awt.ComposeWindow
import com.prof18.feedflow.desktop.home.bywindowsize.CompactView
import com.prof18.feedflow.desktop.home.bywindowsize.ExpandedView
import com.prof18.feedflow.desktop.home.bywindowsize.MediumView
import com.prof18.feedflow.desktop.home.components.NoFeedsDialog
import com.prof18.feedflow.desktop.utils.WindowWidthSizeClass
import com.prof18.feedflow.desktop.utils.calculateWindowSizeClass
import com.prof18.feedflow.shared.presentation.HomeViewModel

@Composable
internal fun HomeScreen(
    window: ComposeWindow,
    paddingValues: PaddingValues,
    homeViewModel: HomeViewModel,
    snackbarHostState: SnackbarHostState,
    listState: LazyListState,
    onAddFeedClick: () -> Unit,
    onImportExportClick: () -> Unit,
) {
    val loadingState by homeViewModel.loadingState.collectAsState()
    val feedState by homeViewModel.feedState.collectAsState()
    val navDrawerState by homeViewModel.navDrawerState.collectAsState()
    val currentFeedFilter by homeViewModel.currentFeedFilter.collectAsState()
    val unReadCount by homeViewModel.unreadCountFlow.collectAsState(initial = 0)

    LaunchedEffect(Unit) {
        homeViewModel.errorState.collect { errorState ->
            snackbarHostState.showSnackbar(
                errorState!!.message.localized(),
                duration = SnackbarDuration.Short,
            )
        }
    }

    val windowSize = calculateWindowSizeClass(window)

    var showDialog by remember { mutableStateOf(false) }
    NoFeedsDialog(
        showDialog = showDialog,
        onDismissRequest = {
            showDialog = false
        },
        onAddFeedClick = onAddFeedClick,
        onImportExportClick = onImportExportClick,
    )

    when (windowSize.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            CompactView(
                feedItems = feedState,
                navDrawerState = navDrawerState,
                unReadCount = unReadCount,
                currentFeedFilter = currentFeedFilter,
                paddingValues = paddingValues,
                loadingState = loadingState,
                lazyListState = listState,
                onAddFeedClick = {
                    showDialog = true
                },
                onFeedFilterSelected = { feedFilter ->
                    homeViewModel.onFeedFilterSelected(feedFilter)
                },
                refreshData = {
                    homeViewModel.getNewFeeds()
                },
                requestNewData = {
                    homeViewModel.requestNewFeedsPage()
                },
                markAsReadOnScroll = { lastVisibleIndex ->
                    homeViewModel.markAsReadOnScroll(lastVisibleIndex)
                },
                markAsRead = { feedItemId ->
                    homeViewModel.markAsRead(feedItemId.id)
                },
            )
        }

        WindowWidthSizeClass.Medium -> {
            MediumView(
                navDrawerState = navDrawerState,
                currentFeedFilter = currentFeedFilter,
                paddingValues = paddingValues,
                loadingState = loadingState,
                feedItems = feedState,
                lazyListState = listState,
                unReadCount = unReadCount,
                onAddFeedClick = {
                    showDialog = true
                },
                onFeedFilterSelected = { feedFilter ->
                    homeViewModel.onFeedFilterSelected(feedFilter)
                },
                refreshData = {
                    homeViewModel.getNewFeeds()
                },
                requestNewData = {
                    homeViewModel.requestNewFeedsPage()
                },
                markAsReadOnScroll = { lastVisibleIndex ->
                    homeViewModel.markAsReadOnScroll(lastVisibleIndex)
                },
                markAsRead = { feedItemId ->
                    homeViewModel.markAsRead(feedItemId.id)
                },
            )
        }

        WindowWidthSizeClass.Expanded -> {
            ExpandedView(
                navDrawerState = navDrawerState,
                currentFeedFilter = currentFeedFilter,
                paddingValues = paddingValues,
                loadingState = loadingState,
                feedItems = feedState,
                lazyListState = listState,
                unReadCount = unReadCount,
                onAddFeedClick = {
                    showDialog = true
                },
                onFeedFilterSelected = { feedFilter ->
                    homeViewModel.onFeedFilterSelected(feedFilter)
                },
                refreshData = {
                    homeViewModel.getNewFeeds()
                },
                requestNewData = {
                    homeViewModel.requestNewFeedsPage()
                },
                markAsReadOnScroll = { lastVisibleIndex ->
                    homeViewModel.markAsReadOnScroll(lastVisibleIndex)
                },
                markAsRead = { feedItemId ->
                    homeViewModel.markAsRead(feedItemId.id)
                },
            )
        }
    }
}