package com.prof18.feedflow.shared.presentation

import com.prof18.feedflow.core.model.FeedItemUrlInfo
import com.prof18.feedflow.shared.domain.ReaderModeExtractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReaderModeViewModel internal constructor(
    private val readerModeExtractor: ReaderModeExtractor,
) : BaseViewModel() {

    private val readerModeMutableState: MutableStateFlow<ReaderModeState> = MutableStateFlow(
        ReaderModeState.Loading,
    )
    val readerModeState = readerModeMutableState.asStateFlow()

    fun getReaderModeHtml(urlInfo: FeedItemUrlInfo) {
        scope.launch {
            readerModeMutableState.value = ReaderModeState.Loading
            val readerModeData = readerModeExtractor.extractReaderContent(urlInfo)
            if (readerModeData != null) {
                readerModeMutableState.value = ReaderModeState.Success(readerModeData)
            } else {
                readerModeMutableState.value = ReaderModeState.HtmlNotAvailable(urlInfo.url)
            }
        }
    }
}
