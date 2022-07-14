package ro.westaco.carhome.presentation.screens.reminder

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.zerobranch.layout.SwipeLayout
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem
import ro.westaco.carhome.data.sources.remote.responses.models.ListItem
import ro.westaco.carhome.data.sources.remote.responses.models.ListSection
import ro.westaco.carhome.data.sources.remote.responses.models.Reminder
import ro.westaco.carhome.presentation.screens.home.HomeViewModel
import ro.westaco.carhome.utils.CatalogUtils
import ro.westaco.carhome.utils.DateTimeUtils
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class DateReminderAdapter(
    private var viewModel: ViewModel,
    private var listItems: ArrayList<ListItem>,
    swipeInterface: SwipeActions
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val ARG_IS_EDIT = "arg_is_edit"
    private val MONTHHEADER = 1
    private val DATAVIEW = 2
    private val ACTION_UP = 1


    var swipeInterface: SwipeActions? = null
    private var tagsCatalog: ArrayList<CatalogItem>? = ArrayList()
    private var invisibleItems: ArrayList<ListItem> = ArrayList()

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): RecyclerView.ViewHolder {
        return if (i == DATAVIEW) {

            // view for normal data.
            val view: View = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_reminder, viewGroup, false)
            ViewHolder(view)
        } else {

            // view type for month or date header
            val view: View = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.reminder_date_item, viewGroup, false)
            ViewHolder1(view)
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, i: Int) {
        if (viewHolder.itemViewType == DATAVIEW) {
            val holder = viewHolder as ViewHolder
            holder.bind(i)
        } else {
            val holder = viewHolder as ViewHolder1
            holder.bind(i)
        }
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    override fun getItemViewType(position: Int) =
        if (listItems[position] is ListSection)
            MONTHHEADER
        else
            DATAVIEW


    inner class ViewHolder1(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var title: TextView = itemView.findViewById(R.id.title)
        private var titleLayout: LinearLayout = itemView.findViewById(R.id.titleLayout)
        private var dropArrow: ImageView = itemView.findViewById(R.id.drowArrow)

        fun bind(pos: Int) {
            val item = listItems[pos] as ListSection
            var numberOfElements = getNumberOfElementsAfterSection(item, listItems)
            if (numberOfElements == 0) {
                numberOfElements = getNumberOfElementsAfterSection(item, invisibleItems)
            }
            title.text = item.title + " (" + numberOfElements + ")"
            dropArrow.visibility = View.VISIBLE
            item.isItemVisible?.let {
                if (it) {
                    dropArrow.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
                } else {
                    dropArrow.setImageResource(R.drawable.ic_baseline_keyboard_arrow_right_24)
                }
            }
            titleLayout.setOnClickListener {
                item.isItemVisible?.let { visibility ->
                    changeListItemsWithVisibility(visibility, pos, item)
                    item.isItemVisible = !visibility
                    notifyDataSetChanged()
                }

            }
        }

        private fun getNumberOfElementsAfterSection(
            item: ListSection,
            list: ArrayList<ListItem>
        ): Int {
            var numberOfElements = 0
            var firstItem = list.find {
                it.sectionName == item.sectionName
            }
            var first = list.indexOf(firstItem)
            var lastItem = list.findLast {
                it.sectionName == item.sectionName
            }
            var last = list.indexOf(lastItem)
            for (position in first..last) {
                if (list[position].sectionName == item.sectionName && list[position] !is ListSection)
                    numberOfElements += 1
            }
            return numberOfElements
        }

        private fun changeListItemsWithVisibility(
            visibility: Boolean,
            pos: Int,
            item: ListSection
        ) {
            if (visibility) {
                var removedItems = 0
                val clonedListItems = ArrayList<ListItem>()
                clonedListItems.addAll(listItems)
                clonedListItems.forEachIndexed { index, listItem ->
                    if (listItem.sectionName == item.title && listItem !is ListSection) {
                        invisibleItems.add(listItems[index - removedItems])
                        listItems.removeAt(index - removedItems)
                        removedItems += 1
                    }
                }
            } else {
                var insertedItems = 1
                var removedItems = 0
                val clonedListItems = ArrayList<ListItem>()
                clonedListItems.addAll(invisibleItems)
                clonedListItems.forEachIndexed { index, invisibleItem ->
                    if (invisibleItem.sectionName == item.title) {
                        listItems.add(pos + insertedItems, invisibleItems[index - removedItems])
                        invisibleItems.removeAt(index - removedItems)
                        insertedItems += 1
                        removedItems += 1
                    }
                }
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var title: TextView = itemView.findViewById(R.id.title)
        private var notes: TextView = itemView.findViewById(R.id.notes)
        private var day: TextView = itemView.findViewById(R.id.day)
        private var month: TextView = itemView.findViewById(R.id.month)
        private var year: TextView = itemView.findViewById(R.id.year)
        private var tagIndicator: ImageView =
            itemView.findViewById(R.id.tagIndicator)
        private var tagCircle: ImageView = itemView.findViewById(R.id.tagCircle)
        private var tag: TextView = itemView.findViewById(R.id.tag)
        private var timeLeft: TextView = itemView.findViewById(R.id.timeLeft)
        private var timeLeftCircle: View =
            itemView.findViewById(R.id._separator)
        private var mSwiper: SwipeLayout = itemView.findViewById(R.id.mSwiper)
        private var left_view: ImageView = itemView.findViewById(R.id.left_view)
        private var right_view: ImageView =
            itemView.findViewById(R.id.right_view)
        private var monthAndYearGroup: LinearLayout =
            itemView.findViewById(R.id.monthAndYearGroup)
        private var dragItem: ConstraintLayout =
            itemView.findViewById(R.id.drag_item)
        private var hourTextView: TextView = itemView.findViewById(R.id.hourTextView)
        private var strikeTrough: View = itemView.findViewById(R.id.strikeTrough)

        fun bind(pos: Int) {
            val item = listItems[pos] as Reminder
            item.completed?.let {
                if (it) {
                    strikeTrough.visibility = View.VISIBLE
                    title.setTypeface(null, Typeface.ITALIC)
                    title.setTextColor(Color.GRAY)
                } else {
                    strikeTrough.visibility = View.GONE
                    title.setTypeface(null, Typeface.BOLD)
                    title.setTextColor(Color.parseColor("#303065"))
                }
            }
            title.text = item.title
            notes.text = item.notes
            if (item.tags?.isNotEmpty() == true) {
                val firstTag = item.tags[0]
                val tagCatalog = firstTag?.let { CatalogUtils.findById(tagsCatalog, it) }
                val tagname = tagCatalog?.name
                if (tagname.isNullOrEmpty()) {
                    tag.isVisible = false
                    tagCircle.isInvisible = true
                } else {
                    tag.isVisible = true
                    tagCircle.isVisible = true
                    tag.text = tagname

                    tagIndicator.setColorFilter(
                        Color.parseColor("#" + tagCatalog.color),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                    tagCircle.setColorFilter(
                        Color.parseColor("#" + tagCatalog.color),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )

                }
            } else {
                tag.isVisible = false
                tagCircle.isInvisible = true
            }


            var composedServerDate: Date? = null
            if (item.dueDate != null) {
                composedServerDate = item.dueDate
                if (!item.dueTime.isNullOrEmpty()) {
                    composedServerDate =
                        DateTimeUtils.addStringTimeToDate(item.dueDate, item.dueTime)
                }
            }
            composedServerDate?.let {
                val ctx = itemView.context
                try {
                    day.text = SimpleDateFormat("dd", Locale.US).format(it)
                    month.text = SimpleDateFormat("MMM", Locale.US).format(it)
                    year.text = "'${SimpleDateFormat("yy", Locale.US).format(it)}"
                    hourTextView.text = SimpleDateFormat("HH:mm", Locale.US).format(it)

                    val timeLeftMillis = it.time - Date().time
                    val timeLeftMillisPos =
                        if (timeLeftMillis < 0) -timeLeftMillis else timeLeftMillis
                    val daysLeft = TimeUnit.MILLISECONDS.toDays(timeLeftMillisPos)
                    val hoursLeft = TimeUnit.MILLISECONDS.toHours(timeLeftMillisPos)
                    val minutesLeft = TimeUnit.MILLISECONDS.toMinutes(timeLeftMillisPos)

                    var formattedTimeLeft = ""

                    if (daysLeft > 0) {
                        formattedTimeLeft = ctx.getString(R.string.days_template, daysLeft)
                    } else if (hoursLeft > 0) {
                        formattedTimeLeft =
                            ctx.getString(
                                R.string.hours_template,
                                hoursLeft
                            )
                    } else if (minutesLeft > 0) {
                        formattedTimeLeft = ctx.getString(
                            R.string.minutes_template,
                            minutesLeft
                        )
                    }

                    if (timeLeftMillis < 0) {
                        formattedTimeLeft =
                            ctx.getString(
                                R.string.duetime_in_past,
                                formattedTimeLeft
                            )
                        timeLeft.setTextColor(
                            ContextCompat.getColor(
                                ctx,
                                R.color.tag_pink
                            )
                        )
                    } else {
                        formattedTimeLeft =
                            ctx.getString(
                                R.string.duetime_in_future,
                                formattedTimeLeft
                            )
                        timeLeft.setTextColor(
                            ContextCompat.getColor(
                                ctx,
                                R.color.tag_orange
                            )
                        )
                    }

                    timeLeft.text = formattedTimeLeft
                    timeLeft.isVisible = true
                    timeLeftCircle.isVisible = true

                } catch (e: Exception) {
                    day.text = SimpleDateFormat("dd", Locale.US).format(it)
                    month.text = SimpleDateFormat("MMM", Locale.US).format(it)
                    year.text = "'${SimpleDateFormat("yy", Locale.US).format(it)}"
                    hourTextView.text = "-"

                    timeLeft.isVisible = false
                    timeLeftCircle.isVisible = false
                }
            }


            dragItem.setOnTouchListener { _, p1 ->
                left_view.visibility = View.VISIBLE
                right_view.visibility = View.VISIBLE
                if (p1.action == ACTION_UP) {
                    if (viewModel is ReminderViewModel) {
                        (viewModel as ReminderViewModel).onUpdate(item)
                    }
                    if (viewModel is HomeViewModel) {
                        (viewModel as HomeViewModel).onUpdate(item)
                    }
                }
                true
            }


            mSwiper.setOnActionsListener(object : SwipeLayout.SwipeActionsListener {
                override fun onOpen(direction: Int, isContinuous: Boolean) {
                    if (direction == SwipeLayout.RIGHT) {
                        tagIndicator.isInvisible = true
                    }
                }

                override fun onClose() {
                    tagIndicator.isInvisible = false
                }
            })

            left_view.setOnClickListener {
                swipeInterface?.onUpdate(item)
            }

            right_view.setOnClickListener {
                swipeInterface?.onDelete(item)
            }
        }
    }

    init {
        this.swipeInterface = swipeInterface
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(
        reminders: List<ListItem>?,
        tags: List<CatalogItem>,
        invisibleListItems: List<ListItem>?
    ) {
        this.invisibleItems = ArrayList(invisibleListItems ?: listOf())
        this.listItems = ArrayList(reminders ?: listOf())
        this.tagsCatalog = ArrayList(tags)

        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearAll() {
        this.listItems.clear()
        this.invisibleItems.clear()
        notifyDataSetChanged()
    }

    interface SwipeActions {
        fun onDelete(item: Reminder)
        fun onUpdate(item: Reminder)
    }


}