package chat.sphinx.web_view.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import chat.sphinx.concept_repository_chat.ChatRepository
import chat.sphinx.concept_repository_contact.ContactRepository
import chat.sphinx.concept_repository_feed.FeedRepository
import chat.sphinx.concept_repository_media.RepositoryMedia
import chat.sphinx.concept_repository_message.MessageRepository
import chat.sphinx.web_view.navigation.WebViewNavigator
import chat.sphinx.wrapper_chat.ChatMetaData
import chat.sphinx.wrapper_common.ItemId
import chat.sphinx.wrapper_common.dashboard.ChatId
import chat.sphinx.wrapper_common.feed.toFeedId
import chat.sphinx.wrapper_common.lightning.Sat
import chat.sphinx.wrapper_contact.Contact
import chat.sphinx.wrapper_feed.Feed
import chat.sphinx.wrapper_feed.FeedItem
import chat.sphinx.wrapper_message.FeedBoost
import dagger.hilt.android.lifecycle.HiltViewModel
import io.matthewnelson.android_feature_navigation.util.navArgs
import io.matthewnelson.android_feature_viewmodel.BaseViewModel
import io.matthewnelson.android_feature_viewmodel.updateViewState
import io.matthewnelson.concept_coroutines.CoroutineDispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

internal inline val WebViewFragmentArgs.chatId: ChatId
    get() = ChatId(argChatId)

@HiltViewModel
internal class WebViewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    dispatchers: CoroutineDispatchers,
    val navigator: WebViewNavigator,
    private val chatRepository: ChatRepository,
    private val feedRepository: FeedRepository,
    private val contactRepository: ContactRepository,
    private val messageRepository: MessageRepository,
    private val repositoryMedia: RepositoryMedia,
): BaseViewModel<WebViewViewState>(dispatchers, WebViewViewState.Idle)
{
    private val args: WebViewFragmentArgs by savedStateHandle.navArgs()

    private val feedItemSharedFlow: SharedFlow<FeedItem?> = flow {
        args.feedItemId?.toFeedId()?.let { feedItemId ->
            emitAll(feedRepository.getFeedItemById(feedItemId))
        } ?: run {
            emit(null)
        }
    }.distinctUntilChanged().shareIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(2_000),
        replay = 1,
    )

    private val feedSharedFlow: SharedFlow<Feed?> = flow {
        args.feedId?.toFeedId()?.let { feedId ->
            emitAll(feedRepository.getFeedById(feedId))
        } ?: run {
            emit(null)
        }
    }.distinctUntilChanged().shareIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(2_000),
        replay = 1,
    )

    init {
        args.chatId?.let { chatId ->
            viewModelScope.launch(mainImmediate) {
                chatRepository.updateChatContentSeenAt(chatId)
            }
        }

        viewModelScope.launch(mainImmediate) {
            val owner = getOwner()
            val feed = getFeed()

            updateViewState(
                WebViewViewState.FeedDataLoaded(
                    fromArticlesList = args.argFromList,
                    viewTitle = args.argTitle,
                    url = args.argUrl,
                    isFeedUrl = feed != null,
                    feedHasDestinations = feed?.hasDestinations == true,
                    ownerPhotoUrl = owner.photoUrl,
                    boostAmount = owner.tipAmount
                )
            )
        }
    }

    private suspend fun getOwner(): Contact {
        return contactRepository.accountOwner.value.let { contact ->
            if (contact != null) {
                contact
            } else {
                var resolvedOwner: Contact? = null
                try {
                    contactRepository.accountOwner.collect { ownerContact ->
                        if (ownerContact != null) {
                            resolvedOwner = ownerContact
                            throw Exception()
                        }
                    }
                } catch (e: Exception) {
                }
                delay(25L)

                resolvedOwner!!
            }
        }
    }

    private suspend fun getFeed(): Feed? {
        feedSharedFlow.replayCache.firstOrNull()?.let { feed ->
            return feed
        }

        feedSharedFlow.firstOrNull()?.let { feed ->
            return feed
        }

        var feed: Feed? = null

        try {
            feedSharedFlow.collect {
                if (it != null) {
                    feed = it
                    throw Exception()
                }
            }
        } catch (e: Exception) {}
        delay(25L)
        return feed
    }

    private suspend fun getFeedItem(): FeedItem? {
        feedItemSharedFlow.replayCache.firstOrNull()?.let { feedItem ->
            return feedItem
        }

        feedItemSharedFlow.firstOrNull()?.let { feedItem ->
            return feedItem
        }

        var feedItem: FeedItem? = null

        try {
            feedItemSharedFlow.collect {
                if (it != null) {
                    feedItem = it
                    throw Exception()
                }
            }
        } catch (e: Exception) {}
        delay(25L)
        return feedItem
    }

    fun sendBoost(customAmount: Sat?) {
        viewModelScope.launch(mainImmediate) {
            (customAmount ?: getOwner().tipAmount)?.let { amount ->
                getFeedItem()?.let { feedItem ->
                    getFeed()?.let { feed ->
                        if (amount.value > 0) {

                            val chatId = args.chatId

                            messageRepository.sendBoost(
                                chatId,
                                FeedBoost(
                                    feedId = feed.id,
                                    itemId = feedItem.id,
                                    timeSeconds =0,
                                    amount = amount
                                )
                            )

                            feed.destinations.let { destinations ->
                                repositoryMedia.streamFeedPayments(
                                    chatId,
                                    ChatMetaData(
                                        itemId = feedItem.id,
                                        itemLongId = ItemId(-1),
                                        satsPerMinute = amount,
                                        timeSeconds = 0,
                                        speed = 1.0
                                    ),
                                    feed.id.value,
                                    feedItem.id.value,
                                    destinations
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
