package chat.sphinx.activitymain.navigation.navigators.primary

import chat.sphinx.activitymain.navigation.drivers.DetailNavigationDriver
import chat.sphinx.activitymain.navigation.drivers.PrimaryNavigationDriver
import chat.sphinx.chat_contact.navigation.ToChatContactScreen
import chat.sphinx.chat_group.navigation.ToChatGroupScreen
import chat.sphinx.chat_tribe.navigation.ToChatTribeScreen
import chat.sphinx.chat_tribe.navigation.TribeChatNavigator
import chat.sphinx.contact_detail.navigation.ToContactDetailScreen
import chat.sphinx.join_tribe.navigation.ToJoinTribeDetail
import chat.sphinx.new_contact.navigation.ToNewContactDetail
import chat.sphinx.payment_send.navigation.ToPaymentSendDetail
import chat.sphinx.podcast_player.navigation.ToPodcastPlayerScreen
import chat.sphinx.podcast_player.objects.ParcelablePodcast
import chat.sphinx.tribe_detail.navigation.ToTribeDetailScreen
import chat.sphinx.wrapper_chat.Chat
import chat.sphinx.wrapper_chat.ChatType
import chat.sphinx.wrapper_common.dashboard.ChatId
import chat.sphinx.wrapper_common.dashboard.ContactId
import chat.sphinx.wrapper_common.tribe.TribeJoinLink
import javax.inject.Inject

internal class TribeChatNavigatorImpl @Inject constructor(
    navigationDriver: PrimaryNavigationDriver,
    private val detailDriver: DetailNavigationDriver,
): TribeChatNavigator(navigationDriver)
{

    override suspend fun toPaymentSendDetail(contactId: ContactId, chatId: ChatId?) {
        detailDriver.submitNavigationRequest(ToPaymentSendDetail(contactId, chatId))
    }

    override suspend fun toPodcastPlayerScreen(chatId: ChatId, podcast: ParcelablePodcast) {
        detailDriver.submitNavigationRequest(ToPodcastPlayerScreen(chatId, podcast))
    }

    override suspend fun toChatDetail(chatId: ChatId, contactId: ContactId?) {
        detailDriver.submitNavigationRequest(ToContactDetailScreen(chatId, contactId))
    }

    override suspend fun toTribeDetailScreen(chatId: ChatId, podcast: ParcelablePodcast?) {
        detailDriver.submitNavigationRequest(ToTribeDetailScreen(chatId, podcast))
    }

    override suspend fun toAddContactDetail() {
        detailDriver.submitNavigationRequest(
            ToNewContactDetail()
        )
    }

    override suspend fun toChat(chat: Chat, contactId: ContactId?) {
        when (chat.type) {
            is ChatType.Conversation -> {
                contactId?.let {
                    navigationDriver.submitNavigationRequest(
                        ToChatContactScreen(chat.id, contactId)
                    )
                }
            }
            is ChatType.Group -> {
                navigationDriver.submitNavigationRequest(
                    ToChatGroupScreen(chat.id)
                )
            }
            is ChatType.Tribe -> {
                navigationDriver.submitNavigationRequest(
                    ToChatTribeScreen(chat.id)
                )
            }
            is ChatType.Unknown -> {
                // ChatType unsupported
            }
        }
    }

    override suspend fun toJoinTribeDetail(tribeLink: TribeJoinLink) {
        detailDriver.submitNavigationRequest(ToJoinTribeDetail(tribeLink))
    }
}
