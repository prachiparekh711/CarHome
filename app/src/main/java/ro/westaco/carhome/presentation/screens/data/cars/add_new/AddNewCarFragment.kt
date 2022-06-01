package ro.westaco.carhome.presentation.screens.data.cars.add_new

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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_add_new_car.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.requests.VehicleEvent
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem
import ro.westaco.carhome.data.sources.remote.responses.models.Country
import ro.westaco.carhome.data.sources.remote.responses.models.LeasingCompany
import ro.westaco.carhome.data.sources.remote.responses.models.VehicleDetails
import ro.westaco.carhome.di.ApiModule
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.common.DeleteDialogFragment
import ro.westaco.carhome.presentation.screens.data.cars.details.CarDetailsOtherAttachmentAdapter
import ro.westaco.carhome.presentation.screens.data.cars.leasingCompany.LeasingCompanyFragment
import ro.westaco.carhome.presentation.screens.home.PdfActivity
import ro.westaco.carhome.utils.*
import ro.westaco.carhome.utils.DialogUtils.Companion.showErrorInfo
import java.io.File

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

            vin_number = it.get(ARG_QUERY_VEHICLE).toString()
            regi_number = it.get(Regi_Number).toString()
        }
    }


    override fun initUi() {
        progressbar = Progressbar(requireContext())
        progressbar?.showPopup()
        licensePlate.setText(regi_number)
        vin.setText(vin_number)

        rcyViewOther.layoutManager = LinearLayoutManager(context)
        back.setOnClickListener {
            viewModel.onBack()
        }

        home.setOnClickListener {
            viewModel.onMain()
        }

        root.setOnClickListener {
            viewModel.onRootClicked()
        }

        switch_button.setOnCheckedChangeListener(object : SwitchButton.OnCheckedChangeListener {
            override fun onCheckedChanged(view: SwitchButton?, isChecked: Boolean) {

                /* leasingCompany.isVisible = isChecked*/
                leasingCompany.isEnabled = isChecked

            }
        })

        leasingCompany.setOnClickListener {
            if (viewModel.leasingCompaniesData.isEmpty()) {
                showErrorInfo(requireContext(), getString(R.string.no_companies_found))
            } else {
                val dialog = LeasingCompanyFragment()
                dialog.listener = this
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
                year.text.toString(),
                maxAllowableMass.text.toString(),
                engineSize.text.toString(),
                power.text.toString(),
                fuelType.selectedItemPosition,
                noSeats.text.toString(),
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
                year.setText((it.manufacturingYear ?: "").toString())
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
                        progressbar?.showPopup()
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
            val result = FileUtil.checkPermission(requireContext())
            if (result) {
                callFileManagerForOther()
            } else {
                FileUtil.requestPermission(requireActivity())
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
                    viewModel.leasingCompaniesData.clear()
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
            progressbar?.dismissPopup()
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

        viewModel.actionStream.observe(viewLifecycleOwner) {
            when (it) {
                is AddNewCarViewModel.ACTION.SetLeasingCompanyData -> setLeasingCompanyData(it.leasingCompaniesData)
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

    private fun setLeasingCompanyData(leasingCompaniesData: ArrayList<LeasingCompany>) {
        if (isEdit && vehicleDetails != null) {
            if (switch_button.isChecked) {
                leasingCompanyItem =
                    LeasingCompanyUtils.findById(
                        leasingCompaniesData,
                        vehicleDetails?.leasingCompany
                    )
                leasingCompany.setText(leasingCompanyItem?.name)
            }
        }
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
                    vehicleId?.let { viewModel.onAttach(it, "OTHER", dlFile) }
                }
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        when (requestCode) {
            200 -> if (grantResults.isNotEmpty()) {
                val readExternalStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val writeExternalStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (readExternalStorage && writeExternalStorage) {
                    callFileManagerForOther()
                } else {
                    showErrorInfo(requireContext(), getString(R.string.allow_permission))

                }
            }
        }
    }
}