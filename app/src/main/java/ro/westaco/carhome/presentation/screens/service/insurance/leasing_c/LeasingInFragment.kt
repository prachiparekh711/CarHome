package ro.westaco.carhome.presentation.screens.service.insurance.leasing_c

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.LeasingCompany
import ro.westaco.carhome.presentation.screens.service.insurance.InsuranceViewModel

//C- Leasing Company data for CarDetails
class LeasingInFragment(val leasingCompanyList: ArrayList<LeasingCompany>) :
    BottomSheetDialogFragment(),
    LeasingInAdapter.OnItemInteractionListener {
    lateinit var viewModel: InsuranceViewModel
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
        const val TAG = "Occupation"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireParentFragment()).get(InsuranceViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view: View = inflater.inflate(R.layout.leasing_company_ins_lay, container, false)

        if (showsDialog) {
            dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        view.findViewById<View>(R.id.close).setOnClickListener {
            dismissAllowingStateLoss()
        }

        val etSearchTrain: EditText? = view.findViewById(R.id.etSearchTrain)

        val categories = view.findViewById<RecyclerView>(R.id.categories)

        categories.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        val adapter = LeasingInAdapter(
            requireContext(),
            leasingCompanyList, null, this, enableSelection = true
        )
        categories.adapter = adapter

        etSearchTrain?.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (s.toString().isNotEmpty()) {

                    adapter.filter.filter(s.toString())

                } else {


                }

            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        return view
    }

    override fun onChecked(item: LeasingCompany) {

        listener?.onCompanyUpdated(item)
        dismissAllowingStateLoss()
    }


}