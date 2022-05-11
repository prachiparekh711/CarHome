package ro.westaco.carhome.presentation.screens.service.insurance.leasing_c

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.LeasingCompany
import ro.westaco.carhome.utils.CountryCityUtils
import java.util.*

class LeasingInAdapter(
    private val context: Context,
    private var categories: MutableList<LeasingCompany>,
    private var contactListFiltered: MutableList<LeasingCompany>? = null,
    val onItemInteractionListener: OnItemInteractionListener,
    private val enableSelection: Boolean = false,

    ) : RecyclerView.Adapter<LeasingInAdapter.ViewHolder>(), Filterable {

    private var selectedPos = -1

    interface OnItemInteractionListener {
        fun onChecked(item: LeasingCompany)
    }

    init {

        this.contactListFiltered = categories
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
        private var companyLogo: ImageView = itemView.findViewById(R.id.companyLogo)
        private var mainRL: ConstraintLayout = itemView.findViewById(R.id.mainRL)
        private var tv_letter: TextView = itemView.findViewById(R.id.tv_letter)
        private var check: AppCompatImageView = itemView.findViewById(R.id.check)

        fun bind(item: LeasingCompany) {
            companyName.text = item.name
            address.text = item.address

            tv_letter.text = CountryCityUtils.firstTwo(item.name)

            /*if (!itemView.isSelected) {
                check.visibility = View.GONE
            }*/

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

                for (i in contactListFiltered?.indices!!) {

                    if (contactListFiltered!![i].name.lowercase(Locale.getDefault()).contains(

                            p0.toString().lowercase(Locale.getDefault())
                        )
                    ) {
                        filterList.add(contactListFiltered!![i])
                    }
                }

                results.count = filterList.size
                results.values = filterList

            } else {

                results.count = contactListFiltered?.size!!

                results.values = contactListFiltered
            }

            return results
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun publishResults(p0: CharSequence?, results: FilterResults?) {
            categories = results?.values as MutableList<LeasingCompany>
            notifyDataSetChanged()
        }


    }

    fun getSelectedCar(): LeasingCompany? {
        if (selectedPos < 0) return null
        return categories[selectedPos]
    }
}