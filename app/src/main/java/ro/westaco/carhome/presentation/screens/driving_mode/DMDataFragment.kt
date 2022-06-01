package ro.westaco.carhome.presentation.screens.driving_mode

import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_d_m_data.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.*
import ro.westaco.carhome.di.ApiModule
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.home.HomeViewModel
import ro.westaco.carhome.presentation.screens.home.PdfActivity
import ro.westaco.carhome.presentation.screens.home.adapter.RecentDocumentAdapter
import ro.westaco.carhome.presentation.screens.reminder.DateReminderAdapter
import ro.westaco.carhome.presentation.screens.settings.history.HistoryAdapter
import ro.westaco.carhome.utils.DialogUtils.Companion.showErrorInfo
import ro.westaco.carhome.utils.Progressbar
import java.text.SimpleDateFormat

@AndroidEntryPoint
class DMDataFragment : BaseFragment<HomeViewModel>(),
    RecentDocumentAdapter.OnItemInteractionListener,
    HistoryAdapter.OnItemInteractionListener {

    companion object {
        fun newInstance(): DMDataFragment {
            return DMDataFragment()
        }
    }

    lateinit var adapter: HistoryAdapter
    private lateinit var reminderAdapter: DateReminderAdapter
    private lateinit var recentDocAdapter: RecentDocumentAdapter
    var allFilterList: ArrayList<CatalogItem> = ArrayList()
    var progressbar: Progressbar? = null

    override fun getContentView() = R.layout.fragment_d_m_data

    override fun getStatusBarColor() =
        ContextCompat.getColor(requireContext(), R.color.white)

    override fun initUi() {
        progressbar = Progressbar(requireContext())
        viewReminder.setOnClickListener {
            val lbm = context?.let { LocalBroadcastManager.getInstance(it) }
            val localIn = Intent("DASHBOARD_VIEW")
            localIn.putExtra("open", "REMINDER")
            lbm?.sendBroadcast(localIn)
        }

        viewHistory.setOnClickListener {
            viewModel.onHistoryClicked()
        }

        viewDocument.setOnClickListener {
            viewModel.onDocumentClicked()
        }

    }

    override fun setObservers() {
        viewModel.documentLivedata.observe(viewLifecycleOwner) { docList ->
            if (docList.isNullOrEmpty()) {
                noDocumentLL.isVisible = true
                documentRL.isVisible = false
            } else {
                noDocumentLL.isVisible = false
                documentRL.isVisible = true
                mDocumentRV.layoutManager =
                    LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                recentDocAdapter = RecentDocumentAdapter(requireContext(), arrayListOf(), this)
                mDocumentRV.adapter = recentDocAdapter
                recentDocAdapter.setItems(docList)
            }
        }


        viewModel.stateStream.observe(viewLifecycleOwner) { state ->
            if (state == HomeViewModel.STATE.DOCUMENT_NOT_FOUND) {
                showErrorInfo(requireContext(), getString(R.string.doc_not_found))
            }
        }

        viewModel.remindersTabData.observe(viewLifecycleOwner) { tags ->
            if (tags != null)
                allFilterList = tags
        }

        viewModel.remindersLiveData.observe(viewLifecycleOwner) { reminderList ->
            if (reminderList.isNullOrEmpty()) {
                reminderRL.isVisible = false
                noReminderLL.isVisible = true
            } else {
                val listItems = ArrayList<ListItem>(reminderList.size)
                reminderRL.isVisible = true
                noReminderLL.isVisible = false

                val swipeInterface = object : DateReminderAdapter.SwipeActions {
                    override fun onDelete(item: Reminder) {
                        viewModel.onDelete(item)
                    }

                    override fun onUpdate(item: Reminder) {
                        viewModel.onUpdate(item)
                    }
                }
                reminderAdapter = DateReminderAdapter(
                    viewModel,
                    arrayListOf(),
                    swipeInterface
                )
                reminderRv.adapter = reminderAdapter
                reminderList.sortWith { o1, o2 ->
                    val date1 = SimpleDateFormat("yyyy-MM-dd").parse(o1.dueDate)
                    val date2 = SimpleDateFormat("yyyy-MM-dd").parse(o2.dueDate)
                    date1.compareTo(date2)
                }
                reminderAdapter.clearAll()
                if (reminderList.size < 3) {
                    for (i in reminderList.indices)
                        listItems.add(reminderList[i])
                } else {
                    for (i in 0..2)
                        listItems.add(reminderList[i])
                }
                reminderAdapter.setItems(listItems, allFilterList)
            }
        }

        viewModel.historyLiveData.observe(viewLifecycleOwner) { historyList ->
            if (historyList.isNullOrEmpty()) {
                historyRL.isVisible = false
                noHistoryLL.isVisible = true
            } else {
                historyRL.isVisible = true
                noHistoryLL.isVisible = false
                historyRv.layoutManager =
                    LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                adapter = HistoryAdapter(requireContext(), arrayListOf(), this, "HOME")
                historyRv.adapter = adapter
                val list: ArrayList<HistoryItem> = ArrayList()
                if (historyList.size < 3) {
                    for (i in historyList.indices)
                        list.add(historyList[i])
                } else {
                    for (i in 0..2)
                        list.add(historyList[i])
                }
                adapter.setItems(list)
            }
        }
    }

    override fun onItemClick(item: HistoryItem) {
        viewModel.onHistoryDetail(item)
    }

    override fun onItemClick(item: RowsItem) {
        val url = ApiModule.BASE_URL_RESOURCES + item.href
        val intent = Intent(requireContext(), PdfActivity::class.java)
        intent.putExtra(PdfActivity.ARG_DATA, url)
        intent.putExtra(PdfActivity.ARG_FROM, "DOCUMENT")
        requireContext().startActivity(intent)
    }
}