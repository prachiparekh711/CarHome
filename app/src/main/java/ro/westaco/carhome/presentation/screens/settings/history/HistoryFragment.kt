package ro.westaco.carhome.presentation.screens.settings.history

import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_history.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.HistoryItem
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.dashboard.DashboardViewModel.Companion.serviceExpanded

//C- History section
@AndroidEntryPoint
class HistoryFragment : BaseFragment<HistoryViewModel>(),
    HistoryAdapter.OnItemInteractionListener {
    lateinit var adapter: HistoryAdapter

    override fun getContentView() = R.layout.fragment_history

    override fun initUi() {
        back.setOnClickListener {
            viewModel.onBack()
        }

        home.setOnClickListener {
            viewModel.onMain()
        }

        mService.setOnClickListener {
            serviceExpanded = true
            viewModel.onMain()
        }
    }

    override fun setObservers() {
        viewModel.historyLiveData.observe(viewLifecycleOwner) { historyList ->
            if (historyList.isNullOrEmpty()) {
                llEmpty.isVisible = true
                historyRV.isVisible = false
            } else {
                llEmpty.isVisible = false
                historyRV.isVisible = true
                historyRV.layoutManager =
                    LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                adapter = HistoryAdapter(requireContext(), arrayListOf(), this, "SETTING")
                historyRV.adapter = adapter
                adapter.setItems(historyList)
            }
        }
    }

    override fun onItemClick(item: HistoryItem) {
        viewModel.onHistoryDetail(item)
    }
}