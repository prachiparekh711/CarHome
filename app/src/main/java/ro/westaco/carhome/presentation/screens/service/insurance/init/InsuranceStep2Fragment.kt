package ro.westaco.carhome.presentation.screens.service.insurance.init

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_insurance_step2.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.requests.RcaOfferRequest
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem
import ro.westaco.carhome.data.sources.remote.responses.models.RcaDurationItem
import ro.westaco.carhome.dialog.DialogUtils.Companion.showErrorInfo
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.service.insurance.adapter.DurationAdapter
import ro.westaco.carhome.utils.CatalogUtils
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class InsuranceStep2Fragment : BaseFragment<InsuranceStep2ViewModel>(),
    DurationAdapter.OnItemInteractionListener {

    var dialogDuration: BottomSheetDialog? = null
    var durationAdapter: DurationAdapter? = null
    var durationRV: RecyclerView? = null
    var close: ImageView? = null
    var dismiss: TextView? = null
    var ctaDuration: TextView? = null
    var durationPosition: Int = 0
    var durationList: ArrayList<RcaDurationItem> = ArrayList()
    var usageDatatype: ArrayList<CatalogItem> = ArrayList()
    var durationItem: RcaDurationItem? = null
    var rcaOfferRequest: RcaOfferRequest? = null
    var policyExpirationDate: String? = null
    lateinit var xStr: String   //    X   =   Start date
    lateinit var yStr: String   //    Y   =   End date
    lateinit var x: Date   //    X   =   Start date
    lateinit var y: Date   //    Y   =   End date
    var isValidrange = false

    override fun getContentView() = R.layout.fragment_insurance_step2

    companion object {
        const val ARG_REQUEST = "arg_request"
        const val ARG_EXPIRE_STR = "arg_expire_str"
    }

    @SuppressLint("SimpleDateFormat")
    override fun initUi() {

        cancel.setOnClickListener {
            viewModel.onBack()
        }

        knowMore.setOnClickListener {
            viewModel.onDirectClaim()
        }
    }

    override fun onResume() {
        super.onResume()
        arguments?.let {
            rcaOfferRequest = it.getSerializable(ARG_REQUEST) as? RcaOfferRequest?
            policyExpirationDate = it.getString(ARG_EXPIRE_STR)
        }

        val view = layoutInflater.inflate(R.layout.rca_duration_layout, null)
        dialogDuration = BottomSheetDialog(requireContext())
        dialogDuration?.setCancelable(true)
        dialogDuration?.setContentView(view)
        durationRV = view.findViewById(R.id.durationRV)
        close = view.findViewById(R.id.close)
        dismiss = view.findViewById(R.id.dismiss)
        ctaDuration = view.findViewById(R.id.cta)
        durationRV?.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        durationAdapter = DurationAdapter(requireContext(), durationPosition, this)
        durationRV?.adapter = durationAdapter
        close?.setOnClickListener {
            dialogDuration?.dismiss()
        }

        dismiss?.setOnClickListener {
            dialogDuration?.dismiss()
        }

        ctaDuration?.setOnClickListener {
            durationItem = durationList[durationPosition]
            dialogDuration?.dismiss()

            radioGroup.removeAllViews()
            val rdbtn = RadioButton(requireContext())
            rdbtn.id = durationList[durationPosition].id
            rdbtn.text = durationList[durationPosition].name
            rdbtn.setPadding(20, 0, 0, 0)
            rdbtn.buttonTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.ctaBackground
                )
            )
            rdbtn.isChecked = true
            rdbtn.setTextColor(requireContext().resources.getColor(R.color.items_color))
            radioGroup.addView(rdbtn)
        }


        isValidrange = if (policyExpirationDate == null) {
            dateDefaultCase(isActive = false, policyDate = null)
        } else {
            val dateFormat: DateFormat =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            val policyDate = dateFormat.parse(policyExpirationDate)

            if (System.currentTimeMillis() > policyDate.time) {
                //               Expires
                dateDefaultCase(isActive = false, policyDate = null)
            } else {
                //               Active
                dateDefaultCase(isActive = true, policyDate = policyDate)
            }
        }

        checkData()

        if (isValidrange) {
            dateError.isVisible = false
            dateInfo.isVisible = true
            startDate.setTextColor(requireContext().resources.getColor(R.color.appPrimary))
            startDate.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_calendar_visible,
                0
            )

        } else {
            // ( If active Insurance found in CarHome ) //warning here

            startDate.setTextColor(requireContext().resources.getColor(R.color.delete_dialog_color))
            startDate.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.error_icon,
                0
            )
            dateError.isVisible = true
            dateInfo.isVisible = false
        }

        startDate.setOnClickListener {
            if (isValidrange)
                viewModel.onDateClicked(it, dateToMilis(startDate.text.toString()))
            else
                showErrorInfo(
                    requireContext(),
                    requireContext().resources.getString(R.string.date_exceed_limit)
                )
        }

        check.setOnCheckedChangeListener { compoundButton, b ->
            checkData()
        }

        mContinue.setOnClickListener {

            if (check.isChecked) {

                if (startDate.text.toString().isNotEmpty()) {

                    rcaOfferRequest?.beginDate =
                        viewModel.convertToServerDate(startDate.text.toString())
                    rcaOfferRequest?.rcaDurationId = durationItem?.id
                    rcaOfferRequest?.vehicle?.vehicleUsageType =
                        usageDatatype[purposeCategory.selectedItemPosition].id.toInt()

                    rcaOfferRequest?.let { it1 -> viewModel.onCta(it1) }

                } else {
                    showErrorInfo(requireContext(), getString(R.string.start_date_info))
                }
            } else {
                showErrorInfo(requireContext(), getString(R.string.check_info))
            }

        }

        back.setOnClickListener {
            viewModel.onBack()
        }
    }

    private fun checkData() {
        if (isValidrange && check.isChecked) {
            mContinue.alpha = 1F
            mContinue.isEnabled = true
        } else {
            mContinue.alpha = 0.4F
            mContinue.isEnabled = false
        }
    }

    private fun dateDefaultCase(isActive: Boolean, policyDate: Date?): Boolean {
        var isValidRange = false
        val calendar1 = Calendar.getInstance()
        calendar1.add(Calendar.DATE, 1)
        x = calendar1.time
        val calendar2 = Calendar.getInstance()
        calendar2.add(Calendar.DATE, 30)
        y = calendar2.time

        val dateFormat: DateFormat = SimpleDateFormat("dd/MM/yyyy")
        xStr = dateFormat.format(x)
        yStr = dateFormat.format(y)
        // ( when No Active Insurance / Insurance Expired)

        if (isActive && policyDate != null) {
            // ( when Insurance Active)
            if (policyDate.after(x) && policyDate.before(y)) {
                xStr = dateFormat.format(policyDate)
                val c = Calendar.getInstance()
                c.time = dateFormat.parse(xStr)
                c.add(Calendar.DATE, 1)
                x = c.time
                isValidRange = true
            } else {
                xStr = dateFormat.format(policyDate)
                isValidRange = false
                // Condition Not satisfy So user not able to purchase
            }
        } else {
            isValidRange = true
        }

        startDate.setText(xStr)
        return isValidRange
    }


    @SuppressLint("SimpleDateFormat")
    fun dateToMilis(str: String): Long {
        val sdf = SimpleDateFormat(getString(R.string.date_format_template))
        val mDate = sdf.parse(str)
        return mDate.time
    }

    override fun setObservers() {

        viewModel.actionStream.observe(viewLifecycleOwner) {
            when (it) {
                is InsuranceStep2ViewModel.ACTION.ShowDatePicker -> showDatePicker(
                    it.view,
                    it.dateInMillis
                )
            }
        }

        viewModel.startDateLiveData.observe(viewLifecycleOwner) { datesMap ->
            datesMap?.forEach {
                (it.key as? TextView)?.text = SimpleDateFormat(
                    getString(R.string.date_format_template), Locale.getDefault()
                ).format(
                    Date(it.value)
                )
            }
            dateError.isVisible = false
        }

        viewModel.durationData.observe(viewLifecycleOwner) { durationList ->
            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                this.durationList = durationList
                if (durationList.isNotEmpty()) {
                    durationAdapter?.setItems(durationList)

                    for (i in durationList.indices) {
                        val rdbtn = RadioButton(requireContext())
                        if (durationList[i].unitCount == 12 || durationList[i].unitCount == 6) {
                            rdbtn.id = durationList[i].id
                            rdbtn.text = durationList[i].name
                            rdbtn.setPadding(20, 0, 0, 0)
                            rdbtn.buttonTintList = ColorStateList.valueOf(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.ctaBackground
                                )
                            )
                            if (durationList[i].unitCount == 12) {
                                rdbtn.isChecked = true
                                durationItem = durationList[i]
                                durationPosition = i
                            }
                            rdbtn.setTextColor(requireContext().resources.getColor(R.color.items_color))
                            radioGroup.addView(rdbtn)

                            rdbtn.setOnCheckedChangeListener { p0, p1 ->
                                if (p1) {
                                    durationItem = durationList[i]
                                    durationPosition = i
                                }
                            }
                        }
                    }

                    moreOption.setOnClickListener {
                        dialogDuration?.show()
                    }
                }
            }
        }

        viewModel.usageData.observe(viewLifecycleOwner) { usageList ->
            this.usageDatatype = usageList
            ArrayAdapter(requireContext(), R.layout.spinner_item, usageList).also { adapter ->
                purposeCategory.adapter = adapter
            }

            rcaOfferRequest?.vehicle?.vehicleUsageType?.toLong()?.let {
                CatalogUtils.findPosById(
                    usageList,
                    it
                )
            }?.let {
                purposeCategory.setSelection(
                    it
                )
            }
        }
    }

    override fun onItemClick(item: RcaDurationItem, position: Int) {
        durationPosition = position
    }

    private var dpd: DatePickerDialog? = null
    fun showDatePicker(view: View, dateInMillis: Long) {
        val mCalendar = Calendar.getInstance()
        val c = mCalendar.apply {
            timeInMillis = dateInMillis
        }

        dpd?.cancel()
        dpd = DatePickerDialog(
            requireContext(), R.style.DialogTheme, { _, year, monthOfYear, dayOfMonth ->
                viewModel.onDatePicked(
                    view,
                    Calendar.getInstance().apply {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, monthOfYear)
                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    }.timeInMillis
                )
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
        )

        val xMillis = dateToMilis(xStr) //    X   =   Start date
        val yMillis = dateToMilis(yStr) //    Y   =   End date

        dpd?.datePicker?.minDate = xMillis
        dpd?.datePicker?.maxDate = yMillis

        dpd?.show()
    }
}