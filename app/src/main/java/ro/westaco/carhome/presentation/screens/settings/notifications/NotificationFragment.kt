package ro.westaco.carhome.presentation.screens.settings.notifications

import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_notification.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem
import ro.westaco.carhome.data.sources.remote.responses.models.ListItem
import ro.westaco.carhome.data.sources.remote.responses.models.ListSection
import ro.westaco.carhome.data.sources.remote.responses.models.Notification
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.utils.DateTimeUtils
import ro.westaco.carhome.utils.DateTimeUtils.getNowSeconds
import ro.westaco.carhome.utils.DateTimeUtils.getTitleDate
import ro.westaco.carhome.views.Progressbar
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
    var notificationFiltersAdapter: NotificationFilterAdapter? = null

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
        mRecycler.layoutManager = LinearLayoutManager(context)
        adapter = NotificationAdapter(requireContext(), arrayListOf(), this)
        mRecycler.adapter = adapter

        notificationFiltersAdapter = NotificationFilterAdapter(requireContext())
        notificationFiltersAdapter?.data = NotificationType.toCatalogItemArray(requireContext())
        notificationFiltersAdapter?.setAllSelected()
        notificationFiltersRV.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        notificationFiltersRV.adapter = notificationFiltersAdapter
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
        }

        viewModel.actionStream.observe(viewLifecycleOwner) {
            when (it) {
                is NotificationViewModel.ACTION.OnBackOfSuccess -> {

                    Handler(Looper.getMainLooper()).postDelayed({
                        progressbar?.dismissPopup()
                    }, 1000)
                }
            }
        }
        notificationFiltersAdapter?.getSelectedTagsLiveData()
            ?.observe(viewLifecycleOwner) { selectedTag ->
                val filteredNotifications = filterNotifications(selectedTag)
                sortList(filteredNotifications)
            }
    }

    private fun filterNotifications(selectedTag: CatalogItem): ArrayList<Notification> {
        val containsAllTag = selectedTag.name == getString(R.string.all_notifications)
        if (containsAllTag)
            return notificationList.toMutableList() as ArrayList
        val containsReadTag = selectedTag.name == getString(R.string.read_notifications)
        if (containsReadTag) {
            val readNotifications = ArrayList<Notification>()
            notificationList.forEach { notification ->
                notification.seenAt?.let { seenAt ->
                    readNotifications.add(notification)
                }
            }
            return readNotifications
        }
        val unreadNotifications = ArrayList<Notification>()
        notificationList.forEach { notification ->
            if (notification.seenAt == null) {
                unreadNotifications.add(notification)
            }
        }
        return unreadNotifications
    }

    var idList: ArrayList<Int> = arrayListOf()
    private val listItems = ArrayList<ListItem>(notificationList.size)
    private fun sortList(notificationList: ArrayList<Notification>) {


        val allList: ArrayList<Notification> = ArrayList()
        val todayList: ArrayList<Notification> = ArrayList()
        notificationList.forEach {
            val date = it.scheduleAt?.let { it1 ->
                SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss").parse(
                    it1
                )
            }
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
        todayTV.isVisible = todayList.isEmpty()

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
            progressbar?.showPopup()
            viewModel.markAsSeen(idList)
            notificationFiltersAdapter?.setAllSelected()
        }
    }

    override fun onItemClick(item: Notification) {
        viewModel.onItemClick(item)
    }

}