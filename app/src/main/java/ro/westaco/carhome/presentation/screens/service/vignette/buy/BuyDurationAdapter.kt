package ro.westaco.carhome.presentation.screens.service.vignette.buy

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.RovignetteDuration
import ro.westaco.carhome.data.sources.remote.responses.models.VignettePrice


class BuyDurationAdapter(
    context: Context,
    repeatPos: Int,
    listener: OnItemInteractionListener?,
    cars: ArrayList<RovignetteDuration>,
    priceList: ArrayList<VignettePrice>,

    ) : RecyclerView.Adapter<BuyDurationAdapter.ViewHolder>() {

    var context: Context? = null
    var selectPos: Int? = 0
    var cars = ArrayList<RovignetteDuration>()
    private var priceList = ArrayList<VignettePrice>()
    var interFace: OnItemInteractionListener? = null

    init {
        this.context = context
        this.selectPos = repeatPos
        this.interFace = listener
        this.cars = cars
        this.priceList = priceList
    }

    interface OnItemInteractionListener {
        fun onItemClick(position: Int, model: VignettePrice)
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int = priceList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = inflater.inflate(R.layout.duration_items, parent, false)
        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var button: ImageView = itemView.findViewById(R.id.mImage)
        private var mText: TextView = itemView.findViewById(R.id.mText)
        private var mMoney: TextView = itemView.findViewById(R.id.mMoney)
        private var mNull: TextView = itemView.findViewById(R.id.mNull)

        @SuppressLint("NewApi", "SimpleDateFormat", "RestrictedApi", "LogNotTimber", "SetTextI18n")

        fun bind(position: Int) {


            mMoney.text =
                priceList[position].paymentValue.toString() + " " + priceList[position].paymentCurrency.toString()
                    .lowercase() +
                        " (" + priceList[position].originalValue.toString() + " " + priceList[position].originalCurrency.toString() + ")"

            for (i in cars.indices) {
                if (priceList[position].vignetteDurationCode == cars[i].code) {
                    mText.text = cars[i].description
                    mNull.text =
                        cars[i].timeUnitCount.toString() + " " + cars[i].timeUnit + " (" + priceList[position].paymentValue.toString() + " " + priceList[position].paymentCurrency.toString() + ")"
                }
            }

            if (selectPos == position) button.setImageResource(R.drawable.radio_checked)

            itemView.setOnClickListener {
                button.setImageResource(R.drawable.radio_checked)
                interFace?.onItemClick(position, priceList[position])
                BuyVignetteFragment.mDurationText?.text = mNull.text.toString().lowercase()
            }

        }

    }

}