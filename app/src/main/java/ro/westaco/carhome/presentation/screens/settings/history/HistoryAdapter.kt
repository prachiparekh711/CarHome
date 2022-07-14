package ro.westaco.carhome.presentation.screens.settings.history

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
import ro.westaco.carhome.data.sources.remote.responses.models.HistoryItem
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(
    private val context: Context,
    private var historyList: ArrayList<HistoryItem>,
    private val listener: OnItemInteractionListener? = null,
    private val from: String? = null
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    interface OnItemInteractionListener {
        fun onItemClick(item: HistoryItem)
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int = historyList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

//        val view = if (from == "SETTING")
        val view = inflater.inflate(R.layout.item_history, parent, false)
//        else
//            inflater.inflate(R.layout.item_history_dm, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var logo: ImageView = itemView.findViewById(R.id.logo)
        private var name: TextView = itemView.findViewById(R.id.name)
        private var carLpn: TextView = itemView.findViewById(R.id.carLpn)
        private var amount: TextView = itemView.findViewById(R.id.amount)
        private var time: TextView = itemView.findViewById(R.id.time)
        private var mView: View = itemView.findViewById(R.id.mView)
        private var mView1: View = itemView.findViewById(R.id.mView1)

        fun bind(position: Int) {
            val item = historyList[position]

            name.text = "${item.serviceName ?: ""}"
            if (item.vehicleBrandName != null && item.vehicleLpn != null) {
                carLpn.text =
                    "${item.vehicleBrandName} ${context.resources.getString(R.string.dot)} ${item.vehicleLpn}"
            } else {
                if (item.vehicleBrandName != null)
                    carLpn.text = item.vehicleBrandName
                if (item.vehicleLpn != null)
                    carLpn.text = item.vehicleLpn
            }
            amount.text = "${item.amount ?: ""} ${item.currency ?: ""}"

            var spf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            val newDate: Date = spf.parse(item.transactionDate)

            /*  if (DateTimeUtils.isSameDay(newDate, Calendar.getInstance().time)) {
                  spf = SimpleDateFormat("HH:mm a")
                  time.text =
                      "${context.resources.getString(R.string.today)} ${spf.format(newDate) ?: ""}"
              } else {*/
            spf = SimpleDateFormat("EEE HH:mm a")
            time.text = "${spf.format(newDate) ?: ""}"
//            }

            val dr = when (item.service) {
                "RO_VIGNETTE" -> R.drawable.ic_rovinieta_dm
                "RO_PASS_TAX" -> R.drawable.ic_bridge_tax_dm
                "RO_RCA" -> R.drawable.ic_insurance_dm
                else -> R.drawable.logo_small
            }

            val options = RequestOptions()
            logo.clipToOutline = true
            Glide.with(context)
                .load(dr)
                .apply(
                    options.fitCenter()
                        .skipMemoryCache(true)
                        .priority(Priority.HIGH)
                        .format(DecodeFormat.PREFER_ARGB_8888)
                )
                .error(dr)
                .into(logo)


            itemView.setOnClickListener {
                listener?.onItemClick(item)
            }

            if (from == "SETTING") {
                mView.isVisible = true
                mView1.isVisible = false
                if (position == historyList.size - 1)
                    mView.isVisible = false
            } else {
                mView.isVisible = false
                mView1.isVisible = true
                if (position == historyList.size - 1)
                    mView1.isVisible = false
            }


        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(historyList: List<HistoryItem>?) {
        this.historyList = ArrayList(historyList ?: listOf())
        notifyDataSetChanged()
    }
}