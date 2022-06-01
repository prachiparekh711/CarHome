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
import ro.westaco.carhome.data.sources.remote.responses.models.Vehicle
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.service.insurance.adapter.DurationAdapter
import ro.westaco.carhome.utils.CatalogUtils
import ro.westaco.carhome.utils.DateTimeUtils
import ro.westaco.carhome.utils.DialogUtils.Companion.showErrorInfo
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
    var vehicleItem: Vehicle? = null

    override fun getContentView() = R.layout.fragment_insurance_step2

    companion object {
        const val ARG_REQUEST = "arg_request"
        const val ARG_CAR = "arg_car"
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
            vehicleItem = it.getSerializable(ARG_CAR) as? Vehicle?
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


        var pastDateStatus = false
        var currentDate = ""
        var dateStr = ""
        if (vehicleItem?.policyExpirationDate == null) {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val tomorrow = calendar.time
            val dateFormat: DateFormat = SimpleDateFormat("dd/MM/yyyy")
            dateStr = dateFormat.format(tomorrow)
            startDate.setText(dateStr)
        } else {
            val dateFormat: DateFormat =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            val date: Date? =
                dateFormat.parse(vehicleItem?.policyExpirationDate)
            val formatter: DateFormat =
                SimpleDateFormat("dd/MM/yyyy")
            dateStr =
                formatter.format(date)
            val c = Calendar.getInstance()
            c.time = formatter.parse(dateStr)

            val calendar = Calendar.getInstance()
            pastDateStatus = DateTimeUtils.isSameDay(calendar, c)
            if (pastDateStatus) {
                dateStr = formatter.format(c.time)
                startDate.setText(dateStr)
                calendar.add(Calendar.DATE, 1)
                currentDate = formatter.format(calendar.time)
                //warning here
                dateInfo.text = requireContext().resources.getString(R.string.ins_date_error)
                dateInfo.setTextColor(requireContext().resources.getColor(R.color.delete_dialog_color))
                startDate.setTextColor(requireContext().resources.getColor(R.color.delete_dialog_color))
                dateInfo.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                dateLabel.endIconDrawable =
                    requireContext().resources.getDrawable(R.drawable.error_icon)
                dateLabel.setEndIconTintList(
                    ContextCompat.getColorStateList(
                        requireContext(),
                        R.color.delete_dialog_color
                    )
                )
                dateInfo1.isVisible = true
            } else {
                c.add(Calendar.DATE, 1)
                dateStr = formatter.format(c.time)
                startDate.setText(dateStr)
                dateInfo1.isVisible = false
                dateInfo.text = requireContext().resources.getString(R.string.rca_date_info)
                dateInfo.setTextColor(requireContext().resources.getColor(R.color.appPrimary))
                dateInfo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_note, 0, 0, 0)
                dateLabel.endIconDrawable =
                    requireContext().resources.getDrawable(R.drawable.ic_calendar_visible)
                dateLabel.setEndIconTintList(
                    ContextCompat.getColorStateList(
                        requireContext(),
                        R.color.appPrimary
                    )
                )
            }
        }

        startDate.setOnClickListener {
            if (pastDateStatus) {
                viewModel.onDateClicked(it, dateToMilis(currentDate))
            } else {
                viewModel.onDateClicked(it, dateToMilis(dateStr))
            }
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
                showErrorInfo(requireContext(), getString(R.string.fill_all_fields))
            }

        }

        back.setOnClickListener {
            viewModel.onBack()
        }
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
            dateInfo1.isVisible = false
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
    private fun showDatePicker(view: View, dateInMillis: Long) {
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

        dpd?.datePicker?.minDate = mCalendar.timeInMillis
        mCalendar.add(Calendar.DATE, 30 + 1)
        dpd?.datePicker?.maxDate = mCalendar.timeInMillis

        dpd?.show()
    }
}