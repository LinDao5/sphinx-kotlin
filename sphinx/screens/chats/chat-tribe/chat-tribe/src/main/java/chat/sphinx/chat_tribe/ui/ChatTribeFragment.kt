package chat.sphinx.chat_tribe.ui

import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import by.kirich1409.viewbindingdelegate.viewBinding
import chat.sphinx.chat_common.ui.BaseChatFragment
import chat.sphinx.chat_common.navigation.ChatNavigator
import chat.sphinx.chat_tribe.R
import chat.sphinx.chat_tribe.databinding.FragmentChatTribeBinding
import chat.sphinx.chat_tribe.navigation.TribeChatNavigator
import chat.sphinx.concept_image_loader.ImageLoader
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
internal class ChatTribeFragment: BaseChatFragment<
        FragmentChatTribeBinding
        >(R.layout.fragment_chat_tribe)
{
    override val binding: FragmentChatTribeBinding by viewBinding(FragmentChatTribeBinding::bind)

    override val header: ConstraintLayout
        get() = binding.layoutChatHeader.layoutConstraintChatHeader
    override val headerChatPicture: ImageView
        get() = binding.layoutChatHeader.layoutChatInitialHolder.imageViewChatPicture
    override val headerConnectivityIcon: ImageView
        get() = binding.layoutChatHeader.imageViewChatHeaderConnectivity
    override val headerInitials: TextView
        get() = binding.layoutChatHeader.layoutChatInitialHolder.textViewInitials
    override val headerLockIcon: ImageView
        get() = binding.layoutChatHeader.imageViewChatHeaderLock
    override val headerMute: ImageView
        get() = binding.layoutChatHeader.imageViewChatHeaderMuted
    override val headerName: TextView
        get() = binding.layoutChatHeader.textViewChatHeaderName
    override val headerNavBack: ImageView
        get() = binding.layoutChatHeader.imageViewChatHeaderNavBack

    override val footer: ConstraintLayout
        get() = binding.layoutChatFooter.layoutConstraintChatFooter

    @Inject
    protected lateinit var imageLoaderInj: ImageLoader<ImageView>
    override val imageLoader: ImageLoader<ImageView>
        get() = imageLoaderInj

    @Inject
    protected lateinit var chatNavigatorInj: TribeChatNavigator
    override val chatNavigator: ChatNavigator
        get() = chatNavigatorInj
}
