package ro.westaco.carhome.presentation.screens.data.cars.details

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.requests.VehicleEvent
import ro.westaco.carhome.data.sources.remote.responses.models.EventType
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class CarDetailsReminderAdapter(
    private val context: Context,
    private var eventsList: ArrayList<VehicleEvent>,
    private var eventsTypeList: ArrayList<EventType>
) : RecyclerView.Adapter<CarDetailsReminderAdapter.ViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int = eventsList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view1: View = inflater.inflate(R.layout.item_car_details_reminder, parent, false)
        return ViewHolder(view1)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var title: TextView = itemView.findViewById(R.id.title)
        private var nextDueDate: TextView = itemView.findViewById(R.id.nextDueDate)

        @SuppressLint("UseSwitchCompatOrMaterialCode")
        private var switchButton: Switch = itemView.findViewById(R.id.switch_button)

        @SuppressLint("SimpleDateFormat", "SetTextI18n")
        fun bind(pos: Int) {

            val item = eventsList[pos]


            for (i in eventsTypeList.indices) {
                if (item.eventType == eventsTypeList[i].id?.toLong()) {
                    title.text = eventsTypeList[i].name
                    break
                }
            }

            val originalFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

            try {

                val serverDate = originalFormat.parse(item.nextDate)

                val timeLeftMillis = serverDate.time - Date().time
                val timeLeftMillisPos = if (timeLeftMillis < 0) -timeLeftMillis else timeLeftMillis
                val daysLeft = TimeUnit.MILLISECONDS.toDays(timeLeftMillisPos)
                val hoursLeft = TimeUnit.MILLISECONDS.toHours(timeLeftMillisPos)
                val minutesLeft = TimeUnit.MILLISECONDS.toMinutes(timeLeftMillisPos)

                var formattedTimeLeft = ""
                if (daysLeft > 0) {
                    formattedTimeLeft = context.getString(R.string.days_template, daysLeft)
                } else if (hoursLeft > 0) {
                    formattedTimeLeft = context.getString(R.string.hours_template, hoursLeft)
                } else if (minutesLeft > 0) {
                    formattedTimeLeft = context.getString(R.string.minutes_template, minutesLeft)
                }

                if (daysLeft <= 20) {
                    if (timeLeftMillis < 0) {
                        formattedTimeLeft =
                            context.getString(R.string.duetime_in_past, formattedTimeLeft)
                        nextDueDate.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.tag_orange
                            )
                        )
                    } else {
                        formattedTimeLeft =
                            context.getString(R.string.duetime_in_future, formattedTimeLeft)
                        nextDueDate.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.orangeExpired
                            )
                        )
                    }
                    nextDueDate.text = formattedTimeLeft
                } else {
                    val parser = SimpleDateFormat("yyyy-MM-dd")
                    val formatter = SimpleDateFormat("dd/MM/yyyy")

                    nextDueDate.text = "Next Due " + formatter.format(parser.parse(item.nextDate))
                }

            } catch (e: Exception) {
                nextDueDate.text = "N/A"
            }


            switchButton.isChecked = item.reminder

        }
    }


    @SuppressLint("NotifyDataSetChanged")
    fun setItems(eventItem: List<VehicleEvent>?) {
        this.eventsList = ArrayList(eventItem ?: listOf())
        notifyDataSetChanged()
    }
}