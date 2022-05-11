package ro.westaco.carhome.presentation.screens.onboarding

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ro.westaco.carhome.R

class OnboardingAdapter(
    private var items: List<OnboardingItem> = arrayListOf()

) : RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = OnboardingViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_onboarding_pager, parent, false)
    )

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        holder.title.text = context.getString(item.titleResId)
        holder.image.setImageDrawable(ContextCompat.getDrawable(context, item.imageResId))
    }

    override fun getItemCount() = items.size

    class OnboardingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val image: ImageView = view.findViewById(R.id.img)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(items: List<OnboardingItem>) {
        this.items = items
        notifyDataSetChanged()
    }
}