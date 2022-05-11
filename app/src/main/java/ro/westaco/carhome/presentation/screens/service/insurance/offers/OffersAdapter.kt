package ro.westaco.carhome.presentation.screens.service.insurance.offers

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestOptions
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import ro.westaco.carhome.data.sources.remote.responses.models.OffersItem
import ro.westaco.carhome.di.ApiModule

class OffersAdapter(val context: Context, val listener: OnItemInteractionListener?) :
    RecyclerView.Adapter<OffersAdapter.ViewHolder>() {

    var list = ArrayList<OffersItem>()

    interface OnItemInteractionListener {
        fun onOfferClick(item: OffersItem)
        fun onPIDClick(item: OffersItem)
        fun onRCAClick(item: OffersItem)
        fun onRCADSClick(item: OffersItem)
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int = list.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_ins_offers, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var insurerImage: ImageView = itemView.findViewById(R.id.insurerImage)
        private var tv_view_offer: TextView = itemView.findViewById(R.id.tv_view_offer)
        private var nViewPID: TextView = itemView.findViewById(R.id.nViewPID)
        private var tv_offer_title: TextView = itemView.findViewById(R.id.tv_offer_title)
        private var description: TextView = itemView.findViewById(R.id.description)
        private var messageFromProvider: TextView = itemView.findViewById(R.id.messageFromProvider)
        private var price: TextView = itemView.findViewById(R.id.price)
        private var priceDs: TextView = itemView.findViewById(R.id.priceDs)
        private var rcaLL: LinearLayout = itemView.findViewById(R.id.rcaLL)
        private var rcadsLL: LinearLayout = itemView.findViewById(R.id.rcadsLL)

        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(position: Int) {

            val item = list[position]

            val url = "${ApiModule.BASE_URL_RESOURCES}${item.insurerLogoHref}"

            val glideUrl = GlideUrl(
                url,
                LazyHeaders.Builder()
                    .addHeader("Authorization", "Bearer ${AppPreferencesDelegates.get().token}")
                    .build()
            )
            val options = RequestOptions()
            insurerImage.clipToOutline = true
            Glide.with(context)
                .load(glideUrl)
                .error(context.resources.getDrawable(R.drawable.ic_profile_picture))
                .apply(
                    options.centerCrop()
                        .skipMemoryCache(true)
                        .priority(Priority.HIGH)
                        .format(DecodeFormat.PREFER_ARGB_8888)
                )
                .into(insurerImage)


            tv_offer_title.text = item.insurerNameLong
            description.text = item.description

            messageFromProvider.isVisible = item.messageFromProvider != null

            messageFromProvider.text = item.messageFromProvider
            price.text = "${item.price} ${item.currency}"
            priceDs.text = "${item.priceDs} ${item.currency}"

            tv_view_offer.setOnClickListener {
                listener?.onOfferClick(item)
            }

            nViewPID.setOnClickListener {
                listener?.onPIDClick(item)
            }

            rcaLL.setOnClickListener {
                listener?.onRCAClick(item)
            }

            rcadsLL.setOnClickListener {
                listener?.onRCADSClick(item)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(offerList: List<OffersItem>?) {
        this.list.clear()
        this.list = java.util.ArrayList(offerList ?: listOf())
        notifyDataSetChanged()
    }


}