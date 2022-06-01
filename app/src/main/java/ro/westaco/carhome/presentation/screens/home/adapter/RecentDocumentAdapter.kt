package ro.westaco.carhome.presentation.screens.home.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.google.android.material.card.MaterialCardView
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import ro.westaco.carhome.data.sources.remote.responses.models.RowsItem
import ro.westaco.carhome.di.ApiModule

class RecentDocumentAdapter(
    private val context: Context,
    private var docList: ArrayList<RowsItem>,
    private val listener: OnItemInteractionListener? = null
) : RecyclerView.Adapter<RecentDocumentAdapter.ViewHolder>() {

    private val appPreferences = AppPreferencesDelegates.get()

    interface OnItemInteractionListener {
        fun onItemClick(item: RowsItem)
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int = docList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_document_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var title: TextView = itemView.findViewById(R.id.title)
        private var logo: ImageView = itemView.findViewById(R.id.logo)
        private var mainBG: RelativeLayout = itemView.findViewById(R.id.mainBG)
        private var logoCard: MaterialCardView = itemView.findViewById(R.id.logoCard)
        private var mView: View = itemView.findViewById(R.id.mView)
        private var pdf: ImageView = itemView.findViewById(R.id.pdf)

        @SuppressLint("NewApi", "UseCompatLoadingForDrawables")
        fun bind(position: Int) {

            val item = docList[position]
            title.text = item.name
            if (item.thumbnailHref != null) {
                val url = "${ApiModule.BASE_URL_RESOURCES}${item.thumbnailHref}"
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
                    .error(context.resources.getDrawable(R.drawable.ic_pdf))
                    .apply(
                        options.centerCrop()
                            .skipMemoryCache(true)
                            .priority(Priority.HIGH)
                            .format(DecodeFormat.PREFER_ARGB_8888)
                    )
                    .into(object : SimpleTarget<Drawable?>() {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: com.bumptech.glide.request.transition.Transition<in Drawable?>?
                        ) {
                            logo.background = context.resources.getDrawable(R.drawable.bg_dm)
                            logo.setImageDrawable(resource)
                        }

                    })
                logoCard.isVisible = true
                pdf.isVisible = false
            } else {
                pdf.setImageDrawable(context.resources.getDrawable(R.drawable.ic_pdf))
                pdf.isVisible = true
                logoCard.isVisible = false
            }
            mainBG.setOnClickListener {
                listener?.onItemClick(item)
            }

            if (position == docList.size - 1)
                mView.isVisible = false
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(cars: List<RowsItem>?) {
        this.docList = ArrayList(cars ?: listOf())
        notifyDataSetChanged()
    }

    fun clearAll() {
        this.docList.clear()
        notifyDataSetChanged()
    }
}