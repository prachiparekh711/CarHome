package ro.westaco.carhome.presentation.screens.data.cars.add_new

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_add_new_car.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.requests.VehicleEvent
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem
import ro.westaco.carhome.data.sources.remote.responses.models.Country
import ro.westaco.carhome.data.sources.remote.responses.models.LeasingCompany
import ro.westaco.carhome.data.sources.remote.responses.models.VehicleDetails
import ro.westaco.carhome.di.ApiModule
import ro.westaco.carhome.dialog.DeleteDialogFragment
import ro.westaco.carhome.dialog.DialogUtils.Companion.showErrorInfo
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.data.cars.details.CarDetailsOtherAttachmentAdapter
import ro.westaco.carhome.presentation.screens.data.cars.leasingCompany.LeasingCompanyFragment
import ro.westaco.carhome.presentation.screens.pdf_viewer.PdfActivity
import ro.westaco.carhome.utils.CatalogUtils
import ro.westaco.carhome.utils.FileUtil
import ro.westaco.carhome.utils.LeasingCompanyUtils
import ro.westaco.carhome.utils.RegexData
import ro.westaco.carhome.views.Progressbar
import ro.westaco.carhome.views.SwitchButton
import java.io.File
import java.util.*

//C- Add CarDetails
@AndroidEntryPoint
class AddNewCarFragment : BaseFragment<AddNewCarViewModel>(),
    LeasingCompanyFragment.OnDialogInteractionListener {
    private var isEdit = false
    private var vin_number = ""
    private var regi_number = ""
    private var vehicleDetails: VehicleDetails? = null
    private var leasingCompanyItem: LeasingCompany? = null
    private lateinit var otherAdapter: CarDetailsOtherAttachmentAdapter
    var otherImageUri: Uri? = null
    var vehicleId: Int? = null
    var progressbar: Progressbar? = null
    var countryList: ArrayList<Country> = ArrayList()
    var vehicleCategoryList: ArrayList<CatalogItem> = ArrayList()
    var vehicleBrandList: ArrayList<CatalogItem> = ArrayList()
    var fuelTypeList: ArrayList<CatalogItem> = ArrayList()
    var leasingList: ArrayList<LeasingCompany> = ArrayList()

    companion object {

        const val ARG_IS_EDIT = "arg_is_edit"
        const val ARG_CAR = "arg_car"
        const val ARG_QUERY_VEHICLE = "queryVehicleInfoRequest"
        const val Regi_Number = "registrationNumber"

    }

    override fun getContentView() = R.layout.fragment_add_new_car

    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isEdit = it.getBoolean(ARG_IS_EDIT)
            vehicleDetails = it.getSerializable(ARG_CAR) as? VehicleDetails?
            vin_number = it.getString(ARG_QUERY_VEHICLE).toString()
            regi_number = it.getString(Regi_Number).toString()

        }
    }

    override fun initUi() {
        progressbar = Progressbar(requireContext())

        if (regi_number.isNotEmpty()) {
            licensePlate.setText(regi_number)
            vin.setText(vin_number)
        }

        if (vehicleDetails != null) {
            licensePlate.setText(vehicleDetails?.licensePlate)
            vin.setText(vehicleDetails?.vehicleIdentificationNumber)
        }

        rcyViewOther.layoutManager = LinearLayoutManager(requireContext())
        back.setOnClickListener {
            viewModel.onBack()
        }

        home.setOnClickListener {
            viewModel.onMain()
        }

        root.setOnClickListener {
            viewModel.onRootClicked()
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
                showErrorInfo(requireContext(), getString(R.string.no_companies_found))
            } else {
                val dialog = LeasingCompanyFragment()
                dialog.listener = this
                dialog.leasingList = leasingList
                dialog.show(childFragmentManager, LeasingCompanyFragment.TAG)
            }
        }

        cta.setOnClickListener {

            if (licensePlate.text?.isNotEmpty() == true) {

                when (countryList[registrationCountry.selectedItemPosition].code) {

                    "ROU" -> {
                        if (!RegexData.checkNumberPlateROU(licensePlate.text.toString())) {
                            showErrorInfo(requireContext(), getString(R.string.license_error))
                            return@setOnClickListener
                        }
                    }

                    "QAT" -> {
                        if (!RegexData.checkNumberPlateQAT(licensePlate.text.toString())) {
                            showErrorInfo(requireContext(), getString(R.string.license_error))
                            return@setOnClickListener
                        }
                    }

                    "UKR" -> {
                        if (!RegexData.checkNumberPlateUKR(licensePlate.text.toString())) {
                            showErrorInfo(requireContext(), getString(R.string.license_error))
                            return@setOnClickListener
                        }
                    }

                    "BGR" -> {
                        if (!RegexData.checkNumberPlateBGR(licensePlate.text.toString())) {
                            showErrorInfo(requireContext(), getString(R.string.license_error))
                            return@setOnClickListener
                        }
                    }

                }

            }

            var eventList = ArrayList<VehicleEvent>()
            if (!vehicleDetails?.vehicleEvents.isNullOrEmpty()) {
                eventList = vehicleDetails?.vehicleEvents as ArrayList<VehicleEvent>
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

        if (isEdit && vehicleDetails != null) {
            vehicleId = vehicleDetails?.id?.toInt()
            title.text = resources.getString(R.string.edit_car)
            vehicleDetails?.let {

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

                otherAdapter = if (it.otherAttachments.isNullOrEmpty()) {
                    CarDetailsOtherAttachmentAdapter(requireContext(), arrayListOf())
                } else {
                    CarDetailsOtherAttachmentAdapter(
                        requireContext(),
                        it.otherAttachments,
                    )
                }
                rcyViewOther.adapter = otherAdapter

                otherAdapter.onItemClick = { attachments, from ->
                    if (from == "VIEW") {
                        attachments.href?.let { it ->
                            val url = ApiModule.BASE_URL_RESOURCES + it
                            val intent = Intent(requireContext(), PdfActivity::class.java)
                            intent.putExtra(PdfActivity.ARG_DATA, url)
                            intent.putExtra(PdfActivity.ARG_FROM, "DOCUMENT")
                            requireContext().startActivity(intent)
                        }
                    } else {
                        val newCarDialog = DeleteDialogFragment()
                        newCarDialog.layoutResId = R.layout.dialog_delete_car
                        newCarDialog.listener =
                            object : DeleteDialogFragment.OnDialogInteractionListener {
                                override fun onPosClicked() {

                                    viewModel.onDeleteCertificateAttachment(
                                        it.id.toInt(),
                                        attachments.id
                                    )

                                    newCarDialog.dismiss()
                                }
                            }

                        newCarDialog.show(childFragmentManager, DeleteDialogFragment.TAG)
                    }
                }

            }
        } else {
            title.text = resources.getString(R.string.add_car_11)
        }

        llUploadOther.setOnClickListener {

            if (ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                Dexter.withContext(requireActivity())
                    .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    .withListener(object : MultiplePermissionsListener {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            report?.let {
                                if (report.areAllPermissionsGranted()) {
                                    callFileManagerForOther()
                                }
                            }
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            permissions: MutableList<PermissionRequest>?,
                            token: PermissionToken?
                        ) {
                            token?.continuePermissionRequest()
                        }
                    }).withErrorListener {}

                    .check()

            } else {
                if (permissionUpload()) {
                    callFileManagerForOther()
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
                    progressbar?.showPopup()
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

    override fun setObservers() {
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
                            it
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
                            it
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
                this.leasingList = it
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
            progressbar?.dismissPopup()
        }

        viewModel.actionStream.observe(viewLifecycleOwner) {
            when (it) {
                is AddNewCarViewModel.ACTION.OnRefresh -> it.vehicleDetails?.let { it1 ->
                    onSuccess(
                        it1
                    )
                }
            }
        }
    }

    private fun onSuccess(vehicle: VehicleDetails) {
        this.vehicleDetails = vehicle
        initUi()
        progressbar?.dismissPopup()
    }

    override fun onCompanyUpdated(company: LeasingCompany) {
        this.leasingCompanyItem = company
        leasingCompany.setText(company.name)
    }


    private fun callFileManagerForOther() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/pdf"
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, otherImageUri)
        startActivityForResult(intent, 102)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 102 && resultCode == Activity.RESULT_OK) {
            if (data != null && data.data != null) {
                val selectedUri: Uri? = data.data
                var selectedFile: String? = null
                if (selectedUri != null) {
                    selectedFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        FileUtil.getFilePathFor11(requireContext(), selectedUri)
                    } else {
                        FileUtil.getPath(selectedUri, requireContext())
                    }
                }
                if (selectedFile != null) {
                    val dlFile = File(selectedFile)
                    vehicleId?.let {
                        progressbar?.showPopup()
                        viewModel.onAttach(it, "OTHER", dlFile)
                    }
                }
            }
        }

    }


    private fun permissionUpload(): Boolean {

        return ActivityCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED

    }
}