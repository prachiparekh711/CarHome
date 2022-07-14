package ro.westaco.carhome.presentation.screens.settings.notifications

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

class NotificationFilterAdapter(
    var context: Context
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var selectedTag: CatalogItem? = null
    var selectedItemLiveData: SingleLiveEvent<CatalogItem> = SingleLiveEvent()
    var data: ArrayList<CatalogItem> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyTopHolder(
            LocationfilterHeaderViewBinding.inflate(LayoutInflater.from(parent.context))
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = data[position]
        val holder1 = holder as MyTopHolder
        if (selectedTag == item) {
            changeItemBackground(true, holder)
        } else {
            changeItemBackground(false, holder)
        }
        holder1.binding.textView.text = item.name
        holder1.itemView.setOnClickListener { v: View? ->
            changeSelectionList(item)
        }
    }


    fun changeSelectionList(item: CatalogItem) {
        if (item != selectedTag) {
            selectedTag = item
        }
        selectedItemLiveData.value = selectedTag
        notifyDataSetChanged()
    }

    fun setAllSelected() {
        selectedTag = data[0]
        notifyDataSetChanged()
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


    fun getSelectedTagsLiveData(): SingleLiveEvent<CatalogItem> {
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