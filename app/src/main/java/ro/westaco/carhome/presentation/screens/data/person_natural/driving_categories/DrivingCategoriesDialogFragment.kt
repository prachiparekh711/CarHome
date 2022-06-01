package ro.westaco.carhome.presentation.screens.data.person_natural.driving_categories

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem

//C- Redesign
class DrivingCategoriesDialogFragment : BottomSheetDialogFragment(),
    DrivingCategoriesAdapter.OnItemInteractionListener {

    companion object {
        const val TAG = "DrivingCategoriesDialog"
        const val COLUMNS = 4
    }

    interface OnDialogInteractionListener {
        fun onDrivingCategoriesUpdated(drivingCategories: ArrayList<CatalogItem>)
    }

    var listener: OnDialogInteractionListener? = null
    var catalogList: ArrayList<CatalogItem>? = ArrayList()
    var selectedList: ArrayList<Int>? = ArrayList()

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View =
            inflater.inflate(R.layout.dialog_driving_license_categories, container, false)

        if (showsDialog) {
            dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        view.findViewById<View>(R.id.close).setOnClickListener {
            dismissAllowingStateLoss()
        }

        view.findViewById<View>(R.id.dissmiss).setOnClickListener {
            dismissAllowingStateLoss()
        }

        view.findViewById<View>(R.id.cta).setOnClickListener {
            dismissAllowingStateLoss()
        }

        val categories = view.findViewById<RecyclerView>(R.id.categories)
        categories.layoutManager = GridLayoutManager(context, COLUMNS)
        val adapter = catalogList?.let {
            DrivingCategoriesAdapter(
                requireContext(),
                it,
                selectedList,
                this
            )
        }
        categories.adapter = adapter

        return view
    }

    private val drivingCategories = arrayListOf<CatalogItem>()
    override fun onChecked(item: CatalogItem, isChecked: Boolean) {
        if (isChecked) {
            drivingCategories.add(item)
        } else {
            drivingCategories.remove(item)
        }
        listener?.onDrivingCategoriesUpdated(drivingCategories)
    }
}