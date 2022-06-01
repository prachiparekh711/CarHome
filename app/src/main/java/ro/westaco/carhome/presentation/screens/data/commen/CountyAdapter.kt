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
import ro.westaco.carhome.presentation.interfaceitem.CountyListClick
import java.util.*

class CountyAdapter(
    val context: Context,
    private var siruta: ArrayList<Siruta>,
    val countyCode: Int?,
    val countyListClick: CountyListClick,
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    companion object {
        var arrayFilterList = ArrayList<Siruta>()
    }

    init {
        arrayFilterList.clear()
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
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {

        val holder = holder as Holder

        holder.tv_county.text = arrayFilterList[position].name

        holder.tv_county.setOnClickListener {
            holder.checks.isVisible = true
            countyListClick.click(position, arrayFilterList[position])
        }

        holder.checks.isVisible = arrayFilterList[position].code == countyCode
    }


    override fun getItemCount(): Int {
        return arrayFilterList.size
    }


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                if (charString == "") {
                    arrayFilterList.addAll(siruta)
                } else {
                    val filteredList1: ArrayList<Siruta> =
                        ArrayList<Siruta>()
                    for (row in siruta) {
                        if (row.name.lowercase(Locale.getDefault())
                                .contains(charString.lowercase(Locale.getDefault()))
                        ) {
                            filteredList1.add(row)
                        }
                    }
                    arrayFilterList = filteredList1
                }
                val filterResults = FilterResults()
                filterResults.values = arrayFilterList
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                arrayFilterList = filterResults.values as ArrayList<Siruta>
                notifyDataSetChanged()
            }
        }
    }

}