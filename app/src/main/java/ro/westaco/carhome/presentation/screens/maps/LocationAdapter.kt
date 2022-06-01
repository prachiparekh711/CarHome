package ro.westaco.carhome.presentation.screens.maps

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.LocationV2Item
import ro.westaco.carhome.databinding.LocationItemListBinding


class LocationAdapter(
    var context: Context,
    locationDataList: ArrayList<LocationV2Item>,
    clickItem: ClickLocationItem
) :
    RecyclerView.Adapter<LocationAdapter.ViewHolder>(), Filterable {
    var locationData: ArrayList<LocationV2Item> = locationDataList
    var filterData: ArrayList<LocationV2Item> = locationDataList
    var mListner: ClickLocationItem = clickItem

    interface ClickLocationItem {
        fun click(pos: Int, openMap: Boolean)
    }

    init {
        filterData = locationData
    }

    private val filter: Filter = object : Filter() {

        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filteredList1: ArrayList<LocationV2Item> = ArrayList()
            if (constraint.toString().isEmpty()) {
                filteredList1.addAll(locationData)
            } else {
                for (location in locationData) {
                    if (location.name?.lowercase()
                            ?.contains(constraint.toString().lowercase()) == true
                    ) {
                        filteredList1.add(location)
                    }
                }
            }

            val filterResults = FilterResults()
            filterResults.values = filteredList1
            return filterResults
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            filterData = results.values as ArrayList<LocationV2Item>
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LocationItemListBinding.inflate(
                LayoutInflater.from(
                    context
                )
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.mText.text = filterData[position].name
        holder.binding.mGarage.text = filterData[position].services
        holder.binding.mLocation.text = filterData[position].fullAddress
        holder.binding.mKm.text = "${filterData[position].distance?.toInt()}km"

        if (filterData[position].openNow == false) {
            holder.binding.mTime.text = context.getString(R.string.closed)
            holder.binding.mTime.setTextColor(context.resources.getColor(R.color.closed))
        } else {
            holder.binding.mTime.text = context.getString(R.string.open_24_hours)
            holder.binding.mTime.setTextColor(context.resources.getColor(R.color.list_time))
        }

        holder.binding.mText.setOnClickListener {
            mListner.click(
                position, false
            )
        }
        holder.binding.mDetail.setOnClickListener {
            mListner.click(
                position, false
            )
        }

        holder.binding.image.setOnClickListener {
            mListner.click(
                position, true
            )
        }
    }

    override fun getItemCount(): Int {
        return filterData.size
    }

    override fun getFilter(): Filter {
        return filter
    }

    class ViewHolder(itemView: LocationItemListBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        var binding: LocationItemListBinding = itemView
    }
}
