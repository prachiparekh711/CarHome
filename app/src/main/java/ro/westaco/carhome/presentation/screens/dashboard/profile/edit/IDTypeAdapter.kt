package ro.westaco.carhome.presentation.screens.dashboard.profile.edit

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem


class IDTypeAdapter(
    context: Context,
    repeatInterface1: TypeInterface,
    typePos: Int
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var context: Context? = null
    var arrayList = ArrayList<CatalogItem>()
    var typeInterface: TypeInterface? = null
    var typePos: Int? = 0

    init {
        this.context = context
        this.typeInterface = repeatInterface1
        this.typePos = typePos
        arrayList.clear()
    }

    interface TypeInterface {
        fun onSelection(model: Int, id: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView: View
        var viewHolder: RecyclerView.ViewHolder? = null
        itemView = LayoutInflater.from(context).inflate(R.layout.linear_item, parent, false)
        viewHolder = MyClassView(itemView)

        return viewHolder
    }

    override fun onBindViewHolder(holders: RecyclerView.ViewHolder, position: Int) {

        val holder = holders as MyClassView
        holder.mTitle?.text = arrayList[position].toString()

        holder.mRelative?.setOnClickListener {
            typePos = position
            typeInterface?.onSelection(position, arrayList[position].id.toInt())
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
        var mRelative: RelativeLayout? = null

        init {
            mTitle = itemView.findViewById(R.id.mTitle)
            mSelection = itemView.findViewById(R.id.mSelection)
            mView = itemView.findViewById(R.id.mView)
            mRelative = itemView.findViewById(R.id.mRelative)
        }
    }

    fun addAll(modelList: ArrayList<CatalogItem>) {
        arrayList.addAll(modelList)
        notifyDataSetChanged()
    }


}