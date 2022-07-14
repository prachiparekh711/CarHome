package ro.westaco.carhome.presentation.screens.data.person_natural.add_new

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputLayout
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_add_new_natural_person.*
import kotlinx.android.synthetic.main.fragment_add_new_natural_person.apartment
import kotlinx.android.synthetic.main.fragment_add_new_natural_person.buildingNo
import kotlinx.android.synthetic.main.fragment_add_new_natural_person.entrance
import kotlinx.android.synthetic.main.fragment_add_new_natural_person.floor
import kotlinx.android.synthetic.main.fragment_add_new_natural_person.streetName
import kotlinx.android.synthetic.main.fragment_add_new_natural_person.zipCode
import org.json.JSONObject
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.requests.Address
import ro.westaco.carhome.data.sources.remote.requests.PhoneCodeModel
import ro.westaco.carhome.data.sources.remote.responses.models.*
import ro.westaco.carhome.di.ApiModule
import ro.westaco.carhome.dialog.DeleteDialogFragment
import ro.westaco.carhome.dialog.DialogUtils.Companion.showErrorInfo
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.interfaceitem.CountyListClick
import ro.westaco.carhome.presentation.screens.dashboard.profile.edit.IDTypeAdapter
import ro.westaco.carhome.presentation.screens.data.commen.CodeDialog
import ro.westaco.carhome.presentation.screens.data.commen.CountryCodeDialog
import ro.westaco.carhome.presentation.screens.data.commen.CountyAdapter
import ro.westaco.carhome.presentation.screens.data.commen.LocalityAdapter
import ro.westaco.carhome.presentation.screens.data.person_natural.driving_categories.DrivingCategoriesDialogFragment
import ro.westaco.carhome.presentation.screens.pdf_viewer.PdfActivity
import ro.westaco.carhome.utils.*
import ro.westaco.carhome.utils.SirutaUtil.Companion.countyList
import ro.westaco.carhome.utils.SirutaUtil.Companion.defaultCity
import ro.westaco.carhome.utils.SirutaUtil.Companion.defaultCounty
import ro.westaco.carhome.utils.SirutaUtil.Companion.fetchCounty
import ro.westaco.carhome.utils.SirutaUtil.Companion.fetchCountyPosition
import ro.westaco.carhome.views.Progressbar
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

