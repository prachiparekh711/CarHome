package ro.westaco.carhome.presentation.screens.maps

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.LocationFilterItem
import ro.westaco.carhome.databinding.FilterViewItemBinding
import ro.westaco.carhome.di.ApiModule
import ro.westaco.carhome.navigation.SingleLiveEvent

class MapFilterBottomSheetAdapter(
    var context: Context,
    var dataSource: List<LocationFilterItem>,
    var selectedItems: ArrayList<LocationFilterItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var intermediateSelectedItems: ArrayList<LocationFilterItem> = ArrayList()
    private var selectedItemsLiveData: SingleLiveEvent<ArrayList<LocationFilterItem>> =
        SingleLiveEvent()

    init {
        intermediateSelectedItems.addAll(selectedItems)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MapFilterViewHolder(
            FilterViewItemBinding.inflate(
                LayoutInflater.from(context)
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = dataSource[position]
        val customHolder = holder as MapFilterViewHolder
        initSelectedItems(item, holder)
        customHolder.binding.filterNameTv.text = item.name
        customHolder.binding.filtersCardView.setOnClickListener {
            changeIfSelected(item, holder)
        }
    }

    private fun initSelectedItems(item: LocationFilterItem, holder: MapFilterViewHolder) {
        val selectedItem = selectedItems.find {
            it.nomLSId == item.nomLSId
        }
        if (selectedItem == null) {
            changeToNotSelected(holder)
        } else {
            changeToSelected(holder)
        }
        initIcons(item, holder)
    }

    private fun initIcons(item: LocationFilterItem, holder: MapFilterViewHolder) {
        if (item.logoHref == null && item.iconHref == null) {
            holder.binding.filtersLinearLayout.visibility = View.VISIBLE
            holder.binding.logoLinearLayout.visibility = View.GONE
            holder.binding.smallIcon.visibility = View.GONE
        }
        if (item.logoHref != null) {
            holder.binding.filtersLinearLayout.visibility = View.GONE
            holder.binding.logoLinearLayout.visibility = View.VISIBLE
            Glide.with(context)
                .load(ApiModule.BASE_URL_RESOURCES + item.logoHref)
                .into(holder.binding.largeIcon)
        } else if (item.logoHref == null) {
            holder.binding.filtersLinearLayout.visibility = View.VISIBLE
            holder.binding.logoLinearLayout.visibility = View.GONE
            if (item.iconHref != null) {
                Glide.with(context)
                    .load(ApiModule.BASE_URL_RESOURCES + item.iconHref)
                    .into(holder.binding.smallIcon)
            }
        }
    }

    private fun changeIfSelected(item: LocationFilterItem, holder: MapFilterViewHolder) {
        val selectedItem = selectedItems.find {
            it.nomLSId == item.nomLSId
        }
        if (selectedItem == null && item.nomLSId == 0) {
            intermediateSelectedItems.clear()
            intermediateSelectedItems.addAll(dataSource)
            notifyDataSetChanged()
            return
        }
        if (selectedItem != null && item.nomLSId == 0) {
            intermediateSelectedItems.clear()
            notifyDataSetChanged()
            return
        }
        if (selectedItem != null) {
            changeToNotSelected(holder)
            intermediateSelectedItems.remove(selectedItem)
            selectedItemsLiveData.value = intermediateSelectedItems
        } else {
            changeToSelected(holder)
            intermediateSelectedItems.add(item)
            selectedItemsLiveData.value = intermediateSelectedItems
        }
    }

    private fun changeToNotSelected(holder: MapFilterViewHolder) {
        holder.binding.filtersLinearLayout.background =
            ContextCompat.getDrawable(context, R.drawable.search_background)
        holder.binding.logoLinearLayout.background =
            ContextCompat.getDrawable(context, R.drawable.search_background)
        holder.binding.filterNameTv.setTextColor(Color.parseColor("#768DA9"))

    }

    private fun changeToSelected(holder: MapFilterViewHolder) {
        holder.binding.filtersLinearLayout.background =
            ContextCompat.getDrawable(context, R.drawable.search_background_selected)
        holder.binding.logoLinearLayout.background =
            ContextCompat.getDrawable(context, R.drawable.search_background_selected)
        holder.binding.filterNameTv.setTextColor(Color.parseColor("#05133A"))
    }

    fun getSelectedItems(): SingleLiveEvent<ArrayList<LocationFilterItem>> {
        return selectedItemsLiveData
    }


    override fun getItemCount(): Int {
        return dataSource.size
    }

    class MapFilterViewHolder(itemView: FilterViewItemBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        var binding: FilterViewItemBinding = itemView
    }


}