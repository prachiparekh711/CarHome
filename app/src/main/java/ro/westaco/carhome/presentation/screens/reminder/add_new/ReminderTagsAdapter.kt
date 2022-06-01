package ro.westaco.carhome.presentation.screens.reminder.add_new

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem

class ReminderTagsAdapter(
    private val context: Context
) : RecyclerView.Adapter<ReminderTagsAdapter.ViewHolder>() {
    companion object {
        const val COLUMNS = 3
    }

    private var tags: ArrayList<CatalogItem> = ArrayList()
    private var selectedTags: ArrayList<CatalogItem> = ArrayList()

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int = tags.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_reminder_tag, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var background: View = itemView.findViewById(R.id.background)
        private var typeIndicator: ImageView = itemView.findViewById(R.id.typeIndicator)
        private var tag: TextView = itemView.findViewById(R.id.tag)

        fun bind(position: Int) {
            val item = tags[position]

            if (selectedTags.contains(item)) {
                tag.setTextColor(ContextCompat.getColor(context, R.color.white))
                background.background =
                    ContextCompat.getDrawable(context, R.drawable.rounded_rect_4_purple_wstroke)
            } else {
                tag.setTextColor(ContextCompat.getColor(context, R.color.textOnWhite))
                background.background =
                    ContextCompat.getDrawable(context, R.drawable.rounded_rect_4_white_wstroke)
            }

            typeIndicator.setColorFilter(
                Color.parseColor("#" + item.color),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
            tag.text = item.name

            itemView.setOnClickListener {
                changeSelectedBackground(item)
                notifyItemChanged(position)
            }
        }

        private fun changeSelectedBackground(item: CatalogItem) {
            if (selectedTags.contains(item)) {
                selectedTags.remove(item)
                tag.setTextColor(ContextCompat.getColor(context, R.color.textOnWhite))
                background.background =
                    ContextCompat.getDrawable(context, R.drawable.rounded_rect_4_white_wstroke)
            } else {
                selectedTags.add(item)
                tag.setTextColor(ContextCompat.getColor(context, R.color.textOnWhite))
                background.background =
                    ContextCompat.getDrawable(context, R.drawable.rounded_rect_4_white_wstroke)
            }
        }
    }

    fun setSelected(selected: List<Long?>) {
        selected.forEach { item ->
            val tag = tags.find {
                it.id == item
            }
            if (tag != null) {
                selectedTags.add(tag)
            }
        }
    }


    internal fun getSelected(): List<CatalogItem> {
        return selectedTags
    }

    fun setItems(tags: List<CatalogItem>?) {
        this.tags = tags as ArrayList<CatalogItem>
        notifyDataSetChanged()
    }

}