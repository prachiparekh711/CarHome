package ro.westaco.carhome.presentation.screens.service.insurance.adapter

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
import ro.westaco.carhome.data.sources.remote.responses.models.NaturalPerson
import ro.westaco.carhome.di.ApiModule

class DriverAdapter(
    val context: Context,
    val listener: OnRemoveListener?
) :
    RecyclerView.Adapter<DriverAdapter.ViewHolder>() {

    var list = ArrayList<NaturalPerson>()

    interface OnRemoveListener {
        fun onRemoveClick(position: Int)
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int = list.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.rca_person_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var mSelectUserImage: ImageView = itemView.findViewById(R.id.mSelectUserImage)
        private var remove: ImageView = itemView.findViewById(R.id.remove)
        private var mSelectUserName: TextView = itemView.findViewById(R.id.mSelectUserName)
        private var mSelectUserEmail: TextView = itemView.findViewById(R.id.mSelectUserEmail)

        fun bind(position: Int) {

            val item = list[position]

            val url = "${ApiModule.BASE_URL_RESOURCES}${item.logoHref}"
            val glideUrl = GlideUrl(
                url,
                LazyHeaders.Builder()
                    .addHeader("Authorization", "Bearer ${AppPreferencesDelegates.get().token}")
                    .build()
            )
            val options = RequestOptions()
            mSelectUserImage.clipToOutline = true
            Glide.with(context)
                .load(glideUrl)
                .error(context.resources.getDrawable(R.drawable.ic_profile_picture))
                .apply(
                    options.centerCrop()
                        .skipMemoryCache(true)
                        .priority(Priority.HIGH)
                        .format(DecodeFormat.PREFER_ARGB_8888)
                )
                .into(mSelectUserImage)

            mSelectUserName.text = "${item.firstName} ${item.lastName}"
            mSelectUserEmail.text = "${item.email}"

            remove.setOnClickListener {

            listener?.onRemoveClick(position)
                list.removeAt(position)
                notifyItemRemoved(position)
            }
        }
    }

    fun setItems(itemList: List<NaturalPerson>) {
        this.list.clear()
        this.list.addAll(itemList)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearItems() {
        this.list.clear()
        notifyDataSetChanged()
    }


}