package ro.westaco.carhome.presentation.screens.data.commen

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.Country
import ro.westaco.carhome.utils.CountryCityUtils
import java.util.*

class CountryPickerAdapter(
    val context: Context,
    val countries: ArrayList<Country>,
    private val countryPick: CountryPick
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    interface CountryPick {

        fun pick(position: Int)
    }

    class Holder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {

        val countryName: TextView = itemView.findViewById(R.id.country_name_tv)
        val flagImv: TextView = itemView.findViewById(R.id.flag_imv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.country_code_picker, parent, false)
        return Holder(view)
    }


    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder, @SuppressLint("RecyclerView") position: Int
    ) {

        val holder = holder as Holder

        holder.flagImv.text =
            CountryCityUtils.getFlagId(countries[position].twoLetterCode.lowercase(Locale.getDefault()))

        holder.countryName.text = countries[position].name

        holder.itemView.setOnClickListener {

            countryPick.pick(position)
        }

    }


    override fun getItemCount(): Int {
        return countries.size
    }


}