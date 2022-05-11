package ro.westaco.carhome.presentation.screens.settings.extras

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ro.westaco.carhome.R
import ro.westaco.carhome.databinding.LocationfilterHeaderViewBinding
import ro.westaco.carhome.presentation.interfaceitem.ClickItem

class FAQTabAdapter(
    var context: Context,
    var data: List<String>,
    var clickItem: ClickItem
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyTopHolder(
            LocationfilterHeaderViewBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    var prevSelectedPos = 0


    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val holder1 = holder as MyTopHolder
        holder1.itemView.setOnClickListener { v: View? ->
            if (holder1.itemView.isClickable) {
                clickItem.click(position)
                notifyItemChanged(prevSelectedPos)
                notifyItemChanged(position)
                prevSelectedPos = position
            }
        }

        if (position == prevSelectedPos) {
            holder1.binding.textView.background.setColorFilter(
                ContextCompat.getColor(context, R.color.appPrimary),
                android.graphics.PorterDuff.Mode.SRC
            )
            holder1.binding.textView.setTextColor(context.resources.getColor(R.color.white))
        } else {
            holder1.binding.textView.background.setColorFilter(
                ContextCompat.getColor(context, R.color.itemBgStroke),
                android.graphics.PorterDuff.Mode.SRC_OUT
            )

            holder1.binding.textView.setTextColor(context.resources.getColor(R.color.unselected))
        }

        holder1.binding.textView.text = data[position]
    }

    class MyTopHolder(itemView: LocationfilterHeaderViewBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        var binding: LocationfilterHeaderViewBinding

        init {
            binding = itemView
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

}
