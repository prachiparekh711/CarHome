package ro.westaco.carhome.presentation.screens.settings.notifications

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_notification.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.ListItem
import ro.westaco.carhome.data.sources.remote.responses.models.ListSection
import ro.westaco.carhome.data.sources.remote.responses.models.Notification
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.utils.DateTimeUtils
import ro.westaco.carhome.utils.DateTimeUtils.getNowSeconds
import ro.westaco.carhome.utils.DateTimeUtils.getTitleDate
import ro.westaco.carhome.utils.Progressbar
import java.text.SimpleDateFormat
import java.util.*


//C-    Notification Section
@AndroidEntryPoint
class NotificationFragment : BaseFragment<NotificationViewModel>(),

    NotificationAdapter.OnItemInteractionListener {

    private lateinit var adapter: NotificationAdapter

    override fun getContentView() = R.layout.fragment_notification
    var progressbar: Progressbar? = null
    var notificationList: ArrayList<Notification> = ArrayList()

    companion object {
        const val TAG = "Notification"
    }

    override fun initUi() {

        back.setOnClickListener {
            viewModel.onBack()
        }

        home.setOnClickListener {
            viewModel.onSettingClick()
        }

        progressbar = Progressbar(requireContext())
        progressbar?.showPopup()
        mRecycler.layoutManager = LinearLayoutManager(context)
        adapter = NotificationAdapter(requireContext(), arrayListOf(), this)
        mRecycler.adapter = adapter
    }

    override fun setObservers() {
        viewModel.notificationLivedata.observe(viewLifecycleOwner) { notificationList ->
            if (notificationList.isNullOrEmpty()) {
                mRecycler.visibility = View.GONE
                mark_read.visibility = View.GONE
                mTodayLL.visibility = View.GONE
                emptyStateGroup.visibility = View.VISIBLE
            } else {
                this.notificationList = notificationList
                mRecycler.visibility = View.VISIBLE
                mark_read.visibility = View.VISIBLE
                emptyStateGroup.visibility = View.GONE

                sortList(notificationList)


            }
            progressbar?.dismissPopup()
        }
    }

    var idList: ArrayList<Int> = arrayListOf()
    private val listItems = ArrayList<ListItem>(notificationList.size)
    private fun sortList(notificationList: ArrayList<Notification>) {


        val allList: ArrayList<Notification> = ArrayList()
        val todayList: ArrayList<Notification> = ArrayList()
        notificationList.forEach {
            val date = SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss").parse(it.scheduleAt)
            if (DateTimeUtils.isSameDay(date, Calendar.getInstance().time)) {
                todayList.add(it)
            } else {
                allList.add(it)
            }
            it.id?.let { it1 -> idList.add(it1) }
        }

        todayList.sortWith { o1, o2 ->
            val date1 = SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss").parse(o1.scheduleAt)
            val date2 = SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss").parse(o2.scheduleAt)
            date1.compareTo(date2)
        }

        allList.sortWith { o1, o2 ->
            val date1 = SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss").parse(o1.scheduleAt)
            val date2 = SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss").parse(o2.scheduleAt)
            date2.compareTo(date1)
        }

        mTodayLL.isVisible = todayList.isEmpty()

        val newList: ArrayList<Notification> = ArrayList()
        newList.addAll(todayList)
        newList.addAll(allList)

        adapter.clearAll()
        listItems.clear()
        var prevCode = ""
        val now = getNowSeconds()
        val today = getTitleDate(now, requireContext(), true)
        newList.forEach {
            val date = SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss").parse(it.scheduleAt)
            val code = getTitleDate(date.time, requireContext(), true)

//            if (date.after(Date()) || isSameDay(date, Calendar.getInstance().time)) {
            if (code != prevCode) {
                val day = getTitleDate(date.time, requireContext(), true)
                val isToday = day == today
                val listSection = ListSection(day, code, isToday, !isToday && date.time < now)
                listItems.add(listSection)
                prevCode = code
            }
            listItems.add(it)
//            }
        }

        adapter.setItems(listItems)

        mark_read.setOnClickListener {
            viewModel.markAsSeen(idList)
        }
    }

    override fun onItemClick(item: Notification) {
        viewModel.onItemClick(item)
    }

}