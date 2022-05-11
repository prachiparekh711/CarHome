package ro.westaco.carhome.presentation.screens.home

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.VehicleProgressItem


class CarProgressAdapter(
    private val context: Context,
    private var cars: ArrayList<VehicleProgressItem>,
    private val listener: OnCarItemInteractionListener? = null,
) : RecyclerView.Adapter<CarProgressAdapter.ViewHolder>() {


    interface OnCarItemInteractionListener {
        fun onItemClick(item: VehicleProgressItem)
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int = cars.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = inflater.inflate(R.layout.car_progress_item, parent, false)

        val width = context.resources.displayMetrics?.widthPixels

        if (width != null) {
            if (itemCount > 1) {
                val params = RecyclerView.LayoutParams(
                    (width / 1.2).toInt(),
                    RecyclerView.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 20, 0)
                view.layoutParams = params
            } else {
                val params =
                    RecyclerView.LayoutParams(
                        RecyclerView.LayoutParams.MATCH_PARENT,
                        RecyclerView.LayoutParams.WRAP_CONTENT
                    )
                params.setMargins(0, 0, 20, 0)
                view.layoutParams = params
            }
        }

        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var carLbl: TextView = itemView.findViewById(R.id.carLbl)
        private var carProgress: ProgressBar = itemView.findViewById(R.id.carProgress)
        private var carProgressLbl: TextView = itemView.findViewById(R.id.carProgressLbl)
        private var mAdd: TextView = itemView.findViewById(R.id.mAdd)
        private var mLayout: RelativeLayout = itemView.findViewById(R.id.mLayout)

        @SuppressLint("NotifyDataSetChanged")
        fun bind(position: Int) {
            val item = cars[position]
            carLbl.text = item.description
            if (item.completionPercent != null) {
                carProgress.max = 100
                carProgress.progress = item.completionPercent
                carProgressLbl.text =
                    "${context.resources.getString(R.string.progress, item.completionPercent)}"
            } else {
                carProgressLbl.text = "${context.resources.getString(R.string.progress, 0)}"
            }

            mAdd.setOnClickListener {
                listener?.onItemClick(item)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(cars: List<VehicleProgressItem>) {
        this.cars = ArrayList(cars)
        notifyDataSetChanged()
    }


}