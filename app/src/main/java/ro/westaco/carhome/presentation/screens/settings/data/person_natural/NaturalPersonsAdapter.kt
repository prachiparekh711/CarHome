package ro.westaco.carhome.presentation.screens.settings.data.person_natural

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import ro.westaco.carhome.data.sources.remote.responses.models.NaturalPerson
import ro.westaco.carhome.di.ApiModule
import java.util.*


class NaturalPersonsAdapter(
    private val context: Context,
    private var naturalPersons: ArrayList<NaturalPerson>,
    private val listener: OnItemInteractionListener? = null
) : RecyclerView.Adapter<NaturalPersonsAdapter.ViewHolder>() {

    private val appPreferences = AppPreferencesDelegates.get()

    interface OnItemInteractionListener {
        fun onClick(item: NaturalPerson)
        fun onEdit(item: NaturalPerson)
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
        private var tv_f_l: TextView = itemView.findViewById(R.id.tv_f_l)


        @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n", "LogNotTimber")
        fun bind(item: NaturalPerson) {
            fullName.text = "${item.firstName} ${item.lastName}"
            val singlechar = "${item.firstName} ${item.lastName}"

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

                tv_f_l.visibility = View.INVISIBLE
                avatar.visibility = View.VISIBLE

                val url = "${ApiModule.BASE_URL_RESOURCES}${item.logoHref}"
                val glideUrl = GlideUrl(
                    url, LazyHeaders.Builder()
                        .addHeader("Authorization", "Bearer ${appPreferences.token}")
                        .build()
                )


                val options = RequestOptions()
                avatar.clipToOutline = true

                var requestOptions = RequestOptions()
                requestOptions = requestOptions.transforms(CenterCrop(), RoundedCorners(90))

                Glide.with(context)
                    .load(glideUrl)
                    .apply(requestOptions)
                    .into(avatar)
            } else {

                tv_f_l.visibility = View.VISIBLE
                avatar.visibility = View.INVISIBLE
                tv_f_l.text = singlechar.replace(
                    "^\\s*([a-zA-Z]).*\\s+([a-zA-Z])\\S+$".toRegex(),
                    "$1$2"
                ).uppercase(Locale.getDefault())
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(naturalPersons: List<NaturalPerson>?) {
        this.naturalPersons = ArrayList(naturalPersons ?: listOf())
        notifyDataSetChanged()
    }


}