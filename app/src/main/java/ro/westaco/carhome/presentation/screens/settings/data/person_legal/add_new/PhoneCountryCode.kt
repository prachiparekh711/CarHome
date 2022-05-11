package ro.westaco.carhome.presentation.screens.settings.data.person_legal.add_new

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ro.westaco.carhome.R
import ro.westaco.carhome.presentation.screens.settings.data.person_natural.add_new.PhoneCodeModel
import ro.westaco.carhome.utils.CountryCityUtils
import java.util.*

class PhoneCountryCode(
    val context: Context,
    val countries: ArrayList<PhoneCodeModel>,
    val listener: countryCodePhone
) : RecyclerView.Adapter<PhoneCountryCode.ViewModel>() {

    interface countryCodePhone {

        fun phoneCountry(countries: PhoneCodeModel, position: Int)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhoneCountryCode.ViewModel {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.phone_code, parent, false)
        return ViewModel(view)
    }

    override fun onBindViewHolder(holder: PhoneCountryCode.ViewModel, position: Int) {
        holder.onBind()
    }

    override fun getItemCount(): Int {
        return countries.size
    }

    inner class ViewModel(items: View) : RecyclerView.ViewHolder(items) {

        var mLinear: LinearLayout = items.findViewById(R.id.mLinear)
        var mTitle: TextView = items.findViewById(R.id.mTitle)
        var mCountry: TextView = items.findViewById(R.id.mCountry)
        var mImage: TextView = items.findViewById(R.id.mImage)
        var mSelection: ImageView = items.findViewById(R.id.mSelection)
        var mView: View = items.findViewById(R.id.mView)
//        var mCard: MaterialCardView = items.findViewById(R.id.mCard)

        @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
        fun onBind() {

            val item = countries[position]
            val ch = CountryCityUtils.firstTwo(item.key.toString())

//            mImage.text = CountryCityUtils.getFlagId(ch?.lowercase(Locale.getDefault()).toString())
            mImage.text = CountryCityUtils.getFlagId(ch?.lowercase(Locale.getDefault()).toString())

            mTitle.text = "+" + countries[position].value
            val loc = ch?.let { Locale("", it) }
            mCountry.text = loc?.displayCountry


            itemView.setOnClickListener {

                listener.phoneCountry(item, position)

            }

        }
    }
}