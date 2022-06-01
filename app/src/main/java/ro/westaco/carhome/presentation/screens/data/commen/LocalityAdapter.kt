package ro.westaco.carhome.presentation.screens.data.commen

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
    private var siruta: ArrayList<Siruta>,
    val localityListClick: LocalityListClick
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private var contactListFiltered: ArrayList<Siruta>? = null

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

    @SuppressLint("NotifyDataSetChanged")
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
            val filterList: ArrayList<Siruta> = ArrayList<Siruta>()
            if (p0 != null && p0.isNotEmpty()) {

                for (i in siruta.indices) {

                    if (siruta[i].name.lowercase(Locale.getDefault())
                            .contains(p0.toString().lowercase(Locale.getDefault()))
                    ) {
                        filterList.add(siruta[i])
                    }
                }

                results.count = filterList.size
                results.values = filterList

            } else {
                results.count = filterList.size
                results.values = contactListFiltered
            }

            return results
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun publishResults(p0: CharSequence?, results: FilterResults?) {


            siruta = results?.values as ArrayList<Siruta>
            notifyDataSetChanged()
        }


    }
}