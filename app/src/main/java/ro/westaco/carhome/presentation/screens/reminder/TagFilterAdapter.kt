package ro.westaco.carhome.presentation.screens.reminder

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem
import ro.westaco.carhome.databinding.LocationfilterHeaderViewBinding
import ro.westaco.carhome.navigation.SingleLiveEvent

class TagFilterAdapter(
    var context: Context
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var selectedTags: ArrayList<CatalogItem> = ArrayList()
    var selectedItemLiveData: SingleLiveEvent<ArrayList<CatalogItem>> = SingleLiveEvent()
    var data: ArrayList<CatalogItem> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyTopHolder(
            LocationfilterHeaderViewBinding.inflate(LayoutInflater.from(parent.context))
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = data[position]
        val holder1 = holder as MyTopHolder
        if (selectedTags.contains(item)) {
            changeItemBackground(true, holder)
        } else {
            changeItemBackground(false, holder)
        }
        holder1.binding.textView.text = item.name
        holder1.itemView.setOnClickListener { v: View? ->
            changeSelectionList(item, holder1)
        }
    }


    fun changeSelectionList(item: CatalogItem, holder: MyTopHolder) {
        if (selectedTags.contains(item)) {
            if (item.name == "All") {
                selectedTags.clear()
            } else {
                selectedTags.remove(item)
                val allTag = selectedTags.find {
                    it.name == "All"
                }
                if (allTag != null) {
                    selectedTags.remove(allTag)
                }
            }
        } else {
            if (item.name == "All") {
                selectedTags.clear()
                selectedTags.addAll(data)
            } else {
                selectedTags.add(item)
                val allTag = selectedTags.find {
                    it.name == "All"
                }
                if (allTag == null && selectedTags.size == data.size - 1) {
                    selectedTags.add(data[0])
                }
            }
        }
        selectedItemLiveData.value = selectedTags
        notifyItemRangeChanged(0, data.size)
    }

    fun changeItemBackground(isSelected: Boolean, holder: MyTopHolder) {
        if (isSelected) {
            holder.binding.textView.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.appPrimary
                )
            )
            holder.binding.textView.background =
                ContextCompat.getDrawable(context, R.drawable.search_background_selected)
        } else {
            holder.binding.textView.setTextColor(ContextCompat.getColor(context, R.color.skip))
            holder.binding.textView.background =
                ContextCompat.getDrawable(context, R.drawable.search_background)
        }
    }

    fun getSelectedTagsLiveData(): SingleLiveEvent<ArrayList<CatalogItem>> {
        return selectedItemLiveData
    }

    override fun getItemCount(): Int {
        return data.size
    }


    class MyTopHolder(itemView: LocationfilterHeaderViewBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        var binding: LocationfilterHeaderViewBinding = itemView

    }

    fun clearAll() {
        data.clear()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(tags: List<CatalogItem>?) {
        this.data = java.util.ArrayList(tags ?: listOf())
        notifyDataSetChanged()
    }


}
