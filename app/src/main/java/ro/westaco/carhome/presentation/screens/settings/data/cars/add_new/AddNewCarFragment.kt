package ro.westaco.carhome.presentation.screens.settings.data.cars.add_new

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
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_add_new_car.*
import okhttp3.ResponseBody
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.requests.VehicleEvent
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem
import ro.westaco.carhome.data.sources.remote.responses.models.Country
import ro.westaco.carhome.data.sources.remote.responses.models.LeasingCompany
import ro.westaco.carhome.data.sources.remote.responses.models.VehicleDetails
import ro.westaco.carhome.di.ApiModule
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.common.DeleteDialogFragment
import ro.westaco.carhome.presentation.screens.settings.data.cars.details.CarDetailsOtherAttachmentAdapter
import ro.westaco.carhome.presentation.screens.settings.data.cars.leasingCompany.LeasingCompanyFragment
import ro.westaco.carhome.utils.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

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
    var vehicleId: Int = 0
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
                if (isChecked) {

                    leasingCompany.visibility = View.VISIBLE
                } else {


                    leasingCompany.visibility = View.GONE

                }

            }
        })

        leasingCompany.setOnClickListener {
            if (viewModel.leasingCompaniesData.isNullOrEmpty()) {
                Toast.makeText(
                    requireContext(),
                    requireContext().resources.getString(R.string.no_companies_found),
                    Toast.LENGTH_SHORT
                ).show()
            } else {

                val dialog = LeasingCompanyFragment()
                dialog.listener = this
                dialog.show(childFragmentManager, LeasingCompanyFragment.TAG)
            }
        }

        cta.setOnClickListener {
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
            vehicleId = vehicleDetails?.id?.toInt()!!
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
                leasingCompany.isVisible = it.leasingCompany != null

                if (it.otherAttachments.isNullOrEmpty()) {
                    otherAdapter = CarDetailsOtherAttachmentAdapter(requireContext(), arrayListOf())
                } else {
                    otherAdapter = CarDetailsOtherAttachmentAdapter(
                        requireContext(),
                        it.otherAttachments,
                    )
                }
                rcyViewOther.adapter = otherAdapter

                otherAdapter.onItemClick = { attachments, from ->
                    if (from == "VIEW") {
                        progressbar?.showPopup()
                        attachments.href?.let { it ->
                            val baseUrl = ApiModule.BASE_URL_RESOURCES + it
                            viewModel.fetchData(baseUrl)
                        }
                    } else {

                        /* viewModel.onDeleteCertificateAttachment(
                             it.id.toInt(),
                             attachments.id
                         )*/

                        val NewcarDialog = DeleteDialogFragment()
                        NewcarDialog.layoutResId = R.layout.dialog_delete_car
                        NewcarDialog.listener =
                            object : DeleteDialogFragment.OnDialogInteractionListener {
                                override fun onPosClicked() {

                                    viewModel.onDeleteCertificateAttachment(
                                        it.id.toInt(),
                                        attachments.id
                                    )

                                    NewcarDialog.dismiss()
                                }
                            }

                        NewcarDialog.show(childFragmentManager, DeleteDialogFragment.TAG)
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
                    id: Long
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
                    id: Long
                ) {

                    viewModel.fetchVehicleSubCategory(vehicleCategoryList[position].id.toInt())
                }
            }
    }

    override fun setObservers() {
        viewModel.vehicleSubCategoryData.observe(viewLifecycleOwner) { subCategoryList ->
            ArrayAdapter(requireContext(), R.layout.spinner_item, subCategoryList).also { adapter ->
                vehicleSubCategory.adapter = adapter
            }
            vehicleSubCategory.setSelection(
                CatalogUtils.findPosById(
                    subCategoryList,
                    subCategoryList[0].id
                )
            )
        }

        viewModel.vehicleUsageData.observe(viewLifecycleOwner) { usageList ->
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

        viewModel.attachmentData.observe(viewLifecycleOwner) { attachmentData ->
            progressbar?.dismissPopup()

            /*                if (attachmentData != null) {
                                val dir: File
                                val root = Environment.getExternalStorageDirectory().absolutePath.toString()
                                val myDir = File(root, "DCIM")
                                myDir.mkdirs()

                                dir = File(myDir, context?.resources?.getString(R.string.app_name))
                                if (!dir.exists()) {
                                    dir.mkdirs()
                                }
                                saveFile(
                                    attachmentData,
                                    dir.absolutePath + "/Attachment_" + System.currentTimeMillis() + ".pdf"
                                )
                            }*/
        }

        viewModel.countryData.observe(viewLifecycleOwner) { countryList ->
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

        viewModel.vehicleCategoryData.observe(viewLifecycleOwner) { catList ->
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

        viewModel.vehicleBrandData.observe(viewLifecycleOwner) { brandList ->
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

        viewModel.fuelTypeData.observe(viewLifecycleOwner) { fuelList ->
            this.fuelTypeList = fuelList
            ArrayAdapter(requireContext(), R.layout.spinner_item, fuelTypeList).also { adapter ->
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

        viewModel.actionStream.observe(viewLifecycleOwner) {
            when (it) {
                is AddNewCarViewModel.ACTION.setLeasingCompanyData -> setLeasingCompanyData(it.leasingCompaniesData)
                is AddNewCarViewModel.ACTION.onRefresh -> onSuccess(it.vehicleDetails)
            }
        }
    }

    fun saveFile(body: ResponseBody?, pathWhereYouWantToSaveFile: String): String {
        if (body == null)
            return ""
        var input: InputStream? = null
        try {
            input = body.byteStream()
            val fos = FileOutputStream(pathWhereYouWantToSaveFile)
            fos.use { output ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                }
                output.flush()
            }
            Toast.makeText(
                requireContext(),
                resources.getString(R.string.dwld_success),
                Toast.LENGTH_SHORT
            ).show()
            return pathWhereYouWantToSaveFile
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                resources.getString(R.string.dwld_error),
                Toast.LENGTH_SHORT
            ).show()
        } finally {
            input?.close()

        }
        return ""
    }

    fun onSuccess(vehicle: VehicleDetails) {
        this.vehicleDetails = vehicle
        initUi()
    }

    fun setLeasingCompanyData(leasingCompaniesData: ArrayList<LeasingCompany>) {
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


    fun callFileManagerForOther() {
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
                    viewModel.onAttach(vehicleId, "OTHER", dlFile)
                }
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            200 -> if (grantResults.size > 0) {
                val READ_EXTERNAL_STORAGE = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val WRITE_EXTERNAL_STORAGE = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (READ_EXTERNAL_STORAGE && WRITE_EXTERNAL_STORAGE) {
                    callFileManagerForOther()
                } else {
                    Toast.makeText(
                        requireContext(),
                        requireContext().resources.getString(R.string.allow_permission),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}