package ro.westaco.carhome.presentation.screens.service.insurance.offers

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
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestOptions
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import ro.westaco.carhome.data.sources.remote.responses.models.NaturalPersonDetails
import ro.westaco.carhome.di.ApiModule

class InsOfferDetailsAdapter(var context: Context) :
    RecyclerView.Adapter<InsOfferDetailsAdapter.ViewHolder>() {

    var list = ArrayList<NaturalPersonDetails>()

    private val appPreferences = AppPreferencesDelegates.get()
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = inflater.inflate(R.layout.driver_list_items, parent, false)
        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var mDriverImage: ImageView = itemView.findViewById(R.id.mDriverImage)
        private var mDriverName: TextView = itemView.findViewById(R.id.mDriverName)
        private var mDriverFirstName: TextView = itemView.findViewById(R.id.mDriverFirstName)
        private var mDriverLastName: TextView = itemView.findViewById(R.id.mDriverLastName)
        private var mDriverSerialNo: TextView = itemView.findViewById(R.id.mDriverSerialNo)
        private var mDriverPIN: TextView = itemView.findViewById(R.id.mDriverPIN)

        fun onBind(position: Int) {

            val item = list[position]

            val url = "${ApiModule.BASE_URL_RESOURCES}${item.logoHref}"
            val glideUrl = GlideUrl(
                url,
                LazyHeaders.Builder()
                    .addHeader("Authorization", "Bearer ${appPreferences.token}").build()
            )

            val options = RequestOptions()
            mDriverImage.clipToOutline = true
            Glide.with(context)
                .load(glideUrl)
                .error(context.resources.getDrawable(R.drawable.ic_profile_picture))
                .apply(
                    options.centerCrop()
                        .skipMemoryCache(true)
                        .priority(Priority.HIGH)
                        .format(DecodeFormat.PREFER_ARGB_8888)
                )
                .into(mDriverImage)

            mDriverName.text = item.firstName + " " + item.lastName
            mDriverFirstName.text = item.firstName
            mDriverLastName.text = item.lastName
            mDriverPIN.text = item.cnp


        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(offerList: List<NaturalPersonDetails>?) {
        this.list.clear()
        this.list = java.util.ArrayList(offerList ?: listOf())
        notifyDataSetChanged()
    }
}