package ro.westaco.carhome.presentation.screens.home.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
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
        private var mainCard: CardView = itemView.findViewById(R.id.mainCard)
        private var logo: AppCompatImageView = itemView.findViewById(R.id.logo)
        private var carNo: TextView = itemView.findViewById(R.id.carNo)
        private var rostatus: TextView = itemView.findViewById(R.id.rostatus)
        private var roexpire: TextView = itemView.findViewById(R.id.roexpire)
        private var ivImg: ImageView = itemView.findViewById(R.id.iv_alrt)


        fun bind(position: Int) {
            val item = cars[position]
            if (item.vehicleBrand.isNullOrEmpty() && item.model.isNullOrEmpty())
                makeAndModel.text = "N/A"
            else
                makeAndModel.text = "${item.vehicleBrand ?: ""} ${item.model ?: ""}"
            carNo.text = item.licensePlate

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

            if (!item.vignetteExpirationDate.isNullOrEmpty()) {

                var serverDate: Date = try {
                    originalFormatList.parse(item.vignetteExpirationDate) as Date
                } catch (e: Exception) {
                    val originalFormat = SimpleDateFormat(
                        context.getString(R.string.server_standard_datetime_format_template1),
                        Locale.US
                    )
                    originalFormat.parse(item.vignetteExpirationDate) as Date
                }

                val timeLeftMillis = serverDate.time - Date().time
                val timeLeftMillisPos =
                    if (timeLeftMillis < 0) -timeLeftMillis else timeLeftMillis
                val daysLeft = TimeUnit.MILLISECONDS.toDays(timeLeftMillisPos)

                if (timeLeftMillis > 0) {

                    rostatus.setBackgroundResource(R.drawable.active_status_back)
                    rostatus.text = context.getString(R.string.status_active)
                    when {
                        daysLeft in 0..7 -> {
                            ivImg.setImageResource(R.drawable.ic_error_status)
                            roexpire.setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.colore_maroon
                                )
                            )
                            roexpire.text =
                                context.getString(R.string.expires_on) + "\u0020" + setDayMonthFormat(
                                    item.vignetteExpirationDate
                                )

                        }
                        daysLeft in 8..30 -> {
                            ivImg.setImageResource(R.drawable.ic_clock_status)
                            roexpire.setTextColor(ContextCompat.getColor(context, R.color.yellow))
                            roexpire.text =
                                context.getString(R.string.expires_on) + "\u0020" + setDayMonthFormat(
                                    item.vignetteExpirationDate
                                )

                        }
                        daysLeft > 30 -> {
                            ivImg.setImageResource(R.drawable.ic_emoji_shape)
                            roexpire.setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.greenActive
                                )
                            )
                            roexpire.text =
                                context.getString(R.string.expires_on) + "\u0020" + setDayMonthFormat(
                                    item.vignetteExpirationDate
                                )

                        }
                        daysLeft < 0 -> {
                            ivImg.setImageResource(R.drawable.ic_emoji_inactive)
                            roexpire.setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.delete_dialog_color
                                )
                            )
                            roexpire.text =
                                context.getString(R.string.expires_on) + "\u0020" + setDayMonthFormat(
                                    item.vignetteExpirationDate
                                )
                        }
                    }

                } else {
                    rostatus.setBackgroundResource(R.drawable.inactive_status_back)
                    rostatus.text = context.getString(R.string.purchases_exp_inactive)
                }


            } else {
                ivImg.setImageResource(R.drawable.ic_emoji_inactive)
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


    var originalFormatList = SimpleDateFormat(
        context.getString(R.string.server_standard_datetime_format_template),
        Locale.US
    )


    @SuppressLint("SimpleDateFormat")
    @Throws(ParseException::class)
    fun setDayMonthFormat(unformattedDate: String?): String? {
        @SuppressLint("SimpleDateFormat") val dateformat =
            SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss.SSS\'Z\'").parse(unformattedDate)
        return SimpleDateFormat("dd/MM/yyyy").format(dateformat)
    }
}