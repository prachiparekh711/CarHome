package ro.westaco.carhome.presentation.screens.settings.data.person_natural.add_new

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import de.hdodenhof.circleimageview.CircleImageView
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.Country
import ro.westaco.carhome.utils.CountryCityUtils
import java.util.*

class CountryPickerAdapter(
    val context: Context,
    val countries: ArrayList<Country>,
    val countyrPick: CountyrPick
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    interface CountyrPick {

        fun pick(position: Int)
    }

    class Holder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {

        val country_name_tv: TextView = itemView.findViewById(R.id.country_name_tv)
        val flag_imv: CircleImageView = itemView.findViewById(R.id.flag_imv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.country_code_picker, parent, false)
        return Holder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {

        val holder = holder as Holder

        var requestOptions = RequestOptions()
        requestOptions = requestOptions.transforms(CenterCrop(), RoundedCorners(90))
        Glide.with(context)
            .load(
                CountryCityUtils.getFlagDrawableResId(
                    countries[position].twoLetterCode.lowercase(
                        Locale.getDefault()
                    )
                )
            )
            .apply(requestOptions).into(holder.flag_imv)

        holder.country_name_tv.text = countries[position].name

        holder.itemView.setOnClickListener {

            countyrPick.pick(position)
        }

    }


    override fun getItemCount(): Int {
        return countries.size
    }


}