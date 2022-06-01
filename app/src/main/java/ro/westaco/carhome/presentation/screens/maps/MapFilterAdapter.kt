package ro.westaco.carhome.presentation.screens.maps

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ro.westaco.carhome.data.sources.remote.responses.models.LocationFilterItem
import ro.westaco.carhome.data.sources.remote.responses.models.SectionModel
import ro.westaco.carhome.databinding.MapFilterHeaderBinding
import ro.westaco.carhome.navigation.SingleLiveEvent

class MapFilterAdapter(
    var context: Context,
    var data: ArrayList<SectionModel>,
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var removeFromListEvent: SingleLiveEvent<Int> = SingleLiveEvent()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MapViewHolder(
            MapFilterHeaderBinding.inflate(LayoutInflater.from(parent.context))
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item: LocationFilterItem? = getItemForPositionInSectionModel(position)
        val mapViewHolder =
            holder as MapViewHolder
        mapViewHolder.binding.filterLinearLayout.setOnClickListener {
            removeFromListEvent.value = position
            notifyDataSetChanged()
        }
        mapViewHolder.binding.filterTextView.text = item?.name

    }

    private fun getItemForPositionInSectionModel(position: Int): LocationFilterItem? {
        var itemCount = 0
        var newPosition = 0
        val item: LocationFilterItem? = null
        data.forEach {
            if (position < it.filters.size)
                return it.filters[position]
            if (itemCount + it.filters.size > position) {
                newPosition = position % itemCount
                return it.filters[newPosition]
            } else {
                itemCount += it.filters.size
            }

        }
        return item
    }

    fun getRemoveFromListEvent(): SingleLiveEvent<Int> {
        return removeFromListEvent
    }

    override fun getItemCount(): Int {
        var itemsCount = 0
        data.forEach {
            itemsCount += it.filters.size
        }
        return itemsCount
    }


    class MapViewHolder(itemView: MapFilterHeaderBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        var binding: MapFilterHeaderBinding = itemView

    }

}