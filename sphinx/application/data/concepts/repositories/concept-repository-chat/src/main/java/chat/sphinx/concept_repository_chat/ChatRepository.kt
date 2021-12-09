package chat.sphinx.concept_repository_chat

import chat.sphinx.concept_network_query_chat.model.ChatDto
import chat.sphinx.concept_network_query_chat.model.TribeDto
import chat.sphinx.concept_repository_chat.model.CreateTribe
import chat.sphinx.kotlin_response.LoadResponse
import chat.sphinx.kotlin_response.Response
import chat.sphinx.kotlin_response.ResponseError
import chat.sphinx.wrapper_chat.Chat
import chat.sphinx.wrapper_chat.ChatAlias
import chat.sphinx.wrapper_chat.ChatHost
import chat.sphinx.wrapper_chat.TribeData
import chat.sphinx.wrapper_common.feed.FeedUrl
import chat.sphinx.wrapper_common.chat.ChatUUID
import chat.sphinx.wrapper_common.dashboard.ChatId
import chat.sphinx.wrapper_common.dashboard.ContactId
import chat.sphinx.wrapper_common.feed.FeedId
import chat.sphinx.wrapper_common.feed.Subscribed
import chat.sphinx.wrapper_feed.Feed
import chat.sphinx.wrapper_feed.FeedItem
import chat.sphinx.wrapper_meme_server.PublicAttachmentInfo
import chat.sphinx.wrapper_podcast.Podcast
import chat.sphinx.wrapper_podcast.PodcastSearchResultRow
import kotlinx.coroutines.flow.Flow

/**
 * All [Chat]s are cached to the DB such that a network refresh will update
 * them, and thus proc any [Flow] being collected
 * */
interface ChatRepository {
    val getAllChats: Flow<List<Chat>>
    fun getChatById(chatId: ChatId): Flow<Chat?>
    fun getChatByUUID(chatUUID: ChatUUID): Flow<Chat?>

    /**
     * Returns a [chat.sphinx.wrapper_chat.ChatType.Conversation] or `null`
     * for the provided [contactId]
     * */
    fun getConversationByContactId(contactId: ContactId): Flow<Chat?>

    /**
     * Throws [NoSuchElementException] on collection if [Chat.contactIds]
     * is empty.
     * */
    fun getUnseenMessagesByChatId(chatId: ChatId): Flow<Long?>
    
    val networkRefreshChats: Flow<LoadResponse<Boolean, ResponseError>>

    suspend fun getAllChatsByIds(chatIds: List<ChatId>): List<Chat>
    /**
     * Returns `true` if the user has muted the chat and there is a need
     * to notify them that they won't receive messages anymore.
     *
     * Returns `false` if the user has _un_ muted the chat and there is no
     * need to notify.
     *
     * Returns error if something went wrong (networking)
     * */
    suspend fun toggleChatMuted(chat: Chat): Response<Boolean, ResponseError>

    suspend fun updateChatContentSeenAt(chatId: ChatId)

    fun joinTribe(
        tribeDto: TribeDto,
    ): Flow<LoadResponse<ChatDto, ResponseError>>

    suspend fun updateFeedContent(
        chatId: ChatId,
        host: ChatHost,
        feedUrl: FeedUrl,
        chatUUID: ChatUUID?,
        subscribed: Subscribed,
        currentEpisodeId: FeedId?
    )

    fun getFeedByChatId(chatId: ChatId): Flow<Feed?>
    fun getFeedById(feedId: FeedId): Flow<Feed?>
    fun getFeedItemById(feedItemId: FeedId): Flow<FeedItem?>

    suspend fun toggleFeedSubscribeState(feedId: FeedId, currentSubscribeState: Subscribed)

    suspend fun updateTribeInfo(chat: Chat): TribeData?
    suspend fun createTribe(createTribe: CreateTribe): Response<Any, ResponseError>
    suspend fun updateTribe(chatId: ChatId, createTribe: CreateTribe): Response<Any, ResponseError>
    suspend fun exitAndDeleteTribe(chat: Chat): Response<Boolean, ResponseError>

    suspend fun updateChatProfileInfo(
        chatId: ChatId,
        alias: ChatAlias? = null,
        profilePic: PublicAttachmentInfo? = null,
    ): Response<ChatDto, ResponseError>

    suspend fun kickMemberFromTribe(chatId: ChatId, contactId: ContactId): Response<Any, ResponseError>
}
