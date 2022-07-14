package ro.westaco.carhome.presentation.screens.service.insurance.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.LeasingCompany
import ro.westaco.carhome.utils.CountryCityUtils
import java.util.*

class LeasingInAdapter(
    private val context: Context,
    private var categories: MutableList<LeasingCompany>,
    val onItemInteractionListener: OnItemInteractionListener,

    ) : RecyclerView.Adapter<LeasingInAdapter.ViewHolder>(), Filterable {

    private var selectedPos = -1

    interface OnItemInteractionListener {
        fun onChecked(item: LeasingCompany)
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int = categories.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_leasing_ins_company, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var companyName: TextView = itemView.findViewById(R.id.companyName)
        private var address: TextView = itemView.findViewById(R.id.address)
        private var mainRL: ConstraintLayout = itemView.findViewById(R.id.mainRL)
        private var tvLetter: TextView = itemView.findViewById(R.id.tv_letter)

        fun bind(item: LeasingCompany) {
            companyName.text = item.name
            address.text = item.address

            tvLetter.text = CountryCityUtils.firstTwo(item.name)

            mainRL.setOnClickListener {

                onItemInteractionListener.onChecked(item)

            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(categories: List<LeasingCompany>?) {
        this.categories = ArrayList(categories ?: listOf())
        notifyDataSetChanged()
    }


    override fun getFilter(): Filter {

        return customFilter
    }

    private val customFilter = object : Filter() {

        override fun performFiltering(p0: CharSequence?): FilterResults {

            val results = FilterResults()

            if (p0 != null && p0.isNotEmpty()) {

                val filterList: MutableList<LeasingCompany> = java.util.ArrayList()

                if (categories.isNotEmpty()) {
                    for (i in categories.indices) {

                        if (categories[i].name.lowercase(Locale.getDefault()).contains(

                                p0.toString().lowercase(Locale.getDefault())
                            )
                        ) {
                            filterList.add(categories.get(i))
                        }
                    }
                }

                results.count = filterList.size
                results.values = filterList

            } else {

                results.count = categories.size

                results.values = categories
            }

            return results
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun publishResults(p0: CharSequence?, results: FilterResults?) {
            categories = results?.values as MutableList<LeasingCompany>
            notifyDataSetChanged()
        }


    }

}