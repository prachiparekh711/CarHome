package ro.westaco.carhome.presentation.screens.data.person_natural

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
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
import ro.westaco.carhome.data.sources.remote.responses.models.NaturalPerson
import ro.westaco.carhome.di.ApiModule
import ro.westaco.carhome.presentation.screens.main.MainActivity


class NaturalPersonsAdapter(
    private val context: Context,
    private var naturalPersons: ArrayList<NaturalPerson>,
    private val listener: OnItemInteractionListener? = null
) : RecyclerView.Adapter<NaturalPersonsAdapter.ViewHolder>() {

    private val appPreferences = AppPreferencesDelegates.get()

    interface OnItemInteractionListener {
        fun onClick(item: NaturalPerson)
        fun onEdit(item: NaturalPerson)
        fun onDial(item: NaturalPerson)
        fun onMail(item: NaturalPerson)
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int = naturalPersons.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_natural_person, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(naturalPersons[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var avatar: ImageView = itemView.findViewById(R.id.avatar)
        private var iv_phone: ImageView = itemView.findViewById(R.id.iv_phone)
        private var iv_mail: ImageView = itemView.findViewById(R.id.iv_mail)
        private var fullName: TextView = itemView.findViewById(R.id.fullName)
        private var youText: TextView = itemView.findViewById(R.id.youText)

        fun bind(item: NaturalPerson) {
            fullName.text = "${item.firstName ?: ""} ${item.lastName ?: ""}"

            if (item.id?.toInt() == MainActivity.activeId) {
                iv_phone.isVisible = false
                iv_mail.isVisible = false
                youText.isVisible = true
            } else {
                youText.isVisible = false
            }

            if (item.phone.isNullOrEmpty()) {

                iv_phone.visibility = View.GONE
            }

            if (item.email.isNullOrEmpty()) {

                iv_mail.visibility = View.GONE
            }

            itemView.setOnClickListener {
                listener?.onClick(item)
            }

            if (item.logoHref != null) {

                val url = "${ApiModule.BASE_URL_RESOURCES}${item.logoHref}"
                val glideUrl = GlideUrl(
                    url, LazyHeaders.Builder()
                        .addHeader("Authorization", "Bearer ${appPreferences.token}")
                        .build()
                )

                val options = RequestOptions()
                avatar.clipToOutline = true

                Glide.with(context)
                    .load(glideUrl)
                    .error(context.resources.getDrawable(R.drawable.ic_user))
                    .apply(
                        options.centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .priority(Priority.HIGH)
                            .format(DecodeFormat.PREFER_ARGB_8888)
                    )
                    .into(avatar)
            } else {
                avatar.setImageResource(R.drawable.ic_user)
            }

            iv_phone.setOnClickListener {
                listener?.onDial(item)
            }

            iv_mail.setOnClickListener {
                listener?.onMail(item)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(naturalPersons: List<NaturalPerson>?) {
        this.naturalPersons = ArrayList(naturalPersons ?: listOf())
        notifyDataSetChanged()
    }


}