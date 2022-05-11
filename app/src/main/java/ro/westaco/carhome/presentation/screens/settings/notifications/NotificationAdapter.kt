package ro.westaco.carhome.presentation.screens.settings.notifications

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.ListItem
import ro.westaco.carhome.data.sources.remote.responses.models.ListSection
import ro.westaco.carhome.data.sources.remote.responses.models.Notification
import ro.westaco.carhome.utils.DateTimeUtils
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(
    private val context: Context,
    private var listItems: ArrayList<ListItem>,
    private val listener: OnItemInteractionListener? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val MONTHHEADER = 1
    private val DATAVIEW = 2

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    interface OnItemInteractionListener {
        fun onItemClick(item: Notification)
    }

    override fun getItemCount(): Int = listItems.size

    override fun getItemViewType(position: Int) =
        if (listItems[position] is ListSection)
            MONTHHEADER
        else
            DATAVIEW

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == DATAVIEW) {

            // view for normal data.
            val view: View = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_notification, viewGroup, false)
            ViewHolder(view)
        } else {

            // view type for month or date header
            val view: View = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.reminder_date_item, viewGroup, false)
            ViewHolder1(view)
        }

    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {

        if (viewHolder.itemViewType == DATAVIEW) {
            val holder = viewHolder as ViewHolder
            holder.bind(position)
        } else {
            val holder = viewHolder as ViewHolder1
            holder.bind(position)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var title: TextView = itemView.findViewById(R.id.title)
        private var body: TextView = itemView.findViewById(R.id.body)
        private var schedule: TextView = itemView.findViewById(R.id.schedule)


        @SuppressLint("NewApi")
        fun bind(position: Int) {
            val item = listItems[position] as Notification
            title.text = item.title
            body.text = item.body

            val date = SimpleDateFormat("yyyy-MM-dd").parse(item.scheduleAt)
            if (DateTimeUtils.isSameDay(date, Calendar.getInstance().time)) {
                schedule.text = "${context.resources.getText(R.string.today)}  ${
                    DateTimeUtils.convertDate(
                        item.scheduleAt,
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US),
                        SimpleDateFormat(
                            "HH:mm a",
                            Locale.US
                        )
                    )
                }"
            } else {
                schedule.text = DateTimeUtils.convertDate(
                    item.scheduleAt,
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US),
                    SimpleDateFormat(
                        "dd MMM, HH:mm a",
                        Locale.US
                    )
                )
            }

            itemView.setOnClickListener {

                listener?.onItemClick(item)
            }

        }
    }

    inner class ViewHolder1(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var title: TextView = itemView.findViewById(ro.westaco.carhome.R.id.title)

        fun bind(pos: Int) {
            val item = listItems[pos] as ListSection
            title.text = item.title
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(reminders: List<ListItem>?) {
        this.listItems = ArrayList(reminders ?: listOf())
        notifyDataSetChanged()
    }

    fun clearAll() {
        this.listItems.clear()
        notifyDataSetChanged()
    }
}