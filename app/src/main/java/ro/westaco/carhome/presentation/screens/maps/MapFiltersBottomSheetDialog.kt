package ro.westaco.carhome.presentation.screens.maps

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ro.westaco.carhome.data.sources.remote.responses.models.SectionModel
import ro.westaco.carhome.databinding.BottomSheetMapFiltersBinding
import ro.westaco.carhome.utils.observeOnce

class MapFiltersBottomSheetDialog(var viewModel: LocationViewModel) : BottomSheetDialogFragment() {
    private var bottomSheetMapFiltersBinding: BottomSheetMapFiltersBinding? = null
    private lateinit var categoriesFilterAdapter: CategoriesFilterAdapter
    private var selectedItems: ArrayList<SectionModel> = ArrayList()
    private var intermediateSelectedItems: ArrayList<SectionModel> = ArrayList()
    private var allFilterList: ArrayList<SectionModel> = ArrayList()

    init {
        val observer = Observer<List<SectionModel>> {
            it.forEach { section ->
                selectedItems.add(SectionModel(section.category, ArrayList()))
            }
        }
        viewModel.filterDataMaps.observeOnce(this, observer)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bottomSheetMapFiltersBinding =
            BottomSheetMapFiltersBinding.inflate(inflater, container, false)
        setUI()
        setObservers()
        return bottomSheetMapFiltersBinding?.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setOnShowListener {

            val bottomSheetDialog = it as BottomSheetDialog
            val parentLayout =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let { it1 ->
                val behaviour = BottomSheetBehavior.from(it1)
                setupFullHeight(it1)
                behaviour.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        return dialog
    }

    private fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        bottomSheet.layoutParams = layoutParams
    }

    private fun setUI() {
        categoriesFilterAdapter = CategoriesFilterAdapter(
            requireContext(),
            viewLifecycleOwner,
            allFilterList,
            selectedItems
        )
        bottomSheetMapFiltersBinding!!.filtersRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        bottomSheetMapFiltersBinding!!.filtersRecyclerView.adapter = categoriesFilterAdapter
        bottomSheetMapFiltersBinding!!.closeImageView.setOnClickListener {
            this.dismiss()
        }
        bottomSheetMapFiltersBinding!!.cancelButton.setOnClickListener {
            this.dismiss()
        }
        bottomSheetMapFiltersBinding!!.applyButton.setOnClickListener {
            this.dismiss()
            selectedItems.clear()
            selectedItems.addAll(intermediateSelectedItems)
            viewModel.selectedItemsLiveData.value = selectedItems
        }
    }

    private fun setObservers() {
        viewModel.filterDataMaps.observe(viewLifecycleOwner) {
            if (selectedItems.size == 0) {
                categoriesFilterAdapter.initIntermediateSelectedItems(it as ArrayList<SectionModel>)
            } else {
                categoriesFilterAdapter.initIntermediateWithSelectedItems()
            }
            categoriesFilterAdapter.dataSource = it as ArrayList<SectionModel>
            categoriesFilterAdapter.notifyDataSetChanged()
        }
        categoriesFilterAdapter.getIntermediateSelectedItems()
            .observe(viewLifecycleOwner) { selectedFilters ->
                intermediateSelectedItems.clear()
                intermediateSelectedItems.addAll(selectedFilters)
            }
//        mapFilterBottomSheetAdapter.getSelectedItems().observe(viewLifecycleOwner) { selectedFilters ->
//            intermediateSelectedItems= selectedFilters
//        }
    }

}

class TouchEventInterceptorLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val handled = super.dispatchTouchEvent(ev)
        requestDisallowInterceptTouchEvent(true)
        return handled
    }
}