package ro.westaco.carhome.presentation.screens.reminder

import android.annotation.SuppressLint
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_reminder.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem
import ro.westaco.carhome.data.sources.remote.responses.models.ListItem
import ro.westaco.carhome.data.sources.remote.responses.models.ListSection
import ro.westaco.carhome.data.sources.remote.responses.models.Reminder
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.interfaceitem.ClickItem
import ro.westaco.carhome.utils.DateTimeUtils.getNowSeconds
import ro.westaco.carhome.utils.DateTimeUtils.getTitleDate
import ro.westaco.carhome.utils.DateTimeUtils.isSameDay
import ro.westaco.carhome.utils.Progressbar
import java.text.SimpleDateFormat
import java.util.*


//C- Edit & Delete Reminder
//C- Reformatting according to tags filter
@AndroidEntryPoint
class ReminderFragment : BaseFragment<ReminderViewModel>() {
    private lateinit var adapter: DateReminderAdapter
    var progressbar: Progressbar? = null
    var reminderList: ArrayList<Reminder> = ArrayList()
    var allFilterList: ArrayList<CatalogItem> = ArrayList()
    var layoutManager: LinearLayoutManager? = null

    companion object {
        const val TAG = "ReminderFragment"
    }

    override fun getContentView() = R.layout.fragment_reminder

    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)

    override fun initUi() {
        progressbar = Progressbar(requireContext())
        progressbar?.showPopup()
        layoutManager = LinearLayoutManager(context)
        list.layoutManager = layoutManager

        fab.setOnClickListener {
            viewModel.onFabClicked()
        }

        notification.setOnClickListener {
            viewModel.onNotificationsClicked()
        }
    }

    override fun setObservers() {
        viewModel.remindersTabData.observe(viewLifecycleOwner) { tags ->

            val clickInterface = ClickItem { pos ->
                adapter.clearAll()
                if (pos != 0) {
                    val serviceID = allFilterList[pos].id
                    val filterLocationList: ArrayList<Reminder> = ArrayList()
                    for (i in reminderList.indices) {
                        val serviceIDList = reminderList[i].tags
                        if (serviceIDList != null) {

                            if (serviceIDList.contains(serviceID)) {
                                filterLocationList.add(reminderList[i])
                            }
                        }
                    }
                    sortList(filterLocationList)
                } else {
                    sortList(reminderList)
                }
            }

            val tagAdapter = TagFilterAdapter(
                requireActivity(),
                clickInterface
            )

            tagAdapter.clearAll()

            allFilterList.clear()
            allFilterList = tags
            if (allFilterList.isNotEmpty()) {
                allFilterList.add(0, CatalogItem(0, "All"))
                tagAdapter.setItems(allFilterList)
                recycler.layoutManager = LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                recycler.adapter = tagAdapter

                viewModel.fetchReminderList()
            }
        }

        viewModel.remindersLiveData.observe(viewLifecycleOwner) { reminders ->
            if (reminders.isNullOrEmpty()) {
                list.visibility = View.GONE
                mTodayLL.visibility = View.VISIBLE
            } else {
                list.visibility = View.VISIBLE
                mTodayLL.visibility = View.GONE
                val swipeInterface = object : DateReminderAdapter.SwipeActions {
                    override fun onDelete(item: Reminder) {
                        viewModel.onDelete(item)
                    }

                    override fun onUpdate(item: Reminder) {
                        viewModel.onUpdate(item)
                    }
                }

                adapter = DateReminderAdapter(
                    arrayListOf(),
                    swipeInterface
                )
                list.adapter = adapter
                this.reminderList = reminders

                sortList(reminderList)

                list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    var hasStarted = false
                    var hasEnded = false
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)

                        if (mTodayLL.isVisible) {
                            notes.isVisible = dy <= 0
                        }

                        if (listItems.isNotEmpty()) {

                            if (mTodayLL.isVisible) {

                                val pos = layoutManager?.findFirstCompletelyVisibleItemPosition()

                                if (pos != null) {

                                    if (pos == 0)

                                        todayTV.text =
                                            requireContext().resources.getString(R.string.today)
                                    else {

                                        val title = listItems[pos]

                                        if (title is ListSection) {
                                            todayTV.text = title.title
                                        }
                                    }
                                }

                            }
                        }

                    }

                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)

                        hasStarted = newState === SCROLL_STATE_DRAGGING
                        hasEnded = newState === SCROLL_STATE_IDLE

                    }

                })
            }
            progressbar?.dismissPopup()
        }
    }


    private val listItems = ArrayList<ListItem>(reminderList.size)

    @SuppressLint("SimpleDateFormat")
    private fun sortList(reminderList: ArrayList<Reminder>) {

        val allList: ArrayList<Reminder> = ArrayList()
        val todayList: ArrayList<Reminder> = ArrayList()
        reminderList.forEach {
            if (!it.dueTime.isNullOrEmpty() && !it.dueDate.isNullOrEmpty()) {
                val date = SimpleDateFormat("yyyy-MM-dd").parse(it.dueDate)
                if (isSameDay(date, Calendar.getInstance().time)) {
                    todayList.add(it)
                } else {
                    allList.add(it)
                }
            }
        }

        todayList.sortWith { o1, o2 ->
            val date1 = SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(o1.dueDate + " " + o1.dueTime)
            val date2 = SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(o2.dueDate + " " + o2.dueTime)
            date1.compareTo(date2)
        }

        allList.sortWith { o1, o2 ->
            val date1 = SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(o1.dueDate + " " + o1.dueTime)
            val date2 = SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(o2.dueDate + " " + o2.dueTime)
            date2.compareTo(date1)
        }

        mTodayLL.isVisible = todayList.isEmpty()

        val newList: ArrayList<Reminder> = ArrayList()
        newList.addAll(todayList)
        newList.addAll(allList)

        adapter.clearAll()
        listItems.clear()
        var prevCode = ""
        val now = getNowSeconds()
        val today = getTitleDate(now, requireContext())
        newList.forEach {
            val date = SimpleDateFormat("yyyy-MM-dd").parse(it.dueDate)
            val code = getTitleDate(date.time, requireContext())

//            if (date.after(Date()) || isSameDay(date, Calendar.getInstance().time)) {
            if (code != prevCode) {
                val day = getTitleDate(date.time, requireContext())
                val isToday = day == today
                val listSection = ListSection(day, code, isToday, !isToday && date.time < now)
                listItems.add(listSection)
                prevCode = code
            }
            listItems.add(it)
//            }
        }
        adapter.setItems(listItems, allFilterList)

    }


}