package ro.westaco.carhome.presentation.screens.service.bridgetax.init

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.BridgeTaxPrices
import java.util.*

class PassTaxAdapter(
    val context: Context,
    val arrayList: ArrayList<BridgeTaxPrices>,
    val listener: OnItemInteractionListener?,
    repeatPos: Int,

    ) :
    RecyclerView.Adapter<PassTaxAdapter.ViewHolder>() {

    var selectPos = 0

    init {
        this.selectPos = repeatPos
    }


    interface OnItemInteractionListener {
        fun onItemClick(model: BridgeTaxPrices, position: Int)
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = inflater.inflate(R.layout.duration_items, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: PassTaxAdapter.ViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var button: ImageView = itemView.findViewById(R.id.mImage)
        private var mText: TextView = itemView.findViewById(R.id.mText)
        private var mMoney: TextView = itemView.findViewById(R.id.mMoney)
        private var mView: View = itemView.findViewById(R.id.mView)


        fun bind(position: Int) {

            val item = arrayList[position]

            mText.text = item.description
            mMoney.text =
                "${item.paymentValue} ${item.paymentCurrency?.lowercase(Locale.getDefault())} (${item.paymentValue} ${item.paymentCurrency})"

            if (selectPos == position)
                button.setImageResource(R.drawable.radio_checked)
            else
                button.setImageResource(
                    R.drawable.radio_unchecked
                )

            itemView.setOnClickListener {
                selectPos = position
                listener?.onItemClick(item, position)
                notifyDataSetChanged()
            }

            mView.isVisible = position != arrayList.size - 1
        }

    }

    override fun getItemCount(): Int {
        return arrayList.size
    }
}