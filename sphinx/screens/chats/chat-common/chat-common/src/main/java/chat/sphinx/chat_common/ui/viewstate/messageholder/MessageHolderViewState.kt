package chat.sphinx.chat_common.ui.viewstate.messageholder

import chat.sphinx.chat_common.R
import chat.sphinx.chat_common.model.MessageLinkPreview
import chat.sphinx.chat_common.ui.viewstate.InitialHolderViewState
import chat.sphinx.chat_common.ui.viewstate.selected.MenuItemState
import chat.sphinx.chat_common.util.SphinxLinkify
import chat.sphinx.resources.getString
import chat.sphinx.wrapper_chat.Chat
import chat.sphinx.wrapper_chat.isConversation
import chat.sphinx.wrapper_chat.isTribeOwnedByAccount
import chat.sphinx.wrapper_common.chatTimeFormat
import chat.sphinx.wrapper_common.lightning.Sat
import chat.sphinx.wrapper_common.message.isProvisionalMessage
import chat.sphinx.wrapper_contact.Contact
import chat.sphinx.wrapper_message.*
import chat.sphinx.wrapper_message_media.MessageMedia
import chat.sphinx.wrapper_message_media.isImage
import chat.sphinx.wrapper_message_media.isSphinxText
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// TODO: Remove
inline val Message.isCopyLinkAllowed: Boolean
    get() = retrieveTextToShow()?.let {
        SphinxLinkify.SphinxPatterns.COPYABLE_LINKS.matcher(it).find()
    } ?: false

internal inline val MessageHolderViewState.isReceived: Boolean
    get() = this is MessageHolderViewState.Received

internal inline val MessageHolderViewState.showReceivedBubbleArrow: Boolean
    get() = background is BubbleBackground.First && this is MessageHolderViewState.Received

internal val MessageHolderViewState.showSentBubbleArrow: Boolean
    get() = background is BubbleBackground.First && this is MessageHolderViewState.Sent

