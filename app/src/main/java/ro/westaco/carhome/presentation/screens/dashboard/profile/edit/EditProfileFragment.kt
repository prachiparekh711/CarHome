package ro.westaco.carhome.presentation.screens.dashboard.profile.edit

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.android.synthetic.main.fragment_edit_profile.apartment
import kotlinx.android.synthetic.main.fragment_edit_profile.buildingNo
import kotlinx.android.synthetic.main.fragment_edit_profile.entrance
import kotlinx.android.synthetic.main.fragment_edit_profile.floor
import kotlinx.android.synthetic.main.fragment_edit_profile.streetName
import kotlinx.android.synthetic.main.fragment_edit_profile.zipCode
import org.json.JSONObject
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.requests.Address
import ro.westaco.carhome.data.sources.remote.requests.PhoneCodeModel
import ro.westaco.carhome.data.sources.remote.responses.models.*
import ro.westaco.carhome.di.ApiModule
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.common.DeleteDialogFragment
import ro.westaco.carhome.presentation.interfaceitem.CountyListClick
import ro.westaco.carhome.presentation.screens.dashboard.profile.occupation.OccupationDialogFragment
import ro.westaco.carhome.presentation.screens.data.commen.CodeDialog
import ro.westaco.carhome.presentation.screens.data.commen.CountryCodeDialog
import ro.westaco.carhome.presentation.screens.data.commen.CountyAdapter
import ro.westaco.carhome.presentation.screens.data.commen.LocalityAdapter
import ro.westaco.carhome.presentation.screens.data.person_natural.driving_categories.DrivingCategoriesDialogFragment
import ro.westaco.carhome.presentation.screens.home.PdfActivity
import ro.westaco.carhome.utils.*
import ro.westaco.carhome.utils.DialogUtils.Companion.showErrorInfo
import ro.westaco.carhome.utils.SirutaUtil.Companion.defaultCity
import ro.westaco.carhome.utils.SirutaUtil.Companion.defaultCounty
import ro.westaco.carhome.utils.SirutaUtil.Companion.fetchCountyPosition
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


