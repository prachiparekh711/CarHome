package ro.westaco.carhome.presentation.screens.home

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.Vehicle
import ro.westaco.carhome.di.ApiModule
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class CarDetailAdapter(
    private val context: Context,
    private var cars: ArrayList<Vehicle>,
    private val listener: OnCarDetailListener? = null
) : RecyclerView.Adapter<CarDetailAdapter.ViewHolder>() {

    interface OnCarDetailListener {
        fun onItemClick(item: Vehicle)
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int {

        return if (cars.size > 3) {
            3
        } else {
            cars.size
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.car_detail_home_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var makeAndModel: TextView = itemView.findViewById(R.id.makeAndModel)
        private var logo: AppCompatImageView = itemView.findViewById(R.id.logo)
        private var carNo: TextView = itemView.findViewById(R.id.carNo)
        private var rcstatus: TextView = itemView.findViewById(R.id.rcstatus)
        private var rostatus: TextView = itemView.findViewById(R.id.rostatus)

        private var rcexpire: TextView = itemView.findViewById(R.id.rcexpire)
        private var roexpire: TextView = itemView.findViewById(R.id.roexpire)
        private var iv_alrt: ImageView = itemView.findViewById(R.id.iv_alrt)
        private var iv_alrt_rc: ImageView = itemView.findViewById(R.id.iv_alrt_rc)


        @SuppressLint("SetTextI18n")
        fun bind(position: Int) {
            val item = cars[position]
            makeAndModel.text = "${item.vehicleBrand ?: ""} ${item.model ?: ""}"
            carNo.text = context.resources.getString(R.string.car_no_str, item.licensePlate)

            itemView.setOnClickListener {
                listener?.onItemClick(item)
            }

            val options = RequestOptions()
            logo.clipToOutline = true
            Glide.with(context)
                .load(ApiModule.BASE_URL_RESOURCES + item.vehicleBrandLogo)
                .apply(
                    options.fitCenter()
                        .skipMemoryCache(true)
                        .priority(Priority.HIGH)
                        .format(DecodeFormat.PREFER_ARGB_8888)
                )
                .error(R.drawable.carhome_icon_roviii)
                .into(logo)


            /**
             * rca status
             */
            /*if (!item.policyExpirationDate.isNullOrEmpty()) {

                val serverDate = originalFormat.parse(item.policyExpirationDate)
                val timeLeftMillis = serverDate.time - Date().time
                val timeLeftMillisPos =
                    if (timeLeftMillis < 0) -timeLeftMillis else timeLeftMillis
                val daysLeft = TimeUnit.MILLISECONDS.toDays(timeLeftMillisPos)
                val hoursLeft = TimeUnit.MILLISECONDS.toHours(timeLeftMillisPos)
                val minutesLeft = TimeUnit.MILLISECONDS.toMinutes(timeLeftMillisPos)
                val formattedTimeLeft = when {
                    daysLeft > 0 -> context.getString(R.string.days_template, daysLeft)
                    hoursLeft > 0 -> context.getString(R.string.hours_template, hoursLeft)
                    minutesLeft > 0 -> context.getString(
                        R.string.minutes_template,
                        minutesLeft
                    )
                    else -> ""
                }


                if (timeLeftMillis > 0) {

                    rcstatus.setBackgroundResource(R.drawable.active_status_back)
                    rcstatus.text = context.getString(R.string.status_active)

                    when {
                        daysLeft in 0..7 -> {
                            iv_alrt_rc.setImageResource(R.drawable.ic_error_status)
                            rcexpire.setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.colore_maroon
                                )
                            )
                            rcexpire.text =
                                "Expires on " + setdaymonthFormat(item.policyExpirationDate)

                        }
                        daysLeft in 8..30 -> {
                            iv_alrt_rc.setImageResource(R.drawable.ic_clock_status)
                            rcexpire.setTextColor(ContextCompat.getColor(context, R.color.yellow))
                            rcexpire.text =
                                "Expires on " + setdaymonthFormat(item.policyExpirationDate)

                        }
                        daysLeft > 30 -> {
                            iv_alrt_rc.setImageResource(R.drawable.ic_emoji_shape)
                            rcexpire.setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.greenActive
                                )
                            )
                            rcexpire.text =
                                "Expires on " + setdaymonthFormat(item.policyExpirationDate)

                        }
                        daysLeft < 0 -> {
                            iv_alrt_rc.setImageResource(R.drawable.ic_emoji_inactive)
                            rcexpire.setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.delete_dialog_color
                                )
                            )
                            rcexpire.text =
                                "Expires on " + setdaymonthFormat(item.policyExpirationDate)
                        }
                    }

                } else {

                    rcstatus.setBackgroundResource(R.drawable.inactive_status_back)
                    rcstatus.text = context.getString(R.string.purchases_exp_inactive)

                }


            } else {

                iv_alrt_rc.setImageResource(R.drawable.ic_emoji_inactive)
                rcexpire.text = context.getString(R.string.no_info)
                rcexpire.setTextColor(ContextCompat.getColor(context, R.color.gray))
                rcstatus.setBackgroundResource(R.drawable.inactive_status_back)
                rcstatus.text = "N/A"

            }*/


            /**
             * rovinieta status
             */
            if (!item.vignetteExpirationDate.isNullOrEmpty()) {

                val serverDate = originalFormat.parse(item.vignetteExpirationDate)

                val timeLeftMillis = serverDate.time - Date().time
                val timeLeftMillisPos =
                    if (timeLeftMillis < 0) -timeLeftMillis else timeLeftMillis
                val daysLeft = TimeUnit.MILLISECONDS.toDays(timeLeftMillisPos)
                val hoursLeft = TimeUnit.MILLISECONDS.toHours(timeLeftMillisPos)
                val minutesLeft = TimeUnit.MILLISECONDS.toMinutes(timeLeftMillisPos)
                val formattedTimeLeft = when {
                    daysLeft > 0 -> context.getString(R.string.days_template, daysLeft)
                    hoursLeft > 0 -> context.getString(
                        R.string.hours_template,
                        hoursLeft
                    )
                    minutesLeft > 0 -> context.getString(
                        R.string.minutes_template,
                        minutesLeft
                    )
                    else -> ""
                }



                if (timeLeftMillis > 0) {

                    rostatus.setBackgroundResource(R.drawable.active_status_back)
                    rostatus.text = context.getString(R.string.status_active)


                    when {
                        daysLeft in 0..7 -> {

                            iv_alrt.setImageResource(R.drawable.ic_error_status)

                            roexpire.setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.colore_maroon
                                )
                            )
                            roexpire.text =
                                context.getString(R.string.expires_on) + "\u0020" + setdaymonthFormat(
                                    item.vignetteExpirationDate
                                )

                        }
                        daysLeft in 8..30 -> {
                            iv_alrt.setImageResource(R.drawable.ic_clock_status)
                            roexpire.setTextColor(ContextCompat.getColor(context, R.color.yellow))
                            roexpire.text =
                                context.getString(R.string.expires_on) + "\u0020" + setdaymonthFormat(
                                    item.vignetteExpirationDate
                                )

                        }
                        daysLeft > 30 -> {
                            iv_alrt.setImageResource(R.drawable.ic_emoji_shape)
                            roexpire.setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.greenActive
                                )
                            )
                            roexpire.text =
                                context.getString(R.string.expires_on) + "\u0020" + setdaymonthFormat(
                                    item.vignetteExpirationDate
                                )

                        }
                        daysLeft < 0 -> {
                            iv_alrt.setImageResource(R.drawable.ic_emoji_inactive)
                            roexpire.setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.delete_dialog_color
                                )
                            )
                            roexpire.text =
                                context.getString(R.string.expires_on) + "\u0020" + setdaymonthFormat(
                                    item.vignetteExpirationDate
                                )
                        }
                    }

                } else {
                    rostatus.setBackgroundResource(R.drawable.inactive_status_back)
                    rostatus.text = context.getString(R.string.purchases_exp_inactive)

                }


            } else {
                iv_alrt.setImageResource(R.drawable.ic_emoji_inactive)
                roexpire.setTextColor(ContextCompat.getColor(context, R.color.gray))
                roexpire.text = context.getString(R.string.no_info)
                rostatus.setBackgroundResource(R.drawable.inactive_status_back)
                rostatus.text = "N/A"
            }


        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(cars: List<Vehicle>) {
        this.cars = ArrayList(cars)
        notifyDataSetChanged()
    }


    val originalFormat = SimpleDateFormat(
        context.getString(R.string.server_standard_datetime_format_template),
        Locale.US
    )


    @SuppressLint("SimpleDateFormat")
    @Throws(ParseException::class)
    fun setdaymonthFormat(unformattedDate: String?): String? {
        @SuppressLint("SimpleDateFormat") val dateformat =
            SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss.SSS\'Z\'").parse(unformattedDate)
        return SimpleDateFormat("dd/MM/yyyy").format(dateformat)
    }
}