//C- Rebuilt Section
@AndroidEntryPoint
class AddNewNaturalPersonFragment : BaseFragment<AddNewNaturalPersonViewModel>(),
    DrivingCategoriesDialogFragment.OnDialogInteractionListener,
    CountryCodeDialog.CountryCodePicker,
    CodeDialog.CountyPickerItems {
    private var isEdit = false
    private var naturalPersonDetails: NaturalPersonDetails? = null

    var dlAttachment: Attachments? = null
    var idAttachment: Attachments? = null
    var address: Address? = null
    var typePos = 0
    var typeID = 0
    var drivingCat: ArrayList<CatalogItem> = ArrayList()
    var progressbar: Progressbar? = null
    var streetTypeList: ArrayList<CatalogItem> = ArrayList()
    var licenseCatList: ArrayList<CatalogItem> = ArrayList()
    var idTypeList: ArrayList<CatalogItem> = ArrayList()

    var selectedPhoneCode: String? = null
    var countriesList: ArrayList<Country> = ArrayList()
    var cityList: ArrayList<Siruta> = ArrayList()
    var countryItem: Country? = null
    var countyPosition: Int? = null
    var localityPosition: Int? = null
    var countyDialog: BottomSheetDialog? = null
    var localityDialog: BottomSheetDialog? = null
    lateinit var bottomSheet: BottomSheetDialog

    //    verify for validation to edit Insurance person( Owner, User, Driver)
    var verifyNaturalItem: VerifyRcaPerson? = null

    override fun getContentView() = R.layout.fragment_add_new_natural_person

    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)

    companion object {
        const val ARG_IS_EDIT = "arg_is_edit"
        const val ARG_NATURAL_PERSON = "arg_natural_person"
        const val ARG_VERIFY_ITEM = "arg_verify_list"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            isEdit = it.getBoolean(ARG_IS_EDIT)
            naturalPersonDetails = it.getSerializable(ARG_NATURAL_PERSON) as? NaturalPersonDetails?
            verifyNaturalItem =
                it.getSerializable(ARG_VERIFY_ITEM) as VerifyRcaPerson?
        }
        bottomSheet = BottomSheetDialog(requireContext())
    }

    override fun initUi() {

        progressbar = Progressbar(requireContext())

        countySpinnerText.setOnClickListener {
            openCountyDialog()
        }

        localitySpinnerText?.setOnClickListener {
            localityDialog?.show()
        }

        cancel.setOnClickListener {
            viewModel.onBack()
        }

        name_lay.setOnClickListener {

            if (li_name_h.visibility == View.VISIBLE) {
                li_name_h.visibility = View.GONE
                name_lay.setBackgroundColor(resources.getColor(R.color.white))
                name_arrow.setImageResource(R.drawable.ic_arrow_circle_down)
            } else {
                name_lay.setBackgroundColor(resources.getColor(R.color.expande_colore))
                li_name_h.visibility = View.VISIBLE
                name_arrow.setImageResource(R.drawable.ic_arrow_circle_up)
            }
        }

        personal_info_lay.setOnClickListener {
            personalInfoView2.isVisible = !personalInfoView2.isVisible
            personalInfoSection()
        }

        identity_lay.setOnClickListener {
            identity_hidden_view.isVisible = !identity_hidden_view.isVisible
            idSection()
        }

        license_lay.setOnClickListener {
            license_hidden_view.isVisible = !license_hidden_view.isVisible
            drivingLicenseSection()
        }

        fullAddress_lay.setOnClickListener {
            address_hidden_view.isVisible = !address_hidden_view.isVisible
            addressSection()
        }

        document_lay.setOnClickListener {
            if (doc_hidden_view.visibility == View.VISIBLE) {
                doc_hidden_view.visibility = View.GONE
                document_lay.setBackgroundColor(resources.getColor(R.color.white))
                doc_arrow.setImageResource(R.drawable.ic_arrow_circle_down)
            } else {
                document_lay.setBackgroundColor(resources.getColor(R.color.expande_colore))
                doc_hidden_view.visibility = View.VISIBLE
                doc_arrow.setImageResource(R.drawable.ic_arrow_circle_up)
            }
        }

        back.setOnClickListener {
            viewModel.onBack()
        }

        root.setOnClickListener {
            viewModel.onRootClicked()
        }

        dob.setOnClickListener {
            viewModel.onDateClicked(
                it,
                naturalPersonDetails?.dateOfBirth?.let { it1 -> dateToMilis(it1) })
        }

        expdate.setOnClickListener {
            viewModel.onDateClicked(
                it,
                naturalPersonDetails?.identityDocument?.expirationDate?.let { it1 -> dateToMilis(it1) })
        }

        drivLicenseIssueDate.setOnClickListener {
            viewModel.onDateClicked(
                it,
                naturalPersonDetails?.drivingLicense?.issueDate?.let { it1 -> dateToMilis(it1) })
        }

        drivLicenseExpDate.setOnClickListener {
            viewModel.onDateClicked(
                it,
                naturalPersonDetails?.drivingLicense?.expirationDate?.let { it1 -> dateToMilis(it1) })
        }

        cta.setOnClickListener {

            if (!check.isChecked) {
                showErrorInfo(requireContext(), getString(R.string.check_info))
                return@setOnClickListener
            }

            if (verifyNaturalItem != null) {
                if (!verifyRcaFieldOnComplete()) {
                    val warningsItemList = verifyNaturalItem?.validationResult?.warnings
                    var dialogBody = ""
                    if (warningsItemList?.isNotEmpty() == true) {
                        var warningStr = ""
                        for (i in warningsItemList.indices) {
                            val field = requireContext().resources?.getIdentifier(
                                "${warningsItemList[i]?.field}",
                                "string",
                                requireContext().packageName
                            )
                                ?.let { requireContext().resources?.getString(it) }
                            warningStr =
                                "$warningStr${field} : ${warningsItemList.get(i)?.warning}\n"
                        }
                        dialogBody = "$dialogBody\n$warningStr"
                    }
                    showErrorInfo(
                        requireContext(),
                        dialogBody
                    )
                    return@setOnClickListener
                }
            }

            if (!cnp.text.toString()
                    .isEmpty() && !RegexData.checkCNPNumberIsValid(cnpNumber = cnp.text.toString())
            ) {
                showErrorInfo(requireContext(), getString(R.string.reg_invalid_cnp))
                return@setOnClickListener
            }

            if (!email.text.toString()
                    .isEmpty() && !RegexData.checkEmailRegex(email.text.toString())
            ) {
                showErrorInfo(
                    requireContext(),
                    getString(R.string.invalid_email)
                )
                return@setOnClickListener
            }

            val streetTypeItem =
                sp_quata?.selectedItemPosition?.let { it1 -> streetTypeList[it1].id }
                    ?.let { it2 ->
                        CatalogUtils.findById(
                            streetTypeList,
                            it2
                        )
                    }

            var regionStr: String? = null
            var sirutaCode: Int? = null
            var localityStr: String? = null

            if (countryItem?.code == "ROU") {
                if (countyPosition != -1 && localityPosition != -1) {
                    regionStr = countyPosition?.let { countyList[it].name }
                    sirutaCode = localityPosition?.let { cityList[it].code }
                    localityStr = localityPosition?.let { cityList[it].name }
                } else {
                    regionStr = defaultCounty?.name
                    sirutaCode = defaultCity?.code
                    localityStr = defaultCity?.name
                }
            } else {
                regionStr = stateProvinceText.text.toString()
                sirutaCode = null
                localityStr = localityAreaText.text.toString()
            }

            var addressItem: Address? = null
            if (countryItem != null && streetName?.text != null && buildingNo?.text != null) {
                addressItem = Address(
                    zipCode = zipCode.text.toString().ifBlank { null },
                    streetType = streetTypeItem,
                    sirutaCode = sirutaCode,
                    locality = localityStr,
                    streetName = streetName.text.toString().ifBlank { null },
                    buildingNo = buildingNo.text.toString().ifBlank { null },
                    countryCode = countryItem?.code,
                    block = blockName.text.toString().ifBlank { null },
                    region = regionStr,
                    entrance = entrance.text.toString().ifBlank { null },
                    floor = floor.text.toString(),
                    apartment = apartment.text.toString().ifBlank { null }
                )
            }

            val pos = selectedPhoneCode?.let { it1 ->
                Country.findPositionForTwoLetterCode(
                    countriesList,
                    it1
                )
            }

            var phoneCountryCode: String? = null
            if (pos != null)
                phoneCountryCode = countriesList[pos].code

            viewModel.onSave(
                naturalPersonDetails?.id?.toLong(),
                lastName.text.toString().ifBlank { null },
                address = addressItem,
                idTypeSpinner.text.toString().ifBlank { null },
                typeID,
                series.text.toString().ifBlank { null },
                number.text.toString().ifBlank { null },
                expdate.text.toString().ifBlank { null },
                cnp.text.toString().ifBlank { null },
                dob.text.toString().ifBlank { null },
                firstName.text.toString().ifBlank { null },
                phone.text.toString().ifBlank { null },
                phoneCountryCode = phoneCountryCode,
                drivLicenseId.text.toString().ifBlank { null },
                drivLicenseIssueDate.text.toString().ifBlank { null },
                drivLicenseExpDate.text.toString().ifBlank { null },
                drivingCat,
                email.text.toString().ifBlank { null },
                isEdit
            )
        }

        if (isEdit && naturalPersonDetails != null) {
            address = naturalPersonDetails?.address
            cta.text = getString(R.string.save_changes)

            firstName.setText(naturalPersonDetails?.firstName)
            lastName.setText(naturalPersonDetails?.lastName)
            dob.setText(viewModel.convertFromServerDate(naturalPersonDetails?.dateOfBirth))
            phone.setText(naturalPersonDetails?.phone)
            email.setText(naturalPersonDetails?.email)

            titleItems.text = getString(R.string.natural_pers_details)
            cta.text = getString(R.string.save_changes)

            //  (identityDocument) Object
            val identityDocument = naturalPersonDetails?.identityDocument
            series.setText(identityDocument?.series)
            number.setText(identityDocument?.number)
            expdate.setText(viewModel.convertFromServerDate(identityDocument?.expirationDate))
            cnp.setText(naturalPersonDetails?.cnp)
            typeID = naturalPersonDetails?.identityDocument?.documentType?.id ?: 0

            naturalPersonDetails?.drivingLicense?.licenseId?.let { drivLicenseId.setText(it) }
            naturalPersonDetails?.drivingLicense?.issueDate?.let {
                drivLicenseIssueDate.setText(viewModel.convertFromServerDate(it))
            }
            naturalPersonDetails?.drivingLicense?.expirationDate?.let {
                drivLicenseExpDate.setText(viewModel.convertFromServerDate(it))
            }

            //  (drivingLicenseAttachment) Object
            dlAttachment = naturalPersonDetails?.drivingLicenseAttachment
            if (dlAttachment?.href.isNullOrEmpty()) {
                lblCertificate.visibility = View.GONE
                btnDeleteCertificate.visibility = View.GONE
                iv_doc.setImageDrawable(requireContext().resources.getDrawable(R.drawable.ic_document_upload))
            } else {
                lblCertificate.visibility = View.VISIBLE
                btnDeleteCertificate.visibility = View.VISIBLE
                lblCertificate.text = dlAttachment?.name
                iv_doc.setImageDrawable(requireContext().resources.getDrawable(R.drawable.ic_pdf_selected))
            }

            //  (identityDocumentAttachment) Object
            idAttachment = naturalPersonDetails?.identityDocumentAttachment
            if (idAttachment?.href.isNullOrEmpty()) {
                lblId.visibility = View.GONE
                btnDeleteId.visibility = View.GONE
                iv_cnp.setImageDrawable(requireContext().resources.getDrawable(R.drawable.ic_document_upload))
            } else {
                lblId.visibility = View.VISIBLE
                btnDeleteId.visibility = View.VISIBLE
                lblId.text = idAttachment?.name
                iv_cnp.setImageDrawable(requireContext().resources.getDrawable(R.drawable.ic_pdf_selected))
            }

            streetName.setText(address?.streetName)
            buildingNo.setText(address?.buildingNo)
            blockName.setText(address?.block)
            entrance.setText(address?.entrance)
            apartment.setText(address?.apartment)
            floor.setText(address?.floor)
            zipCode.setText(address?.zipCode)

            address?.countryCode?.let { changeCountryState(it) }
            if (address?.countryCode == "ROU") {
                countySpinnerText.setText(address?.region)
                localitySpinnerText.setText(address?.locality)
            } else {
                stateProvinceText.setText(address?.region)
                localityAreaText.setText(address?.locality)
            }

            countyPosition =
                address?.region?.let { fetchCountyPosition(it) }

            localityPosition =
                address?.locality?.let { fetchCountyPosition(it) }

        } else {

            changeCountryState("ROU")
            countySpinnerText.setText(defaultCounty?.name)
            localitySpinnerText.setText(defaultCity?.name)

            countyPosition = defaultCounty?.name?.let { fetchCountyPosition(it) }
            localityPosition = defaultCity?.name?.let { fetchCountyPosition(it) }
        }

        llUploadCertificate.setOnClickListener {


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
                                    callFileManagerForLicense()
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
                    callFileManagerForLicense()
                }
            }

        }

        llUploadId.setOnClickListener {

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
                                    callFileManagerForID()
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
                    callFileManagerForID()
                }
            }

        }

        btnDeleteCertificate.setOnClickListener {

            val drivingLicenseDialog = DeleteDialogFragment()
            drivingLicenseDialog.layoutResId = R.layout.n_person_delete_doc
            drivingLicenseDialog.listener =
                object : DeleteDialogFragment.OnDialogInteractionListener {
                    override fun onPosClicked() {

                        naturalPersonDetails?.drivingLicenseAttachment?.id?.let { it1 ->
                            naturalPersonDetails?.id?.let { it2 ->
                                viewModel.onDeleteAttachment(
                                    it2,
                                    it1,
                                    requireContext().getString(R.string.attchment_dl)
                                )
                            }
                        }
                        drivingLicenseDialog.dismiss()
                    }
                }

            drivingLicenseDialog.show(childFragmentManager, DeleteDialogFragment.TAG)

        }

        btnDeleteId.setOnClickListener {

            val cnpDialog = DeleteDialogFragment()
            cnpDialog.layoutResId = R.layout.n_person_delete_doc_id
            cnpDialog.listener = object : DeleteDialogFragment.OnDialogInteractionListener {
                override fun onPosClicked() {

                    naturalPersonDetails?.identityDocumentAttachment?.id?.let { it1 ->
                        naturalPersonDetails?.id?.let { it2 ->
                            viewModel.onDeleteAttachment(
                                it2,
                                it1,
                                requireContext().getString(R.string.attchment_id)
                            )
                        }
                    }

                    cnpDialog.dismiss()
                }
            }

            cnpDialog.show(childFragmentManager, DeleteDialogFragment.TAG)

        }

        lblCertificate.setOnClickListener {
            dlAttachment?.href?.let { it1 ->
                val url = ApiModule.BASE_URL_RESOURCES + it1
                val intent = Intent(requireContext(), PdfActivity::class.java)
                intent.putExtra(PdfActivity.ARG_DATA, url)
                intent.putExtra(PdfActivity.ARG_FROM, "DOCUMENT")
                requireContext().startActivity(intent)
            }
        }

        lblId.setOnClickListener {
            idAttachment?.href?.let { it1 ->
                val url = ApiModule.BASE_URL_RESOURCES + it1
                val intent = Intent(requireContext(), PdfActivity::class.java)
                intent.putExtra(PdfActivity.ARG_DATA, url)
                intent.putExtra(PdfActivity.ARG_FROM, "DOCUMENT")
                requireContext().startActivity(intent)
            }
        }

        if (verifyNaturalItem != null) {
            verificationForRca()
        } else {
            changeHint(fNameLabel, resources.getString(R.string.first_name_cc))
            changeHint(lNameLabel, resources.getString(R.string.last_name_cc))
        }
    }

    private fun setPhoneCountryData() {
        val phoneModelList: ArrayList<PhoneCodeModel> = ArrayList()
        val obj = FileUtil.loadJSONFromAsset(requireContext())?.let { JSONObject(it) }
        var phoneCountryItem: Country? = null
        if (isEdit && naturalPersonDetails != null) {
            val pos = naturalPersonDetails?.phoneCountryCode?.let { it1 ->
                Country.findPositionForCode(
                    countriesList,
                    it1
                )
            }
            if (pos != null)
                phoneCountryItem = countriesList[pos]
        }

        var romanCode: PhoneCodeModel? = null
        if (obj != null) {
            for (key in obj.keys()) {
                val keyStr = key as String
                val keyValue = obj[keyStr]
                val code = PhoneCodeModel(keyStr, keyValue as String?)
                phoneModelList.add(code)
                if (phoneCountryItem != null && phoneCountryItem.twoLetterCode == code.key) {
                    romanCode = code
                } else {
                    if (code.key == "RO" && romanCode == null) {
                        romanCode = code
                    }
                }
            }
        }

        phoneCode.text = "+ ${romanCode?.value}"
        selectedPhoneCode = romanCode?.key
        phoneFlag.text =
            CountryCityUtils.getFlagId(
                CountryCityUtils.firstTwo(
                    romanCode?.key?.lowercase(Locale.getDefault()).toString()
                ).toString()
            )

        val phoneCodeDialog = CodeDialog(requireActivity(), phoneModelList, this)

        cc_dialog.setOnClickListener {
            phoneCodeDialog.show(requireActivity().supportFragmentManager, null)
        }
    }

    private fun openCountyDialog() {

        val view = layoutInflater.inflate(R.layout.county_layout, null)
        countyDialog = BottomSheetDialog(requireContext())
        countyDialog?.setCancelable(true)
        countyDialog?.setContentView(view)
        countyDialog?.show()

        val rvCounty: RecyclerView? = view.findViewById(R.id.rv_county)
        val etSearchTrain: EditText? = view.findViewById(R.id.etSearchTrain)
        val mClose: ImageView? = view.findViewById(R.id.mClose)
        rvCounty?.layoutManager = LinearLayoutManager(requireContext())

        if (countyList.isNotEmpty()) {

            val adapter =
                CountyAdapter(
                    requireContext(),
                    countyList,
                    countyCode = fetchCounty(countySpinnerText.text.toString())?.code
                        ?: countyList[0].code,
                    countyListClick = object :
                        CountyListClick {
                        override fun click(position: Int, siruta: Siruta) {
                            countyPosition = position
                            countySpinnerText.setText(siruta.name)
                            localityBlock(siruta)
                            countyDialog?.dismiss()
                        }
                    })


            rvCounty?.adapter = adapter
            adapter.filter.filter("")

            etSearchTrain?.addTextChangedListener(object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    if (s.toString().isNotEmpty()) {
                        adapter.filter.filter(s.toString())
                    } else {
                        adapter.filter.filter("")
                    }
                }

                override fun afterTextChanged(s: Editable?) {

                }


            })

        }

        mClose?.setOnClickListener {
            countyDialog?.dismiss()
        }

    }

    private fun localityBlock(county: Siruta) {

        val view = layoutInflater.inflate(R.layout.locality_layout, null)
        localityDialog = BottomSheetDialog(requireContext())
        localityDialog?.setCancelable(true)
        localityDialog?.setContentView(view)

        val rvLocality: RecyclerView? = view.findViewById(R.id.rv_locality)
        val etSearchTrain: EditText? = view.findViewById(R.id.etSearchTrain)
        val mClose: ImageView? = view.findViewById(R.id.mClose)
        rvLocality?.layoutManager = LinearLayoutManager(requireContext())

        cityList = SirutaUtil.fetchCity(county)


        localitySpinnerText.setText(cityList[0].name)
        val adapter = LocalityAdapter(
            requireContext(),
            cityList,
            localityListClick = object : LocalityAdapter.LocalityListClick {
                override fun localityclick(position: Int, siruta: Siruta) {
                    localityPosition = position
                    localitySpinnerText.setText(siruta.name)
                    localityDialog?.dismiss()
                }
            })
        rvLocality?.adapter = adapter

        etSearchTrain?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int,
            ) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) {
                    adapter.filter.filter(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        mClose?.setOnClickListener {
            localityDialog?.dismiss()
        }

    }

    @SuppressLint("SimpleDateFormat")
    fun dateToMilis(str: String): Long {

        val sdf = SimpleDateFormat(getString(R.string.server_date_format_template))

        val mDate = sdf.parse(str)
        return mDate.time

    }

    private fun callFileManagerForLicense() {
        try {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/pdf"
            }
            startActivityForResult(intent, 101)
        } catch (e: ActivityNotFoundException) {
        }

    }

    private fun callFileManagerForID() {
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

        viewModel.datesMapLiveData.observe(viewLifecycleOwner) { datesMap ->
            datesMap?.forEach {
                (it.key as? TextView)?.text = SimpleDateFormat(
                    getString(R.string.date_format_template), Locale.getDefault()
                ).format(
                    Date(it.value)
                )
            }
        }


        viewModel.countryData.observe(viewLifecycleOwner) { countryData ->
            if (countryData != null) {
                this.countriesList = countryData
                li_dialog.setOnClickListener {
                    val countryCodeDialog =
                        CountryCodeDialog(requireActivity(), countriesList, this)
                    countryCodeDialog.show(requireActivity().supportFragmentManager, null)
                }

                if (isEdit && naturalPersonDetails?.address != null) {
                    val pos = address?.countryCode?.let {
                        Country.findPositionForCode(
                            countriesList,
                            it
                        )
                    }
                    this.countryItem = pos?.let { countriesList[it] }
                    countryNameTV.text = countryItem?.name
                    countryItem?.twoLetterCode?.lowercase(
                        Locale.getDefault()
                    )?.let {
                        CountryCityUtils.getFlagId(
                            it
                        )
                    }?.let {
                        cuntryFlagIV.text =
                            it

                    }
                } else {
                    val pos = Country.findPositionForCode(countriesList, "ROU")
                    this.countryItem = pos.let { countriesList[it] }
                    countryNameTV.text = countryItem?.name
                    countryItem?.twoLetterCode?.lowercase(
                        Locale.getDefault()
                    )?.let {
                        CountryCityUtils.getFlagId(
                            it
                        )
                    }?.let {
                        cuntryFlagIV.text =
                            it
                    }
                }
            }

            setPhoneCountryData()
        }

        viewModel.licenseCategoryData.observe(viewLifecycleOwner) { licenseCategoryData ->
            if (licenseCategoryData != null) {
                this.licenseCatList = licenseCategoryData
                drivLicenseCateg.setOnClickListener {
                    val dialog = DrivingCategoriesDialogFragment()
                    dialog.listener = this
                    dialog.catalogList = licenseCategoryData
                    dialog.show(childFragmentManager, DrivingCategoriesDialogFragment.TAG)
                }

                if (isEdit && naturalPersonDetails != null) {
                    val drivingLicense = naturalPersonDetails?.drivingLicense
                    val categoryList = ArrayList<String>()
                    if (drivingLicense?.vehicleCategories != null && drivingLicense.vehicleCategories.isNotEmpty()) {
                        for (i in drivingLicense.vehicleCategories.indices) {
                            val catelog = CatalogUtils.findById(
                                licenseCategoryData,
                                drivingLicense.vehicleCategories[i].toLong()
                            )
                            catelog?.name?.let { categoryList.add(it) }
                            if (catelog != null) {
                                drivingCat.add(catelog)
                            }
                        }
                    }

                    categoryList.let { drivLicenseCateg.setText(it.joinToString(", ")) }
                }
            }
        }

        viewModel.idTypeData.observe(viewLifecycleOwner) { idTypeData ->
            if (idTypeData != null) {
                this.idTypeList = idTypeData
                val dialog = BottomSheetDialog(requireContext())


                if (isEdit && naturalPersonDetails != null) {
                    for (i in idTypeList.indices) {
                        if (idTypeList[i].id.toInt() == naturalPersonDetails?.identityDocument?.documentType?.id) {
                            typePos = i
                            idTypeSpinner.setText(idTypeList[i].toString())
                        }
                    }
                    idTypeSpinner.setText(naturalPersonDetails?.identityDocument?.documentType?.name)
                }

                val typeInterface = object : IDTypeAdapter.TypeInterface {
                    override fun onSelection(model: Int, id: Int) {
                        typePos = model
                        typeID = id
                        idTypeSpinner.setText(idTypeList[model].toString())
                        dialog.dismiss()
                    }
                }
                val adapter = IDTypeAdapter(requireContext(), typeInterface, typePos)
                adapter.arrayList.clear()
                val view = layoutInflater.inflate(R.layout.id_type_bottomsheet, null)
                val mRecycler = view.findViewById<RecyclerView>(R.id.mTypeRec)
                val mBack = view.findViewById<ImageView>(R.id.mClose)
                val mDismiss = view.findViewById<TextView>(R.id.mDismiss)
                val cta = view.findViewById<TextView>(R.id.cta)

                mRecycler.layoutManager =
                    LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                mRecycler.layoutAnimation = null
                mRecycler.adapter = adapter
                adapter.addAll(idTypeList)
                bottomSheet.setCancelable(false)
                bottomSheet.setContentView(view)

                idTypeSpinner?.setOnClickListener {
                    bottomSheet.show()
                }

                mBack.setOnClickListener { bottomSheet.dismiss() }
                mDismiss.setOnClickListener { bottomSheet.dismiss() }
                cta.setOnClickListener { bottomSheet.dismiss() }
            }
        }

        viewModel.streetTypeData.observe(viewLifecycleOwner) { streetTypeData ->
            if (streetTypeData != null) {
                this.streetTypeList = streetTypeData
                val arryadapter =
                    ArrayAdapter(requireContext(), R.layout.drop_down_list, streetTypeList)
                sp_quata.adapter = arryadapter

                if (isEdit && naturalPersonDetails != null) {
                    naturalPersonDetails?.address?.streetType?.id?.let {
                        CatalogUtils.findPosById(
                            streetTypeList,
                            it
                        )
                    }?.let {
                        sp_quata?.setSelection(
                            it
                        )
                    }
                }
            }
        }

        viewModel.actionStream.observe(viewLifecycleOwner) {
            when (it) {
                is AddNewNaturalPersonViewModel.ACTION.ShowDatePicker -> showDatePicker(
                    it.view,
                    it.dateInMillis
                )
                is AddNewNaturalPersonViewModel.ACTION.OnDeleteSuccess -> onDeleteSuccess(it.attachType)
                is AddNewNaturalPersonViewModel.ACTION.OnUploadSuccess -> onUploadSuccess(
                    it.attachType,
                    it.attachment
                )
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

                /*dobs.setEndIconDrawable(R.drawable.ic_calendar_visible)
                dobs.endIconMode = TextInputLayout.END_ICON_CUSTOM*/

                viewModel.onDatePicked(
                    view, Calendar.getInstance().apply {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, monthOfYear)
                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    }.timeInMillis
                )
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
        )

        if (view == dob || view == drivLicenseIssueDate)
            dpd?.datePicker?.maxDate = System.currentTimeMillis()
        else {
            dpd?.datePicker?.minDate = System.currentTimeMillis() + (1000 * 24 * 60 * 60)
        }
        dpd?.show()
    }

    override fun onDrivingCategoriesUpdated(drivingCategories: ArrayList<CatalogItem>) {
        drivingCat = drivingCategories
        drivLicenseCateg.setText(drivingCategories.joinToString(", "))
    }

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
                    lblCertificate.visibility = View.VISIBLE
                    lblCertificate.text = requireContext().resources.getString(R.string.uploading_)
                    btnDeleteCertificate.visibility = View.VISIBLE
                    val dlFile = File(selectedFile)
                    naturalPersonDetails?.id?.let {
                        progressbar?.showPopup()
                        viewModel.onAttach(
                            it,
                            "DRIVING_LICENSE",
                            dlFile
                        )
                    }
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
                    lblId.visibility = View.VISIBLE
                    lblId.text = requireContext().resources.getString(R.string.uploading_)
                    btnDeleteId.visibility = View.VISIBLE
                    val idFile = File(selectedFile)
                    naturalPersonDetails?.id?.let {
                        progressbar?.showPopup()
                        viewModel.onAttach(
                            it,
                            "IDENTITY_DOCUMENT",
                            idFile
                        )
                    }
                }
            }
        }

    }

    private fun onDeleteSuccess(attachType: String) {
        if (attachType == "DRIVING_LICENSE") {
            lblCertificate.visibility = View.GONE
            btnDeleteCertificate.visibility = View.GONE
            iv_doc.setImageDrawable(requireContext().resources.getDrawable(R.drawable.ic_document_upload))
        } else {
            lblId.visibility = View.GONE
            btnDeleteId.visibility = View.GONE
            iv_cnp.setImageDrawable(requireContext().resources.getDrawable(R.drawable.ic_document_upload))
        }
    }

    private fun onUploadSuccess(attachType: String, attachments: Attachments) {
        progressbar?.dismissPopup()
        if (attachType == "DRIVING_LICENSE") {
            lblCertificate.visibility = View.VISIBLE
            btnDeleteCertificate.visibility = View.VISIBLE
            lblCertificate.text = attachments.name
            iv_doc.setImageDrawable(requireContext().resources.getDrawable(R.drawable.ic_pdf_selected))
        } else {
            lblId.visibility = View.VISIBLE
            btnDeleteId.visibility = View.VISIBLE
            lblId.text = attachments.name
            iv_cnp.setImageDrawable(requireContext().resources.getDrawable(R.drawable.ic_pdf_selected))
        }
    }

    override fun OnCountryPick(item: Country, flagDrawableResId: String) {
        this.countryItem = item
        countryNameTV.text = item.name
        cuntryFlagIV.text = flagDrawableResId
        changeCountryState(item.code)
    }

    private fun changeCountryState(code: String) {
        if (code == "ROU") {
            spinnerCounty.isVisible = true
            spinnerLocality.isVisible = true
            stateProvinceLabel.isVisible = false
            localityAreaLabel.isVisible = false
        } else {
            spinnerCounty.isVisible = false
            spinnerLocality.isVisible = false
            stateProvinceLabel.isVisible = true
            localityAreaLabel.isVisible = true
        }
    }

    override fun pickCountry(countries: PhoneCodeModel) {
        phoneCode.text = "+ ${countries.value}"
        selectedPhoneCode = countries.key
        phoneFlag.text =
            CountryCityUtils.getFlagId(
                CountryCityUtils.firstTwo(
                    countries.key?.lowercase(Locale.getDefault()).toString()
                ).toString()
            )
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

    private fun verificationForRca() {
        val warningsItemList = verifyNaturalItem?.validationResult?.warnings
        if (warningsItemList?.size != 0) {
            if (!warningsItemList.isNullOrEmpty()) {
                for (i in warningsItemList.indices) {
                    when (warningsItemList[i]?.field) {
                        "firstName" ->
                            changeHint(fNameLabel, resources.getString(R.string.first_name_cc))
                        "lastName" ->
                            changeHint(lNameLabel, resources.getString(R.string.last_name_cc))
                        "cnp" -> {
                            changeHint(cnpLabel, resources.getString(R.string.profile_cnp_cc))
                            identity_hidden_view.isVisible = true
                        }
                        "dateOfBirth" -> {
                            changeHint(
                                dobLabel,
                                resources.getString(R.string.natural_date_of_birth_cc)
                            )
                            personalInfoView2.isVisible = true
                        }
                        "identityDocument.series" -> {
                            changeHint(seriesLabel, resources.getString(R.string.profile_series_cc))
                            identity_hidden_view.isVisible = true
                        }
                        "identityDocument.number" -> {
                            changeHint(
                                idNumberLabel,
                                resources.getString(R.string.profile_number_cc)
                            )
                            identity_hidden_view.isVisible = true
                        }
                        "identityDocument.expirationDate" -> {
                            changeHint(
                                idExpDateLabel,
                                resources.getString(R.string.profile_exp_date_cc)
                            )
                            identity_hidden_view.isVisible = true
                        }
                        "identityDocument.documentType", "identityDocument.documentType.id", "identityDocument.documentType.name" -> {
                            changeHint(idTypeLabel, resources.getString(R.string.id_type_cc))
                            identity_hidden_view.isVisible = true
                        }
                        "drivingLicense.licenseId" -> {
                            changeHint(licenseNoLabel, resources.getString(R.string.license_no_cc))
                            license_hidden_view.isVisible = true
                        }
                        "drivingLicense.issueDate" -> {
                            changeHint(
                                dlIssueDateLabel,
                                resources.getString(R.string.d_license_issue_cc)
                            )
                            license_hidden_view.isVisible = true
                        }
                        "drivingLicense.expirationDate" -> {
                            changeHint(
                                dlExpDateLabel,
                                resources.getString(R.string.d_license_exp_cc)
                            )
                            license_hidden_view.isVisible = true
                        }
                        "drivingLicense.vehicleCategories" -> {
                            changeHint(
                                drivingCatLabel,
                                resources.getString(R.string.d_license_cat_cc)
                            )
                            license_hidden_view.isVisible = true
                        }
                        "phone" -> {
                            changeHint(phoneLabel, resources.getString(R.string.phone_num_cc))
                            personalInfoView2.isVisible = true
                        }
                        "phoneCountryCode" -> {
                            phoneCountryCodeLabel.text =
                                requireContext().resources.getString(R.string.country_cc)
                            personalInfoView2.isVisible = true
                        }
                        "email" -> {
                            changeHint(emailLabel, resources.getString(R.string.email_hint_cc))
                            personalInfoView2.isVisible = true
                        }
                        "address.countryCode" -> {
//                            countryCodeLabel.text =
//                                requireContext().resources.getString(R.string.country_cc)
                            changeTextViewHint(
                                countryCodeLabel,
                                resources.getString(R.string.country_cc)
                            )
                            address_hidden_view.isVisible = true
                        }
                        "address.region" -> {
                            changeHint(
                                spinnerCounty,
                                resources.getString(R.string.address_county_cc)
                            )
                            address_hidden_view.isVisible = true
                        }
                        "address.locality", "address.sirutaCode" -> {
                            changeHint(
                                spinnerLocality,
                                resources.getString(R.string.address_city_cc)
                            )
                            address_hidden_view.isVisible = true
                        }
                        "address.streetType", "address.streetType.id", "address.streetType.name" -> {
//                            streetTypeLabel.text =
//                                requireContext().resources.getString(R.string.street_type_cc)
                            changeTextViewHint(
                                streetTypeLabel,
                                resources.getString(R.string.street_type_cc)
                            )
                            address_hidden_view.isVisible = true
                        }
                        "address.streetName" -> {
                            changeHint(
                                streetNameLabel,
                                resources.getString(R.string.address_street_name_cc)
                            )
                            address_hidden_view.isVisible = true
                        }
                        "address.block" -> {
                            changeHint(blockLabel, resources.getString(R.string.block_cc))
                            address_hidden_view.isVisible = true
                        }
                        "address.entrance" -> {
                            changeHint(entranceLabel, resources.getString(R.string.entrance_cc))
                            address_hidden_view.isVisible = true
                        }
                        "address.floor" -> {
                            changeHint(floorLabel, resources.getString(R.string.floor_cc))
                            address_hidden_view.isVisible = true
                        }
                        "address.apartment" -> {
                            changeHint(apartmentLabel, resources.getString(R.string.apartment_cc))
                            address_hidden_view.isVisible = true
                        }
                        "address.zipCode" -> {
                            changeHint(zipCodeLabel, resources.getString(R.string.zip_code_cc))
                            address_hidden_view.isVisible = true
                        }
                        "address.buildingNo" -> {
                            changeHint(
                                buildingNoLabel,
                                resources.getString(R.string.profile_number_cc)
                            )
                            address_hidden_view.isVisible = true
                        }
                    }
                }
                personalInfoSection()
                idSection()
                drivingLicenseSection()
                addressSection()
            }
        }
    }

    private fun changeHint(tvLayout: TextInputLayout, str: String) {
        tvLayout.hint = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(
                str,
                Html.FROM_HTML_MODE_COMPACT
            )
        } else {
            Html.fromHtml(str)
        }
    }

    private fun changeTextViewHint(tvLayout: TextView, str: String) {
        tvLayout.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(
                str,
                Html.FROM_HTML_MODE_COMPACT
            )
        } else {
            Html.fromHtml(str)
        }
    }

    private fun personalInfoSection() {
        if (personalInfoView2.visibility == View.VISIBLE) {
            personal_info_lay.setBackgroundColor(resources.getColor(R.color.white))
            p_arrow.setImageResource(R.drawable.ic_arrow_circle_down)
        } else {
            personal_info_lay.setBackgroundColor(resources.getColor(R.color.expande_colore))
            p_arrow.setImageResource(R.drawable.ic_arrow_circle_up)
        }
    }

    private fun idSection() {
        if (identity_hidden_view.visibility == View.VISIBLE) {
            identity_lay.setBackgroundColor(resources.getColor(R.color.white))
            id_arrow.setImageResource(R.drawable.ic_arrow_circle_down)
        } else {
            identity_lay.setBackgroundColor(resources.getColor(R.color.expande_colore))
            id_arrow.setImageResource(R.drawable.ic_arrow_circle_up)
        }
    }

    private fun drivingLicenseSection() {
        if (license_hidden_view.visibility == View.VISIBLE) {
            license_lay.setBackgroundColor(resources.getColor(R.color.white))
            license_arrow.setImageResource(R.drawable.ic_arrow_circle_down)
        } else {
            license_lay.setBackgroundColor(resources.getColor(R.color.expande_colore))
            license_arrow.setImageResource(R.drawable.ic_arrow_circle_up)
        }
    }

    private fun addressSection() {
        if (address_hidden_view.visibility == View.VISIBLE) {
            fullAddress_lay.setBackgroundColor(resources.getColor(R.color.white))
            adds_arrow.setImageResource(R.drawable.ic_arrow_circle_down)
        } else {
            fullAddress_lay.setBackgroundColor(resources.getColor(R.color.expande_colore))
            adds_arrow.setImageResource(R.drawable.ic_arrow_circle_up)
        }
    }

    private fun verifyRcaFieldOnComplete(): Boolean {
        var fieldComplete = true
        val warningsItemList = verifyNaturalItem?.validationResult?.warnings
        if (warningsItemList?.size != 0) {
            if (!warningsItemList.isNullOrEmpty()) {
                for (i in warningsItemList.indices) {
                    when (warningsItemList[i]?.field) {
                        "firstName" -> {
                            if (firstName.text.isNullOrEmpty())
                                fieldComplete = false
                        }
                        "lastName" -> {
                            if (lastName.text.isNullOrEmpty())
                                fieldComplete = false
                        }
                        "cnp" -> {
                            if (cnp.text.isNullOrEmpty()) {
                                fieldComplete = false
                                identity_hidden_view.isVisible = true
                            }
                        }
                        "dateOfBirth" -> {
                            if (dob.text.isNullOrEmpty()) {
                                fieldComplete = false
                                personalInfoView2.isVisible = true
                            }
                        }
                        "identityDocument.series" -> {
                            if (series.text.isNullOrEmpty()) {
                                fieldComplete = false
                                identity_hidden_view.isVisible = true
                            }
                        }
                        "identityDocument.number" -> {
                            if (number.text.isNullOrEmpty()) {
                                fieldComplete = false
                                identity_hidden_view.isVisible = true
                            }
                        }
                        "identityDocument.expirationDate" -> {
                            if (expdate.text.isNullOrEmpty()) {
                                fieldComplete = false
                                identity_hidden_view.isVisible = true
                            }
                        }
                        "identityDocument.documentType", "identityDocument.documentType.id", "identityDocument.documentType.name" -> {
                            if (idTypeSpinner.text.isNullOrEmpty()) {
                                fieldComplete = false
                                identity_hidden_view.isVisible = true
                            }
                        }
                        "drivingLicense.licenseId" -> {
                            if (drivLicenseId.text.isNullOrEmpty()) {
                                fieldComplete = false
                                license_hidden_view.isVisible = true
                            }
                        }
                        "drivingLicense.issueDate" -> {
                            if (drivLicenseIssueDate.text.isNullOrEmpty()) {
                                fieldComplete = false
                                license_hidden_view.isVisible = true
                            }
                        }
                        "drivingLicense.expirationDate" -> {
                            if (drivLicenseExpDate.text.isNullOrEmpty()) {
                                fieldComplete = false
                                license_hidden_view.isVisible = true
                            }
                        }
                        "drivingLicense.vehicleCategories" -> {
                            if (drivLicenseCateg.text.isNullOrEmpty()) {
                                fieldComplete = false
                                license_hidden_view.isVisible = true
                            }
                        }
                        "phone" -> {
                            if (phone.text.isNullOrEmpty()) {
                                fieldComplete = false
                                personalInfoView2.isVisible = true
                            }
                        }
                        "phoneCountryCode" -> {
                            if (phoneCode.text.isNullOrEmpty()) {
                                fieldComplete = false
                                personalInfoView2.isVisible = true
                            }
                        }
                        "email" -> {
                            if (email.text.isNullOrEmpty()) {
                                fieldComplete = false
                                personalInfoView2.isVisible = true
                            }
                        }
                        "address.countryCode" -> {
                            if (countryNameTV.text.isNullOrEmpty()) {
                                fieldComplete = false
                                address_hidden_view.isVisible = true
                            }
                        }
                        "address.region" -> {
                            if (countySpinnerText.text.isNullOrEmpty()) {
                                fieldComplete = false
                                address_hidden_view.isVisible = true
                            }
                        }
                        "address.locality", "address.sirutaCode" -> {
                            if (localitySpinnerText.text.isNullOrEmpty()) {
                                fieldComplete = false
                                address_hidden_view.isVisible = true
                            }
                        }
                        "address.streetType", "address.streetType.id", "address.streetType.name" -> {
                            if (sp_quata.selectedItemPosition < 0) {
                                fieldComplete = false
                                address_hidden_view.isVisible = true
                            }
                        }
                        "address.streetName" -> {
                            if (streetName.text.isNullOrEmpty()) {
                                fieldComplete = false
                                address_hidden_view.isVisible = true
                            }
                        }
                        "address.block" -> {
                            if (blockName.text.isNullOrEmpty()) {
                                fieldComplete = false
                                address_hidden_view.isVisible = true
                            }
                        }
                        "address.entrance" -> {
                            if (entrance.text.isNullOrEmpty()) {
                                fieldComplete = false
                                address_hidden_view.isVisible = true
                            }
                        }
                        "address.floor" -> {
                            if (floor.text.isNullOrEmpty()) {
                                fieldComplete = false
                                address_hidden_view.isVisible = true
                            }
                        }
                        "address.apartment" -> {
                            if (apartment.text.isNullOrEmpty()) {
                                fieldComplete = false
                                address_hidden_view.isVisible = true
                            }
                        }
                        "address.zipCode" -> {
                            if (zipCode.text.isNullOrEmpty()) {
                                fieldComplete = false
                                address_hidden_view.isVisible = true
                            }
                        }
                        "address.buildingNo" -> {
                            if (buildingNo.text.isNullOrEmpty()) {
                                fieldComplete = false
                                address_hidden_view.isVisible = true
                            }
                        }
                    }
                }
                personalInfoSection()
                idSection()
                drivingLicenseSection()
                addressSection()
            }
        }
        return fieldComplete
    }

}