internal sealed class MessageHolderViewState(
    val message: Message,
    chat: Chat,
    val background: BubbleBackground,
    val initialHolder: InitialHolderViewState,
    private val messageSenderName: (Message) -> String,
    private val accountOwner: () -> Contact,
    private val previewProvider: suspend (link: MessageLinkPreview) -> LayoutState.Bubble.ContainerThird.LinkPreview?,
    private val paidTextAttachmentContentProvider: suspend (message: Message) -> LayoutState.Bubble.ContainerThird.Message?,
) {

    companion object {
        val unsupportedMessageTypes: List<MessageType> by lazy {
            listOf(
                MessageType.Attachment,
                MessageType.Invoice,
                MessageType.Payment,
                MessageType.GroupAction.TribeDelete,
            )
        }
    }

    var viewsWidthSet: Boolean = false

    val unsupportedMessageType: LayoutState.Bubble.ContainerThird.UnsupportedMessageType? by lazy(LazyThreadSafetyMode.NONE) {
        if (
            unsupportedMessageTypes.contains(message.type) &&
            message.messageMedia?.mediaType?.isImage != true && message.messageMedia?.mediaType?.isSphinxText != true
        ) {
            LayoutState.Bubble.ContainerThird.UnsupportedMessageType(
                messageType = message.type,
                gravityStart = this is Received,
            )
        } else {
            null
        }
    }

    val statusHeader: LayoutState.MessageStatusHeader? by lazy(LazyThreadSafetyMode.NONE) {
        if (background is BubbleBackground.First) {
            LayoutState.MessageStatusHeader(
                if (chat.type.isConversation()) null else message.senderAlias?.value,
                if (initialHolder is InitialHolderViewState.Initials) initialHolder.colorKey else message.getColorKey(),
                this is Sent,
                this is Sent && message.id.isProvisionalMessage && message.status.isPending(),
                this is Sent && (message.status.isReceived() || message.status.isConfirmed()),
                this is Sent && message.status.isFailed(),
                message.messageContentDecrypted != null || message.messageMedia?.mediaKeyDecrypted != null,
                message.date.chatTimeFormat(),
            )
        } else {
            null
        }
    }

    val deletedMessage: LayoutState.DeletedMessage? by lazy(LazyThreadSafetyMode.NONE) {
        if (message.status.isDeleted()) {
            LayoutState.DeletedMessage(
                gravityStart = this is Received,
                timestamp = message.date.chatTimeFormat()
            )
        } else {
            null
        }
    }

    val bubbleDirectPayment: LayoutState.Bubble.ContainerSecond.DirectPayment? by lazy(LazyThreadSafetyMode.NONE) {
        if (message.type.isDirectPayment()) {
            LayoutState.Bubble.ContainerSecond.DirectPayment(showSent = this is Sent, amount = message.amount)
        } else {
            null
        }
    }

    val bubbleMessage: LayoutState.Bubble.ContainerThird.Message? by lazy(LazyThreadSafetyMode.NONE) {
        message.retrieveTextToShow()?.let { text ->
            if (text.isNotEmpty()) {
                LayoutState.Bubble.ContainerThird.Message(text = text)
            } else {
                null
            }
        }
    }

    val bubblePaidMessage: LayoutState.Bubble.ContainerThird.PaidMessage? by lazy(LazyThreadSafetyMode.NONE) {
        if (message.retrieveTextToShow() != null || !message.isPaidTextMessage) {
            null
        } else {
            val purchaseStatus = message.retrievePurchaseStatus()

            if (this is Sent) {
                LayoutState.Bubble.ContainerThird.PaidMessage(
                    true,
                    purchaseStatus
                )
            } else {
                LayoutState.Bubble.ContainerThird.PaidMessage(
                    false,
                    purchaseStatus
                )
            }
        }
    }

    val bubbleCallInvite: LayoutState.Bubble.ContainerSecond.CallInvite? by lazy(LazyThreadSafetyMode.NONE) {
        message.retrieveSphinxCallLink()?.let { callLink ->
            LayoutState.Bubble.ContainerSecond.CallInvite(!callLink.startAudioOnly)
        }
    }

    val bubbleBotResponse: LayoutState.Bubble.ContainerSecond.BotResponse? by lazy(LazyThreadSafetyMode.NONE) {
        if (message.type.isBotRes()) {
            message.retrieveBotResponseHtmlString()?.let { html ->
                LayoutState.Bubble.ContainerSecond.BotResponse(
                    html
                )
            }
        } else {
            null
        }
    }

    val bubblePaidMessageReceivedDetails: LayoutState.Bubble.ContainerFourth.PaidMessageReceivedDetails? by lazy(LazyThreadSafetyMode.NONE) {
        if (!message.isPaidMessage || this is Sent) {
            null
        } else {
            message.retrievePurchaseStatus()?.let { purchaseStatus ->
                LayoutState.Bubble.ContainerFourth.PaidMessageReceivedDetails(
                    amount = message.messageMedia?.price ?: Sat(0),
                    purchaseStatus = purchaseStatus,
                    showStatusIcon = purchaseStatus.isPurchaseAccepted() ||
                            purchaseStatus.isPurchaseDenied(),
                    showProcessingProgressBar = purchaseStatus.isPurchaseProcessing(),
                    showStatusLabel = purchaseStatus.isPurchaseProcessing() ||
                            purchaseStatus.isPurchaseAccepted() ||
                            purchaseStatus.isPurchaseDenied(),
                    showPayElements = purchaseStatus.isPurchasePending()
                )
            }
        }
    }

    val bubblePaidMessageSentStatus: LayoutState.Bubble.ContainerSecond.PaidMessageSentStatus? by lazy(LazyThreadSafetyMode.NONE) {
        if (!message.isPaidMessage || this !is Sent) {
            null
        } else {
            message.retrievePurchaseStatus()?.let { purchaseStatus ->
                LayoutState.Bubble.ContainerSecond.PaidMessageSentStatus(
                    amount = message.messageMedia?.price ?: Sat(0),
                    purchaseStatus = purchaseStatus
                )
            }
        }
    }

    val bubbleImageAttachment: LayoutState.Bubble.ContainerSecond.ImageAttachment? by lazy(LazyThreadSafetyMode.NONE) {
        message.retrieveImageUrlAndMessageMedia()?.let { mediaData ->
            LayoutState.Bubble.ContainerSecond.ImageAttachment(
                mediaData.first,
                mediaData.second,
                (this is Received && message.isPaidPendingMessage)
            )
        }
    }

    val bubblePodcastBoost: LayoutState.Bubble.ContainerSecond.PodcastBoost? by lazy(LazyThreadSafetyMode.NONE) {
        message.podBoost?.let { podBoost ->
            LayoutState.Bubble.ContainerSecond.PodcastBoost(
                podBoost.amount,
            )
        }
    }

    // don't use by lazy as this uses a for loop and needs to be initialized on a background
    // thread (so, while the MHVS is being created)
    val bubbleReactionBoosts: LayoutState.Bubble.ContainerFourth.Boost? =
        message.reactions?.let { nnReactions ->
            if (nnReactions.isEmpty()) {
                null
            } else {
                val set: MutableSet<BoostReactionImageHolder> = LinkedHashSet(1)
                var total: Long = 0
                for (reaction in nnReactions) {
//                    if (chatType?.isConversation() != true) {
//                        reaction.senderPic?.value?.let { url ->
//                            set.add(SenderPhotoUrl(url))
//                        } ?: reaction.senderAlias?.value?.let { alias ->
//                            set.add(SenderInitials(alias.getInitials()))
//                        }
//                    }
                    total += reaction.amount.value
                }

//                if (chatType?.isConversation() == true) {
//
//                    // TODO: Use Account Owner Initial Holder depending on sent/received
//                    @Exhaustive
//                    when (initialHolder) {
//                        is InitialHolderViewState.Initials -> {
//                            set.add(SenderInitials(initialHolder.initials))
//                        }
//                        is InitialHolderViewState.None -> {}
//                        is InitialHolderViewState.Url -> {
//                            set.add(SenderPhotoUrl(initialHolder.photoUrl.value))
//                        }
//                    }
//                }

                LayoutState.Bubble.ContainerFourth.Boost(
                    totalAmount = Sat(total),
                    senderPics = set,
                )
            }
        }

    val bubbleReplyMessage: LayoutState.Bubble.ContainerFirst.ReplyMessage? by lazy {
        message.replyMessage?.let { nnReplyMessage ->

            var mediaUrl: String? = null
            var messageMedia: MessageMedia? = null

            nnReplyMessage.retrieveImageUrlAndMessageMedia()?.let { mediaData ->
                mediaUrl = mediaData.first
                messageMedia = mediaData.second
            }

            LayoutState.Bubble.ContainerFirst.ReplyMessage(
                showSent = this is Sent,
                messageSenderName(nnReplyMessage),
                nnReplyMessage.getColorKey(),
                nnReplyMessage.retrieveTextToShow() ?: "",
                mediaUrl,
                messageMedia
            )
        }
    }

    val groupActionIndicator: LayoutState.GroupActionIndicator? by lazy(LazyThreadSafetyMode.NONE) {
        val type = message.type
        if (!type.isGroupAction()) {
            null
        } else {
            LayoutState.GroupActionIndicator(
                actionType = type,
                isAdminView = if (chat.ownerPubKey == null || accountOwner().nodePubKey == null) {
                    false
                } else {
                    chat.ownerPubKey == accountOwner().nodePubKey
                },
                chatType = chat.type,
                subjectName = message.senderAlias?.value ?: ""
            )
        }
    }

    val messageLinkPreview: MessageLinkPreview? by lazy {
        MessageLinkPreview.parse(bubbleMessage)
    }

    @Volatile
    private var linkPreviewLayoutState: LayoutState.Bubble.ContainerThird.LinkPreview? = null
    private val previewLock = Mutex()
    suspend fun retrieveLinkPreview(): LayoutState.Bubble.ContainerThird.LinkPreview? {
        return messageLinkPreview?.let { nnPreview ->
            linkPreviewLayoutState ?: previewLock.withLock {
                linkPreviewLayoutState ?: previewProvider.invoke(nnPreview)
                    ?.also { linkPreviewLayoutState = it }
            }
        }
    }

    private val paidTextMessageContentLock = Mutex()
    suspend fun retrievePaidTextMessageContent(): LayoutState.Bubble.ContainerThird.Message? {
        return bubbleMessage ?: paidTextMessageContentLock.withLock {
            bubbleMessage ?: paidTextAttachmentContentProvider.invoke(message)
        }
    }

    val selectionMenuItems: List<MenuItemState>? by lazy(LazyThreadSafetyMode.NONE) {
        if (
            background is BubbleBackground.Gone         ||
            message.podBoost != null
        ) {
            null
        } else {
            // TODO: check message status

            val list = ArrayList<MenuItemState>(4)

            if (this is Received && message.isBoostAllowed) {
                list.add(MenuItemState.Boost)
            }

            if (message.isMediaAttachmentAvailable) {
                list.add(MenuItemState.SaveFile)
            }

            if (message.isCopyLinkAllowed) {
                list.add(MenuItemState.CopyLink)
            }

            if (message.isCopyAllowed) {
                list.add(MenuItemState.CopyText)
            }

            if (message.isReplyAllowed) {
                list.add(MenuItemState.Reply)
            }

            if (message.isResendAllowed) {
                list.add(MenuItemState.Resend)
            }

            if (this is Sent || chat.isTribeOwnedByAccount(accountOwner().nodePubKey)) {
                list.add(MenuItemState.Delete)
            }

            if (list.isEmpty()) {
                null
            } else {
                list.sortBy { it.sortPriority }
                list
            }
        }
    }

    class Sent(
        message: Message,
        chat: Chat,
        background: BubbleBackground,
        replyMessageSenderName: (Message) -> String,
        accountOwner: () -> Contact,
        previewProvider: suspend (link: MessageLinkPreview) -> LayoutState.Bubble.ContainerThird.LinkPreview?,
        paidTextMessageContentProvider: suspend (message: Message) -> LayoutState.Bubble.ContainerThird.Message?,
    ) : MessageHolderViewState(
        message,
        chat,
        background,
        InitialHolderViewState.None,
        replyMessageSenderName,
        accountOwner,
        previewProvider,
        paidTextMessageContentProvider,
    )

    class Received(
        message: Message,
        chat: Chat,
        background: BubbleBackground,
        initialHolder: InitialHolderViewState,
        replyMessageSenderName: (Message) -> String,
        accountOwner: () -> Contact,
        previewProvider: suspend (link: MessageLinkPreview) -> LayoutState.Bubble.ContainerThird.LinkPreview?,
        paidTextMessageContentProvider: suspend (message: Message) -> LayoutState.Bubble.ContainerThird.Message?,
    ) : MessageHolderViewState(
        message,
        chat,
        background,
        initialHolder,
        replyMessageSenderName,
        accountOwner,
        previewProvider,
        paidTextMessageContentProvider,
    )
}
