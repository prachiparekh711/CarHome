package ro.westaco.carhome.presentation.screens.home.purchases

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.PurchaseCategory

class PurchaseCategoriesAdapter(
    private val context: Context,
    private var categories: ArrayList<PurchaseCategory>
) : RecyclerView.Adapter<PurchaseCategoriesAdapter.ViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int = categories.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var category: TextView = itemView.findViewById(R.id.category)
        private var purchases: RecyclerView = itemView.findViewById(R.id.purchases)

        fun bind(item: PurchaseCategory) {
            category.text = item.category

            item.purchases?.let { items ->
                purchases.layoutManager = LinearLayoutManager(context)
                purchases.adapter = PurchasesAdapter(itemView.context, items)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(categories: ArrayList<PurchaseCategory>?) {
        this.categories = ArrayList(categories ?: listOf())
        notifyDataSetChanged()
    }
}