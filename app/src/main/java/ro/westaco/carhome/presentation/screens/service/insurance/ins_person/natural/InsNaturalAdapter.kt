package ro.westaco.carhome.presentation.screens.service.insurance.ins_person.natural

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
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import ro.westaco.carhome.data.sources.remote.responses.models.NaturalPerson
import ro.westaco.carhome.data.sources.remote.responses.models.VerifyRcaPerson
import ro.westaco.carhome.data.sources.remote.responses.models.WarningsItem
import ro.westaco.carhome.di.ApiModule
import ro.westaco.carhome.presentation.screens.main.MainActivity
import ro.westaco.carhome.presentation.screens.service.insurance.init.InsuranceFragment
import ro.westaco.carhome.presentation.screens.service.insurance.ins_person.SelectUserFragment.Companion.verifyNaturalList
import java.util.*

class InsNaturalAdapter(
    private val context: Context,
    private var naturalPersons: ArrayList<NaturalPerson>,
    val onListener: OnItemSelectListView
) : RecyclerView.Adapter<InsNaturalAdapter.ViewHolder>() {

    private var selectedPos = -1
    private val appPreferences = AppPreferencesDelegates.get()

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int {
        return naturalPersons.size
    }

    interface OnItemSelectListView {
        fun onItemListUsers(
            newItems: NaturalPerson,
            isEdit: Boolean,
            isView: Boolean,
            verifyItem: VerifyRcaPerson?
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_ins_natural, parent, false)
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
        private var youText: TextView = itemView.findViewById(R.id.youText)
        private var action: AppCompatImageView = itemView.findViewById(R.id.action)

        @SuppressLint("SetTextI18n")
        fun bind(item: NaturalPerson, position: Int) {
            youText.isVisible = item.id?.toInt() == MainActivity.activeId

            tv_name.text = "${item.firstName ?: ""} ${item.lastName ?: ""}"
            val singleChar = "${item.firstName} ${item.lastName}"

            avatar.clipToOutline = true

            var requestOptions = RequestOptions()
            requestOptions = requestOptions.transforms(CenterCrop(), RoundedCorners(90))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)

            if (selectedPos == position) {
                Glide.with(context)
                    .load(context.resources.getDrawable(R.drawable.ic_person_select_tick))
                    .apply(requestOptions)
                    .into(avatar)
            } else {
                if (item.logoHref != null) {

                    tv_f_l.visibility = View.INVISIBLE
                    avatar.visibility = View.VISIBLE

                    val url = "${ApiModule.BASE_URL_RESOURCES}${item.logoHref}"
                    val glideUrl = GlideUrl(
                        url,
                        LazyHeaders.Builder()
                            .addHeader("Authorization", "Bearer ${appPreferences.token}").build()
                    )

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
            }


            tv_address.text = item.email

            var rcaVerifyItem: VerifyRcaPerson? = null
            var warningList = ArrayList<WarningsItem>()
            if (InsuranceFragment.IS_PERSON_EDITABLE) {
                rcaVerifyItem = verifyNaturalList?.find { it.id == item.id?.toInt() }
                if (rcaVerifyItem != null) {
                    warningList =
                        rcaVerifyItem.validationResult?.warnings as ArrayList<WarningsItem>
                    if (warningList.size == 0) {
                        action.setImageDrawable(context.resources.getDrawable(R.drawable.ic_view_person))
                        li_address_missing.visibility = View.GONE
                        tv_address.visibility = View.VISIBLE
                    } else {
                        action.setImageDrawable(context.resources.getDrawable(R.drawable.ic_edit_person))
                        li_address_missing.visibility = View.VISIBLE
                        tv_address.visibility = View.GONE
                    }
                } else {
                    action.setImageDrawable(context.resources.getDrawable(R.drawable.ic_view_person))
                    li_address_missing.visibility = View.GONE
                    tv_address.visibility = View.VISIBLE
                }
            } else {
                action.setImageDrawable(context.resources.getDrawable(R.drawable.ic_view_person))
                li_address_missing.visibility = View.GONE
                tv_address.visibility = View.VISIBLE
            }


            itemView.setOnClickListener {
                val prevSelectedPos = selectedPos
                if (prevSelectedPos != -1)
                    notifyItemChanged(prevSelectedPos)
                selectedPos = position
                notifyItemChanged(selectedPos)
                if (InsuranceFragment.IS_PERSON_EDITABLE) {
                    if (warningList.isNotEmpty())
                        onListener.onItemListUsers(item, isEdit = true, false, rcaVerifyItem)
                    else
                        onListener.onItemListUsers(item, isEdit = false, false, rcaVerifyItem)
                } else {
                    onListener.onItemListUsers(item, isEdit = false, false, rcaVerifyItem)
                }
            }

            action.setOnClickListener {
                if (warningList.isEmpty()) {
                    //                    Person View
                    onListener.onItemListUsers(item, isEdit = false, true, null)
                } else {
//                    Person Edit
                    onListener.onItemListUsers(item, isEdit = true, false, rcaVerifyItem)
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(naturalPersons: List<NaturalPerson>?) {
        this.naturalPersons = ArrayList(naturalPersons ?: listOf())
        notifyDataSetChanged()
    }

}