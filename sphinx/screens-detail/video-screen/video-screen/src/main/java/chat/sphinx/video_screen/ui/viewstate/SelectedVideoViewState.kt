package chat.sphinx.video_screen.ui.viewstate

import chat.sphinx.wrapper_common.DateTime
import chat.sphinx.wrapper_common.feed.FeedId
import chat.sphinx.wrapper_common.feed.FeedUrl
import chat.sphinx.wrapper_feed.FeedDescription
import chat.sphinx.wrapper_feed.FeedTitle
import io.matthewnelson.concept_views.viewstate.ViewState

internal sealed class SelectedVideoViewState: ViewState<SelectedVideoViewState>() {

    object Idle: SelectedVideoViewState()

    class VideoSelected(
        val id: FeedId,
        val title: FeedTitle,
        val description: FeedDescription?,
        val url: FeedUrl,
        val date: DateTime?,
    ): SelectedVideoViewState()
}