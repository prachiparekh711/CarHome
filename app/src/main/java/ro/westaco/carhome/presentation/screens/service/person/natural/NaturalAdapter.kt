package ro.westaco.carhome.presentation.screens.service.person.natural

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
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


class NaturalAdapter(
    private val context: Context,
    private var naturalPersons: ArrayList<NaturalPerson>,
    val onListener: OnItemSelectListView
) : RecyclerView.Adapter<NaturalAdapter.ViewHolder>() {

    private var selectedPos = -1
    private val appPreferences = AppPreferencesDelegates.get()

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int {
        return naturalPersons.size
    }

    interface OnItemSelectListView {
        fun onItemListUsers(newItems: NaturalPerson)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_bill_natural, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(naturalPersons[position], position)

    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var avatar: ImageView = itemView.findViewById(R.id.avatar)
        private var tv_name: TextView = itemView.findViewById(R.id.tv_name)
        private var tv_f_l: TextView = itemView.findViewById(R.id.tv_f_l)
        private var li_address_missing: LinearLayout =
            itemView.findViewById(R.id.li_address_missing)
        private var tv_address: TextView = itemView.findViewById(R.id.tv_address)
        private var check: AppCompatImageView = itemView.findViewById(R.id.tick_circle)

        @SuppressLint("SetTextI18n")
        fun bind(item: NaturalPerson, position: Int) {

            tv_name.text = "${item.firstName ?: ""} ${item.lastName ?: ""}"
            val singleChar = "${item.firstName} ${item.lastName}"

            if (item.logoHref != null) {

                tv_f_l.visibility = View.INVISIBLE
                avatar.visibility = View.VISIBLE

                val url = "${ApiModule.BASE_URL_RESOURCES}${item.logoHref}"
                val glideUrl = GlideUrl(
                    url,
                    LazyHeaders.Builder()
                        .addHeader("Authorization", "Bearer ${appPreferences.token}").build()
                )

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
                tv_f_l.text = singleChar.replace(
                    "^\\s*([a-zA-Z]).*\\s+([a-zA-Z])\\S+$".toRegex(),
                    "$1$2"
                ).uppercase(Locale.getDefault())
            }


            if (item.email.isNullOrEmpty()) {

                li_address_missing.visibility = View.VISIBLE
                tv_address.visibility = View.GONE

            } else {

                li_address_missing.visibility = View.GONE
                tv_address.visibility = View.VISIBLE
                tv_address.text = item.email

            }

            check.isVisible = selectedPos == position

            itemView.setOnClickListener {
                val prevSelectedPos = selectedPos
                selectedPos = position
                onListener.onItemListUsers(item)
                notifyItemChanged(prevSelectedPos)
                notifyItemChanged(selectedPos)
            }

        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun Items(naturalPersons: List<NaturalPerson>?) {
        this.naturalPersons = ArrayList(naturalPersons ?: listOf())
        notifyDataSetChanged()
    }

}