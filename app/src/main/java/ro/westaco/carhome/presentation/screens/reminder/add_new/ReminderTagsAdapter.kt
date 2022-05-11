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
    private val context: Context,
    var tagPos: Int
) : RecyclerView.Adapter<ReminderTagsAdapter.ViewHolder>() {
    companion object {
        const val COLUMNS = 3
    }

    private var tags: ArrayList<CatalogItem> = ArrayList()

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

            if (tagPos == position) {
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
                val prevSelectedPos = tagPos
                tagPos = position
                notifyItemChanged(prevSelectedPos)
                notifyItemChanged(tagPos)
            }
        }
    }

    internal fun getSelected(): CatalogItem? {
        return try {
            if (tagPos == -1) null else tags[tagPos]
        } catch (e: Exception) {
            null
        }
    }

    fun setItems(tags: List<CatalogItem>?) {
        this.tags = tags as ArrayList<CatalogItem>
        notifyDataSetChanged()
    }

}