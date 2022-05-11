package ro.westaco.carhome.presentation.screens.service.insurance.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.RcaDurationItem

class DurationAdapter(
    val context: Context,
    var pos: Int,
    val listener: OnItemInteractionListener?
) :
    RecyclerView.Adapter<DurationAdapter.ViewHolder>() {

    var list = ArrayList<RcaDurationItem>()

    interface OnItemInteractionListener {
        fun onItemClick(item: RcaDurationItem, position: Int)
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int = list.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_objective, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var mRadioBtn: RadioButton = itemView.findViewById(R.id.mRadioBtn)

        @SuppressLint("NotifyDataSetChanged")
        fun bind(position: Int) {

            val item = list[position]

            mRadioBtn.text = item.name
            mRadioBtn.isChecked = pos == position
            mRadioBtn.setTextColor(context.resources.getColor(R.color.items_color))

            mRadioBtn.setOnClickListener {
                pos = position
                listener?.onItemClick(item, position)
                notifyDataSetChanged()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(durationList: List<RcaDurationItem>?) {
        this.list = java.util.ArrayList(durationList ?: listOf())
        notifyDataSetChanged()
    }


}