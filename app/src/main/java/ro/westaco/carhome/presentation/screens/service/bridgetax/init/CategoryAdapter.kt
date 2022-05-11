package ro.westaco.carhome.presentation.screens.service.bridgetax.init

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.ServiceCategory

class CategoryAdapter(
    context: Context,
    repeatPos: Int,
    listener: OnItemInteractionListener?,
    cars: ArrayList<ServiceCategory>
) :
    RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {
    var context: Context? = null
    var cars = ArrayList<ServiceCategory>()
    var interFace: OnItemInteractionListener? = null
    private var selectedPos = 0

    init {
        this.context = context
        this.interFace = listener
        this.selectedPos = repeatPos
        this.cars = cars
    }

    interface OnItemInteractionListener {
        fun onItemClick(position: Int, mText: TextView)
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int = cars.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.bridgetax_category_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var mText: TextView = itemView.findViewById(R.id.mText)
        private var mLinear: RelativeLayout = itemView.findViewById(R.id.mLinear)


        @SuppressLint("NewApi", "SimpleDateFormat", "NotifyDataSetChanged", "ResourceAsColor")
        fun bind(position: Int) {

            val item = cars[position]

            mText.text = item.shortDescription
            if (selectedPos == position) {
                mText.setBackgroundResource(R.drawable.rounded_rect_100_vignette_full)
                mText.setTextColor(context?.resources!!.getColor(R.color.white))
            }

            itemView.setOnClickListener {
                mText.setBackgroundResource(R.drawable.rounded_rect_100_vignette_full)
                interFace?.onItemClick(position, mText)
            }
        }
    }

}