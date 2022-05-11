package ro.westaco.carhome.presentation.screens.reminder.add_new

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.LocationV2Item
import ro.westaco.carhome.databinding.LocationItemListBinding
import ro.westaco.carhome.presentation.interfaceitem.MapClickItem

class MapAdapter(
    var context: Context,
    locationDataList: ArrayList<LocationV2Item>,
    var clickItem: ClickLocationItem,
    var clickImage: MapClickItem

) :
    RecyclerView.Adapter<MapAdapter.ViewHolder>(), Filterable {

    var locationData: ArrayList<LocationV2Item> = locationDataList
    var filterData: ArrayList<LocationV2Item> = locationDataList

    interface ClickLocationItem {
        fun click(pos: Int, openMap: Boolean)
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

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.mText.text = filterData[position].name
        holder.binding.mGarage.text = filterData[position].services
        holder.binding.mLocation.text = filterData[position].fullAddress

        holder.binding.mKm.text = "${filterData[position].distance?.toInt()} km"
        if (filterData[position].openNow == false) {
            holder.binding.mTime.text = context.getString(R.string.closed)
            holder.binding.mTime.setTextColor(context.resources.getColor(R.color.closed))
        } else {
            holder.binding.mTime.text = context.getString(R.string.open_24_hours)
            holder.binding.mTime.setTextColor(context.resources.getColor(R.color.list_time))
        }
        holder.binding.image.setOnClickListener { v: View? ->
            clickItem.click(
                position, true
            )
        }

        holder.binding.mDetail.setOnClickListener { v: View? ->
            clickImage.click(
                position
            )
        }


    }

    override fun getItemCount(): Int {
        return filterData.size
    }

    class ViewHolder(itemView: LocationItemListBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        var binding: LocationItemListBinding = itemView

    }


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val filteredList1: MutableList<LocationV2Item> = ArrayList()
                if (charSequence.toString().isEmpty()) {
                    filteredList1.addAll(locationData)
                } else {
                    for (location in locationData) {
                        if (location.name?.lowercase()
                                ?.contains(charSequence.toString().lowercase()) == true
                        ) {
                            filteredList1.add(location)
                        }
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList1
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                filterData = filterResults.values as ArrayList<LocationV2Item>
                notifyDataSetChanged()
            }
        }
    }

}
