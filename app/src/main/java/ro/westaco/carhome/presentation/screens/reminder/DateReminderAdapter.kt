package ro.westaco.carhome.presentation.screens.reminder

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.zerobranch.layout.SwipeLayout
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem
import ro.westaco.carhome.data.sources.remote.responses.models.ListItem
import ro.westaco.carhome.data.sources.remote.responses.models.ListSection
import ro.westaco.carhome.data.sources.remote.responses.models.Reminder
import ro.westaco.carhome.utils.CatalogUtils
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class DateReminderAdapter(
    private var listItems: ArrayList<ListItem>,
    swipeInterface: SwipeActions
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private val MONTHHEADER = 1
    private val DATAVIEW = 2

    var swipeInterface: SwipeActions? = null
    private var tagsCatalog: ArrayList<CatalogItem>? = ArrayList()

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
        private var title: TextView = itemView.findViewById(ro.westaco.carhome.R.id.title)

        fun bind(pos: Int) {
            val item = listItems[pos] as ListSection
            title.text = item.title
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var title: TextView = itemView.findViewById(ro.westaco.carhome.R.id.title)
        private var notes: TextView = itemView.findViewById(ro.westaco.carhome.R.id.notes)
        private var day: TextView = itemView.findViewById(ro.westaco.carhome.R.id.day)
        private var month: TextView = itemView.findViewById(ro.westaco.carhome.R.id.month)
        private var year: TextView = itemView.findViewById(ro.westaco.carhome.R.id.year)
        private var tagIndicator: ImageView =
            itemView.findViewById(ro.westaco.carhome.R.id.tagIndicator)
        private var tagCircle: ImageView = itemView.findViewById(ro.westaco.carhome.R.id.tagCircle)
        private var tag: TextView = itemView.findViewById(ro.westaco.carhome.R.id.tag)
        private var timeLeft: TextView = itemView.findViewById(ro.westaco.carhome.R.id.timeLeft)
        private var timeLeftCircle: View =
            itemView.findViewById(ro.westaco.carhome.R.id._separator)
        private var mSwiper: SwipeLayout = itemView.findViewById(ro.westaco.carhome.R.id.mSwiper)
        private var left_view: ImageView = itemView.findViewById(ro.westaco.carhome.R.id.left_view)
        private var right_view: ImageView =
            itemView.findViewById(ro.westaco.carhome.R.id.right_view)
        private var monthAndYearGroup: LinearLayout =
            itemView.findViewById(ro.westaco.carhome.R.id.monthAndYearGroup)

        @SuppressLint("SetTextI18n")
        fun bind(pos: Int) {

            val item = listItems[pos] as Reminder
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


            var composedServerDate: String? = null
            if (!item.dueDate.isNullOrEmpty()) {
                composedServerDate = item.dueDate
                if (!item.dueTime.isNullOrEmpty()) {
                    composedServerDate += " " + item.dueTime
                }
            }
            composedServerDate?.let {
                val ctx = itemView.context
                try {
                    val originalFormat = SimpleDateFormat(
                        ctx.getString(R.string.server_datetime_format_template),
                        Locale.US
                    )
                    val serverDate = originalFormat.parse(it)

                    day.text = SimpleDateFormat("dd", Locale.US).format(serverDate)
                    month.text = SimpleDateFormat("MMM", Locale.US).format(serverDate)
                    year.text = "'${SimpleDateFormat("yy", Locale.US).format(serverDate)}"

                    val timeLeftMillis = serverDate.time - Date().time
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
                    val originalFormat = SimpleDateFormat(
                        ctx.getString(ro.westaco.carhome.R.string.server_date_format_template),
                        Locale.US
                    )
                    val serverDate = originalFormat.parse(it)!!
                    day.text = SimpleDateFormat("dd", Locale.US).format(serverDate)
                    month.text = SimpleDateFormat("MMM", Locale.US).format(serverDate)
                    year.text = "${SimpleDateFormat("yy", Locale.US).format(serverDate)}"
                    timeLeft.isVisible = false
                    timeLeftCircle.isVisible = false
                }

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
    fun setItems(reminders: List<ListItem>?, tags: List<CatalogItem>) {
        this.listItems = ArrayList(reminders ?: listOf())
        this.tagsCatalog = ArrayList(tags)

        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearAll() {
        this.listItems.clear()
        notifyDataSetChanged()
    }

    interface SwipeActions {
        fun onDelete(item: Reminder)
        fun onUpdate(item: Reminder)
    }


}