//C- Edit Profile
@AndroidEntryPoint
class EditProfileFragment : BaseFragment<EditProfileDetailsViewModel>(),
    DrivingCategoriesDialogFragment.OnDialogInteractionListener,
    OccupationDialogFragment.OnDialogInteractionListener,
    CountryCodeDialog.CountryCodePicker,
    CodeDialog.CountyPickerItems {

    private var profileItem: ProfileItem? = null
    var dlAttachment: Attachments? = null
    var idAttachment: Attachments? = null
    var dialogAddress: BottomSheetDialog? = null

    var address: Address? = null
    var occupationItem: CatalogItem? = null
    var selectedPhoneCode: String? = null
    var typePos = 0
    var typeID = 0
    var countriesList: ArrayList<Country> = ArrayList()
    var drivingCat: ArrayList<CatalogItem> = ArrayList()
    var selectedDLCatList: ArrayList<Int> = ArrayList()
    var licenseCatList: ArrayList<CatalogItem> = ArrayList()
    var idTypeList: ArrayList<CatalogItem> = ArrayList()
    var occupationList: ArrayList<CatalogItem> = ArrayList()
    var streetTypeList: ArrayList<CatalogItem> = ArrayList()
    var sirutaList: ArrayList<Siruta> = ArrayList()
    var progressbar: Progressbar? = null
    var cityList: ArrayList<Siruta> = ArrayList()
    var countryItem: Country? = null
    var countyPosition: Int? = null
    var localityPosition: Int? = null
    var countyDialog: BottomSheetDialog? = null
    var localityDialog: BottomSheetDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            profileItem = it.getSerializable(ARG_PROFILE) as? ProfileItem?
        }
    }

    override fun onResume() {
        super.onResume()
        progressbar?.dismissPopup()
    }

    companion object {
        const val ARG_PROFILE = "arg_profile"
    }

    override fun getContentView() = R.layout.fragment_edit_profile

    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)

    override fun initUi() {
        progressbar = Progressbar(requireContext())


        name_lay_.setOnClickListener {

            if (li_name_h_.visibility == View.VISIBLE) {
                li_name_h_.visibility = View.GONE
                name_lay_.setBackgroundColor(resources.getColor(R.color.white))
                name_arrow_.setImageResource(R.drawable.ic_arrow_circle_down)
            } else {
                name_lay_.setBackgroundColor(resources.getColor(R.color.expande_colore))
                li_name_h_.visibility = View.VISIBLE
                name_arrow_.setImageResource(R.drawable.ic_arrow_circle_up)
            }
        }

        personal_info_lay_.setOnClickListener {
            if (p_hidden_view_.visibility == View.VISIBLE) {
                p_hidden_view_.visibility = View.GONE
                personal_info_lay_.setBackgroundColor(resources.getColor(R.color.white))
                p_arrow_.setImageResource(R.drawable.ic_arrow_circle_down)
            } else {
                personal_info_lay_.setBackgroundColor(resources.getColor(R.color.expande_colore))
                p_hidden_view_.visibility = View.VISIBLE
                p_arrow_.setImageResource(R.drawable.ic_arrow_circle_up)
            }
        }

        identity_lay_.setOnClickListener {
            if (identity_hidden_view_.visibility == View.VISIBLE) {
                identity_hidden_view_.visibility = View.GONE
                identity_lay_.setBackgroundColor(resources.getColor(R.color.white))
                id_arrow_.setImageResource(R.drawable.ic_arrow_circle_down)
            } else {
                identity_lay_.setBackgroundColor(resources.getColor(R.color.expande_colore))
                identity_hidden_view_.visibility = View.VISIBLE
                id_arrow_.setImageResource(R.drawable.ic_arrow_circle_up)
            }
        }

        license_lay_.setOnClickListener {
            if (license_hidden_view_.visibility == View.VISIBLE) {
                license_hidden_view_.visibility = View.GONE
                license_lay_.setBackgroundColor(resources.getColor(R.color.white))
                license_arrow_.setImageResource(R.drawable.ic_arrow_circle_down)
            } else {
                license_lay_.setBackgroundColor(resources.getColor(R.color.expande_colore))
                license_hidden_view_.visibility = View.VISIBLE
                license_arrow_.setImageResource(R.drawable.ic_arrow_circle_up)
            }
        }

        fullAddress_lay_.setOnClickListener {
            if (adds_hidden_view_.visibility == View.VISIBLE) {
                adds_hidden_view_.visibility = View.GONE
                fullAddress_lay_.setBackgroundColor(resources.getColor(R.color.white))
                adds_arrow_.setImageResource(R.drawable.ic_arrow_circle_down)
            } else {
                fullAddress_lay_.setBackgroundColor(resources.getColor(R.color.expande_colore))
                adds_hidden_view_.visibility = View.VISIBLE
                adds_arrow_.setImageResource(R.drawable.ic_arrow_circle_up)
            }
        }

        doc_fix_lay_.setOnClickListener {
            if (doc_hidden_view_.visibility == View.VISIBLE) {
                doc_hidden_view_.visibility = View.GONE
                doc_fix_lay_.setBackgroundColor(resources.getColor(R.color.white))
                doc_arrow_.setImageResource(R.drawable.ic_arrow_circle_down)
            } else {
                doc_fix_lay_.setBackgroundColor(resources.getColor(R.color.expande_colore))
                doc_hidden_view_.visibility = View.VISIBLE
                doc_arrow_.setImageResource(R.drawable.ic_arrow_circle_up)
            }
        }

        countySpinnerTextItems.setOnClickListener {
            openCountyDialog()
        }

        localitySpinnerTextItems.setOnClickListener {
            localityDialog?.show()
        }

        dialogAddress = BottomSheetDialog(requireContext())
        dialogAddress?.setCancelable(false)

        back.setOnClickListener {
            viewModel.onBack()
        }

        home.setOnClickListener {
            viewModel.onMain()
        }

        cta.setOnClickListener {

            if (lid.text?.isNotEmpty() == true) {

                when (countryItem?.code) {

                    "ROU" -> {
                        if (!RegexData.checkNumberPlateROU(lid.text.toString())) {
                            showErrorInfo(
                                requireContext(),
                                getString(R.string.license_error)
                            )
                            return@setOnClickListener
                        }
                    }

                    "QAT" -> {
                        if (!RegexData.checkNumberPlateQAT(lid.text.toString())) {
                            showErrorInfo(
                                requireContext(),
                                getString(R.string.license_error)
                            )
                            return@setOnClickListener
                        }
                    }

                    "UKR" -> {
                        if (!RegexData.checkNumberPlateUKR(lid.text.toString())) {
                            showErrorInfo(
                                requireContext(),
                                getString(R.string.license_error)
                            )
                            return@setOnClickListener
                        }
                    }

                    "BGR" -> {
                        if (!RegexData.checkNumberPlateBGR(lid.text.toString())) {
                            showErrorInfo(
                                requireContext(),
                                getString(R.string.license_error)
                            )
                            return@setOnClickListener
                        }
                    }

                }
            }

            if (cnp.text?.isNotEmpty() == true) {
                RegexData.checkCNPNumberIsValid(cnpNumber = cnp.text.toString())
            }

            val streetTypeItem =
                sp_quata_?.selectedItemPosition?.let { it1 -> streetTypeList[it1].id }
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
                    regionStr = countyPosition?.let { SirutaUtil.countyList[it].name }
                    sirutaCode = localityPosition?.let { cityList[it].code }
                    localityStr = localityPosition?.let { cityList[it].name }
                } else {
                    regionStr = defaultCounty?.name
                    sirutaCode = defaultCity?.code
                    localityStr = defaultCity?.name
                }
            } else {
                regionStr = stateProvinceTextItems.text.toString()
                sirutaCode = null
                localityStr = localityAreaTextItems.text.toString()
            }


            var addressItem: Address? = null
            addressItem = Address(
                zipCode = (zipCode?.text ?: "").toString(),
                streetType = streetTypeItem,
                sirutaCode = sirutaCode,
                locality = localityStr,
                streetName = (streetName?.text ?: "").toString(),
                addressDetail = null,
                buildingNo = (buildingNo?.text ?: "").toString(),
                countryCode = countryItem?.code,
                block = (blockName?.text ?: "").toString(),
                region = regionStr,
                entrance = (entrance?.text ?: "").toString(),
                floor = (floor?.text ?: "").toString(),
                apartment = (apartment?.text ?: "").toString()

            )

            val pos = selectedPhoneCode?.let { it1 ->
                Country.findPositionForTwoLetterCode(
                    countriesList,
                    it1
                )
            }

            var phoneCountryCode: String? = null
            if (pos != null)
                phoneCountryCode = countriesList[pos].code


            if (RegexData.checkCNPNumberIsValid(cnp.text.toString())) {

                viewModel.onCta(
                    lastName.text.toString(),
                    addressItem,
                    idTypeSpinner.text.toString(),
                    typeID,
                    series.text.toString(),
                    number.text.toString(),
                    expdate.text.toString(),
                    cnp.text.toString(),
                    "",
                    dob.text.toString(),
                    firstname.text.toString(),
                    occupationItem,
                    phone.text.toString(),
                    phoneCountryCode = phoneCountryCode,
                    lid.text.toString(),
                    dlIssue.text.toString(),
                    dlExpiry.text.toString(),
                    selectedDLCatList,
                    email.text.toString(),
                    check1.isChecked
                )

            } else {
                showErrorInfo(requireContext(), getString(R.string.reg_invalid_cnp))

            }


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
            email.setText(profileItem?.email)
            phone.setText(profileItem?.phone)
            if (profileItem?.dateOfBirth?.isNotEmpty() == true) {
                changeTint(dob)
                dob.setText(viewModel.convertFromServerDate(profileItem?.dateOfBirth))
            }

            //  (address) Object
            address = profileItem?.address
            streetName?.setText(profileItem?.address?.streetName)
            buildingNo?.setText(profileItem?.address?.buildingNo)
            blockName?.setText(profileItem?.address?.block)
            entrance?.setText(profileItem?.address?.entrance)
            floor?.setText(profileItem?.address?.floor)
            apartment?.setText(profileItem?.address?.apartment)
            zipCode?.setText(profileItem?.address?.zipCode)


            address?.countryCode?.let { changeCountryState(it) }
            if (address?.countryCode == "ROU") {
                countySpinnerTextItems.setText(address?.region)
                localitySpinnerTextItems.setText(address?.locality)
            } else {
                stateProvinceTextItems.setText(address?.region)
                localityAreaTextItems.setText(address?.locality)
            }

            countyPosition =
                address?.region?.let { fetchCountyPosition(it) }

            localityPosition =
                address?.locality?.let { fetchCountyPosition(it) }


            val identityDocument = profileItem?.identityDocument
            series.setText(identityDocument?.series)
            number.setText(identityDocument?.number)

            if (identityDocument?.expirationDate?.isNotEmpty() == true) {
                changeTint(expdate)
                expdate.setText(viewModel.convertFromServerDate(identityDocument.expirationDate))
            }

            cnp.setText(profileItem?.cnp)
            typeID = profileItem?.identityDocument?.documentType?.id ?: 0
            idTypeSpinner.setText(profileItem?.identityDocument?.documentType?.name)

            //  (drivingLicense) Object
            val drivingLicense = profileItem?.drivingLicense
            drivingLicense?.licenseId?.let { lid.setText(it) }
            drivingLicense?.issueDate?.let {
                changeTint(dlIssue)
                dlIssue.setText(viewModel.convertFromServerDate(it))
            }
            drivingLicense?.expirationDate?.let {
                changeTint(dlExpiry)
                dlExpiry.setText(viewModel.convertFromServerDate(it))
            }

            occupationItem = profileItem?.occupationCorIsco08

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

                val drivingLicenseDialog = DeleteDialogFragment()
                drivingLicenseDialog.layoutResId = R.layout.n_person_delete_doc
                drivingLicenseDialog.listener =
                    object : DeleteDialogFragment.OnDialogInteractionListener {
                        override fun onPosClicked() {

                            profileItem?.drivingLicenseAttachment?.id?.let { it1 ->
                                profileItem?.id?.let { it2 ->
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

                        profileItem?.identityDocumentAttachment?.id?.let { it1 ->
                            profileItem?.id?.let { it2 ->
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

            dlAttachment = profileItem?.drivingLicenseAttachment
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
            idAttachment = profileItem?.identityDocumentAttachment
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

        } else {

            changeCountryState("ROU")
            countySpinnerTextItems.setText(defaultCounty?.name)
            localitySpinnerTextItems.setText(defaultCity?.name)

            countyPosition = defaultCounty?.name?.let { fetchCountyPosition(it) }
            localityPosition = defaultCity?.name?.let { fetchCountyPosition(it) }

        }

        /**
         * Document
         */
        /* llUploadCertificate.setOnClickListener {
             val result = FileUtil.checkPermission(requireContext())
             if (result) {
                 callFileManagerForLicense()
             } else {
                 FileUtil.requestPermission(requireActivity())
             }

         }*/
        /**
         * Document
         */
        /* llUploadId.setOnClickListener {
             val result = FileUtil.checkPermission(requireContext())
             if (result) {
                 callFileManagerForID()
             } else {
                 FileUtil.requestPermission(requireActivity())
             }
         }*/

        /**
         * Document
         */
        /* btnDeleteCertificate.setOnClickListener {

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


         }*/

        /*btnDeleteId.setOnClickListener {

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

        }*/

        lblCertificate.setOnClickListener {
            dlAttachment?.href?.let { it1 ->
                progressbar?.showPopup()
                val url = ApiModule.BASE_URL_RESOURCES + it1
                val intent = Intent(requireContext(), PdfActivity::class.java)
                intent.putExtra(PdfActivity.ARG_DATA, url)
                intent.putExtra(PdfActivity.ARG_FROM, "DOCUMENT")
                requireContext().startActivity(intent)
            }
        }

        lblId.setOnClickListener {
            idAttachment?.href?.let { it1 ->
                progressbar?.showPopup()
                val url = ApiModule.BASE_URL_RESOURCES + it1
                val intent = Intent(requireContext(), PdfActivity::class.java)
                intent.putExtra(PdfActivity.ARG_DATA, url)
                intent.putExtra(PdfActivity.ARG_FROM, "DOCUMENT")
                requireContext().startActivity(intent)
            }
        }

    }

    private fun dateToMilis(str: String): Long {
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

            if (countryData != null) {
                this.countriesList = countryData
                li_dialog.setOnClickListener {
                    val countryCodeDialog =
                        CountryCodeDialog(requireActivity(), countriesList, this)
                    countryCodeDialog.show(requireActivity().supportFragmentManager, null)
                }

                if (profileItem != null) {
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
            progressbar?.dismissPopup()

        }

        viewModel.sirutaData.observe(viewLifecycleOwner) { sirutaData ->
            if (sirutaData != null) {
                this.sirutaList = sirutaData

                sirutaList.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
                address = profileItem?.address

                val countyList: ArrayList<Siruta> = ArrayList()
                for (i in sirutaList.indices) {
                    if (sirutaList[i].parentCode == null)
                        countyList.add(sirutaList[i])
                }
                countyList.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })

            }
            progressbar?.dismissPopup()
        }

        viewModel.occupationData.observe(viewLifecycleOwner) { occupationData ->
            if (occupationData != null) {
                this.occupationList = occupationData

            }
        }

        viewModel.licenseCategoryData.observe(viewLifecycleOwner) { licenseCategoryData ->
            selectedDLCatList.clear()
            if (licenseCategoryData != null) {
                this.licenseCatList = licenseCategoryData

                selectedDLCatList = profileItem?.drivingLicense?.vehicleCategories as ArrayList<Int>
                licenseCategory.setOnClickListener {
                    val dialog = DrivingCategoriesDialogFragment()
                    dialog.listener = this
                    dialog.catalogList = licenseCatList
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
                    categorylist.sort()
                    categorylist.let { licenseCategory.setText(it.joinToString(", ")) }
                }
            }
        }

        viewModel.idTypeData.observe(viewLifecycleOwner) { idTypeData ->
            if (idTypeData != null) {
                this.idTypeList = idTypeData

                val dialog = BottomSheetDialog(requireContext())

                for (i in idTypeList.indices) {

                    if (idTypeList[i].id.toInt() == profileItem?.identityDocument?.documentType?.id) {
                        typePos = i
                        idTypeSpinner.setText(idTypeList[i].toString())
                    }
                }

                val typeInterface = object : IDTypeAdapter.TypeInterface {
                    override fun onSelection(model: Int, id: Int) {
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
                    idTypeSpinner.setText(idTypeList[typePos].toString())
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
        }

        viewModel.streetTypeData.observe(viewLifecycleOwner) { streetTypeData ->

            if (streetTypeData != null) {
                this.streetTypeList = streetTypeData
                val arryadapter =
                    ArrayAdapter(requireContext(), R.layout.drop_down_list, streetTypeList)
                sp_quata_.adapter = arryadapter

                if (profileItem != null) {
                    profileItem?.address?.streetType?.id?.let {
                        CatalogUtils.findPosById(
                            streetTypeList,
                            it
                        )
                    }?.let {
                        sp_quata_?.setSelection(
                            it
                        )
                    }
                }
            }
        }

        viewModel.actionStream.observe(viewLifecycleOwner) {
            when (it) {
                is EditProfileDetailsViewModel.ACTION.ShowDatePicker -> showDatePicker(
                    it.view,
                    it.dateInMillis
                )
                is EditProfileDetailsViewModel.ACTION.OnDeleteSuccess -> onDeleteSuccess(it.attachType)

                is EditProfileDetailsViewModel.ACTION.OnUploadSuccess -> onUploadSuccess(
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
        licenseCategory.setText(drivingCategories.joinToString(", "))
    }

    var values: ContentValues? = null

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
                    btnDeleteCertificate.visibility = View.VISIBLE
                    val dlFile = File(selectedFile)
                    profileItem?.id?.let {
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
                    btnDeleteId.visibility = View.VISIBLE
                    val idFile = File(selectedFile)
                    profileItem?.id?.let {
                        viewModel.onAttach(
                            it,
                            "IDENTITY_DOCUMENT",
                            idFile
                        )
                    }
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
            200 -> if (grantResults.isNotEmpty()) {
                val readExternalStorage =
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                val writeExternalStorage =
                    grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (readExternalStorage && writeExternalStorage) {
                    // perform action when allow permission success
                } else {
                    showErrorInfo(requireContext(), getString(R.string.allow_permission))

                }
            }
        }
    }

    private fun setPhoneCountryData() {

        val phoneModelList: ArrayList<PhoneCodeModel> = ArrayList()
        val obj = FileUtil.loadJSONFromAsset(requireContext())?.let { JSONObject(it) }
        var phoneCountryItem: Country? = null
        if (profileItem != null) {
            val pos = profileItem?.phoneCountryCode?.let { it1 ->
                Country.findPositionForCode(
                    countriesList,
                    it1
                )
            }
            if (pos != null)
                phoneCountryItem = countriesList[pos]
        }

        var romanCode: PhoneCodeModel? = null
        obj?.keys()?.forEach { key ->
            val keyStr = key as String
            val keyValue = obj.get(keyStr)
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

        phoneCodeItems.text = "+ ${romanCode?.value}"
        selectedPhoneCode = romanCode?.key
        phoneFlagItems.text =
            CountryCityUtils.getFlagId(
                CountryCityUtils.firstTwo(
                    romanCode?.key?.lowercase(Locale.getDefault()).toString()
                ).toString()
            )

        val phoneCodeDialog = CodeDialog(requireActivity(), phoneModelList, this)

        cc_dialog_items.setOnClickListener {
            phoneCodeDialog.show(requireActivity().supportFragmentManager, null)
        }
    }

    override fun onOccupationUpdated(occupationItem1: CatalogItem) {
        this.occupationItem = occupationItem1
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

    override fun pickCountry(countries: PhoneCodeModel) {
        phoneCodeItems.text = "+ ${countries.value}"
        selectedPhoneCode = countries.key
        phoneFlagItems.text =
            CountryCityUtils.getFlagId(
                CountryCityUtils.firstTwo(
                    countries.key?.lowercase(Locale.getDefault()).toString()
                ).toString()
            )
    }

    override fun OnCountryPick(item: Country, flagDrawableResId: String) {
        this.countryItem = item
        countryNameTV.text = item.name
        cuntryFlagIV.text = flagDrawableResId
        changeCountryState(item.code)
    }

    private fun changeCountryState(code: String) {
        if (code == "ROU") {
            spinnerCountyItems.isVisible = true
            spinnerLocalityItems.isVisible = true
            stateProvinceLabelItems.isVisible = false
            localityAreaLabelItems.isVisible = false
        } else {
            spinnerCountyItems.isVisible = false
            spinnerLocalityItems.isVisible = false
            stateProvinceLabelItems.isVisible = true
            localityAreaLabelItems.isVisible = true
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

        if (SirutaUtil.countyList.isNotEmpty()) {

            val adapter =
                CountyAdapter(
                    requireContext(),
                    SirutaUtil.countyList,
                    countyCode = SirutaUtil.fetchCounty(countySpinnerTextItems.text.toString())?.code
                        ?: SirutaUtil.countyList[0].code,
                    countyListClick = object :
                        CountyListClick {
                        override fun click(position: Int, siruta: Siruta) {
                            countyPosition = position
                            countySpinnerTextItems.setText(siruta.name)
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


        localitySpinnerTextItems.setText(cityList[0].name)
        val adapter = LocalityAdapter(
            requireContext(),
            cityList,
            localityListClick = object : LocalityAdapter.LocalityListClick {
                override fun localityclick(position: Int, siruta: Siruta) {
                    localityPosition = position
                    localitySpinnerTextItems.setText(siruta.name)
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

    fun changeTint(view: TextView) {
        TextViewCompat.setCompoundDrawableTintList(
            view,
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.appPrimary
                )
            )
        )
    }

}