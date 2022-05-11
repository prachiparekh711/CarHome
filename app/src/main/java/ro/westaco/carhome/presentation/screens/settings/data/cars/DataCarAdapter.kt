package ro.westaco.carhome.presentation.screens.settings.data.cars

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.Vehicle
import ro.westaco.carhome.di.ApiModule
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class DataCarAdapter(
    private val context: Context,
    private var cars: ArrayList<Vehicle>,
    private val listener: OnItemInteractionListener? = null

) : RecyclerView.Adapter<DataCarAdapter.ViewHolder>() {

    interface OnItemInteractionListener {
        fun onItemClick(item: Vehicle)
        fun onBuyClick(item: Vehicle, service: String)
        fun onDocClick(item: Vehicle, service: String)
    }


    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int = cars.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_data_car, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var logo: ImageView = itemView.findViewById(R.id.logo)
        private var makeAndModel: TextView = itemView.findViewById(R.id.makeAndModel)
        private var carNumber: TextView = itemView.findViewById(R.id.carNumber)
        private var statusRca: TextView = itemView.findViewById(R.id.statusRca)
        private var spacerRca: View = itemView.findViewById(R.id.spacerRca)
        private var policyExpiryRca: TextView = itemView.findViewById(R.id.policyExpiryRca)
        private var statusRo: TextView = itemView.findViewById(R.id.statusRo)
        private var spacerRo: View = itemView.findViewById(R.id.spacerRo)
        private var policyExpiryRo: TextView = itemView.findViewById(R.id.policyExpiryRo)
        private var statusBt: TextView = itemView.findViewById(R.id.statusBt)
        private var actionRcaTV: TextView = itemView.findViewById(R.id.actionRcaTV)
        private var actionRoTV: TextView = itemView.findViewById(R.id.actionRoTV)
        private var actionBrTV: TextView = itemView.findViewById(R.id.actionBrTV)
        private var mainLL: LinearLayout = itemView.findViewById(R.id.mainLL)
        private var bridgeLL: LinearLayout = itemView.findViewById(R.id.bridgeLL)
        private var rovinietaLL: LinearLayout = itemView.findViewById(R.id.rovinietaLL)
        private var rcaLL: LinearLayout = itemView.findViewById(R.id.rcaLL)


        private val originalFormat = SimpleDateFormat(
            context.getString(R.string.server_standard_datetime_format_template),
            Locale.US
        )

        @SuppressLint("SetTextI18n")
        fun bind(position: Int) {
            val item = cars[position]
            makeAndModel.text = "${item.vehicleBrand ?: ""} ${item.model ?: ""}"
            carNumber.text = item.licensePlate

            rcaDetails(item)
            roDetails(item)
            brDetails(item)

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

            mainLL.setOnClickListener {
                listener?.onItemClick(item)
            }

            bridgeLL.setOnClickListener {
                listener?.onItemClick(item)
            }

            rovinietaLL.setOnClickListener {
                listener?.onItemClick(item)
            }

            rcaLL.setOnClickListener {
                listener?.onItemClick(item)
            }
        }

        private fun rcaDetails(item: Vehicle) {

            if (!item.policyExpirationDate.isNullOrEmpty()) {
                val serverDate = originalFormat.parse(item.policyExpirationDate)

                val timeLeftMillis = serverDate.time - Date().time
                val timeLeftMillisPos = if (timeLeftMillis < 0) -timeLeftMillis else timeLeftMillis
                val daysLeft = TimeUnit.MILLISECONDS.toDays(timeLeftMillisPos)
                val hoursLeft = TimeUnit.MILLISECONDS.toHours(timeLeftMillisPos)
                val minutesLeft = TimeUnit.MILLISECONDS.toMinutes(timeLeftMillisPos)
                val formattedTimeLeft = when {
                    daysLeft > 0 -> context.getString(R.string.days_template, daysLeft)
                    hoursLeft > 0 -> context.getString(R.string.hours_template, hoursLeft)
                    minutesLeft > 0 -> context.getString(R.string.minutes_template, minutesLeft)
                    else -> ""
                }

                if (timeLeftMillis > 0) {
                    policyExpiryRca.text = context.getString(
                        R.string.purchases_expin_template,
                        formattedTimeLeft
                    )

                    statusRca.setTextColor(ContextCompat.getColor(context, R.color.greenActive))
                    spacerRca.isVisible = true
                    statusRca.text = context.getString(R.string.status_active)
                } else {
                    spacerRca.isVisible = false
                    statusRca.text = context.getString(R.string.purchases_exp_inactive)
                    statusRca.setTextColor(ContextCompat.getColor(context, R.color.redExpired))
                }

                if (item.rcaDocumentHref == null || timeLeftMillis < 0) {
                    actionRcaTV.background =
                        context.resources.getDrawable(R.drawable.buy_background)
                    actionRcaTV.text = context.getString(R.string.buy_vignette_cta)

                    actionRcaTV.setOnClickListener {
                        listener?.onBuyClick(item, "RO_RCA")
                    }
                } else {
                    actionRcaTV.background =
                        context.resources.getDrawable(R.drawable.cta_background)
                    actionRcaTV.text = context.getString(R.string.document)

                    actionRcaTV.setOnClickListener {
                        if (item.rcaDocumentHref != null)
                            listener?.onDocClick(item, "RO_RCA")
                    }
                }
            } else {
                spacerRca.isVisible = false
                statusRca.text = context.getString(R.string.no_info)
                statusRca.setTextColor(ContextCompat.getColor(context, R.color.orangeWarning))
                actionRcaTV.background = context.resources.getDrawable(R.drawable.buy_background)
                actionRcaTV.text = context.getString(R.string.buy_vignette_cta)

                actionRcaTV.setOnClickListener {
                    listener?.onBuyClick(item, "RO_RCA")
                }
            }
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        private fun roDetails(item: Vehicle) {

            if (!item.vignetteExpirationDate.isNullOrEmpty()) {

                val serverDate = originalFormat.parse(item.vignetteExpirationDate)

                val timeLeftMillis = serverDate.time - Date().time
                val timeLeftMillisPos = if (timeLeftMillis < 0) -timeLeftMillis else timeLeftMillis
                val daysLeft = TimeUnit.MILLISECONDS.toDays(timeLeftMillisPos)
                val hoursLeft = TimeUnit.MILLISECONDS.toHours(timeLeftMillisPos)
                val minutesLeft = TimeUnit.MILLISECONDS.toMinutes(timeLeftMillisPos)
                val formattedTimeLeft = when {
                    daysLeft > 0 -> context.getString(R.string.days_template, daysLeft)
                    hoursLeft > 0 -> context.getString(R.string.hours_template, hoursLeft)
                    minutesLeft > 0 -> context.getString(R.string.minutes_template, minutesLeft)
                    else -> ""
                }

                if (timeLeftMillis > 0) {
                    policyExpiryRo.text = context.getString(
                        R.string.purchases_expin_template, formattedTimeLeft
                    )

                    statusRo.setTextColor(ContextCompat.getColor(context, R.color.greenActive))
                    spacerRo.isVisible = true
                    statusRo.text = context.getString(R.string.status_active)
                } else {
                    spacerRo.isVisible = false
                    statusRo.text = context.getString(R.string.purchases_exp_inactive)
                    statusRo.setTextColor(ContextCompat.getColor(context, R.color.redExpired))
                }

                if (item.vignetteTicketHref == null || timeLeftMillis < 0) {
                    actionRoTV.background = context.resources.getDrawable(R.drawable.buy_background)
                    actionRoTV.text = context.getString(R.string.buy_vignette_cta)

                    actionRoTV.setOnClickListener {
                        listener?.onBuyClick(item, "RO_VIGNETTE")
                    }
                } else {
                    actionRoTV.background = context.resources.getDrawable(R.drawable.cta_background)
                    actionRoTV.text = context.getString(R.string.document)
                    actionRoTV.setOnClickListener {
                        if (item.vignetteTicketHref != null)
                            listener?.onDocClick(item, "RO_VIGNETTE")
                    }
                }
            } else {
                spacerRo.isVisible = false
                statusRo.text = context.getString(R.string.no_info)
                statusRo.setTextColor(ContextCompat.getColor(context, R.color.orangeWarning))
                actionRoTV.background = context.resources.getDrawable(R.drawable.buy_background)
                actionRoTV.text = context.getString(R.string.buy_vignette_cta)

                actionRoTV.setOnClickListener {
                    listener?.onBuyClick(item, "RO_VIGNETTE")
                }
            }
        }

        private fun brDetails(item: Vehicle) {
            if (item.passTaxLastPurchase.isNullOrEmpty()) {
                statusBt.setTextColor(ContextCompat.getColor(context, R.color.orangeWarning))
                statusBt.text = context.getString(R.string.no_info)
            } else {
                statusBt.text = item.passTaxLastPurchase
                statusBt.setTextColor(ContextCompat.getColor(context, R.color.greenActive))
            }

            if (item.passTaxDocumentHref == null) {
                actionBrTV.background = context.resources.getDrawable(R.drawable.buy_background)
                actionBrTV.text = context.getString(R.string.buy_vignette_cta)

                actionBrTV.setOnClickListener {
                    listener?.onBuyClick(item, "RO_PASS_TAX")
                }
            } else {
                actionBrTV.background = context.resources.getDrawable(R.drawable.cta_background)
                actionBrTV.text = context.getString(R.string.document)

                actionBrTV.setOnClickListener {
                    if (item.passTaxDocumentHref != null)
                        listener?.onDocClick(item, "RO_PASS_TAX")
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(cars: List<Vehicle>?) {
        this.cars = ArrayList(cars ?: listOf())
        notifyDataSetChanged()
    }
}