package chat.sphinx.concept_repository_message

import chat.sphinx.concept_repository_message.model.SendPaymentRequest
import chat.sphinx.concept_repository_message.model.SendMessage
import chat.sphinx.concept_repository_message.model.SendPayment
import chat.sphinx.kotlin_response.LoadResponse
import chat.sphinx.kotlin_response.Response
import chat.sphinx.kotlin_response.ResponseError
import chat.sphinx.wrapper_chat.Chat
import chat.sphinx.wrapper_common.dashboard.ChatId
import chat.sphinx.wrapper_common.dashboard.ContactId
import chat.sphinx.wrapper_common.feed.FeedId
import chat.sphinx.wrapper_common.lightning.Sat
import chat.sphinx.wrapper_common.message.MessageId
import chat.sphinx.wrapper_common.message.MessageUUID
import chat.sphinx.wrapper_common.payment.PaymentTemplate
import chat.sphinx.wrapper_message.FeedBoost
import chat.sphinx.wrapper_message.Message
import chat.sphinx.wrapper_message.MessageContentDecrypted
import chat.sphinx.wrapper_message.MessageType
import chat.sphinx.wrapper_podcast.Podcast
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun getAllMessagesToShowByChatId(chatId: ChatId, limit: Long): Flow<List<Message>>
    fun getMessageById(messageId: MessageId): Flow<Message?>
    fun getTribeLastMemberRequestByContactId(contactId: ContactId, chatId: ChatId, ): Flow<Message?>
    fun getMessageByUUID(messageUUID: MessageUUID): Flow<Message?>
    fun getPaymentsTotalFor(feedId: FeedId): Flow<Sat?>

    suspend fun getAllMessagesByUUID(messageUUIDs: List<MessageUUID>): List<Message>

    fun updateMessageContentDecrypted(messageId: MessageId, messageContentDecrypted: MessageContentDecrypted)

    suspend fun readMessages(chatId: ChatId)

    fun sendMessage(sendMessage: SendMessage?)

    suspend fun payAttachment(message: Message) : Response<Any, ResponseError>

    fun resendMessage(
        message: Message,
        chat: Chat,
    )

    fun flagMessage(
        message: Message,
        chat: Chat,
    )

    fun sendBoost(
        chatId: ChatId,
        boost: FeedBoost
    )

    suspend fun deleteMessage(message: Message) : Response<Any, ResponseError>

    suspend fun getPaymentTemplates() : Response<List<PaymentTemplate>, ResponseError>

    suspend fun sendPayment(
        sendPayment: SendPayment?
    ): Response<Any, ResponseError>

    suspend fun sendPaymentRequest(
        requestPayment: SendPaymentRequest
    ): Response<Any, ResponseError>

    suspend fun payPaymentRequest(message: Message) : Response<Any, ResponseError>

    suspend fun boostMessage(
        chatId: ChatId,
        pricePerMessage: Sat,
        escrowAmount: Sat,
        messageUUID: MessageUUID,
    ): Response<Any, ResponseError>

    suspend fun processMemberRequest(
        contactId: ContactId,
        messageId: MessageId,
        type: MessageType,
    ): LoadResponse<Any, ResponseError>
}
