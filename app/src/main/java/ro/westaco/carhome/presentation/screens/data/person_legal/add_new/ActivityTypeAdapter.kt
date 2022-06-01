package ro.westaco.carhome.presentation.screens.data.person_legal.add_new

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem

class ActivityTypeAdapter(
    val context: Context,
    private val repeatInterface1: TypeInterface,
    var typePos: Int
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var arrayList = ArrayList<CatalogItem>()

    init {
        arrayList.clear()
    }

    interface TypeInterface {
        fun onSelection(pos: Int, model: CatalogItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var viewHolder: RecyclerView.ViewHolder? = null
        val itemView: View =
            LayoutInflater.from(context).inflate(R.layout.linear_item1, parent, false)
        viewHolder = MyClassView(itemView)

        return viewHolder
    }

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holders: RecyclerView.ViewHolder, position: Int) {

        val holder = holders as MyClassView

        val item = arrayList[position]
        holder.mTitle?.text = item.name

        holder.itemView.setOnClickListener {
            typePos = position
            holder.mSelection?.isVisible = true
            repeatInterface1.onSelection(position, item)
            notifyDataSetChanged()
        }

        holder.mSelection?.isInvisible = typePos != position

        holder.mView?.isVisible = position != arrayList.size - 1
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    class MyClassView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mTitle: TextView? = null
        var mSelection: ImageView? = null
        var mView: View? = null

        init {
            mTitle = itemView.findViewById(R.id.mTitle)
            mSelection = itemView.findViewById(R.id.mSelection)
            mView = itemView.findViewById(R.id.mView)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addAll(modelList: ArrayList<CatalogItem>) {
        arrayList.addAll(modelList)
        notifyDataSetChanged()
    }

}