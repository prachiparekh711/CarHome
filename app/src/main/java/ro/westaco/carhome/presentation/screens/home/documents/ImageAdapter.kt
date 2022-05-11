package ro.westaco.carhome.presentation.screens.home.documents

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
import com.bumptech.glide.request.RequestOptions
import ro.westaco.carhome.R
import java.io.File

class ImageAdapter(
    private val context: Context,
    private var imgList: ArrayList<File>,
    private val listener: OnItemInteractionListener? = null
) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    interface OnItemInteractionListener {
        fun onItemClick(position: Int)
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int = imgList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var mImage: ImageView = itemView.findViewById(R.id.mImage)
        private var number: TextView = itemView.findViewById(R.id.number)
        private var mRemove: ImageView = itemView.findViewById(R.id.mRemove)

        @SuppressLint("NewApi")
        fun bind(position: Int) {
            val item = imgList[position]

            val options = RequestOptions()
            mImage.clipToOutline = true
            Glide.with(context)
                .load(item)
                .apply(
                    options.centerCrop()
                        .skipMemoryCache(true)
                        .priority(Priority.HIGH)
                        .format(DecodeFormat.PREFER_ARGB_8888)
                )
                .error(R.drawable.logo_small)
                .into(mImage)

            number.text = (position.plus(1)).toString()

            mRemove.setOnClickListener {
                listener?.onItemClick(position)
                imgList.removeAt(position)
                notifyItemRemoved(position)
                notifyDataSetChanged()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(cars: List<File>?) {
        this.imgList = ArrayList(cars ?: listOf())
        notifyDataSetChanged()
    }

    fun clearAll() {
        this.imgList.clear()
        notifyDataSetChanged()
    }
}