package ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.RovignetteDuration
import ro.westaco.carhome.data.sources.remote.responses.models.VignettePrice


class BuyDurationAdapter(
    val context: Context,
    val listener: OnItemInteractionListener?,
    var durationList: ArrayList<RovignetteDuration>


) : RecyclerView.Adapter<BuyDurationAdapter.ViewHolder>() {

    var repeatPos = 0
    private var priceList = ArrayList<VignettePrice>()

    interface OnItemInteractionListener {
        fun onDurationClick(model: VignettePrice, rovignetteDuration: RovignetteDuration)
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

        private var mText: TextView = itemView.findViewById(R.id.mText)
        private var textNew: TextView = itemView.findViewById(R.id.textNew)

        private var mMoney: TextView = itemView.findViewById(R.id.mMoney)
        private var mLinear: LinearLayout = itemView.findViewById(R.id.mLinear)


        @SuppressLint("SetTextI18n")
        fun bind(position: Int) {


            mMoney.text =
                priceList[position].paymentValue.toString() + " " + priceList[position].paymentCurrency.toString()
                    .lowercase() +
                        " (" + priceList[position].originalValue.toString() + " " + priceList[position].originalCurrency.toString() + ")"

            for (i in durationList.indices) {

                if (priceList[position].vignetteDurationCode == durationList[i].code) {

                    mText.text = durationList[i].timeUnit
                    mText.text = durationList[i].timeUnit
                    val str = durationList[i].timeUnitCount.toString()
                    val num = str.replace(Regex("[^0-9]"), "")
                    if (num.length == 1) {
                        textNew.text = "0$num"
                    } else {
                        textNew.text = num
                    }
                }
            }

            if (!itemView.isSelected) {
                mLinear.setBackgroundResource(R.drawable.rounded_rect_100_bridge_back_noview)
            }

            if (repeatPos == position) {
                mLinear.setBackgroundResource(R.drawable.rounded_rect_100_bridge_back)
            }

            itemView.setOnClickListener {

                val prevSelectedPos = repeatPos
                repeatPos = position
                mLinear.setBackgroundResource(R.drawable.rounded_rect_100_bridge_back)
                notifyItemChanged(prevSelectedPos)
                listener?.onDurationClick(priceList[position], durationList[position])
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(priceList: List<VignettePrice>?) {
        this.priceList = ArrayList(priceList ?: listOf())
        notifyDataSetChanged()
    }

}