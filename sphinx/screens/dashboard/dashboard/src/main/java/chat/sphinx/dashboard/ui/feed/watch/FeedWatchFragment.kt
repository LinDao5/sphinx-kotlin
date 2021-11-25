package chat.sphinx.dashboard.ui.feed.watch

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import chat.sphinx.concept_image_loader.ImageLoader
import chat.sphinx.dashboard.R
import chat.sphinx.dashboard.databinding.FragmentFeedWatchBinding
import chat.sphinx.dashboard.ui.adapter.FeedFollowingAdapter
import chat.sphinx.dashboard.ui.adapter.FeedWatchNowAdapter
import chat.sphinx.dashboard.ui.viewstates.FeedWatchViewState
import dagger.hilt.android.AndroidEntryPoint
import io.matthewnelson.android_feature_screens.ui.sideeffect.SideEffectFragment
import io.matthewnelson.android_feature_screens.util.goneIfFalse
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
internal class FeedWatchFragment : SideEffectFragment<
        Context,
        FeedWatchSideEffect,
        FeedWatchViewState,
        FeedWatchViewModel,
        FragmentFeedWatchBinding
        >(R.layout.fragment_feed_watch)
{
    @Inject
    @Suppress("ProtectedInFinal")
    protected lateinit var imageLoader: ImageLoader<ImageView>

    override val viewModel: FeedWatchViewModel by viewModels()
    override val binding: FragmentFeedWatchBinding by viewBinding(FragmentFeedWatchBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListenNowAdapter()
        setupFollowingAdapter()
    }

    private fun setupListenNowAdapter() {
        binding.recyclerViewWatchNow.apply {
            val watchNowAdapter = FeedWatchNowAdapter(
                imageLoader,
                viewLifecycleOwner,
                onStopSupervisor,
                viewModel
            )

            this.setHasFixedSize(false)
            adapter = watchNowAdapter
            itemAnimator = null
        }
    }

    private fun setupFollowingAdapter() {
        binding.recyclerViewFollowing.apply {
            val followingAdapter = FeedFollowingAdapter(
                imageLoader,
                viewLifecycleOwner,
                onStopSupervisor,
                viewModel,
                viewModel
            )

            this.setHasFixedSize(false)
            adapter = followingAdapter
            itemAnimator = null
        }
    }
    override suspend fun onSideEffectCollect(sideEffect: FeedWatchSideEffect) {
        sideEffect.execute(binding.root.context)
    }

    companion object {
        fun newInstance(): FeedWatchFragment {
            return FeedWatchFragment()
        }
    }

    override suspend fun onViewStateFlowCollect(viewState: FeedWatchViewState) {
        // TODO("Not yet implemented")
    }

    override fun subscribeToViewStateFlow() {
        super.subscribeToViewStateFlow()

        onStopSupervisor.scope.launch(viewModel.mainImmediate) {
            viewModel.feedsHolderViewStateFlow.collect { list ->
                toggleElements(
                    list.isNotEmpty()
                )
            }
        }
    }

    private fun toggleElements(contentAvailable: Boolean) {
        binding.apply {
            scrollViewContent.goneIfFalse(contentAvailable)
            textViewPlaceholder.goneIfFalse(!contentAvailable)
        }
    }
}
