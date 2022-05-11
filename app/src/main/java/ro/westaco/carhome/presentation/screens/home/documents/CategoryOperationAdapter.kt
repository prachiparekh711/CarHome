package ro.westaco.carhome.presentation.screens.home.documents

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.Categories

class CategoryOperationAdapter(
    private val context: Context,
    private var categoryList: ArrayList<Categories>,
    private val listener: OnItemInteractionListener? = null,
) : RecyclerView.Adapter<CategoryOperationAdapter.ViewHolder>() {

    interface OnItemInteractionListener {
        fun onItemClick(item: Categories)
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int = categoryList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_document_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var title: TextView = itemView.findViewById(R.id.title)
        private var logo: ImageView = itemView.findViewById(R.id.logo)

        @SuppressLint("NewApi")
        fun bind(position: Int) {
            val item = categoryList[position]
            title.text = item.name

            itemView.setOnClickListener {
                listener?.onItemClick(item)
            }

            if (position == 0) {
                logo.setImageDrawable(context.resources.getDrawable(R.drawable.ic_add_folder))
            } else {
                logo.setImageDrawable(context.resources.getDrawable(R.drawable.ic_folder))
            }
        }

    }


    @SuppressLint("NotifyDataSetChanged")
    fun setItems(cars: List<Categories>?) {
        this.categoryList = ArrayList(cars ?: listOf())
        notifyDataSetChanged()
    }

    fun clearAll() {
        this.categoryList.clear()
        notifyDataSetChanged()
    }
}