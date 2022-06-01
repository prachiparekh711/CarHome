package ro.westaco.carhome.presentation.screens.reminder

import android.annotation.SuppressLint
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_reminder.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem
import ro.westaco.carhome.data.sources.remote.responses.models.ListItem
import ro.westaco.carhome.data.sources.remote.responses.models.ListSection
import ro.westaco.carhome.data.sources.remote.responses.models.Reminder
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.utils.DateTimeUtils.getNowSeconds
import ro.westaco.carhome.utils.DateTimeUtils.getTitleDate
import ro.westaco.carhome.utils.Progressbar
import java.text.SimpleDateFormat
import java.util.*


//C- Edit & Delete Reminder
//C- Reformatting according to tags filter
@AndroidEntryPoint
class ReminderFragment : BaseFragment<ReminderViewModel>() {

    private var adapter: DateReminderAdapter? = null
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
            val tagAdapter = TagFilterAdapter(
                requireActivity()
            )
            tagAdapter.clearAll()
            allFilterList.clear()
            if (tags != null) {
                allFilterList = tags
            }
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
            tagAdapter.getSelectedTagsLiveData().observe(this) { selectedTags ->
                val filteredList: ArrayList<Reminder> = filterList(selectedTags)
                sortList(filteredList)
            }
        }


        viewModel.remindersLiveData.observe(viewLifecycleOwner) { reminders ->
            if (reminders.isNullOrEmpty()) {
                list.visibility = View.GONE
            } else {
                list.visibility = View.VISIBLE
                val swipeInterface = object : DateReminderAdapter.SwipeActions {
                    override fun onDelete(item: Reminder) {
                        viewModel.onDelete(item)
                    }
                    override fun onUpdate(item: Reminder) {
                        viewModel.onUpdate(item)
                    }
                }

                adapter = DateReminderAdapter(
                    viewModel,
                    arrayListOf(),
                    swipeInterface
                )
                list.adapter = adapter
                this.reminderList = reminders
                sortList(reminderList)
            }
            progressbar?.dismissPopup()
        }
    }

    private fun filterList(
        selectedTags: ArrayList<CatalogItem>
    ): ArrayList<Reminder> {
        val filteredList: ArrayList<Reminder> = ArrayList()
        reminderList.forEach { reminder ->
            if (reminder.tags != null) {
                val result = reminder.tags.flatMap { id ->
                    selectedTags.filter { tag -> tag.id == id }
                }
                if (result.size == getSelectedTagsSize(selectedTags)) {
                    filteredList.add(reminder)
                }
            }
        }
        return filteredList
    }

    private fun getSelectedTagsSize(selectedTags: ArrayList<CatalogItem>): Int {
        val allTag = selectedTags.find {
            it.name == "All"
        }
        return if (allTag != null) {
            selectedTags.size - 1
        } else {
            selectedTags.size
        }
    }

    private val listItems = ArrayList<ListItem>(reminderList.size)

    @SuppressLint("SimpleDateFormat")
    private fun sortList(reminderList: ArrayList<Reminder>) {
        reminderList.sortWith { o1, o2 ->
            if (o1.dueTime != null && o2.dueTime != null) {
                val date1 =
                    SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(o1.dueDate + " " + o1.dueTime)
                val date2 =
                    SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(o2.dueDate + " " + o2.dueTime)
                date1.compareTo(date2)
            } else {
                val date1 = SimpleDateFormat("yyyy-MM-dd").parse(o1.dueDate)
                val date2 = SimpleDateFormat("yyyy-MM-dd").parse(o2.dueDate)
                date1.compareTo(date2)
            }

        }
        if (adapter != null) {
            adapter?.clearAll()
            listItems.clear()
            var prevCode = ""
            val now = getNowSeconds()
            val today = getTitleDate(now, requireContext(), true)
            reminderList.forEach {
                val date = SimpleDateFormat("yyyy-MM-dd").parse(it.dueDate)
                val code = getTitleDate(date.time, requireContext(), false)
                if (code != prevCode) {
                    val titleItem = getTitleDate(date.time, requireContext(), false)
                    val day = getTitleDate(date.time, requireContext(), true)
                    val isToday = day == today
                    val listSection =
                        ListSection(titleItem, code, isToday, !isToday && date.time < now)
                    listItems.add(listSection)
                    prevCode = code
                }
                listItems.add(it)
            }
            adapter?.setItems(listItems, allFilterList)
            snapToCurrentReminder()
        }
    }

    private fun snapToCurrentReminder() {
        val smoothScroller: SmoothScroller = object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }
        smoothScroller.targetPosition = getPositionOfTheClosestTimePeriod()
        list.layoutManager?.startSmoothScroll(smoothScroller)
    }

    private fun getPositionOfTheClosestTimePeriod(): Int {
        var position = 0
        var minTimePeriod = Long.MAX_VALUE
        listItems.forEachIndexed { index, listItem ->
            if (listItem is Reminder) {
                val date = SimpleDateFormat("yyyy-MM-dd").parse(listItem.dueDate)
                val nowTime = Calendar.getInstance()
                val neededTime = Calendar.getInstance()
                neededTime.timeInMillis = date.time
                val timeDiff = neededTime.timeInMillis - nowTime.timeInMillis
                if (timeDiff in 0 until minTimePeriod) {
                    minTimePeriod = timeDiff
                    position = index
                }
            }
        }
        if (listItems.size != 0) {
            for (iterator in position downTo 0) {
                if (listItems[iterator] is ListSection) {
                    position = iterator
                    break
                }
            }
        }
        return position
    }

}