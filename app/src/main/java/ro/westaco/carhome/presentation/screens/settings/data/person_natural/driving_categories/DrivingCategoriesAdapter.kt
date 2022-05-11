package ro.westaco.carhome.presentation.screens.settings.data.person_natural.driving_categories

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ToggleButton
import androidx.recyclerview.widget.RecyclerView
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem

class DrivingCategoriesAdapter(
    private val context: Context,
    private var categories: ArrayList<CatalogItem>,
    private var selectedList: ArrayList<Int>?,
    private val listener: OnItemInteractionListener? = null
) : RecyclerView.Adapter<DrivingCategoriesAdapter.ViewHolder>() {

    interface OnItemInteractionListener {
        fun onChecked(item: CatalogItem, isChecked: Boolean)
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int = categories.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_driving_license_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var check: ToggleButton = itemView.findViewById(R.id.check)

        fun bind(item: CatalogItem) {
            check.textOff = item.name
            check.textOn = item.name
            check.text = item.name

            check.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    check.setTextColor(context.resources.getColor(R.color.white))
                } else {
                    check.setTextColor(context.resources.getColor(R.color.textOnWhite))
                }
                listener?.onChecked(item, isChecked)
            }

            check.isChecked = selectedList?.contains(item.id.toInt()) == true
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(categories: List<CatalogItem>?) {
        this.categories = ArrayList(categories ?: listOf())
        notifyDataSetChanged()
    }
}