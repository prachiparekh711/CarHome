package ro.westaco.carhome.presentation.screens.settings.data.cars.leasingCompany

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.LeasingCompany
import ro.westaco.carhome.presentation.screens.settings.data.cars.add_new.AddNewCarViewModel

//C- Leasing Company data for CarDetails
class LeasingCompanyFragment : BottomSheetDialogFragment(),
    LeasingCompanyAdapter.OnItemInteractionListener {
    lateinit var viewModel: AddNewCarViewModel
    var listener: OnDialogInteractionListener? = null
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    interface OnDialogInteractionListener {
        fun onCompanyUpdated(company: LeasingCompany)
    }

    companion object {
        const val TAG = "OccupationDialog"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireParentFragment()).get(AddNewCarViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View =
            inflater.inflate(R.layout.leasing_company_layout, container, false)

        if (showsDialog) {
            dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        view.findViewById<View>(R.id.close).setOnClickListener {
            dismissAllowingStateLoss()
        }

        val categories = view.findViewById<RecyclerView>(R.id.categories)
        categories.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        val adapter = LeasingCompanyAdapter(
            requireContext(),
            viewModel.leasingCompaniesData,
            this
        )
        categories.adapter = adapter

        return view
    }

    override fun onChecked(item: LeasingCompany) {
        listener?.onCompanyUpdated(item)
        dismissAllowingStateLoss()
    }
}