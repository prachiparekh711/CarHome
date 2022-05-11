package ro.westaco.carhome.presentation.screens.service.vignette.buy

import android.annotation.SuppressLint
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


class BuyVignetteAdapter(
    context: Context,
    repeatPos: Int,
    listener: OnItemInteractionListener?,
    cars: ArrayList<ServiceCategory>
) :
    RecyclerView.Adapter<BuyVignetteAdapter.ViewHolder>() {
    var context: Context? = null
    var cars = ArrayList<ServiceCategory>()
    var interFace: OnItemInteractionListener? = null
    private var selectedPos = 0

    init {
        this.context = context
        this.interFace = listener
        this.selectedPos = repeatPos
        this.cars = cars
    }

    interface OnItemInteractionListener {
        fun onItemClick(position: Int, mText: TextView)
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int = cars.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.vignette_items, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var logo: ImageView = itemView.findViewById(R.id.mImage)
        private var mText: TextView = itemView.findViewById(R.id.mText)
        private var mLinear: LinearLayout = itemView.findViewById(R.id.mLinear)

        @SuppressLint("NewApi", "SimpleDateFormat", "NotifyDataSetChanged", "ResourceAsColor")
        fun bind(position: Int) {

            val item = cars[position]

            val decodedString: ByteArray = Base64.decode(item.logoBase64, Base64.DEFAULT)
            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

            val options = RequestOptions()
            logo.clipToOutline = true
            context?.let {
                Glide.with(it)
                    .load(decodedByte)
                    .apply(
                        options.fitCenter()
                            .skipMemoryCache(true)
                            .priority(Priority.HIGH)
                            .format(DecodeFormat.PREFER_ARGB_8888)
                    )
                    .into(logo)
            }

            mText.text = item.shortDescription
            if (selectedPos == position) {
                mLinear.setBackgroundResource(R.drawable.rounded_rect_100_vignette_full)
                mText.setTextColor(context?.resources!!.getColor(R.color.white))
            }

            itemView.setOnClickListener {

                mLinear.setBackgroundResource(R.drawable.rounded_rect_100_vignette_full)

                interFace?.onItemClick(position, mText)

            }

        }
    }

}