package chat.sphinx.dashboard.navigation

import androidx.navigation.NavController
import chat.sphinx.wrapper_common.chat.ChatId
import io.matthewnelson.concept_navigation.BaseNavigationDriver
import io.matthewnelson.concept_navigation.Navigator

abstract class DashboardNavigator(
    navigationDriver: BaseNavigationDriver<NavController>
): Navigator<NavController>(navigationDriver) {
    abstract suspend fun toChatContact(chatId: ChatId)
    abstract suspend fun toChatGroup(chatId: ChatId)
    abstract suspend fun toChatTribe(chatId: ChatId)
}
