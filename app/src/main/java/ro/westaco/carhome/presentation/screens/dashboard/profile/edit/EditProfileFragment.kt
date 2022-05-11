package ro.westaco.carhome.presentation.screens.dashboard.profile.edit

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.requests.Address
import ro.westaco.carhome.data.sources.remote.responses.models.*
import ro.westaco.carhome.databinding.AddressLayoutBinding
import ro.westaco.carhome.di.ApiModule
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.common.DeleteDialogFragment
import ro.westaco.carhome.presentation.screens.dashboard.profile.occupation.OccupationDialogFragment
import ro.westaco.carhome.presentation.screens.settings.data.person_natural.driving_categories.DrivingCategoriesDialogFragment
import ro.westaco.carhome.utils.CatalogUtils
import ro.westaco.carhome.utils.FileUtil
import ro.westaco.carhome.utils.Progressbar
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


//C- Edit Profile
@AndroidEntryPoint
class EditProfileFragment : BaseFragment<EditProfileDetailsViewModel>(),
    DrivingCategoriesDialogFragment.OnDialogInteractionListener,
    OccupationDialogFragment.OnDialogInteractionListener {

    private var profileItem: ProfileItem? = null
    var dlAttachment: Attachments? = null
    var idAttachment: Attachments? = null
    var dialogAddress: BottomSheetDialog? = null

    var address: Address? = null
    var occupationItem: CatalogItem? = null
    var typePos = 0
    var typeID = 0
    var countriesList: ArrayList<Country> = ArrayList()
    var drivingCat: ArrayList<CatalogItem> = ArrayList()
    var selectedDLCatList: ArrayList<Int> = ArrayList()
    var licenseCatList: ArrayList<CatalogItem> = ArrayList()
    var idTypeList: ArrayList<CatalogItem> = ArrayList()
    var occupationList: ArrayList<CatalogItem> = ArrayList()
    var sirutaList: ArrayList<Siruta> = ArrayList()
    var progressbar: Progressbar? = null
    var addressBinding: AddressLayoutBinding? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            profileItem = it.getSerializable(ARG_PROFILE) as? ProfileItem?

        }
    }

    companion object {
        const val ARG_PROFILE = "arg_profile"
    }

    override fun getContentView() = R.layout.fragment_edit_profile

    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)

    override fun initUi() {
        progressbar = Progressbar(requireContext())

        addressBinding = DataBindingUtil.inflate<AddressLayoutBinding>(
            layoutInflater,
            R.layout.address_layout,
            null,
            false
        )

        if (profileItem?.address?.countryCode == "ROU") {

            addressBinding?.inFilledCounty?.isVisible = false
            addressBinding?.countyLL?.isVisible = true
            addressBinding?.inFilledLocality?.isVisible = false
            addressBinding?.cityLL?.isVisible = true
        } else {
            addressBinding?.inFilledCounty?.isVisible = true
            addressBinding?.countyLL?.isVisible = false
            addressBinding?.inFilledLocality?.isVisible = true
            addressBinding?.cityLL?.isVisible = false
        }

        dialogAddress = BottomSheetDialog(requireContext())
        dialogAddress?.setCancelable(false)
        addressBinding?.root?.let { dialogAddress?.setContentView(it) }

        addressBinding?.mClose?.setOnClickListener {
            dialogAddress?.dismiss()
        }

        addressBinding?.mContinue?.setOnClickListener {
            var sirutaCode: Int? = null
            var localityStr: String? = null
            var regionStr: String? = null
            if (sirutaList[addressBinding?.countySpinner?.selectedItemPosition
                    ?: 0].name == "ROU"
            ) {
                sirutaCode = sirutaList[addressBinding?.countySpinner?.selectedItemPosition
                    ?: 0].code
                localityStr =
                    sirutaList[addressBinding?.citySpinner?.selectedItemPosition ?: 0].name
            } else {
                sirutaCode = null
                localityStr = (addressBinding?.filledLocality?.text ?: "").toString()
                regionStr = (addressBinding?.filledCounty?.text ?: "").toString()
            }

            address = Address(
                zipCode = (addressBinding?.zipCode?.text ?: "").toString(),
                streetType = null,
                sirutaCode = sirutaCode,
                locality = localityStr,
                streetName = (addressBinding?.streetName?.text ?: "").toString(),
                addressDetail = null,
                buildingNo = (addressBinding?.streetNumber?.text ?: "").toString(),
                countryCode = countriesList[addressBinding?.countrySpinner?.selectedItemPosition
                    ?: 0].code,
                block = (addressBinding?.blockName?.text ?: "").toString(),
                region = regionStr,
                entrance = (addressBinding?.entrance?.text ?: "").toString(),
                floor = (addressBinding?.floorName?.text ?: "").toString(),
                apartment = (addressBinding?.apartment?.text ?: "").toString()
            )
            dialogAddress?.dismiss()
        }

        back.setOnClickListener {
            viewModel.onBack()
        }

        home.setOnClickListener {
            viewModel.onMain()
        }

        cta.setOnClickListener {

            viewModel.onCta(
                lastName.text.toString(),
                address,
                idTypeSpinner.text.toString(),
                typeID,
                series.text.toString(),
                number.text.toString(),
                expdate.text.toString(),
                cnp.text.toString(),
                employer.text.toString(),
                dob.text.toString(),
                firstname.text.toString(),
                occupationItem,
                phone.text.toString(),
                lid.text.toString(),
                dlIssue.text.toString(),
                dlExpiry.text.toString(),
                selectedDLCatList,
                email.text.toString(),
                check1.isChecked
            )
        }

        fullAddress.setOnClickListener {
            dialogAddress?.show()
        }

        dob.setOnClickListener {
            viewModel.onDateClicked(it, profileItem?.dateOfBirth?.let { it1 -> dateToMilis(it1) })
        }

        expdate.setOnClickListener {
            viewModel.onDateClicked(
                it,
                profileItem?.identityDocument?.expirationDate?.let { it1 -> dateToMilis(it1) })
        }

        dlIssue.setOnClickListener {
            viewModel.onDateClicked(
                it,
                profileItem?.drivingLicense?.issueDate?.let { it1 -> dateToMilis(it1) })
        }

        dlExpiry.setOnClickListener {
            viewModel.onDateClicked(
                it,
                profileItem?.drivingLicense?.expirationDate?.let { it1 -> dateToMilis(it1) })
        }

        if (profileItem != null) {
            firstname.setText(profileItem?.firstName)
            lastName.setText(profileItem?.lastName)
            email.text = profileItem?.email
            phone.setText(profileItem?.phone)
            dob.text = viewModel.convertFromServerDate(profileItem?.dateOfBirth)

            //  (address) Object
            address = profileItem?.address
            addressBinding?.streetName?.setText(profileItem?.address?.streetName)
            addressBinding?.streetNumber?.setText(profileItem?.address?.buildingNo)
            addressBinding?.blockName?.setText(profileItem?.address?.block)
            addressBinding?.entrance?.setText(profileItem?.address?.entrance)
            addressBinding?.floorName?.setText(profileItem?.address?.floor)
            addressBinding?.apartment?.setText(profileItem?.address?.apartment)
            addressBinding?.zipCode?.setText(profileItem?.address?.zipCode)

            addressBinding?.filledCounty?.setText(address?.region)
            addressBinding?.filledLocality?.setText(address?.locality)

            fullAddress.text = address?.addressDetail
            employer.setText(profileItem?.employerName)

            val identityDocument = profileItem?.identityDocument
            series.setText(identityDocument?.series)
            number.setText(identityDocument?.number)
            expdate.text = viewModel.convertFromServerDate(identityDocument?.expirationDate)
            cnp.setText(profileItem?.cnp)
            typeID = profileItem?.identityDocument?.documentType?.id ?: 0
            idTypeSpinner.text = profileItem?.identityDocument?.documentType?.name

            //  (drivingLicense) Object
            val drivingLicense = profileItem?.drivingLicense
            drivingLicense?.licenseId?.let { lid.setText(it) }
            drivingLicense?.issueDate?.let {
                dlIssue.text = viewModel.convertFromServerDate(it)
            }
            drivingLicense?.expirationDate?.let {
                dlExpiry.text = viewModel.convertFromServerDate(it)
            }

            //  (occupationCorIsco08) Object
            occupationItem = profileItem?.occupationCorIsco08
            occupation.text = occupationItem?.name ?: ""

            //  (drivingLicenseAttachment) Object
            dlAttachment = profileItem?.drivingLicenseAttachment
            if (dlAttachment?.href.isNullOrEmpty()) {
                llUploadCertificate.visibility = View.VISIBLE
                llCertificate.visibility = View.GONE
            } else {
                llUploadCertificate.visibility = View.GONE
                btnDeleteCertificate.visibility = View.VISIBLE
                llCertificate.visibility = View.VISIBLE
                lblCertificate.text = dlAttachment?.name
            }

            //  (identityDocumentAttachment) Object
            idAttachment = profileItem?.identityDocumentAttachment
            if (idAttachment?.href.isNullOrEmpty()) {
                llUploadId.visibility = View.VISIBLE
                llId.visibility = View.GONE
            } else {
                llUploadId.visibility = View.GONE
                btnDeleteId.visibility = View.VISIBLE
                llId.visibility = View.VISIBLE
                lblId.text = idAttachment?.name
            }
        }

        llUploadCertificate.setOnClickListener {
            val result = FileUtil.checkPermission(requireContext())
            if (result) {
                callFileManagerForLicense()
            } else {
                FileUtil.requestPermission(requireActivity())
            }

        }

        llUploadId.setOnClickListener {
            val result = FileUtil.checkPermission(requireContext())
            if (result) {
                callFileManagerForID()
            } else {
                FileUtil.requestPermission(requireActivity())
            }
        }

        btnDeleteCertificate.setOnClickListener {

            /* profileItem?.drivingLicenseAttachment?.id?.let { it1 ->
                 viewModel.onDeleteAttachment(
                     it1,
                     requireContext().getString(R.string.attchment_dl)
                 )
             }*/

            val DrivingLicenseDialog = DeleteDialogFragment()
            DrivingLicenseDialog.layoutResId = R.layout.n_person_delete_doc
            DrivingLicenseDialog.listener =
                object : DeleteDialogFragment.OnDialogInteractionListener {
                    override fun onPosClicked() {

                        profileItem?.drivingLicenseAttachment?.id?.let { it1 ->
                            viewModel.onDeleteAttachment(
                                it1,
                                requireContext().getString(R.string.attchment_dl)
                            )
                        }

                        DrivingLicenseDialog.dismiss()
                    }
                }

            DrivingLicenseDialog.show(childFragmentManager, DeleteDialogFragment.TAG)


        }

        btnDeleteId.setOnClickListener {

            /*profileItem?.identityDocumentAttachment?.id?.let { it1 ->
                viewModel.onDeleteAttachment(
                    it1,
                    requireContext().getString(R.string.attchment_id)
                )
            }*/

            val CNPDialog = DeleteDialogFragment()
            CNPDialog.layoutResId = R.layout.n_person_delete_doc_id
            CNPDialog.listener = object : DeleteDialogFragment.OnDialogInteractionListener {
                override fun onPosClicked() {

                    profileItem?.identityDocumentAttachment?.id?.let { it1 ->
                        viewModel.onDeleteAttachment(
                            it1,
                            requireContext().getString(R.string.attchment_id)
                        )
                    }

                    CNPDialog.dismiss()
                }
            }

            CNPDialog.show(childFragmentManager, DeleteDialogFragment.TAG)

        }

        lblCertificate.setOnClickListener {
            dlAttachment?.href?.let { it ->
                progressbar?.showPopup()
                val baseUrl = ApiModule.BASE_URL_RESOURCES + it
                viewModel.fetchData(baseUrl)
            }
        }

        lblId.setOnClickListener {
            idAttachment?.href?.let { it ->
                progressbar?.showPopup()
                val baseUrl = ApiModule.BASE_URL_RESOURCES + it
                viewModel.fetchData(baseUrl)
            }
        }
    }


    fun dateToMilis(str: String): Long {
        val sdf = SimpleDateFormat(getString(R.string.server_date_format_template))

        val mDate = sdf.parse(str)
        return mDate.time

    }

    fun callFileManagerForLicense() {
        try {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/pdf"
            }
            startActivityForResult(intent, 101)
        } catch (e: ActivityNotFoundException) {
        }
    }

    fun callFileManagerForID() {
        try {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/pdf"
            }
            startActivityForResult(intent, 102)
        } catch (e: ActivityNotFoundException) {
        }
    }


    override fun setObservers() {
        viewModel.attachmentData.observe(viewLifecycleOwner) { attachmentData ->
            progressbar?.dismissPopup()

        }

        viewModel.profileDateLiveData.observe(viewLifecycleOwner) { datesMap ->
            datesMap?.forEach {
                (it.key as? TextView)?.text = SimpleDateFormat(
                    getString(R.string.date_format_template), Locale.getDefault()
                ).format(
                    Date(it.value)
                )
            }
        }

        viewModel.countryData.observe(viewLifecycleOwner) { countryData ->
            this.countriesList = countryData
            val arryadapter =
                ArrayAdapter(requireContext(), R.layout.drop_down_list, countriesList)
            addressBinding?.countrySpinner?.adapter = arryadapter

            address = profileItem?.address
            if (address?.countryCode != null) {
                for (i in countriesList.indices) {
                    if (countriesList[i].code == address?.countryCode) {
                        address?.countryCode?.let { it1 ->
                            Country.findPositionForCode(countriesList, it1)
                        }?.let { it2 ->
                            addressBinding?.countrySpinner?.setSelection(it2)
                        }
                    }
                }
            } else {
                addressBinding?.countrySpinner?.setSelection(
                    Country.findPositionForCode(
                        countriesList
                    ), false
                )
            }

            addressBinding?.countrySpinner?.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        if (countriesList[p2].code == "ROU") {
                            addressBinding?.inFilledCounty?.isVisible = false
                            addressBinding?.countyLL?.isVisible = true
                            addressBinding?.inFilledLocality?.isVisible = false
                            addressBinding?.cityLL?.isVisible = true
                        } else {
                            addressBinding?.inFilledCounty?.isVisible = true
                            addressBinding?.countyLL?.isVisible = false
                            addressBinding?.inFilledLocality?.isVisible = true
                            addressBinding?.cityLL?.isVisible = false
                        }

                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                    }

                }
        }

        viewModel.sirutaData.observe(viewLifecycleOwner) { sirutaData ->
            this.sirutaList = sirutaData
            sirutaList.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
            address = profileItem?.address

            val countyList: ArrayList<Siruta> = ArrayList()
            for (i in sirutaList.indices) {
                if (sirutaList[i].parentCode == null)
                    countyList.add(sirutaList[i])
            }
            countyList.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })

            val arryadapter =
                ArrayAdapter(requireContext(), R.layout.drop_down_list, countyList)
            addressBinding?.countySpinner?.adapter = arryadapter
            addressBinding?.countySpinner?.setSelection(0, false)


            addressBinding?.countySpinner?.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }

                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        findLocalityList(countyList[position].code)
                    }

                }

            findLocalityList(address?.sirutaCode)

            progressbar?.dismissPopup()
        }

        viewModel.occupationData.observe(viewLifecycleOwner) { occupationData ->
            this.occupationList = occupationData
            occupation.setOnClickListener {
                val dialog = OccupationDialogFragment()
                dialog.catelogList = occupationList
                dialog.listener = this
                dialog.selectedOccupation = profileItem?.occupationCorIsco08
                dialog.show(childFragmentManager, OccupationDialogFragment.TAG)
            }
        }

        viewModel.licenseCategoryData.observe(viewLifecycleOwner) { licenseCategoryData ->
            selectedDLCatList.clear()
            this.licenseCatList = licenseCategoryData
            selectedDLCatList = profileItem?.drivingLicense?.vehicleCategories as ArrayList<Int>
            licenseCategory.setOnClickListener {
                val dialog = DrivingCategoriesDialogFragment()
                dialog.listener = this
                dialog.catelogList = licenseCatList
                dialog.selectedList = selectedDLCatList
                dialog.show(childFragmentManager, DrivingCategoriesDialogFragment.TAG)
            }

            if (profileItem != null) {
                val drivingLicense = profileItem?.drivingLicense
                val categorylist = ArrayList<String>()
                drivingLicense?.vehicleCategories?.indices?.forEach { i ->
                    val catelog = CatalogUtils.findById(
                        licenseCategoryData,
                        drivingLicense.vehicleCategories[i].toLong()
                    )
                    catelog?.name?.let { categorylist.add(it) }
                    if (catelog != null) {
                        drivingCat.add(catelog)
                    }
                }
                categorylist.let { licenseCategory.text = it.joinToString(", ") }
            }
        }

        viewModel.idTypeData.observe(viewLifecycleOwner) { idTypeData ->
            this.idTypeList = idTypeData
            val dialog = BottomSheetDialog(requireContext())

            for (i in idTypeList.indices) {

                if (idTypeList[i].id.toInt() == profileItem?.identityDocument?.documentType?.id) {
                    typePos = i
                    idTypeSpinner.text = idTypeList[i].toString()
                }
            }

            val typeInterface = object : IDTypeAdapter.TypeInterface {
                override fun OnSelection(model: Int, id: Int) {
                    typePos = model
                    typeID = id
                }
            }
            val adapter = IDTypeAdapter(requireContext(), typeInterface, typePos)

            adapter.arrayList.clear()
            val view = layoutInflater.inflate(R.layout.id_type_bottomsheet, null)
            val mRecycler = view.findViewById<RecyclerView>(R.id.mTypeRec)
            val mBack = view.findViewById<ImageView>(R.id.mClose)
            val cta = view.findViewById<TextView>(R.id.cta)
            val mDismiss = view.findViewById<TextView>(R.id.mDismiss)

            cta.setOnClickListener {
                idTypeSpinner.text = idTypeList[typePos].toString()
                dialog.dismiss()
            }

            mDismiss.setOnClickListener {
                dialog.dismiss()
            }
            mRecycler.layoutManager =
                LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            mRecycler.layoutAnimation = null
            mRecycler.adapter = adapter
            adapter.addAll(idTypeList)
            dialog.setCancelable(true)
            dialog.setContentView(view)


            idTypeSpinner?.setOnClickListener {
                dialog.show()
            }

            mBack.setOnClickListener { dialog.dismiss() }
        }

        viewModel.actionStream.observe(viewLifecycleOwner) {
            when (it) {
                is EditProfileDetailsViewModel.ACTION.ShowDatePicker -> showDatePicker(
                    it.view,
                    it.dateInMillis
                )
                is EditProfileDetailsViewModel.ACTION.onDeleteSuccess -> onDeleteSuccess(it.attachType)

                is EditProfileDetailsViewModel.ACTION.onUploadSuccess -> onUploadSuccess(
                    it.attachType,
                    it.name
                )
            }
        }
    }

    fun onUploadSuccess(attachType: String, name: String) {
        if (attachType == "DRIVING_LICENSE") {
            lblCertificate.visibility = View.VISIBLE
            btnDeleteCertificate.visibility = View.VISIBLE
            lblCertificate.text = name
        } else {
            lblId.visibility = View.VISIBLE
            btnDeleteId.visibility = View.VISIBLE
            lblId.text = name
        }
    }

    private fun findLocalityList(code: Int?) {
        val city = mutableListOf<Siruta>()
        for (i in sirutaList.indices) {
            if (code == sirutaList[i].parentCode) {
                city.add(sirutaList[i])
            }
        }
        city.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })

        val cityadapter =
            ArrayAdapter(requireContext(), R.layout.drop_down_list, city)
        addressBinding?.citySpinner?.adapter = cityadapter
        addressBinding?.citySpinner?.setSelection(0)
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
        if (view == expdate || view == dlExpiry)
            dpd?.datePicker?.minDate = System.currentTimeMillis() + (1000 * 24 * 60 * 60)
        else
            dpd?.datePicker?.maxDate = System.currentTimeMillis()
        dpd?.show()
    }

    override fun onDrivingCategoriesUpdated(drivingCategories: ArrayList<CatalogItem>) {
        drivingCat = drivingCategories
        licenseCategory.text = drivingCategories.joinToString(", ")
    }

    var values: ContentValues? = null

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
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
                    llUploadCertificate.visibility = View.GONE
                    btnDeleteCertificate.visibility = View.INVISIBLE
                    llCertificate.visibility = View.VISIBLE
                    val dlFile = File(selectedFile)
                    lblCertificate.text = requireContext().resources.getString(R.string.uploading_)
                    viewModel.onAttach("DRIVING_LICENSE", dlFile)
                }
            }
        }

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
                    llUploadId.visibility = View.GONE
                    btnDeleteId.visibility = View.INVISIBLE
                    llId.visibility = View.VISIBLE
                    val idFile = File(selectedFile)
                    lblId.text = requireContext().resources.getString(R.string.uploading_)
                    viewModel.onAttach("IDENTITY_DOCUMENT", idFile)
                }
            }
        }

        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            callFileManagerForLicense()
        }

        if (requestCode == 1002 && resultCode == Activity.RESULT_OK) {
            callFileManagerForID()
        }

        if (requestCode == 2296) {
            if (SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    callFileManagerForID()
                }
            }
        }
        if (requestCode == 2297) {
            if (SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    callFileManagerForLicense()
                }
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            200 -> if (grantResults.size > 0) {
                val READ_EXTERNAL_STORAGE =
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                val WRITE_EXTERNAL_STORAGE =
                    grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (READ_EXTERNAL_STORAGE && WRITE_EXTERNAL_STORAGE) {
                    // perform action when allow permission success
                } else {
                    Toast.makeText(
                        requireContext(),
                        requireContext().resources.getString(R.string.allow_permission),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
    }

    fun getFilename(filePath: String): String {
        val file = File(filePath)
        return file.name
    }

    override fun onOccupationUpdated(occupationItem1: CatalogItem) {
        this.occupationItem = occupationItem1
        occupation.text = occupationItem1.name
    }

    fun onDeleteSuccess(attachType: String) {
        if (attachType.equals("DRIVING_LICENSE")) {
            llUploadCertificate.visibility = View.VISIBLE
            llCertificate.visibility = View.GONE
        } else {
            llUploadId.visibility = View.VISIBLE
            llId.visibility = View.GONE
        }
    }
}