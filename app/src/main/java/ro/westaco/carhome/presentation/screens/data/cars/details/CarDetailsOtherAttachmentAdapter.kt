package ro.westaco.carhome.presentation.screens.data.cars.details

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.Attachments

class CarDetailsOtherAttachmentAdapter(
    private val context: Context,
    var attach: ArrayList<Attachments>
) : RecyclerView.Adapter<CarDetailsOtherAttachmentAdapter.ViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int = attach.size

    var onItemClick: ((Attachments, String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_car_details_other_attachment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var lblOtherCertificate: TextView = itemView.findViewById(R.id.lblOtherCertificate)
        private var btnDeleteOtherCertificate: ImageView =
            itemView.findViewById(R.id.btnDeleteOtherCertificate)


        fun bind(pos: Int) {
            val item = attach[pos]
            lblOtherCertificate.text = item.name

            lblOtherCertificate.setOnClickListener {
                onItemClick?.invoke(attach[pos], "VIEW")
            }

            btnDeleteOtherCertificate.setOnClickListener {
                onItemClick?.invoke(attach[pos], "DELETE")
            }
        }
    }

}