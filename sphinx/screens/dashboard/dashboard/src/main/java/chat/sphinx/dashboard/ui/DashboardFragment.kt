package chat.sphinx.dashboard.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import chat.sphinx.dashboard.R
import chat.sphinx.dashboard.databinding.FragmentDashboardBinding
import chat.sphinx.dashboard.ui.viewstates.DashboardChatViewState
import chat.sphinx.resources.SphinxToastUtils
import dagger.hilt.android.AndroidEntryPoint
import io.matthewnelson.android_feature_screens.navigation.CloseAppOnBackPress
import io.matthewnelson.android_feature_screens.ui.base.BaseFragment

@AndroidEntryPoint
internal class DashboardFragment: BaseFragment<
        DashboardChatViewState,
        DashboardViewModel,
        FragmentDashboardBinding
        >(R.layout.fragment_dashboard)
{
    override val viewModel: DashboardViewModel by viewModels()
    override val binding: FragmentDashboardBinding by viewBinding(FragmentDashboardBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CloseAppOnBackPress(view.context)
            .enableDoubleTapToClose(
                viewLifecycleOwner,
                SphinxToastUtils()
            )
            .addCallback(
                viewLifecycleOwner,
                requireActivity()
            )
    }

    override suspend fun onViewStateFlowCollect(viewState: DashboardChatViewState) {
//        TODO("Not yet implemented")
    }
}
