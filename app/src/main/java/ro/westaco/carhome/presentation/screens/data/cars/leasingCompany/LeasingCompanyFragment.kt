package ro.westaco.carhome.presentation.screens.data.cars.leasingCompany

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.LeasingCompany

//C- Leasing Company data for CarDetails
class LeasingCompanyFragment : BottomSheetDialogFragment(),
    LeasingCompanyAdapter.OnItemInteractionListener {
    var listener: OnDialogInteractionListener? = null
    var leasingList: ArrayList<LeasingCompany> = ArrayList()
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
            leasingList,
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