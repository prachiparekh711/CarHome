package ro.westaco.carhome.presentation.screens.home.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem
import ro.westaco.carhome.data.sources.remote.responses.models.Reminder
import ro.westaco.carhome.utils.CatalogUtils
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ReminderAdapter(
    private var listItems: ArrayList<Reminder>,
    private val listener: OnItemInteractionListener? = null
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var tagsCatalog: ArrayList<CatalogItem>? = ArrayList()

    interface OnItemInteractionListener {
        fun onItemClick(item: Reminder)
    }


    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): RecyclerView.ViewHolder {
        val view: View = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_reminder_home, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, i: Int) {
        val holder = viewHolder as ViewHolder
        holder.bind(i)
    }

    override fun getItemCount(): Int {
        return listItems.size
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var title: TextView = itemView.findViewById(R.id.title)
        private var notes: TextView = itemView.findViewById(R.id.notes)
        private var day: TextView = itemView.findViewById(R.id.day)
        private var month: TextView = itemView.findViewById(R.id.month)
        private var year: TextView = itemView.findViewById(R.id.year)
        private var drag_item: ConstraintLayout = itemView.findViewById(R.id.drag_item)
        private var tagIndicator: ImageView =
            itemView.findViewById(R.id.tagIndicator)
        private var tagCircle: ImageView = itemView.findViewById(R.id.tagCircle)
        private var tag: TextView = itemView.findViewById(R.id.tag)
        private var timeLeft: TextView = itemView.findViewById(R.id.timeLeft)
        private var timeLeftCircle: View =
            itemView.findViewById(R.id._separator)

        private var hourTextView: TextView = itemView.findViewById(R.id.hourTextView)

        fun bind(pos: Int) {
            val item = listItems[pos]
            title.text = item.title
            notes.text = item.notes
            if (item.tags?.isNotEmpty() == true) {
                val firstTag = item.tags[0]
                val tagCatalog = firstTag?.let { CatalogUtils.findById(tagsCatalog, it) }
                val tagName = tagCatalog?.name
                if (tagName.isNullOrEmpty()) {
                    tag.isVisible = false
                    tagCircle.isInvisible = true
                } else {
                    tag.isVisible = true
                    tagCircle.isVisible = true
                    tag.text = tagName

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
                    hourTextView.text = SimpleDateFormat("hh:mm", Locale.US).format(serverDate)

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
                        ctx.getString(R.string.server_date_format_template),
                        Locale.US
                    )
                    val serverDate = originalFormat.parse(it)
                    day.text = SimpleDateFormat("dd", Locale.US).format(serverDate)
                    month.text = SimpleDateFormat("MMM", Locale.US).format(serverDate)
                    year.text = "'${SimpleDateFormat("yy", Locale.US).format(serverDate)}"
                    hourTextView.text = "-"

                    timeLeft.isVisible = false
                    timeLeftCircle.isVisible = false
                }
            }

            drag_item.setOnClickListener {
                listener?.onItemClick(item)
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    fun setItems(reminders: List<Reminder>?, tags: List<CatalogItem>) {
        this.listItems = ArrayList(reminders ?: listOf())
        this.tagsCatalog = ArrayList(tags)

        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearAll() {
        this.listItems.clear()
        notifyDataSetChanged()
    }

}