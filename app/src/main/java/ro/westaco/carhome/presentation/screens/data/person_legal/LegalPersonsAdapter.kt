package ro.westaco.carhome.presentation.screens.data.person_legal

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestOptions
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import ro.westaco.carhome.data.sources.remote.responses.models.LegalPerson
import ro.westaco.carhome.di.ApiModule
import ro.westaco.carhome.utils.CountryCityUtils
import java.util.*

class LegalPersonsAdapter(
    private val context: Context,
    private var legalPersonDetails: ArrayList<LegalPerson>,
    private val listener: OnItemInteractionListener? = null
) : RecyclerView.Adapter<LegalPersonsAdapter.ViewHolder>() {

    private val appPreferences = AppPreferencesDelegates.get()
    interface OnItemInteractionListener {
        fun onClick(item: LegalPerson)
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int = legalPersonDetails.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_legal_person, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(legalPersonDetails[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var logo: ImageView = itemView.findViewById(R.id.logo)
        private var name: TextView = itemView.findViewById(R.id.name)
        private var tvLetter: TextView = itemView.findViewById(R.id.tv_f_l)

        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(item: LegalPerson) {
            name.text = item.companyName
            val singlechar = "${item.companyName}"

            itemView.setOnClickListener {
                listener?.onClick(item)
            }

            if (item.logoHref != null) {

                tvLetter.visibility = View.GONE
                logo.visibility = View.VISIBLE

                val url = "${ApiModule.BASE_URL_RESOURCES}${item.logoHref}"
                val glideUrl = GlideUrl(
                    url,
                    LazyHeaders.Builder()
                        .addHeader("Authorization", "Bearer ${appPreferences.token}")
                        .build()
                )

                val options = RequestOptions()
                logo.clipToOutline = true
                Glide.with(context)
                    .load(glideUrl)
                    .error(context.resources.getDrawable(R.drawable.logo_small))
                    .apply(
                        options.centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .priority(Priority.HIGH)
                            .format(DecodeFormat.PREFER_ARGB_8888)
                    )
                    .into(logo)
            } else {

                tvLetter.visibility = View.VISIBLE
                logo.visibility = View.GONE
                tvLetter.text = CountryCityUtils.firstTwo(singlechar.uppercase(Locale.getDefault()))
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(legalPersonDetails: List<LegalPerson>?) {
        this.legalPersonDetails = ArrayList(legalPersonDetails ?: listOf())
        notifyDataSetChanged()
    }
}