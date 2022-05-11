package ro.westaco.carhome.presentation.screens.home.purchases

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.Purchase
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class PurchasesAdapter(
    private val context: Context,
    private var purchases: ArrayList<Purchase>
) : RecyclerView.Adapter<PurchasesAdapter.ViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int = purchases.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_purchase, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var logo: ImageView = itemView.findViewById(R.id.logo)
        private var name: TextView = itemView.findViewById(R.id.name)
        private var expireStatus: TextView = itemView.findViewById(R.id.expireStatus)
        private var timeLeft: TextView = itemView.findViewById(R.id.timeLeft)
        private var separator: View = itemView.findViewById(R.id._separator)

        fun bind(position: Int) {
            val item = purchases[position]
            val ctx = itemView.context

            Picasso.with(ctx)
                .load(item.logoUrl)
                .placeholder(R.drawable.logo_small)
                .into(logo)

            name.text = item.name

            if (!item.expires.isNullOrEmpty()) {
                val originalFormat = SimpleDateFormat(
                    ctx.getString(R.string.server_standard_datetime_format_template),
                    Locale.US
                )
                val serverDate = originalFormat.parse(item.expires)

                val timeLeftMillis = serverDate.time - Date().time

                val timeLeftMillisPos = if (timeLeftMillis < 0) -timeLeftMillis else timeLeftMillis
                val daysLeft = TimeUnit.MILLISECONDS.toDays(timeLeftMillisPos)
                val hoursLeft = TimeUnit.MILLISECONDS.toHours(timeLeftMillisPos)
                val minutesLeft = TimeUnit.MILLISECONDS.toMinutes(timeLeftMillisPos)
                var formattedTimeLeft = when {
                    daysLeft > 0 -> ctx.getString(R.string.days_template, daysLeft)
                    hoursLeft > 0 -> ctx.getString(R.string.hours_template, hoursLeft)
                    minutesLeft > 0 -> ctx.getString(R.string.minutes_template, minutesLeft)
                    else -> ""
                }

                if (timeLeftMillis > 0) {
                    expireStatus.text = ctx.getString(
                        R.string.purchases_exp_template,
                        SimpleDateFormat(
                            ctx.getString(R.string.date_format_template), Locale.US
                        ).format(serverDate)
                    )
                    timeLeft.text =
                        ctx.getString(R.string.duetime_in_future, formattedTimeLeft)

                    expireStatus.setTextColor(
                        ContextCompat.getColor(
                            ctx,
                            R.color.secondaryTextOnWhite
                        )
                    )
                    if (daysLeft > 14) {
                        timeLeft.setTextColor(ContextCompat.getColor(ctx, R.color.greenActive))
                    } else {
                        timeLeft.setTextColor(ContextCompat.getColor(ctx, R.color.orangeWarning))
                    }

                } else {
                    expireStatus.text = ctx.getString(R.string.purchases_exp_inactive)
                    timeLeft.text =
                        ctx.getString(R.string.duetime_in_past, formattedTimeLeft)

                    expireStatus.setTextColor(ContextCompat.getColor(ctx, R.color.redExpired))
                    timeLeft.setTextColor(ContextCompat.getColor(ctx, R.color.redExpired))
                }
            }

            val isLastElem = position == purchases.size - 1
            separator.visibility = if (isLastElem) View.INVISIBLE else View.VISIBLE
        }
    }
}