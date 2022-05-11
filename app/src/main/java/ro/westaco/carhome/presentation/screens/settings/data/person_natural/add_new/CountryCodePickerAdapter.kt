package ro.westaco.carhome.presentation.screens.settings.data.person_natural.add_new

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView
import ro.westaco.carhome.R
import ro.westaco.carhome.utils.CountryCityUtils
import java.util.*


class CountryCodePickerAdapter(

    val context: Context,
    val countries: ArrayList<PhoneCodeModel>,
    val countyrPick: CountyrPick
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    interface CountyrPick {

        fun pick(countries: PhoneCodeModel)
    }

    class Holder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {

        val country_name_tv: TextView = itemView.findViewById(R.id.country_name_tv)
        val flag_imv: CircleImageView = itemView.findViewById(R.id.flag_imv)
        val code_tv: TextView = itemView.findViewById(R.id.code_tv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.country_code, parent, false)
        return Holder(view)
    }

    @SuppressLint("NotifyDataSetChanged", "LogNotTimber", "SetTextI18n")
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {

        val holder = holder as Holder

        val ch = CountryCityUtils.firstTwo(countries[position].key.toString())
        /*var requestOptions = RequestOptions()
        requestOptions = requestOptions.transforms(CenterCrop(), RoundedCorners(90))*/
        Glide.with(context)
            .load(
                CountryCityUtils.getFlagDrawableResId(
                    ch?.lowercase(Locale.getDefault()).toString()
                )
            )
            .into(holder.flag_imv)


        holder.code_tv.text = "+" + countries[position].value

        holder.itemView.setOnClickListener {

            countyrPick.pick(countries[position])
        }

        val loc = Locale("", ch)
        holder.country_name_tv.text = loc.displayCountry


    }


    override fun getItemCount(): Int {
        return countries.size
    }


    fun firstTwo(str: String): String? {
        return if (str.length < 2) str else str.substring(0, 2)
    }


}