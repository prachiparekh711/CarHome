package ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.ServiceCategory

class CategoryAdapter(
    val context: Context,
    var repeatPos: Int,
    val listener: OnItemInteractionListener?,
    val catList: ArrayList<ServiceCategory>
) :
    RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    interface OnItemInteractionListener {
        fun onCategoryClick(position: Int)
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int = catList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.service_vehicle_category_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var mText: TextView = itemView.findViewById(R.id.mText)
        private var mItemsName: TextView = itemView.findViewById(R.id.mItemsName)
        private var mImage: ImageView = itemView.findViewById(R.id.mImage)
        private var mLinear: LinearLayout = itemView.findViewById(R.id.mLinear)

        fun bind(position: Int) {

            val item = catList[position]
            mText.text = item.description
            mItemsName.text = item.shortDescription

            val decodedString: ByteArray = Base64.decode(item.logoBase64, Base64.DEFAULT)
            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            val options = RequestOptions()

            mImage.clipToOutline = true
            context.let {
                Glide.with(it)
                    .load(decodedByte)
                    .apply(
                        options.fitCenter()
                            .skipMemoryCache(true)
                            .priority(Priority.HIGH)
                            .format(DecodeFormat.PREFER_ARGB_8888)
                    )
                    .into(mImage)
            }

            if (!itemView.isSelected) {
                mLinear.setBackgroundResource(R.drawable.rounded_rect_100_bridge_back_noview)
            }

            if (repeatPos == position) {
                mLinear.setBackgroundResource(R.drawable.rounded_rect_100_bridge_back)
            }

            itemView.setOnClickListener {
                val prevSelectedPos = repeatPos
                repeatPos = position
                mLinear.setBackgroundResource(R.drawable.rounded_rect_100_bridge_back)
                notifyItemChanged(prevSelectedPos)
                listener?.onCategoryClick(position)
            }
        }
    }

}