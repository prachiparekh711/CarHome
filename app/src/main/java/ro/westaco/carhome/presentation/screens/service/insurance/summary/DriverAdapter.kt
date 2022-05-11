package ro.westaco.carhome.presentation.screens.service.insurance.summary

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
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestOptions
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import ro.westaco.carhome.data.sources.remote.responses.models.NaturalPersonDetails
import ro.westaco.carhome.di.ApiModule

class DriverAdapter(
    private val context: Context,
    var naturalPersons: ArrayList<NaturalPersonDetails>,
) :
    RecyclerView.Adapter<DriverAdapter.ViewHolder>() {

    private val appPreferences = AppPreferencesDelegates.get()

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = inflater.inflate(R.layout.driver_summary_items, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(naturalPersons[position])
    }

    override fun getItemCount(): Int {
        return naturalPersons.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var mDriverName: TextView = itemView.findViewById(R.id.mDriverName)
        private var mSelectDriverImage: ImageView = itemView.findViewById(R.id.mSelectDriverImage)

        fun bind(
            item: NaturalPersonDetails,
        ) {
            val url = "${ApiModule.BASE_URL_RESOURCES}${item.logoHref}"
            val glideUrl = GlideUrl(
                url,
                LazyHeaders.Builder()
                    .addHeader("Authorization", "Bearer ${appPreferences.token}").build()
            )
            val options = RequestOptions()
            mSelectDriverImage.clipToOutline = true
            Glide.with(context)
                .load(glideUrl)
                .error(context.resources.getDrawable(R.drawable.ic_profile_picture))
                .apply(
                    options.centerCrop()
                        .skipMemoryCache(true)
                        .priority(Priority.HIGH)
                        .format(DecodeFormat.PREFER_ARGB_8888)
                )
                .into(mSelectDriverImage)
            mDriverName.text = item.firstName + " " + item.lastName
        }
    }


}