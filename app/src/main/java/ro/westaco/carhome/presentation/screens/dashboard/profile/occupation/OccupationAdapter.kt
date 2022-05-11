package ro.westaco.carhome.presentation.screens.dashboard.profile.occupation

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem

class OccupationAdapter(
    private val context: Context,
    private var categories: ArrayList<CatalogItem>,
    private val listener: OnItemInteractionListener? = null,
    private var selectedOccupation: CatalogItem? = null

) : RecyclerView.Adapter<OccupationAdapter.ViewHolder>() {

    interface OnItemInteractionListener {
        fun onChecked(item: CatalogItem)
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int = categories.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_occupation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var check: TextView = itemView.findViewById(R.id.check)

        fun bind(item: CatalogItem) {
            check.text = item.name

            check.setOnClickListener {
                check.setTextColor(context.resources.getColor(R.color.white))
                listener?.onChecked(item)
                selectedOccupation = item
                notifyDataSetChanged()
            }

            if (selectedOccupation == item) {
                check.setTextColor(context.resources.getColor(R.color.white))
                check.background = context.resources.getDrawable(R.drawable.cta_background)
            } else {
                check.setTextColor(context.resources.getColor(R.color.textOnWhite))
                check.background = context.resources.getDrawable(R.drawable.bg_card_item)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(categories: List<CatalogItem>?) {
        this.categories = ArrayList(categories ?: listOf())
        notifyDataSetChanged()
    }
}