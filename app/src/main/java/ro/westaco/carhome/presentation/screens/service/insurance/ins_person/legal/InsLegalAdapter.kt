package ro.westaco.carhome.presentation.screens.service.insurance.ins_person.legal

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
import ro.westaco.carhome.data.sources.remote.responses.models.VerifyRcaPerson
import ro.westaco.carhome.data.sources.remote.responses.models.WarningsItem
import ro.westaco.carhome.di.ApiModule
import ro.westaco.carhome.presentation.screens.service.insurance.init.InsuranceFragment
import ro.westaco.carhome.presentation.screens.service.insurance.ins_person.SelectUserFragment
import ro.westaco.carhome.utils.CountryCityUtils
import java.util.*

class InsLegalAdapter(
    private val context: Context,
    private var legalPersons: ArrayList<LegalPerson>,
    val onListenerUsers: OnItemSelectListViewUser
) : RecyclerView.Adapter<InsLegalAdapter.ViewHolder>() {

    private var selectedPos = -1
    private val appPreferences = AppPreferencesDelegates.get()
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int {
        return legalPersons.size
    }

    interface OnItemSelectListViewUser {
        fun onListenerUsers(
            newItems: LegalPerson,
            isEdit: Boolean,
            isView: Boolean,
            verifyItem: VerifyRcaPerson?
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_ins_legal, parent, false)
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

            legalAddress.text = item.fullAddress
            var warningList = ArrayList<WarningsItem>()
            var rcaVerifyItem: VerifyRcaPerson? = null
            if (InsuranceFragment.IS_PERSON_EDITABLE) {
                rcaVerifyItem = SelectUserFragment.verifyLegalList?.find { it.id == item.id }
                if (rcaVerifyItem != null) {
                    warningList =
                        rcaVerifyItem.validationResult?.warnings as ArrayList<WarningsItem>
                    if (warningList.size == 0) {
                        action.setImageDrawable(context.resources.getDrawable(R.drawable.ic_view_person))
                        addressMissing.visibility = View.GONE
                        legalAddress.visibility = View.VISIBLE
                    } else {
                        action.setImageDrawable(context.resources.getDrawable(R.drawable.ic_edit_person))
                        addressMissing.visibility = View.VISIBLE
                        legalAddress.visibility = View.GONE
                    }
                } else {
                    action.setImageDrawable(context.resources.getDrawable(R.drawable.ic_view_person))
                    addressMissing.visibility = View.GONE
                    legalAddress.visibility = View.VISIBLE
                }
            } else {
                action.setImageDrawable(context.resources.getDrawable(R.drawable.ic_view_person))
                addressMissing.visibility = View.GONE
                legalAddress.visibility = View.VISIBLE
            }

            itemView.setOnClickListener {
                val prevSelectedPos = selectedPos
                if (prevSelectedPos != -1)
                    notifyItemChanged(prevSelectedPos)
                selectedPos = position
                if (InsuranceFragment.IS_PERSON_EDITABLE) {
                    if (warningList.isNotEmpty())
                        onListenerUsers.onListenerUsers(item, true, false, rcaVerifyItem)
                    else
                        onListenerUsers.onListenerUsers(item, false, false, rcaVerifyItem)
                } else
                    onListenerUsers.onListenerUsers(item, false, false, rcaVerifyItem)
                notifyItemChanged(selectedPos)
            }

            action.setOnClickListener {
                if (warningList.isEmpty()) {
//                    Person View
                    onListenerUsers.onListenerUsers(item, false, true, null)
                } else {
//                    Person Edit
                    onListenerUsers.onListenerUsers(item, true, false, rcaVerifyItem)
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(naturalPersons: List<LegalPerson>?) {
        this.legalPersons = ArrayList(naturalPersons ?: listOf())
        notifyDataSetChanged()
    }

}