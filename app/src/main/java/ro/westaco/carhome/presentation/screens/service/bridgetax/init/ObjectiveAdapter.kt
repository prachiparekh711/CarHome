package ro.westaco.carhome.presentation.screens.service.bridgetax.init

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.ObjectiveItem

class ObjectiveAdapter(
    context: Context,
    pos: Int,
    listener: OnItemInteractionListener?,
    list: ArrayList<ObjectiveItem>
) :
    RecyclerView.Adapter<ObjectiveAdapter.ViewHolder>() {

    var context: Context? = null
    var list = ArrayList<ObjectiveItem>()
    var listener: OnItemInteractionListener? = null
    private var selectedPos = 0

    init {
        this.context = context
        this.listener = listener
        this.selectedPos = pos
        this.list = list
    }

    interface OnItemInteractionListener {
        fun onItemClick(item: ObjectiveItem, position: Int)
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int = list.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_objective, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var mRadioBtn: RadioButton = itemView.findViewById(R.id.mRadioBtn)

        fun bind(position: Int) {

            val item = list[position]

            mRadioBtn.text = item.description
            mRadioBtn.isChecked = selectedPos == position

            itemView.setOnClickListener {
                listener?.onItemClick(item, position)
            }
        }
    }

}