package ro.westaco.carhome.presentation.screens.service.insurance.car_selection

import android.os.Parcelable
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_ins_car_edit.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.requests.VehicleEvent
import ro.westaco.carhome.data.sources.remote.responses.models.*
import ro.westaco.carhome.dialog.DialogUtils
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.data.cars.leasingCompany.LeasingCompanyFragment
import ro.westaco.carhome.utils.CatalogUtils
import ro.westaco.carhome.utils.LeasingCompanyUtils
import ro.westaco.carhome.utils.RegexData
import ro.westaco.carhome.views.SwitchButton
import java.util.*


@AndroidEntryPoint
class InsCarEditFragment : BaseFragment<InsCarEditViewModel>(),
    LeasingCompanyFragment.OnDialogInteractionListener {
    override fun getContentView() = R.layout.fragment_ins_car_edit

    private var isEdit = false
    private var vehicleDetails: VehicleDetails? = null
    private var leasingCompanyItem: LeasingCompany? = null
    var countryList: ArrayList<Country> = ArrayList()
    var vehicleCategoryList: ArrayList<CatalogItem> = ArrayList()
    var vehicleBrandList: ArrayList<CatalogItem> = ArrayList()
    var fuelTypeList: ArrayList<CatalogItem> = ArrayList()
    var warningsItemList: ArrayList<WarningsItem?>? = null
    private var vin_number = ""
    private var regi_number = ""
    var leasingList: ArrayList<LeasingCompany> = ArrayList()

    companion object {
        const val ARG_CAR_ID = "arg_car_id"
        const val ARG_CAR_WARNING = "arg_car_warning"
        const val ARG_IS_EDIT = "arg_is_edit"
        const val ARG_VIN = "arg_vin"
        const val ARG_REG_NUMBER = "registrationNumber"
    }

    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)

    override fun initUi() {

        arguments?.let {
            isEdit = it.getBoolean(ARG_IS_EDIT)
            warningsItemList =
                it.getParcelableArrayList<Parcelable>(ARG_CAR_WARNING) as ArrayList<WarningsItem?>
            if (isEdit) {
                val vehicleId = it.getInt(ARG_CAR_ID)
                viewModel.fetchVehicleDetail(vehicleId)
            } else {
                vin_number = it.getString(ARG_VIN).toString()
                regi_number = it.getString(ARG_REG_NUMBER).toString()
                licensePlate.setText(regi_number)
                vin.setText(vin_number)
                viewModel.fetchDefaultData()
            }
        }

        back.setOnClickListener {
            viewModel.onBack()
        }

        home.setOnClickListener {
            viewModel.onMain()
        }

        year.setOnClickListener {
            val inflater = this.layoutInflater
            val dialogView: View = inflater.inflate(R.layout.number_picker_dialog, null)
            val numberPicker =
                dialogView.findViewById<View>(R.id.dialog_number_picker) as NumberPicker
            val calendar: Calendar = Calendar.getInstance()
            numberPicker.maxValue = calendar.get(Calendar.YEAR)
            numberPicker.minValue = 1950
            numberPicker.wrapSelectorWheel = false
            val givenYear = vehicleDetails?.manufacturingYear
            if (givenYear != null)
                numberPicker.value = givenYear
            else
                numberPicker.value = calendar.get(Calendar.YEAR)

            numberPicker.setOnValueChangedListener { numberPicker, i, i1 ->

            }
            val builder = MaterialAlertDialogBuilder(
                requireContext(),
                R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog
            )
            builder.setTitle(requireContext().getString(R.string.year_info))
            builder.setView(dialogView)
            builder.setCancelable(true)
            builder.setPositiveButton(requireContext().resources.getString(R.string.ok)) { dialogInterface, i ->
                year.text = numberPicker.value.toString()
            }
            builder.setNegativeButton(requireContext().resources.getString(R.string.cancel)) { dialogInterface, i ->
            }
            builder.show()
        }

        switch_button.setOnCheckedChangeListener(object : SwitchButton.OnCheckedChangeListener {
            override fun onCheckedChanged(view: SwitchButton?, isChecked: Boolean) {
                leasingCompany.isEnabled = isChecked
            }
        })

        leasingCompany.setOnClickListener {
            if (leasingList.isEmpty()) {
                DialogUtils.showErrorInfo(requireContext(), getString(R.string.no_companies_found))
            } else {
                val dialog = LeasingCompanyFragment()
                dialog.listener = this
                dialog.leasingList = leasingList
                dialog.show(childFragmentManager, LeasingCompanyFragment.TAG)
            }
        }

        cta.setOnClickListener {
            if (licensePlate.text?.isNotEmpty() == true) {

                if (countryList.isNotEmpty())
                    when (countryList[registrationCountry.selectedItemPosition].code) {

                        "ROU" -> {
                            if (!RegexData.checkNumberPlateROU(licensePlate.text.toString())) {
                                DialogUtils.showErrorInfo(
                                    requireContext(),
                                    getString(R.string.license_error)
                                )
                                return@setOnClickListener
                            }
                        }

                        "QAT" -> {
                            if (!RegexData.checkNumberPlateQAT(licensePlate.text.toString())) {
                                DialogUtils.showErrorInfo(
                                    requireContext(),
                                    getString(R.string.license_error)
                                )
                                return@setOnClickListener
                            }
                        }

                        "UKR" -> {
                            if (!RegexData.checkNumberPlateUKR(licensePlate.text.toString())) {
                                DialogUtils.showErrorInfo(
                                    requireContext(),
                                    getString(R.string.license_error)
                                )
                                return@setOnClickListener
                            }
                        }

                        "BGR" -> {
                            if (!RegexData.checkNumberPlateBGR(licensePlate.text.toString())) {
                                DialogUtils.showErrorInfo(
                                    requireContext(),
                                    getString(R.string.license_error)
                                )
                                return@setOnClickListener
                            }
                        }

                    }
            }

            var eventList = ArrayList<VehicleEvent>()
            if (!vehicleDetails?.vehicleEvents.isNullOrEmpty()) {
                eventList = vehicleDetails?.vehicleEvents as ArrayList<VehicleEvent>
            }

            var fieldComplete = true
            if (warningsItemList?.size != 0) {
                if (!warningsItemList.isNullOrEmpty()) {
                    for (i in warningsItemList?.indices!!) {
                        when (warningsItemList?.get(i)?.field) {
                            "vehicle.vehicleIdentityCard", "vehicleIdentityCard" -> {
                                if (civ.text.isNullOrEmpty()) {
                                    fieldComplete = false
                                    break
                                }
                            }
                            "vehicle.manufacturingYear", "manufacturingYear" -> {
                                if (year.text.isNullOrEmpty()) {
                                    fieldComplete = false
                                    break
                                }
                            }
                            "vehicle.vehicleIdentificationNumber", "vehicleIdentificationNumber" -> {
                                if (vin.text.isNullOrEmpty()) {
                                    fieldComplete = false
                                    break
                                }
                            }
                            "vehicle.leasingCompany", "leasingCompany" -> {
                                if (leasingCompany.text.isNullOrEmpty()) {
                                    fieldComplete = false
                                    break
                                }
                            }
                            "vehicle.vehicleUsageType", "vehicleUsageType" -> {
                                if (purposeCategory.selectedItemPosition < 0) {
                                    fieldComplete = false
                                    break
                                }
                            }
                            "vehicle.enginePower", "enginePower" -> {
                                if (power.text.isNullOrEmpty()) {
                                    fieldComplete = false
                                    break
                                }
                            }
                            "vehicle.vehicleCategory", "vehicleCategory" -> {
                                if (vehicleCategory.selectedItemPosition < 0) {
                                    fieldComplete = false
                                    break
                                }
                            }
                            "vehicle.registrationCountryCode", "registrationCountryCode" -> {
                                if (registrationCountry.selectedItemPosition < 0) {
                                    fieldComplete = false
                                    break
                                }
                            }
                            "vehicle.vehicleBrand", "vehicleBrand" -> {
                                if (manufacturer.selectedItemPosition < 0) {
                                    fieldComplete = false
                                    break
                                }
                            }
                            "vehicle.maxAllowableMass", "maxAllowableMass" -> {
                                if (maxAllowableMass.text.isNullOrEmpty()) {
                                    fieldComplete = false
                                    break
                                }
                            }
                            "vehicle.licensePlate", "licensePlate" -> {
                                if (licensePlate.text.isNullOrEmpty()) {
                                    fieldComplete = false
                                    break
                                }
                            }
                            "vehicle.noOfSeats", "noOfSeats" -> {
                                if (noSeats.text.isNullOrEmpty()) {
                                    fieldComplete = false
                                    break
                                }
                            }
                            "vehicle.fuelType", "fuelType" -> {
                                if (fuelType.selectedItemPosition < 0) {
                                    fieldComplete = false
                                    break
                                }
                            }
                            "vehicle.engineSize", "engineSize" -> {
                                if (engineSize.text.isNullOrEmpty()) {
                                    fieldComplete = false
                                    break
                                }
                            }
                            "vehicle.vehicleSubCategory", "vehicleSubCategory" -> {
                                if (vehicleSubCategory.selectedItemPosition < 0) {
                                    fieldComplete = false
                                    break
                                }
                            }
                            "vehicle.model", "model" -> {
                                if (model.text.isNullOrEmpty()) {
                                    fieldComplete = false
                                    break
                                }
                            }
                        }
                    }
                }
            }
            if (!fieldComplete) {

                var dialogBody = ""
                if (warningsItemList?.isNotEmpty() == true && warningsItemList != null) {
                    var warningStr = ""
                    for (i in warningsItemList!!.indices) {
                        val field = requireContext().resources?.getIdentifier(
                            "${warningsItemList?.get(i)?.field}",
                            "string",
                            requireContext().packageName
                        )
                            ?.let { requireContext().resources?.getString(it) }
                        warningStr =
                            "$warningStr${field} : ${warningsItemList?.get(i)?.warning}\n"
                    }
                    dialogBody = "$dialogBody\n$warningStr"
                }
                DialogUtils.showErrorInfo(
                    requireContext(),
                    dialogBody
                )
                return@setOnClickListener
            }

            viewModel.onSave(
                vehicleDetails?.id?.toInt(),
                registrationCountry.selectedItemPosition,
                licensePlate.text.toString(),
                vehicleCategory.selectedItemPosition,
                vehicleSubCategory.selectedItemPosition,
                purposeCategory.selectedItemPosition,
                manufacturer.selectedItemPosition,
                model.text.toString(),
                vin.text.toString(),
                year.text.toString().ifBlank { null },
                maxAllowableMass.text.toString().ifBlank { null },
                engineSize.text.toString().ifBlank { null },
                power.text.toString().ifBlank { null },
                fuelType.selectedItemPosition,
                noSeats.text.toString().ifBlank { null },
                civ.text.toString(),
                leasingCompanyItem?.id,
                eventList,
                isEdit
            )
        }

        if (!warningsItemList.isNullOrEmpty()) {
            for (i in warningsItemList?.indices!!) {
                when (warningsItemList?.get(i)?.field) {
                    "vehicle.vehicleIdentityCard", "vehicleIdentityCard" -> {
                        changeTitle(civLabel)
                    }
                    "vehicle.manufacturingYear", "manufacturingYear" -> {
                        changeTitle(yearLabel)
                    }
                    "vehicle.vehicleIdentificationNumber", "vehicleIdentificationNumber" -> {
                        changeTitle(vinLabel)
                    }
                    "vehicle.leasingCompany", "leasingCompany" -> {
                        changeTitle(leasingLabel)
                    }
                    "vehicle.vehicleUsageType", "vehicleUsageType" -> {
                        changeTitle(purposeLabel)
                    }
                    "vehicle.enginePower", "enginePower" -> {
                        changeTitle(powerLabel)
                    }
                    "vehicle.vehicleCategory", "vehicleCategory" -> {
                        changeTitle(vehicleCategoryLabel)
                    }
                    "vehicle.registrationCountryCode", "registrationCountryCode" -> {
                        changeTitle(registrationCountryLabel)
                    }
                    "vehicle.vehicleBrand", "vehicleBrand" -> {
                        changeTitle(manufacturerLabel)
                    }
                    "vehicle.maxAllowableMass", "maxAllowableMass" -> {
                        changeTitle(maxAllowableMassLabel)
                    }
                    "vehicle.licensePlate", "licensePlate" -> {
                        changeTitle(licensePlateLabel)
                    }
                    "vehicle.noOfSeats", "noOfSeats" -> {
                        changeTitle(noSeatsLabel)
                    }
                    "vehicle.fuelType", "fuelType" -> {
                        changeTitle(fuelTypeLabel)
                    }
                    "vehicle.engineSize", "engineSize" -> {
                        changeTitle(engineSizeLabel)
                    }
                    "vehicle.vehicleSubCategory", "vehicleSubCategory" -> {
                        changeTitle(vehicleSubCategoryLabel)
                    }
                    "vehicle.model", "model" -> {
                        changeTitle(modelLabel)
                    }
                }
            }
        }

        registrationCountry?.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
                    viewModel.fetchLeasingCompanies(countryList[position].code)
                }
            }

        vehicleCategory?.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {

                    viewModel.fetchVehicleSubCategory(vehicleCategoryList[position].id.toInt())
                }
            }
    }

    fun changeTitle(tv: TextView) {
        tv.setCompoundDrawablesWithIntrinsicBounds(
            null,
            null,
            requireContext().resources.getDrawable(R.drawable.ic_star),
            null
        )
    }

    override fun setObservers() {
        viewModel.vehicleData.observe(viewLifecycleOwner) { vehicleDetails ->
            if (isEdit && vehicleDetails != null) {
                this.vehicleDetails = vehicleDetails
                vehicleDetails.let {
                    licensePlate.setText(it.licensePlate)
                    model.setText(it.model)
                    vin.setText(it.vehicleIdentificationNumber)
                    year.text = (it.manufacturingYear ?: "").toString()
                    maxAllowableMass.setText((it.maxAllowableMass ?: "").toString())
                    engineSize.setText((it.engineSize ?: "").toString())
                    power.setText((it.enginePower ?: "").toString())
                    noSeats.setText((it.noOfSeats ?: "").toString())
                    civ.setText(it.vehicleIdentityCard)
                    switch_button.isChecked = it.leasingCompany != null
                    leasingCompany.isEnabled = it.leasingCompany != null
                    title.text = requireContext().resources.getString(R.string.edit_car)
                }
            } else {
                title.text = requireContext().resources.getString(R.string.add_car)
            }
            viewModel.fetchDefaultData()
        }

        viewModel.vehicleSubCategoryData.observe(viewLifecycleOwner) { subCategoryList ->
            if (subCategoryList != null) {
                ArrayAdapter(
                    requireContext(),
                    R.layout.spinner_item,
                    subCategoryList
                ).also { adapter ->
                    vehicleSubCategory.adapter = adapter
                }
                subCategoryList[0].let {
                    vehicleSubCategory.setSelection(
                        it.let {
                            CatalogUtils.findPosById(
                                subCategoryList,
                                it.id
                            )
                        }
                    )
                }
            }
        }

        viewModel.vehicleUsageData.observe(viewLifecycleOwner) { usageList ->
            if (usageList != null) {
                ArrayAdapter(requireContext(), R.layout.spinner_item, usageList).also { adapter ->
                    purposeCategory.adapter = adapter
                }

                if (isEdit && vehicleDetails != null) {
                    vehicleDetails?.vehicleUsageType?.let {
                        CatalogUtils.findPosById(
                            usageList,
                            it.toLong()
                        )
                    }?.let {
                        purposeCategory.setSelection(
                            it
                        )
                    }
                } else {
                    purposeCategory.setSelection(
                        CatalogUtils.findPosById(
                            usageList,
                            usageList[0].id
                        )
                    )
                }
            }
        }


        viewModel.countryData.observe(viewLifecycleOwner) { countryList ->
            if (countryList != null) {
                this.countryList = countryList

                ArrayAdapter(requireContext(), R.layout.spinner_item, countryList).also { adapter ->
                    registrationCountry.adapter = adapter
                }

                registrationCountry.setSelection(Country.findPositionForCode(countryList), false)

                if (isEdit && vehicleDetails != null) {
                    vehicleDetails?.registrationCountryCode?.let { it1 ->
                        Country.findPositionForCode(countryList, it1)
                    }?.let { it2 ->
                        registrationCountry.setSelection(it2)
                    }
                }
            }
        }

        viewModel.vehicleCategoryData.observe(viewLifecycleOwner) { catList ->
            if (catList != null) {
                vehicleCategoryList = catList

                ArrayAdapter(
                    requireContext(),
                    R.layout.spinner_item,
                    vehicleCategoryList
                ).also { adapter ->
                    vehicleCategory.adapter = adapter
                }

                if (isEdit && vehicleDetails != null) {
                    vehicleDetails?.vehicleCategory?.let {
                        CatalogUtils.findPosById(
                            vehicleCategoryList,
                            it.toLong()
                        )
                    }?.let {
                        vehicleCategory.setSelection(
                            it
                        )
                    }
                    vehicleDetails?.vehicleCategory?.toInt()
                        ?.let { viewModel.fetchVehicleSubCategory(it) }
                }
            }
        }

        viewModel.vehicleBrandData.observe(viewLifecycleOwner) { brandList ->
            if (brandList != null) {
                this.vehicleBrandList = brandList

                ArrayAdapter(
                    requireContext(),
                    R.layout.spinner_item,
                    vehicleBrandList
                ).also { adapter ->
                    manufacturer.adapter = adapter
                }

                if (isEdit && vehicleDetails != null) {
                    vehicleDetails?.vehicleBrand?.let { it1 ->
                        CatalogUtils.findPosById(
                            vehicleBrandList,
                            it1
                        )
                    }?.let { it2 ->
                        manufacturer.setSelection(
                            it2
                        )
                    }
                }
            }
        }

        viewModel.fuelTypeData.observe(viewLifecycleOwner) { fuelList ->
            if (fuelList != null) {
                this.fuelTypeList = fuelList

                ArrayAdapter(
                    requireContext(),
                    R.layout.spinner_item,
                    fuelTypeList
                ).also { adapter ->
                    fuelType.adapter = adapter
                }

                if (isEdit && vehicleDetails != null) {
                    vehicleDetails?.fuelTypeId?.let {
                        CatalogUtils.findPosById(
                            fuelTypeList,
                            it
                        )
                    }?.let { fuelType.setSelection(it) }
                }
            }
        }

        viewModel.leasingCompaniesData.observe(viewLifecycleOwner) {
            if (it != null) {
                leasingList = it
                if (isEdit && vehicleDetails != null) {
                    if (switch_button.isChecked) {
                        leasingCompanyItem =
                            LeasingCompanyUtils.findById(
                                it,
                                vehicleDetails?.leasingCompany
                            )
                        leasingCompany.setText(leasingCompanyItem?.name)
                    }
                }
            }
        }
    }

    override fun onCompanyUpdated(company: LeasingCompany) {
        this.leasingCompanyItem = company
        leasingCompany.setText(company.name)
    }

}