package ro.westaco.carhome.presentation.screens.dashboard.profile.occupation

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem

//C- Occupation Catalog
class OccupationDialogFragment : BottomSheetDialogFragment(),
    OccupationAdapter.OnItemInteractionListener {
    private var catalogList: ArrayList<CatalogItem>? = ArrayList()
    var listener: OnDialogInteractionListener? = null
    var selectedOccupation: CatalogItem? = null

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    interface OnDialogInteractionListener {
        fun onOccupationUpdated(occupation: CatalogItem)
    }

    companion object {
        const val TAG = "OccupationDialog"
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View =
            inflater.inflate(R.layout.occupation_layout, container, false)

        if (showsDialog) {
            dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        view.findViewById<View>(R.id.close).setOnClickListener {
            dismissAllowingStateLoss()
        }

        view.findViewById<View>(R.id.cta).setOnClickListener {
            dismissAllowingStateLoss()
        }

        val categories = view.findViewById<RecyclerView>(R.id.categories)
        categories.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        val adapter = catalogList?.let {
            OccupationAdapter(
                requireContext(),
                it,
                this,
                selectedOccupation
            )
        }
        categories.adapter = adapter

        return view
    }

    override fun onChecked(item: CatalogItem) {
        listener?.onOccupationUpdated(item)
    }
}