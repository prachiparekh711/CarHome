package ro.westaco.carhome.presentation.screens.maps

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.SectionModel
import ro.westaco.carhome.databinding.TopServiceItemBinding
import ro.westaco.carhome.navigation.SingleLiveEvent

class TopServicesAdapter(
    val context: Context,
    val data: ArrayList<SectionModel>
) : RecyclerView.Adapter<TopServicesAdapter.ViewHolder>() {
    private val selectedItemLiveData: SingleLiveEvent<SectionModel> = SingleLiveEvent()
    private val coloursList: Array<String> =
        arrayOf("0", "#E9F3FF", "#CBFFF8", "#EBFFFE", "#F2F1FF")
    private val imagesList: Array<Int> = arrayOf(
        R.drawable.location,
        R.drawable.car_wash,
        R.drawable.fuel,
        R.drawable.food_drink,
        R.drawable.financial_services
    )


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val topServiceItemBinding = TopServiceItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(topServiceItemBinding)
    }

    override fun onBindViewHolder(holder: TopServicesAdapter.ViewHolder, position: Int) {
        val item = data[position]
        if (item.category == "General") {
            holder.topServiceItemBinding.categoryName.text = "All locations"
            holder.topServiceItemBinding.cardConstraint.background =
                context.getDrawable(R.drawable.top_service_background)
            holder.topServiceItemBinding.categoryImage.visibility = View.VISIBLE
            holder.topServiceItemBinding.categoryImage.setImageResource(imagesList[0])
        } else {
            holder.topServiceItemBinding.categoryName.text = item.category
            holder.topServiceItemBinding.cardConstraint.setBackgroundColor(
                Color.parseColor(
                    coloursList[position]
                )
            )
            holder.topServiceItemBinding.categoryImage.visibility = View.VISIBLE
            holder.topServiceItemBinding.categoryImage.setImageResource(imagesList[position])
        }
        holder.topServiceItemBinding.categoriesTextView.text = item.toString()
        holder.topServiceItemBinding.cardView.setOnClickListener {
            selectedItemLiveData.value = item
        }
    }

    fun getSelectedItemLiveData(): SingleLiveEvent<SectionModel> {
        return selectedItemLiveData
    }

    override fun getItemCount() = data.size

    inner class ViewHolder(val topServiceItemBinding: TopServiceItemBinding) :
        RecyclerView.ViewHolder(topServiceItemBinding.root)
}