package ro.westaco.carhome.presentation.screens.settings.extras

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import ro.westaco.carhome.databinding.FaqItemBinding


class FAQAdapter(
    var context: Context,
    var quesList: List<String>,
    var ansList: List<String>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyTopHolder(
            FaqItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val holder1 = holder as MyTopHolder

        holder1.binding.mLabel.text = quesList[position]
        holder1.binding.mLabelAns.text = ansList[position]

        holder1.binding.mLabel.setOnClickListener {
            holder1.binding.mLabelAns.isVisible = !holder1.binding.mLabelAns.isVisible
        }
    }


    class MyTopHolder(itemView: FaqItemBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        var binding: FaqItemBinding = itemView

    }

    override fun getItemCount(): Int {
        return quesList.size
    }

}
