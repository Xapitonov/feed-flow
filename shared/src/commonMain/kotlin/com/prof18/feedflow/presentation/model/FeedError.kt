package com.prof18.feedflow.presentation.model

sealed interface ErrorState

internal data class FeedErrorState(
    val failingSourceName: String,
) : ErrorState

internal data object DatabaseError : ErrorState