package ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.bill_user.legal

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import ro.westaco.carhome.data.sources.remote.responses.models.LegalPerson
import ro.westaco.carhome.di.ApiModule
import ro.westaco.carhome.utils.CountryCityUtils
import java.util.*


class LegalAdapter(
    private val context: Context,
    private var legalPersons: ArrayList<LegalPerson>,
    val onListenerUsers: OnItemSelectListViewUser
) : RecyclerView.Adapter<LegalAdapter.ViewHolder>() {

    private var selectedPos = -1
    private val appPreferences = AppPreferencesDelegates.get()
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int {
        return legalPersons.size
    }

    interface OnItemSelectListViewUser {
        fun onListenerUsers(newItems: LegalPerson, isView: Boolean)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_bill_legal, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(legalPersons[position], position)

    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var legalAvatar: ImageView = itemView.findViewById(R.id.legal_avatar)
        private var tvLetter: TextView = itemView.findViewById(R.id.tv_l_f_l)
        private var legalName: TextView = itemView.findViewById(R.id.legal_tv_name)
        private var legalAddress: TextView = itemView.findViewById(R.id.tv_legal_address)
        private var addressMissing: LinearLayout =
            itemView.findViewById(R.id.li_legal_address_missing)
        private var action: AppCompatImageView = itemView.findViewById(R.id.action)

        fun bind(item: LegalPerson, position: Int) {

            legalName.text = "${item.companyName}"
            val singleChar = "${item.companyName}"

            legalAvatar.clipToOutline = true

            var requestOptions = RequestOptions()
            requestOptions = requestOptions.transforms(CenterCrop(), RoundedCorners(90))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)


            if (selectedPos == position) {
                Glide.with(context)
                    .load(context.resources.getDrawable(R.drawable.ic_person_select_tick))
                    .apply(requestOptions)
                    .into(legalAvatar)
            } else {
                if (item.logoHref != null) {

                    tvLetter.visibility = View.INVISIBLE
                    legalAvatar.visibility = View.VISIBLE

                    val url = "${ApiModule.BASE_URL_RESOURCES}${item.logoHref}"
                    val glideUrl = GlideUrl(
                        url,
                        LazyHeaders.Builder()
                            .addHeader("Authorization", "Bearer ${appPreferences.token}")
                            .build()
                    )

                    Glide.with(context)
                        .load(glideUrl)
                        .apply(requestOptions)
                        .into(legalAvatar)

                } else {
                    tvLetter.visibility = View.VISIBLE
                    legalAvatar.visibility = View.INVISIBLE
                    tvLetter.text =
                        CountryCityUtils.firstTwo(singleChar.uppercase(Locale.getDefault()))
                }
            }


            if (item.fullAddress.isNullOrEmpty()) {
                addressMissing.visibility = View.VISIBLE
                legalAddress.visibility = View.GONE
            } else {
                addressMissing.visibility = View.GONE
                legalAddress.visibility = View.VISIBLE
                legalAddress.text = item.fullAddress
            }

            itemView.setOnClickListener {
                val prevSelectedPos = selectedPos
                selectedPos = position
                onListenerUsers.onListenerUsers(item, false)
                notifyItemChanged(prevSelectedPos)
                notifyItemChanged(selectedPos)
            }

            action.setOnClickListener {
                onListenerUsers.onListenerUsers(item, true)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(naturalPersons: List<LegalPerson>?) {
        this.legalPersons = ArrayList(naturalPersons ?: listOf())
        notifyDataSetChanged()
    }

}