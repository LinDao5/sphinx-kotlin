package chat.sphinx.dashboard.ui.viewstates

import chat.sphinx.wrapper_podcast.PodcastSearchResultRow
import io.matthewnelson.concept_views.viewstate.ViewState

sealed class FeedViewState: ViewState<FeedViewState>() {

    object Idle: FeedViewState()

    object SearchPlaceHolder: FeedViewState()

    object LoadingSearchResults: FeedViewState()

    data class SearchResults(
        val searchResults: List<PodcastSearchResultRow>
    ): FeedViewState()
}