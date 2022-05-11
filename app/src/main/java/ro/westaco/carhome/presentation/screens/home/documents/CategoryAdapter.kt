package ro.westaco.carhome.presentation.screens.home.documents

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.Categories
import ro.westaco.carhome.presentation.screens.home.documents.DocumentsFragment.Companion.multipleSelection
import ro.westaco.carhome.presentation.screens.home.documents.DocumentsFragment.Companion.selectedCatList
import ro.westaco.carhome.presentation.screens.home.documents.DocumentsFragment.Companion.selectedList


class CategoryAdapter(
    private val context: Context,
    private var categoryList: ArrayList<Categories>,
    private val listener: OnItemInteractionListener? = null,
    private val listener2: DocumentsFragment.uiChangeListener? = null
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

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
        private var mOption: ImageView = itemView.findViewById(R.id.mOption)
        private var selection: ImageView = itemView.findViewById(R.id.selection)
        private var mainBG: RelativeLayout = itemView.findViewById(R.id.mainBG)

        @SuppressLint("NewApi")
        fun bind(position: Int) {
            val item = categoryList[position]
            title.text = item.name

//            Toast.makeText(context , "" + item.id, Toast.LENGTH_SHORT).show()
            itemView.setOnClickListener {
                if (multipleSelection) {
                    item.id?.let { it1 ->
                        if (selectedList.contains(it1)) {
                            selectedList.remove(it1)
                            selectedCatList.remove(it1)
                            if (selectedList.size == 0)
                                multipleSelection = false
                        } else {
                            selectedList.add(it1)
                            selectedCatList.add(it1)
                        }
                        changeItemUI(it1)
                        listener2?.onLongClick()
                    }
                } else {
                    listener?.onItemClick(item)
                }
            }

            item.id?.let { changeItemUI(it) }

            itemView.setOnLongClickListener {
                if (selectedList.size == 0) {
                    item.id?.let { it1 ->
                        selectedList.add(it1)
                        selectedCatList.add(it1)
                        multipleSelection = true
                        changeItemUI(it1)
                        listener2?.onLongClick()
                    }
                }
                true
            }
        }

        private fun changeItemUI(id: Int) {
            if (selectedList.contains(id)) {
                selection.isVisible = true
                mOption.isVisible = false
                mainBG.setBackgroundColor(context.resources.getColor(R.color.appPrimary6))
            } else {
                selection.isVisible = false
                mOption.isVisible = true
                mainBG.setBackgroundColor(context.resources.getColor(R.color.screen_background_alfa28))
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