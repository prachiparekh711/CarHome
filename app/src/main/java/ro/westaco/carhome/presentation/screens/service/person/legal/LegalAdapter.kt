package ro.westaco.carhome.presentation.screens.service.person.legal

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
import ro.westaco.carhome.data.sources.remote.responses.models.LegalPerson
import ro.westaco.carhome.di.ApiModule
import ro.westaco.carhome.presentation.screens.service.person.BillingInfoViewModel.Companion.personID
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
        fun onListenerUsers(newItems: LegalPerson)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_bill_legal, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(legalPersons[position], position)

    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var legal_avatar: ImageView = itemView.findViewById(R.id.legal_avatar)
        private var tick_circle_legal: ImageView = itemView.findViewById(R.id.tick_circle_legal)
        private var tv_l_f_l: TextView = itemView.findViewById(R.id.tv_l_f_l)
        private var legal_tv_name: TextView = itemView.findViewById(R.id.legal_tv_name)
        private var tv_legal_address: TextView = itemView.findViewById(R.id.tv_legal_address)
        private var li_legal_address_missing: LinearLayout =
            itemView.findViewById(R.id.li_legal_address_missing)
        private var check: AppCompatImageView = itemView.findViewById(R.id.tick_circle_legal)

        @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n", "LogNotTimber")
        fun bind(item: LegalPerson, position: Int) {

            legal_tv_name.text = "${item.companyName}"
            val singlechar = "${item.companyName}"

            if (item.logoHref != null) {

                tv_l_f_l.visibility = View.INVISIBLE
                legal_avatar.visibility = View.VISIBLE

                val url = "${ApiModule.BASE_URL_RESOURCES}${item.logoHref}"
                val glideUrl = GlideUrl(
                    url,
                    LazyHeaders.Builder()
                        .addHeader("Authorization", "Bearer ${appPreferences.token}")
                        .build()
                )


                val options = RequestOptions()
                legal_avatar.clipToOutline = true

                var requestOptions = RequestOptions()
                requestOptions = requestOptions.transforms(CenterCrop(), RoundedCorners(90))

                Glide.with(context)
                    .load(glideUrl)
                    .apply(requestOptions)
                    .into(legal_avatar)
            } else {


                tv_l_f_l.visibility = View.VISIBLE
                legal_avatar.visibility = View.INVISIBLE
                tv_l_f_l.text = CountryCityUtils.firstTwo(singlechar.uppercase(Locale.getDefault()))
            }


            if (item.fullAddress.isNullOrEmpty()) {

                li_legal_address_missing.visibility = View.VISIBLE

            } else {

                tv_legal_address.text = item.fullAddress

            }

            check.isVisible = selectedPos == position

            itemView.setOnClickListener {
                val prevSelectedPos = selectedPos
                selectedPos = position
                personID = legalPersons[selectedPos].guid
                onListenerUsers.onListenerUsers(item)
                notifyItemChanged(prevSelectedPos)
                notifyItemChanged(selectedPos)
            }

        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun Items(naturalPersons: List<LegalPerson>?) {
        this.legalPersons = ArrayList(naturalPersons ?: listOf())
        notifyDataSetChanged()
    }

}