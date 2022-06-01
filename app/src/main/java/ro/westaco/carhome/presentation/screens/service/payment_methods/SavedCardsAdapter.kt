package ro.westaco.carhome.presentation.screens.service.payment_methods

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
import ro.westaco.carhome.data.sources.local.dummy.Card

class SavedCardsAdapter(
    private val context: Context,
    private var cards: ArrayList<Card>
) : RecyclerView.Adapter<SavedCardsAdapter.ViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int = cards.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(cards[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var logo: ImageView = itemView.findViewById(R.id.logo)
        private var cardNumber: TextView = itemView.findViewById(R.id.cardNumber)
        private var holder: TextView = itemView.findViewById(R.id.holder)

        fun bind(item: Card) {

            cardNumber.text = item.numberHidden
            holder.text = item.holder

            val options = RequestOptions()
            logo.clipToOutline = true
            Glide.with(context)
                .load(item.logoUrl)
                .apply(
                    options.fitCenter()
                        .skipMemoryCache(true)
                        .priority(Priority.HIGH)
                        .format(DecodeFormat.PREFER_ARGB_8888)
                )
                .into(logo)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(cards: List<Card>?) {
        this.cards = ArrayList(cards ?: listOf())
        notifyDataSetChanged()
    }
}