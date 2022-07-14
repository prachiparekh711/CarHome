package ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.Vehicle
import ro.westaco.carhome.di.ApiModule
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class CarsAdapter(
    private val context: Context,
    private var cars: ArrayList<Vehicle>,
    private val listener: OnSelectCarListner? = null,
    val activeService: String
) : RecyclerView.Adapter<CarsAdapter.ViewHolder>() {
    var selectedPos: Int = -1

    interface OnSelectCarListner {
        fun onItemClick(item: Vehicle)
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int {
        return cars.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_car_list_, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var mSelected: ImageView = itemView.findViewById(R.id.mSelected)
        private var logo: ImageView = itemView.findViewById(R.id.logo)
        private var makeAndModel: TextView = itemView.findViewById(R.id.makeAndModel)
        private var carNumber: TextView = itemView.findViewById(R.id.carNumber)
        private var policyExpiry: TextView = itemView.findViewById(R.id.policyExpiry)
        private var status: TextView = itemView.findViewById(R.id.status)
        private var serviceTitle: TextView = itemView.findViewById(R.id.serviceTitle)
        private var statusTitle: TextView = itemView.findViewById(R.id.statusTitle)
        private var vehicleBrandCar: TextView = itemView.findViewById(R.id.vehicleBrandCar)

        @SuppressLint("SetTextI18n")
        fun bind(position: Int) {

            val item = cars[position]
            if (item.vehicleBrand.isNullOrEmpty() && item.model.isNullOrEmpty())
                makeAndModel.text = "N/A"
            else
                makeAndModel.text = "${item.vehicleBrand ?: ""} ${item.model ?: ""}"

            carNumber.text = item.licensePlate
            vehicleBrandCar.text = item.registrationCountryName
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

            when (activeService) {
                "RO_VIGNETTE" -> {
                    statusTitle.isVisible = true
                    status.isVisible = true
                    serviceTitle.text = context.resources.getString(R.string.ro_expires)

                    if (item.vignetteExpirationDate.isNullOrEmpty()) {
                        policyExpiry.text = "N/A"
                        status.text = "N/A"
                        policyExpiry.setTextColor(context.resources.getColor(R.color.unselected))
                        status.setTextColor(context.resources.getColor(R.color.unselected))
                    } else {
                        val date: Date = try {
                            val dateFormat: DateFormat =
                                SimpleDateFormat(context.getString(R.string.server_standard_datetime_format_template))
                            dateFormat.parse(item.vignetteExpirationDate)
                        } catch (e: Exception) {
                            val dateFormat: DateFormat =
                                SimpleDateFormat(context.getString(R.string.server_standard_datetime_format_template1))
                            dateFormat.parse(item.vignetteExpirationDate)
                        }

                        val formatter: DateFormat =
                            SimpleDateFormat("dd/MM/yyyy")
                        val dateStr: String =
                            formatter.format(date)
                        policyExpiry.text = dateStr
                        policyExpiry.setTextColor(context.resources.getColor(R.color.text_color))

                        val sdf = SimpleDateFormat("dd/MM/yyyy")
                        val strDate = sdf.parse(dateStr)
                        if (strDate != null) {
                            if (System.currentTimeMillis() > strDate.time) {
                                status.text = context.getString(R.string.status_expires)
                                status.setTextColor(context.resources.getColor(R.color.redExpiredRovii))
                            } else {
                                status.text = context.getString(R.string.status_active)
                                status.setTextColor(context.resources.getColor(R.color.list_time))
                            }
                        }

                    }
                }
                "RO_PASS_TAX" -> {
                    if (item.passTaxLastPurchase.isNullOrEmpty()) {
                        policyExpiry.text = "N/A"
                        policyExpiry.setTextColor(context.resources.getColor(R.color.unselected))
                    } else {
                        policyExpiry.text = item.passTaxLastPurchase
                    }
                    statusTitle.isVisible = false
                    status.isVisible = false
                    serviceTitle.text = context.resources.getString(R.string.no_of_pass_)

                }
            }

            if (selectedPos == position)
                mSelected.setImageResource(R.drawable.ic_selected_done_)
            else
                mSelected.setImageResource(R.drawable.ic_selected_blank_)

            itemView.setOnClickListener {
                val prevSelectedPos = selectedPos
                selectedPos = position
                notifyItemChanged(prevSelectedPos)
                notifyItemChanged(selectedPos)
                listener?.onItemClick(item)
            }

        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(cars: List<Vehicle>?) {
        this.cars = ArrayList(cars ?: listOf())
        notifyDataSetChanged()
    }

    fun setPosition(i: Int) {
        val prevSelectedPos = selectedPos
        selectedPos = i
        notifyItemChanged(prevSelectedPos)
    }

}