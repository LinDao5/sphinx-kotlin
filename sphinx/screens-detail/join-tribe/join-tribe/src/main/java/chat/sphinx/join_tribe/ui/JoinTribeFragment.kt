package chat.sphinx.join_tribe.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import chat.sphinx.concept_image_loader.ImageLoader
import chat.sphinx.concept_image_loader.ImageLoaderOptions
import chat.sphinx.concept_image_loader.Transformation
import chat.sphinx.insetter_activity.InsetterActivity
import chat.sphinx.insetter_activity.addNavigationBarPadding
import chat.sphinx.join_tribe.R
import chat.sphinx.join_tribe.databinding.FragmentJoinTribeBinding
import chat.sphinx.menu_bottom.ui.MenuBottomViewState
import chat.sphinx.menu_bottom_profile_pic.BottomMenuPicture
import dagger.hilt.android.AndroidEntryPoint
import io.matthewnelson.android_feature_screens.ui.sideeffect.SideEffectFragment
import io.matthewnelson.android_feature_screens.util.goneIfFalse
import kotlinx.coroutines.launch
import javax.annotation.meta.Exhaustive
import javax.inject.Inject

@AndroidEntryPoint
internal class JoinTribeFragment: SideEffectFragment<
        Context,
        JoinTribeSideEffect,
        JoinTribeViewState,
        JoinTribeViewModel,
        FragmentJoinTribeBinding
        >(R.layout.fragment_join_tribe)
{
    @Inject
    @Suppress("ProtectedInFinal")
    protected lateinit var imageLoader: ImageLoader<ImageView>

    override val viewModel: JoinTribeViewModel by viewModels()
    override val binding: FragmentJoinTribeBinding by viewBinding(FragmentJoinTribeBinding::bind)

    private val bottomMenuPicture: BottomMenuPicture by lazy(LazyThreadSafetyMode.NONE) {
        BottomMenuPicture(
            this,
            onStopSupervisor,
            viewModel
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as InsetterActivity)
            .addNavigationBarPadding(binding.layoutScrollViewContent)
            .addNavigationBarPadding(binding.includeLayoutMenuBottomTribeProfilePic.root)

        binding.apply {
            includeJoinTribeHeader.apply {
                textViewDetailScreenHeaderName.text = getString(R.string.join_tribe_header_name)
                textViewDetailScreenClose.setOnClickListener {
                    lifecycleScope.launch { viewModel.navigator.closeDetailScreen() }
                }
            }

            buttonJoin.setOnClickListener {
                val aliasString = binding.includeTribeMemberInfo.tribeMemberAliasEditText.text.toString()
                viewModel.joinTribe(aliasString)
            }

            includeTribeMemberInfo.apply {

                tribeMemberAliasEditText.setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        return@setOnFocusChangeListener
                    }
                    //Alias needs to be set on Focus change
                    //otherwise view is re created when coming back from camera, and alias is lost
                    persistCustomAlias()
                }

                buttonProfilePicture.setOnClickListener {
                    viewModel.pictureMenuHandler.viewStateContainer.updateViewState(
                        MenuBottomViewState.Open
                    )
                }
            }

            bottomMenuPicture.initialize(
                R.string.bottom_menu_tribe_profile_pic_header_text,
                includeLayoutMenuBottomTribeProfilePic,
                viewLifecycleOwner
            )
        }

        viewModel.loadTribeData()
    }

    private fun persistCustomAlias() {
        val aliasString = binding.includeTribeMemberInfo.tribeMemberAliasEditText.text.toString()
        viewModel.setMyAlias(aliasString)
    }

    override suspend fun onViewStateFlowCollect(viewState: JoinTribeViewState) {
        @Exhaustive
        when (viewState) {
            is JoinTribeViewState.LoadingTribe -> {
                binding.loadingTribeInfoContent.goneIfFalse(true)
            }
            is JoinTribeViewState.ErrorLoadingTribe -> {
                viewModel.navigator.closeDetailScreen()
            }
            is JoinTribeViewState.TribeLoaded -> {
                showTribeData(viewState)
            }
            is JoinTribeViewState.TribeProfileImageUpdated -> {
                binding.includeTribeMemberInfo.apply {
                    imageLoader.load(
                        imageViewProfilePicture,
                        viewState.imageFile,
                        viewModel.imageLoaderDefaults
                    )
                    editTextProfilePictureValue.setText("")
                }
            }

            is JoinTribeViewState.JoiningTribe -> {
                binding.buttonJoin.isEnabled = false
                binding.joinTribeSaveProgress.goneIfFalse(true)
            }
            is JoinTribeViewState.ErrorJoiningTribe -> {
                viewModel.navigator.closeDetailScreen()
            }
            is JoinTribeViewState.TribeJoined -> {
                viewModel.navigator.closeDetailScreen()
            }
        }
    }

    private fun showTribeData(viewState: JoinTribeViewState.TribeLoaded) {
        binding.apply {

            val owner = viewModel.accountOwnerStateFlow.value

            binding.includeTribeMemberInfo.apply {
                val memberAlias = viewState.myAlias ?: owner?.alias?.value
                tribeMemberAliasEditText.setText(memberAlias)

                val memberPhotoUrl = viewState.myPhotoUrl ?: owner?.photoUrl?.value
                loadProfileImage(memberPhotoUrl)
            }

            loadTribeImage(viewState.imageUrl)

            textViewTribeName.text = viewState.name
            textViewTribeDescription.text = viewState.description
            includeTribePrice.textViewPricePerMessage.text = viewState.pricePerMessage
            includeTribePrice.textViewPriceToJoin.text = viewState.priceToJoin
            includeTribePrice.textViewAmountToStake.text = viewState.escrowAmount
            includeTribePrice.textViewTimeToStake.text = viewState.hourToStake

            loadingTribeInfoContent.goneIfFalse(false)
        }
    }

    private fun loadTribeImage(img: String?) {
        onStopSupervisor.scope.launch(viewModel.mainImmediate) {
            if (img != null && img.isNotEmpty()) {
                imageLoader.load(
                    binding.imageViewTribePicture,
                    img,
                    ImageLoaderOptions.Builder()
                        .placeholderResId(R.drawable.ic_tribe_placeholder)
                        .transformation(Transformation.CircleCrop)
                        .build()
                )
            } else {
                binding.imageViewTribePicture
                    .setImageDrawable(
                        ContextCompat.getDrawable(
                            binding.root.context,
                            R.drawable.ic_tribe_placeholder
                        )
                    )

            }
        }
    }

    private fun loadProfileImage(photoUrl: String?) {
        onStopSupervisor.scope.launch(viewModel.mainImmediate) {
            photoUrl?.let {
                binding.includeTribeMemberInfo.apply {
                    editTextProfilePictureValue.setText(it)

                    imageLoader.load(
                        imageViewProfilePicture,
                        it,
                        viewModel.imageLoaderDefaults,
                    )
                }
            }
        }
    }

    override suspend fun onSideEffectCollect(sideEffect: JoinTribeSideEffect) {
        sideEffect.execute(binding.root.context)
    }
}
