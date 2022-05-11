package ro.westaco.carhome.presentation.screens.maps

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.LocationFilterItem
import ro.westaco.carhome.databinding.LocationfilterHeaderViewBinding
import ro.westaco.carhome.presentation.interfaceitem.ClickItem

class LocationFilterAdapter(
    var context: Context,
    var data: List<LocationFilterItem>,
    var clickItem: ClickItem
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var selectedPos = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyTopHolder(
            LocationfilterHeaderViewBinding.inflate(LayoutInflater.from(parent.context))
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val holder1 =
            holder as MyTopHolder
        if (selectedPos == position) {
            holder1.binding.textView.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.appPrimary
                )
            )
            holder1.binding.textView.background =
                ContextCompat.getDrawable(context, R.drawable.search_background_selected)
        } else {
            holder1.binding.textView.setTextColor(ContextCompat.getColor(context, R.color.skip))
            holder1.binding.textView.background =
                ContextCompat.getDrawable(context, R.drawable.search_background)
        }

        holder1.binding.textView.text = data[position].name
        holder1.itemView.setOnClickListener { v: View? ->

            val prevSelectedPos = selectedPos
            selectedPos = position
            notifyItemChanged(prevSelectedPos)
            notifyItemChanged(selectedPos)
            clickItem.click(position)
        }

    }

    override fun getItemCount(): Int {
        return data.size
    }


    class MyTopHolder(itemView: LocationfilterHeaderViewBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        var binding: LocationfilterHeaderViewBinding

        init {
            binding = itemView
        }
    }

}
