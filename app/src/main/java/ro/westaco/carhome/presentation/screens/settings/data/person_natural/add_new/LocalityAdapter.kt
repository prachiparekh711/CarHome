package ro.westaco.carhome.presentation.screens.settings.data.person_natural.add_new

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.Siruta
import java.util.*

class LocalityAdapter(
    val context: Context,
    private var siruta: MutableList<Siruta>,
    private var contactListFiltered: MutableList<Siruta>? = null,
    val localityListClick: LocalityListClick
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {


    var cityPosition = 0

    interface LocalityListClick {

        fun localityclick(position: Int, siruta: Siruta)
    }

    init {

        this.contactListFiltered = siruta
    }

    class Holder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {

        val tv_county: TextView = itemView.findViewById(R.id.tv_county)
        val checks: AppCompatImageView = itemView.findViewById(R.id.check)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.county_item_list, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val holder = holder as Holder

        holder.tv_county.text = siruta[position].name

        holder.tv_county.setOnClickListener {
            cityPosition = position
            holder.checks.isVisible = true
            notifyDataSetChanged()
            localityListClick.localityclick(position, siruta[position])
        }

        holder.checks.isVisible = position == cityPosition

    }

    override fun getItemCount(): Int {
        return siruta.size
    }

    override fun getFilter(): Filter {

        return customFilter
    }

    private val customFilter = object : Filter() {

        override fun performFiltering(p0: CharSequence?): FilterResults {

            val results = FilterResults()

            if (p0 != null && p0.isNotEmpty()) {

                val filterList: MutableList<Siruta> = ArrayList()

                for (i in contactListFiltered?.indices!!) {

                    if (contactListFiltered!![i].name.lowercase(Locale.getDefault())
                            .contains(p0.toString().lowercase(Locale.getDefault()))
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


            siruta = results?.values as MutableList<Siruta>
            notifyDataSetChanged()
        }


    }
}