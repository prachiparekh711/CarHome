package ro.westaco.carhome.presentation.screens.settings.extras

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.TermsResponseItem

class ConsentsAdapter(
    val context: Context,
    val listener: ConsentListener?
) :
    RecyclerView.Adapter<ConsentsAdapter.ViewHolder>() {

    var list = ArrayList<TermsResponseItem>()

    interface ConsentListener {
        fun onConsentClick(item: TermsResponseItem)
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int = list.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.consent_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var title: TextView = itemView.findViewById(R.id.title)
        private var subTitle: TextView = itemView.findViewById(R.id.subTitle)

        fun bind(position: Int) {

            val item = list[position]

            title.text = item.title
            subTitle.text = item.preferredCaption

            itemView.setOnClickListener {
                listener?.onConsentClick(item)
            }
        }
    }

    fun setItems(itemList: List<TermsResponseItem>) {
        this.list.addAll(itemList)
        notifyDataSetChanged()
    }

}