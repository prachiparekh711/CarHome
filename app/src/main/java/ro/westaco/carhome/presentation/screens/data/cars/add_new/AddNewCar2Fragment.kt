package ro.westaco.carhome.presentation.screens.data.cars.add_new

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_add_new_car2.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.requests.AddVehicleRequest
import ro.westaco.carhome.data.sources.remote.requests.VehicleEvent
import ro.westaco.carhome.dialog.DialogUtils.Companion.showErrorInfo
import ro.westaco.carhome.presentation.base.BaseFragment
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class AddNewCar2Fragment : BaseFragment<AddNewCarView2Model>() {

    private var isEdit = false
    private var vehicleItem: AddVehicleRequest? = null
    private var vehicleEventList = ArrayList<VehicleEvent>()
    var rcaEvent: VehicleEvent? = null
    var itpEvent: VehicleEvent? = null
    private var rovignetteEvent: VehicleEvent? = null
    private var maintenanceEvent: VehicleEvent? = null
    var oilEvent: VehicleEvent? = null
    var tyreEvent: VehicleEvent? = null

    companion object {
        const val ARG_IS_EDIT = "arg_is_edit"
        const val ARG_CAR = "arg_car"
    }

    override fun getContentView() = R.layout.fragment_add_new_car2

    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isEdit = it.getBoolean(ARG_IS_EDIT)
            vehicleItem = it.getSerializable(ARG_CAR) as? AddVehicleRequest?
        }
    }

    override fun initUi() {

        back.setOnClickListener {
            viewModel.onBack()
        }

        home.setOnClickListener {
            viewModel.onMain()
        }
        viewTV.setOnClickListener {
            if (moreInfoLL.isVisible) {
                moreInfoLL.isVisible = false
                viewTV.text = requireContext().resources.getString(R.string.view_more)
//                viewTV.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_view_arrow, 0)
            } else {
                moreInfoLL.isVisible = true
                viewTV.text = requireContext().resources.getString(R.string.view_less)
                /* viewTV.setCompoundDrawablesWithIntrinsicBounds(
                     0,
                     0,
                     R.drawable.ic_view_less_arrow,
                     0
                 )*/
            }
        }

        check1.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                cta.alpha =
                    1F
                cta.isClickable = true
            } else {
                cta.alpha =
                    0.5F
                cta.isClickable = false
            }
        }

        cta.setOnClickListener {

            val vehicleEvents: ArrayList<VehicleEvent?> = ArrayList()

            if (!rcaDate.text.isNullOrEmpty() && rca_switch_button.isChecked) {
                rcaEvent = VehicleEvent(
                    100060041, null, viewModel.convertToServerDate(rcaDate.text.toString()),
                    rca_switch_button.isChecked, null, null
                )
                vehicleEvents.add(rcaEvent)
            }

            if (!itpDate.text.isNullOrEmpty() && itp_switch_button.isChecked) {
                itpEvent = VehicleEvent(
                    100060042, null, viewModel.convertToServerDate(itpDate.text.toString()),
                    itp_switch_button.isChecked, null, null
                )
                vehicleEvents.add(itpEvent)
            }

            if (!nextMaintananceDate.text.isNullOrEmpty() && maintanance_switch_button.isChecked) {
                maintenanceEvent = VehicleEvent(
                    100060043,
                    viewModel.convertToServerDate(maintananceDate.text.toString()),
                    viewModel.convertToServerDate(nextMaintananceDate.text.toString()),
                    maintanance_switch_button.isChecked,
                    null,
                    null
                )
                vehicleEvents.add(maintenanceEvent)
            }

            if (!nextOilDate.text.isNullOrEmpty() && oil_switch_button.isChecked) {
                oilEvent = VehicleEvent(
                    100060044, viewModel.convertToServerDate(oilDate.text.toString()),
                    viewModel.convertToServerDate(nextOilDate.text.toString()),
                    oil_switch_button.isChecked, null, null
                )
                vehicleEvents.add(oilEvent)
            }

            if (!tyreDate.text.isNullOrEmpty() && tyre_switch_button.isChecked) {
                tyreEvent = VehicleEvent(
                    100060045, null, viewModel.convertToServerDate(tyreDate.text.toString()),
                    tyre_switch_button.isChecked, null, null
                )
                vehicleEvents.add(tyreEvent)
            }

            if (!roveinietaDate.text.isNullOrEmpty() && roveinieta_switch_button.isChecked) {
                rovignetteEvent = VehicleEvent(
                    100060046, null, viewModel.convertToServerDate(roveinietaDate.text.toString()),
                    roveinieta_switch_button.isChecked, null, null
                )
                vehicleEvents.add(rovignetteEvent)
            }
            if (!check1.isChecked) {
                showErrorInfo(requireContext(), getString(R.string.check_info))
                return@setOnClickListener
            }

            vehicleItem?.let { it1 ->

                viewModel.onCta(
                    isEdit,
                    it1,
                    vehicleEvents
                )
            }
        }

        if (vehicleItem?.vehicleEvents.isNullOrEmpty()) {
            vehicleEventList = ArrayList()
        } else {
            vehicleEventList = vehicleItem?.vehicleEvents as ArrayList<VehicleEvent>
            for (i in vehicleEventList.indices) {
                val vehicleEvent: VehicleEvent = vehicleEventList[i]
                when (vehicleEvent.eventType) {
                    (100060041).toLong() -> {
                        rcaEvent = vehicleEvent
                        rcaDate.text = viewModel.convertFromServerDate(rcaEvent?.nextDate)
                        rca_switch_button.isChecked = rcaEvent?.reminder == true
                    }
                    (100060042).toLong() -> {
                        itpEvent = vehicleEvent
                        itpDate.text = viewModel.convertFromServerDate(itpEvent?.nextDate)
                        itp_switch_button.isChecked = itpEvent?.reminder == true
                    }
                    (100060043).toLong() -> {
                        maintenanceEvent = vehicleEvent
                        maintananceDate.text =
                            viewModel.convertFromServerDate(maintenanceEvent?.lastDate)
                        nextMaintananceDate.text =
                            viewModel.convertFromServerDate(maintenanceEvent?.nextDate)
                        maintanance_switch_button.isChecked = maintenanceEvent?.reminder == true
                    }
                    (100060044).toLong() -> {
                        oilEvent = vehicleEvent
                        oilDate.text = viewModel.convertFromServerDate(oilEvent?.lastDate)
                        nextOilDate.text = viewModel.convertFromServerDate(oilEvent?.nextDate)
                        oil_switch_button.isChecked = oilEvent?.reminder == true
                    }
                    (100060045).toLong() -> {
                        tyreEvent = vehicleEvent
                        tyreDate.text = viewModel.convertFromServerDate(tyreEvent?.nextDate)
                        tyre_switch_button.isChecked = tyreEvent?.reminder == true
                    }
                    (100060046).toLong() -> {
                        rovignetteEvent = vehicleEvent
                        roveinietaDate.text =
                            viewModel.convertFromServerDate(rovignetteEvent?.nextDate)
                        roveinieta_switch_button.isChecked = rovignetteEvent?.reminder == true
                    }
                }
            }
        }

        rcaDate.setOnClickListener {
            viewModel.onDateClicked(it, rcaEvent?.nextDate?.let { it1 -> dateToMilis(it1) })
        }

        itpDate.setOnClickListener {
            viewModel.onDateClicked(it, itpEvent?.nextDate?.let { it1 -> dateToMilis(it1) })
        }

        roveinietaDate.setOnClickListener {
            viewModel.onDateClicked(it, rovignetteEvent?.nextDate?.let { it1 -> dateToMilis(it1) })
        }

        maintananceDate.setOnClickListener {
            viewModel.onDateClicked(it, maintenanceEvent?.lastDate?.let { it1 -> dateToMilis(it1) })
        }

        nextMaintananceDate.setOnClickListener {
            viewModel.onDateClicked(it, maintenanceEvent?.nextDate?.let { it1 -> dateToMilis(it1) })
        }

        oilDate.setOnClickListener {
            viewModel.onDateClicked(it, oilEvent?.lastDate?.let { it1 -> dateToMilis(it1) })
        }

        nextOilDate.setOnClickListener {
            viewModel.onDateClicked(it, oilEvent?.nextDate?.let { it1 -> dateToMilis(it1) })
        }

        tyreDate.setOnClickListener {
            viewModel.onDateClicked(it, tyreEvent?.nextDate?.let { it1 -> dateToMilis(it1) })
        }

    }

    override fun setObservers() {
        viewModel.dateLiveData.observe(viewLifecycleOwner) { datesMap ->
            datesMap?.forEach {
                (it.key as? TextView)?.text = SimpleDateFormat(
                    getString(R.string.date_format_template), Locale.getDefault()
                ).format(
                    Date(it.value)
                )
            }
        }

        viewModel.actionStream.observe(viewLifecycleOwner) {
            when (it) {
                is AddNewCarView2Model.ACTION.ShowDatePicker -> showDatePicker(
                    it.view,
                    it.dateInMillis
                )

                is AddNewCarView2Model.ACTION.ShowError -> showErrorInfo(requireContext(), it.error)
            }
        }


    }

    private var dpd: DatePickerDialog? = null
    private fun showDatePicker(view: View, dateInMillis: Long) {
        val c = Calendar.getInstance().apply {
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

        if (view == maintananceDate || view == oilDate) {
            dpd?.datePicker?.maxDate = System.currentTimeMillis()
        } else {
            dpd?.datePicker?.minDate = System.currentTimeMillis() + (1000 * 24 * 60 * 60)
        }
        dpd?.show()
    }

    private fun dateToMilis(str: String): Long {
        val sdf = SimpleDateFormat(getString(R.string.server_date_format_template))

        val mDate = sdf.parse(str)
        return mDate.time
    }
}