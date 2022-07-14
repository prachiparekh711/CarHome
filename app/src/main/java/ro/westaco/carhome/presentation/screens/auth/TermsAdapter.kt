package ro.westaco.carhome.presentation.screens.auth

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.TermsResponseItem


class TermsAdapter(
    private val context: Context,
    private var termsList: ArrayList<TermsResponseItem>,
    private val listener: OnTermsClickListner
) : RecyclerView.Adapter<TermsAdapter.ViewHolder>() {

    interface OnTermsClickListner {
        fun onTermsClick(item: TermsResponseItem)
        fun onChecked()
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int = termsList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.terms_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(termsList[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var checkBox: ImageView = itemView.findViewById(R.id.checkBox)
        private var termsDescription: TextView = itemView.findViewById(R.id.termsDescription)

        fun bind(item: TermsResponseItem) {
            termsDescription.text = item.preferredCaption
            item.title?.let { setHighLightedText(termsDescription, it, item) }

            termsDescription.setOnClickListener {
                listener.onTermsClick(item)
            }

            if (item.allowed) {
                checkBox.setImageDrawable(context.resources.getDrawable(R.drawable.checkbox_background))
            } else {
                checkBox.setImageDrawable(context.resources.getDrawable(R.drawable.uncheckbox_background))
            }

            checkBox.setOnClickListener {
                item.allowed = !item.allowed
                listener.onChecked()
                notifyDataSetChanged()
            }
        }
    }


    fun setHighLightedText(tv: TextView, textToHighlight: String, item: TermsResponseItem) {
        val tvt = tv.text.toString()
        var ofe = tvt.indexOf(textToHighlight, 0)
        val wordToSpan: Spannable = SpannableString(tv.text)
        var ofs = 0
        while (ofs < tvt.length && ofe != -1) {
            ofe = tvt.indexOf(textToHighlight, ofs)
            if (ofe == -1) break else {
                wordToSpan.setSpan(
                    StyleSpan(Typeface.BOLD),
                    ofe,
                    ofe + textToHighlight.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                wordToSpan.setSpan(
                    object : ClickableSpan() {
                        override fun onClick(textView: View) {

                        }
                    },
                    ofe,
                    ofe + textToHighlight.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                tv.setText(wordToSpan, TextView.BufferType.SPANNABLE)
            }
            ofs = ofe + 1
        }
    }

    fun getItems() = termsList

}