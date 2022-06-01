package ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.BridgeTaxPrices
import java.util.*

class PassTaxAdapter(
    val context: Context,
    val arrayList: ArrayList<BridgeTaxPrices>,
    val listener: OnItemInteractionListener?,
    var repeatPos: Int,

    ) :
    RecyclerView.Adapter<PassTaxAdapter.ViewHolder>() {


    interface OnItemInteractionListener {
        fun onItemClick(model: BridgeTaxPrices, position: Int)
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = inflater.inflate(R.layout.duration_items, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var textNew: TextView = itemView.findViewById(R.id.textNew)
        private var mText: TextView = itemView.findViewById(R.id.mText)
        private var mMoney: TextView = itemView.findViewById(R.id.mMoney)
        private var mLinear: LinearLayout = itemView.findViewById(R.id.mLinear)


        fun bind(position: Int) {

            val item = arrayList[position]

            val str = item.description
            val stu = item.description
            val num = str?.replace(Regex("[^0-9]"), "")
            val new = stu?.replace(Regex("[^a-z]"), "")
            if (num?.length == 1) {
                textNew.text = "0$num"
            } else {
                textNew.text = num
            }

            mText.text = new
            mMoney.text =
                "${item.paymentValue} ${item.paymentCurrency?.lowercase(Locale.getDefault())} (${item.paymentValue} ${item.paymentCurrency})"

            if (!itemView.isSelected) {
                mLinear.setBackgroundResource(R.drawable.rounded_rect_100_bridge_back_noview)
            }

            if (repeatPos == position) {
                mLinear.setBackgroundResource(R.drawable.rounded_rect_100_bridge_back)
            }

            itemView.setOnClickListener {
                val prevSelectedPos = repeatPos
                repeatPos = position
                listener?.onItemClick(item, position)
                mLinear.setBackgroundResource(R.drawable.rounded_rect_100_bridge_back)
                notifyItemChanged(prevSelectedPos)
            }

        }

    }

    override fun getItemCount(): Int {
        return arrayList.size
    }
}