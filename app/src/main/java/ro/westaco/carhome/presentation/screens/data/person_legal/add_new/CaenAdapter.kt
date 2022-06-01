package ro.westaco.carhome.presentation.screens.data.person_legal.add_new

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.Caen

class CaenAdapter(
    val context: Context,
    private val repeatInterface1: TypeInterface,
    var typePos: Int
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
    var arrayList = ArrayList<Caen>()
    var filterData: ArrayList<Caen> = ArrayList()

    init {
        arrayList.clear()
        filterData.clear()
    }

    interface TypeInterface {
        fun onSelection(pos: Int, model: Caen)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView: View
        var viewHolder: RecyclerView.ViewHolder? = null
        itemView = LayoutInflater.from(context).inflate(R.layout.linear_item1, parent, false)
        viewHolder = MyClassView(itemView)

        return viewHolder
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holders: RecyclerView.ViewHolder, position: Int) {

        val holder = holders as MyClassView

        val item = filterData[position]
        holder.mTitle?.text = "${item.code} - ${item.name}"

        holder.itemView.setOnClickListener {
            typePos = position
            holder.mSelection?.isVisible = true
            repeatInterface1.onSelection(position, item)
            notifyDataSetChanged()
        }

        holder.mSelection?.isInvisible = typePos != position

        holder.mView?.isVisible = position != filterData.size - 1
    }

    override fun getItemCount(): Int {
        return filterData.size
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

    fun addAll(modelList: ArrayList<Caen>) {
        arrayList.addAll(modelList)
        filterData.addAll(modelList)
        notifyDataSetChanged()
    }


    private val filter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filteredList1: ArrayList<Caen> = ArrayList()
            if (constraint.toString().isEmpty()) {
                filteredList1.addAll(arrayList)
            } else {
                for (location in arrayList) {
                    if (location.name.lowercase()
                            .contains(constraint.toString().lowercase())
                    ) {
                        filteredList1.add(location)
                    }
                }
            }

            val filterResults = FilterResults()
            filterResults.values = filteredList1
            return filterResults
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            filterData = results.values as ArrayList<Caen>
            notifyDataSetChanged()
        }
    }

    override fun getFilter(): Filter {
        return filter
    }

}