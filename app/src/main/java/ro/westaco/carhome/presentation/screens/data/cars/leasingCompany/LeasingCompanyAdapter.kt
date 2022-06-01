package ro.westaco.carhome.presentation.screens.data.cars.leasingCompany

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.LeasingCompany
import ro.westaco.carhome.utils.CountryCityUtils

class LeasingCompanyAdapter(
    private val context: Context,
    private var categories: ArrayList<LeasingCompany>,
    private val listener: OnItemInteractionListener? = null

) : RecyclerView.Adapter<LeasingCompanyAdapter.ViewHolder>() {

    interface OnItemInteractionListener {
        fun onChecked(item: LeasingCompany)
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int = categories.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_leasing_company, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var companyName: TextView = itemView.findViewById(R.id.companyName)
        private var address: TextView = itemView.findViewById(R.id.address)
        private var mainRL: MaterialCardView = itemView.findViewById(R.id.mainRL)
        private var tvLetter: TextView = itemView.findViewById(R.id.tv_letter)

        fun bind(item: LeasingCompany) {
            companyName.text = item.name
            address.text = item.address

            /*val options = RequestOptions()
            companyLogo.clipToOutline = true
            Glide.with(context)
                .load(item.logoHref)
                .apply(
                    options.centerCrop()
                        .skipMemoryCache(true)
                        .priority(Priority.HIGH)
                        .format(DecodeFormat.PREFER_ARGB_8888)
                )
                .into(companyLogo)*/


            tvLetter.text = CountryCityUtils.firstTwo(item.name)

            mainRL.setOnClickListener {
                listener?.onChecked(item)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(categories: List<LeasingCompany>?) {
        this.categories = ArrayList(categories ?: listOf())
        notifyDataSetChanged()
    }
}