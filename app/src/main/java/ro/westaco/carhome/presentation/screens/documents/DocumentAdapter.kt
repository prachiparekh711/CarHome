package ro.westaco.carhome.presentation.screens.documents

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
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
import com.bumptech.glide.request.target.SimpleTarget
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import ro.westaco.carhome.data.sources.remote.responses.models.RowsItem
import ro.westaco.carhome.di.ApiModule
import ro.westaco.carhome.presentation.screens.documents.DocumentsFragment.Companion.multipleSelection
import ro.westaco.carhome.presentation.screens.documents.DocumentsFragment.Companion.selectedDocList
import ro.westaco.carhome.presentation.screens.documents.DocumentsFragment.Companion.selectedList
import ro.westaco.carhome.utils.FileUtil
import java.text.SimpleDateFormat
import java.util.*


class DocumentAdapter(
    private val context: Context,
    private var docList: ArrayList<RowsItem>,
    private val listener: OnItemInteractionListener? = null,
    private val listener1: OnOptionListener? = null,
    private val listener2: DocumentsFragment.UIChangeListener? = null
) : RecyclerView.Adapter<DocumentAdapter.ViewHolder>() {

    private val appPreferences = AppPreferencesDelegates.get()

    interface OnItemInteractionListener {
        fun onItemClick(item: RowsItem)
    }

    interface OnOptionListener {
        fun onOptionClick(item: RowsItem)
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int = docList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_document, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var name: TextView = itemView.findViewById(R.id.name)
        private var docSize: TextView = itemView.findViewById(R.id.docSize)
        private var docDate: TextView = itemView.findViewById(R.id.docDate)
        private var mOption: ImageView = itemView.findViewById(R.id.mOption)
        private var selection: ImageView = itemView.findViewById(R.id.selection)
        private var logo: ImageView = itemView.findViewById(R.id.logo)
        private var mainLL: LinearLayout = itemView.findViewById(R.id.mainLL)
        private var mainBG: RelativeLayout = itemView.findViewById(R.id.mainBG)

        @SuppressLint("NewApi")
        fun bind(position: Int) {
            val item = docList[position]
            name.text = item.name
            docSize.text = item.fileSize?.toLong()?.let { FileUtil.getSize(it) }
            var spf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            val newDate: Date = spf.parse(item.uploadedDate)
            spf = SimpleDateFormat("dd MMM yyyy")
            docDate.text = "${spf.format(newDate) ?: ""}"

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
                    .asDrawable()
                    .load(glideUrl)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .error(context.resources.getDrawable(R.drawable.ic_pdf))
                    .apply(
                        options.centerCrop()
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
            } else {
                val options = RequestOptions()
                logo.clipToOutline = true
                Glide.with(context)
                    .load(context.resources.getDrawable(R.drawable.ic_pdf))
                    .apply(
                        options.centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .priority(Priority.HIGH)
                            .format(DecodeFormat.PREFER_ARGB_8888)
                    )
                    .into(logo)
                logo.background = null
            }

            mainLL.setOnClickListener {
                if (multipleSelection) {
                    item.id?.let { it1 ->
                        if (selectedList.contains(it1)) {
                            selectedList.remove(it1)
                            selectedDocList.remove(it1)
                            if (selectedList.size == 0)
                                multipleSelection = false
                        } else {
                            selectedList.add(it1)
                            selectedDocList.add(it1)
                        }
                        changeItemUI(it1)
                        listener2?.onLongClick()
                    }
                } else {
                    listener?.onItemClick(item)
                }
            }

            mOption.setOnClickListener {
                listener1?.onOptionClick(item)
            }

            item.id?.let { changeItemUI(it) }

            mainLL.setOnLongClickListener {
                if (selectedList.size == 0) {
                    item.id?.let { it1 ->
                        selectedList.add(it1)
                        selectedDocList.add(it1)
                        multipleSelection = true
                        changeItemUI(it1)
                        listener2?.onLongClick()
                    }
                }
                true
            }
        }

        private fun changeItemUI(id: Int) {
            if (selectedList.contains(id)) {
                selection.isVisible = true
                mOption.isVisible = false
                mainBG.setBackgroundColor(context.resources.getColor(R.color.appPrimary6))
            } else {
                selection.isVisible = false
                mOption.isVisible = true
                mainBG.setBackgroundColor(context.resources.getColor(R.color.screen_background_alfa28))
            }
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