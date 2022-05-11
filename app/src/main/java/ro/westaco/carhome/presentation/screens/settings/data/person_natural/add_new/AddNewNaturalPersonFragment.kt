package ro.westaco.carhome.presentation.screens.settings.data.person_natural.add_new

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_add_new_natural_person.*
import kotlinx.android.synthetic.main.fragment_add_new_natural_person.apartment
import kotlinx.android.synthetic.main.fragment_add_new_natural_person.buildingNo
import kotlinx.android.synthetic.main.fragment_add_new_natural_person.entrance
import kotlinx.android.synthetic.main.fragment_add_new_natural_person.floor
import kotlinx.android.synthetic.main.fragment_add_new_natural_person.streetName
import kotlinx.android.synthetic.main.fragment_add_new_natural_person.zipCode
import okhttp3.ResponseBody
import org.json.JSONObject
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.requests.Address
import ro.westaco.carhome.data.sources.remote.responses.models.*
import ro.westaco.carhome.di.ApiModule
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.common.DeleteDialogFragment
import ro.westaco.carhome.presentation.screens.dashboard.profile.edit.IDTypeAdapter
import ro.westaco.carhome.presentation.screens.settings.data.person_natural.driving_categories.DrivingCategoriesDialogFragment
import ro.westaco.carhome.utils.CatalogUtils
import ro.westaco.carhome.utils.CountryCityUtils
import ro.westaco.carhome.utils.FileUtil
import ro.westaco.carhome.utils.Progressbar
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

//C- Rebuilt Section
@AndroidEntryPoint
class AddNewNaturalPersonFragment : BaseFragment<AddNewNaturalPersonViewModel>(),
    DrivingCategoriesDialogFragment.OnDialogInteractionListener {
    private var isEdit = false
    private var naturalPersonDetails: NaturalPersonDetails? = null

    var dlAttachment: Attachments? = null
    var idAttachment: Attachments? = null
    var countydialog: BottomSheetDialog? = null
    var locality: BottomSheetDialog? = null

    var address: Address? = null
    var typePos = 0
    var typeID = 0
    var drivingCat: ArrayList<CatalogItem> = ArrayList()
    var progressbar: Progressbar? = null
    var countyPosition = 0
    var localityPosition = 0
    var countriesList: ArrayList<Country> = ArrayList()
    var sirutaList: ArrayList<Siruta> = ArrayList()
    var streetTypeList: ArrayList<CatalogItem> = ArrayList()
    var licenseCatList: ArrayList<CatalogItem> = ArrayList()
    var idTypeList: ArrayList<CatalogItem> = ArrayList()

    lateinit var bottomSheet: BottomSheetDialog

    override fun getContentView() = R.layout.fragment_add_new_natural_person

    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            isEdit = it.getBoolean(ARG_IS_EDIT)
            naturalPersonDetails = it.getSerializable(ARG_NATURAL_PERSON) as? NaturalPersonDetails?
        }
        bottomSheet = BottomSheetDialog(requireContext())
    }

    @SuppressLint("SetTextI18n")
    override fun initUi() {

        progressbar = Progressbar(requireContext())
        progressbar?.showPopup()

        contexts = requireContext()
        cna = cnmae
        flgs = flg

        cflgs = cc_flg
        ccna = cc_code

        in_filleds_county = in_filled_county
        in_filleds_locality = in_filled_locality
        spinnerCity1 = spinnerCity
        spinnerCounty1 = spinnerCounty

        val phoneModelList: ArrayList<PhoneCodeModel> = ArrayList()
        val obj = JSONObject(FileUtil.loadJSONFromAsset(requireContext()))

        var romanCode: PhoneCodeModel? = null

        for (key in obj.keys()) {
            val keyStr = key as String
            val keyvalue = obj[keyStr]
            val code = PhoneCodeModel(keyStr, keyvalue as String?)
            phoneModelList.add(code)

            if (code.key == "RO") {
                romanCode = code
            }
        }

        cc_code?.text = "+ ${romanCode?.value}"
        selectedPhoneCode = romanCode?.key
        romanCode?.key?.lowercase(Locale.getDefault())
            ?.let { CountryCityUtils.firstTwo(it).toString() }?.let {
                CountryCityUtils.getFlagDrawableResId(
                    it
                )
            }?.let {
                cc_flg.setImageResource(
                    it
                )
            }
        val phoneCodeDialog = CodeDialog(requireActivity(), phoneModelList)
        cc_dialog.setOnClickListener {
            phoneCodeDialog.show()
        }

        cancel.setOnClickListener {
            viewModel.onBack()
        }

        name_lay.setOnClickListener {

            if (li_name_h.visibility == View.VISIBLE) {

                /*TransitionManager.beginDelayedTransition(base_cardview)*/
                li_name_h.visibility = View.GONE
                name_lay.setBackgroundColor(resources.getColor(R.color.white))
                name_arrow.setImageResource(R.drawable.ic_arrow_circle_down)

            } else {

                /*TransitionManager.beginDelayedTransition(
                    base_cardview)*/
                name_lay.setBackgroundColor(resources.getColor(R.color.expande_colore))
                li_name_h.visibility = View.VISIBLE
                name_arrow.setImageResource(R.drawable.ic_arrow_circle_up)
            }
        }

        personal_info_lay.setOnClickListener {

            if (p_hidden_view.visibility == View.VISIBLE) {

                p_hidden_view.visibility = View.GONE
                personal_info_lay.setBackgroundColor(resources.getColor(R.color.white))
                p_arrow.setImageResource(R.drawable.ic_arrow_circle_down)


            } else {


                personal_info_lay.setBackgroundColor(resources.getColor(R.color.expande_colore))
                p_hidden_view.visibility = View.VISIBLE
                p_arrow.setImageResource(R.drawable.ic_arrow_circle_up)

            }
        }

        identity_lay.setOnClickListener {

            if (identity_hidden_view.visibility == View.VISIBLE) {

                identity_hidden_view.visibility = View.GONE
                identity_lay.setBackgroundColor(resources.getColor(R.color.white))
                id_arrow.setImageResource(R.drawable.ic_arrow_circle_down)


            } else {


                identity_lay.setBackgroundColor(resources.getColor(R.color.expande_colore))
                identity_hidden_view.visibility = View.VISIBLE
                id_arrow.setImageResource(R.drawable.ic_arrow_circle_up)

            }
        }

        license_lay.setOnClickListener {

            if (license_hidden_view.visibility == View.VISIBLE) {

                license_hidden_view.visibility = View.GONE
                license_lay.setBackgroundColor(resources.getColor(R.color.white))
                license_arrow.setImageResource(R.drawable.ic_arrow_circle_down)


            } else {


                license_lay.setBackgroundColor(resources.getColor(R.color.expande_colore))
                license_hidden_view.visibility = View.VISIBLE
                license_arrow.setImageResource(R.drawable.ic_arrow_circle_up)

            }
        }

        fullAddress_lay.setOnClickListener {


            if (adds_hidden_view.visibility == View.VISIBLE) {

                adds_hidden_view.visibility = View.GONE
                fullAddress_lay.setBackgroundColor(resources.getColor(R.color.white))
                adds_arrow.setImageResource(R.drawable.ic_arrow_circle_down)

            } else {

                fullAddress_lay.setBackgroundColor(resources.getColor(R.color.expande_colore))
                adds_hidden_view.visibility = View.VISIBLE
                adds_arrow.setImageResource(R.drawable.ic_arrow_circle_up)

            }
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

        toolbar.setNavigationOnClickListener {

            viewModel.onBack()

        }

        toolbar.setOnMenuItemClickListener {

            true
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

            var sirutaCode: Int? = null
            var locality: String? = null
            var region: String? = null


            val streetTypeItem =
                sp_quata?.selectedItemPosition?.let { it1 -> streetTypeList.get(it1).id }
                    ?.let { it2 ->
                        CatalogUtils.findById(
                            streetTypeList,
                            it2
                        )
                    }



            if (countryItem == null || countryItem?.code == "ROU") {
                for (i in countriesList.indices) {
                    if (countriesList[i].code == "ROU") {
                        countryItem = countriesList[i]
                    }
                }

                val cityList: ArrayList<Siruta> = ArrayList()


                if (cityList.isNotEmpty()) {

                    for (i in sirutaList.indices) {
                        if (sirutaList[i].parentCode == null)
                            cityList.add(sirutaList[i])
                    }
                    sirutaCode = cityList[countyPosition].code
                    val localityList: ArrayList<Siruta> = ArrayList()
                    for (i in sirutaList.indices) {
                        if (sirutaCode == sirutaList[i].parentCode) {
                            localityList.add(sirutaList[i])
                        }
                    }

                    region = null
                    locality = localityList[localityPosition].name
                }


            } else {
                sirutaCode = null
                region = filled_county.text.toString()
                locality = filled_locality.text.toString()
            }

            address = Address(
                zipCode = zipCode.text.toString(),
                streetType = streetTypeItem,
                sirutaCode = sirutaCode,
                locality = locality,
                streetName = streetName.text.toString(),
                addressDetail = null,
                buildingNo = buildingNo.text.toString(),
                countryCode = countryItem?.code,
                block = blockName.text.toString(),
                region = region,
                entrance = entrance.text.toString(),
                floor = floor.text.toString(),
                apartment = apartment.text.toString()
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

            viewModel.onSave(
                naturalPersonDetails?.id?.toLong(),
                lastName.text.toString(),
                address,
                idTypeSpinner.text.toString(),
                typeID,
                series.text.toString(),
                number.text.toString(),
                expdate.text.toString(),
                cnp.text.toString(),
                dob.text.toString(),
                firstName.text.toString(),
                phone.text.toString(),
                phoneCountryCode = phoneCountryCode,
                drivLicenseId.text.toString(),
                drivLicenseIssueDate.text.toString(),
                drivLicenseExpDate.text.toString(),
                drivingCat,
                email.text.toString(),
                check.isChecked,
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
            cc_code.text = naturalPersonDetails?.phoneCountryCode
            email.setText(naturalPersonDetails?.email)

            toolbar.title = getString(R.string.natural_pers_details)
            cta.text = getString(R.string.save_changes)

            streetName.setText(address?.streetName)
            buildingNo.setText(address?.buildingNo)
            blockName.setText(address?.block)
            entrance.setText(address?.entrance)
            apartment.setText(address?.apartment)
            floor.setText(address?.floor)
            zipCode.setText(address?.zipCode)

            localitys.setText(address?.locality)

            //  (identityDocument) Object
            val identityDocument = naturalPersonDetails?.identityDocument
            series.setText(identityDocument?.series)
            number.setText(identityDocument?.number)
            expdate.setText(viewModel.convertFromServerDate(identityDocument?.expirationDate))
            cnp.setText(naturalPersonDetails?.cnp)
            typeID = naturalPersonDetails?.identityDocument?.documentType?.id ?: 0

            cnp.setText(naturalPersonDetails?.cnp)


            naturalPersonDetails?.drivingLicense?.licenseId?.let { drivLicenseId.setText(it) }
            naturalPersonDetails?.drivingLicense?.issueDate?.let {
                drivLicenseIssueDate.setText(viewModel.convertFromServerDate(it))
            }
            naturalPersonDetails?.drivingLicense?.expirationDate?.let {
                drivLicenseExpDate.setText(viewModel.convertFromServerDate(it))
            }

            //  (occupationCorIsco08) Object
            /* occupationItem = naturalPersonDetails?.occupationCorIsco08
             occupation.text = occupationItem?.name ?: ""*/

            //  (drivingLicenseAttachment) Object
            dlAttachment = naturalPersonDetails?.drivingLicenseAttachment
            if (dlAttachment?.href.isNullOrEmpty()) {
                lblCertificate.visibility = View.GONE
                btnDeleteCertificate.visibility = View.GONE
            } else {
                lblCertificate.visibility = View.VISIBLE
                btnDeleteCertificate.visibility = View.VISIBLE
                lblCertificate.text = dlAttachment?.name
            }

            //  (identityDocumentAttachment) Object
            idAttachment = naturalPersonDetails?.identityDocumentAttachment
            if (idAttachment?.href.isNullOrEmpty()) {
                lblId.visibility = View.GONE
                btnDeleteId.visibility = View.GONE
            } else {
                lblId.visibility = View.VISIBLE
                btnDeleteId.visibility = View.VISIBLE
                lblId.text = idAttachment?.name
            }

            if (address?.countryCode == "ROU") {
                spinnerCity.isVisible = true
                spinnerCounty.isVisible = true
                in_filled_county.visibility = View.GONE
                in_filled_locality.visibility = View.GONE
            } else {
                spinnerCity.isVisible = false
                spinnerCounty.isVisible = false
                in_filled_county.visibility = View.VISIBLE
                in_filled_locality.visibility = View.VISIBLE
            }
        } else {
            spinnerCity.isVisible = true
            spinnerCounty.isVisible = true
            in_filled_county.visibility = View.GONE
            in_filled_locality.visibility = View.GONE
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

            /*naturalPersonDetails?.drivingLicenseAttachment?.id?.let { it1 ->
                naturalPersonDetails?.id?.let { it2 ->
                    viewModel.onDeleteAttachment(
                        it2,
                        it1,
                        requireContext().getString(R.string.attchment_dl)
                    )
                }
            }*/


            val DrivingLicenseDialog = DeleteDialogFragment()
            DrivingLicenseDialog.layoutResId = R.layout.n_person_delete_doc
            DrivingLicenseDialog.listener =
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
                        DrivingLicenseDialog.dismiss()
                    }
                }

            DrivingLicenseDialog.show(childFragmentManager, DeleteDialogFragment.TAG)

        }
        btnDeleteId.setOnClickListener {

            /*naturalPersonDetails?.identityDocumentAttachment?.id?.let { it1 ->
                naturalPersonDetails?.id?.let { it2 ->
                    viewModel.onDeleteAttachment(
                        it2,
                        it1,
                        requireContext().getString(R.string.attchment_id)
                    )
                }
            }*/

            val CNPDialog = DeleteDialogFragment()
            CNPDialog.layoutResId = R.layout.n_person_delete_doc_id
            CNPDialog.listener = object : DeleteDialogFragment.OnDialogInteractionListener {
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
        localitys?.setOnClickListener {
            locality?.show()
        }
    }


    private fun opencountydialog() {

        val cityList: ArrayList<Siruta> = ArrayList()

        val view = layoutInflater.inflate(R.layout.county_layout, null)
        countydialog = BottomSheetDialog(requireContext())
        countydialog?.setCancelable(true)
        countydialog?.setContentView(view)
        countydialog?.show()

        val rv_county: RecyclerView? = view.findViewById(R.id.rv_county)
        val etSearchTrain: EditText? = view.findViewById(R.id.etSearchTrain)
        val mClose: ImageView? = view.findViewById(R.id.mClose)
        rv_county?.layoutManager = LinearLayoutManager(requireContext())

        if (sirutaList.isNotEmpty()) {

            for (i in sirutaList.indices) {

                if (sirutaList[i].parentCode == null)

                    cityList.add(sirutaList[i])
            }

            cityList.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })

            val adapter = CountyAdapter(
                requireContext(),
                cityList,
                countyPosition = countyPosition,
                countyListClick = object :
                    CountyListClick {
                    override fun click(position: Int, code: Siruta) {
                        countyPosition = position
                        county.setText(code.name)
                        locality(code.code.toString())
                        countydialog?.dismiss()

                    }
                })
            rv_county?.adapter = adapter

            etSearchTrain?.addTextChangedListener(object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                @SuppressLint("NotifyDataSetChanged")
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    if (s.toString().isNotEmpty()) {

                        adapter.filter.filter(s.toString())

                    } else {


                    }

                }

                override fun afterTextChanged(s: Editable?) {

                }


            })

        }

        mClose?.setOnClickListener {

            countydialog?.dismiss()
        }

    }

    private fun locality(code: String) {

        val city = mutableListOf<Siruta>()

        val view = layoutInflater.inflate(R.layout.locality_layout, null)
        locality = BottomSheetDialog(requireContext())
        locality?.setCancelable(true)
        locality?.setContentView(view)

        val rv_locality: RecyclerView? = view.findViewById(R.id.rv_locality)
        val etSearchTrain: EditText? = view.findViewById(R.id.etSearchTrain)
        val mClose: ImageView? = view.findViewById(R.id.mClose)
        rv_locality?.layoutManager = LinearLayoutManager(requireContext())

        if (sirutaList.isNotEmpty()) {
            for (i in sirutaList.indices) {
                if (code == sirutaList[i].parentCode.toString()) {
                    city.add(sirutaList[i])
                }
            }


            city.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })

            val adapter = LocalityAdapter(
                requireContext(),
                city,
                localityListClick = object : LocalityAdapter.LocalityListClick {
                    override fun localityclick(position: Int, siruta: Siruta) {
                        localityPosition = position
                        localitys.setText(siruta.name)
                        locality?.dismiss()
                    }
                })
            rv_locality?.adapter = adapter

            etSearchTrain?.addTextChangedListener(object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                @SuppressLint("NotifyDataSetChanged")
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    if (s.toString().isNotEmpty()) {

                        adapter.filter.filter(s.toString())

                    } else {


                    }

                }

                override fun afterTextChanged(s: Editable?) {

                }

            })
        }


        mClose?.setOnClickListener {

            locality?.dismiss()
        }

    }

    @SuppressLint("SimpleDateFormat")
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


    private fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        bottomSheet.layoutParams = layoutParams
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

        viewModel.attachmentData.observe(viewLifecycleOwner) { attachmentData ->
            progressbar?.dismissPopup()

            /*      if (attachmentData != null) {
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

        viewModel.countryData.observe(viewLifecycleOwner) { countryData ->
            this.countriesList = countryData
            val countryCodeDialog = CountryCodeDialog(requireActivity(), countriesList, "Natural")
            li_dialog.setOnClickListener {
                countryCodeDialog.show()
            }

            if (isEdit && naturalPersonDetails != null) {
                for (i in countriesList.indices) {
                    if (countriesList[i].code == naturalPersonDetails?.address?.countryCode) {
                        cnmae.text = countriesList[i].name
                        flg.setImageResource(
                            CountryCityUtils.getFlagDrawableResId(
                                countriesList[i].twoLetterCode.lowercase(
                                    Locale.getDefault()
                                )
                            )
                        )
                    }
                }
            } else {
                for (i in countriesList.indices) {
                    if (countriesList[i].code == "ROU") {
                        cnmae.text = countriesList[i].name
                        flg.setImageResource(
                            CountryCityUtils.getFlagDrawableResId(
                                countriesList[i].twoLetterCode.lowercase(
                                    Locale.getDefault()
                                )
                            )
                        )
                    }
                }

            }
        }

        viewModel.sirutaData.observe(viewLifecycleOwner) { sirutaData ->

            this.sirutaList = sirutaData

            county.setOnClickListener {
                opencountydialog()
            }

            if (isEdit && naturalPersonDetails != null) {
                address = naturalPersonDetails?.address
                for (i in sirutaList.indices) {
                    if (sirutaList[i].code == address?.sirutaCode) {
                        county.setText(sirutaList[i].name)
                    }
                }

            } else {
                sirutaList.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
                var firstSirutaCode: Int? = null
                for (i in sirutaList.indices) {
                    if (sirutaList[i].parentCode == null) {
                        firstSirutaCode = sirutaList[i].code
                        county.setText(sirutaList[i].name)
                        break
                    }
                }

                val localityList: ArrayList<Siruta> = ArrayList()
                for (i in sirutaList.indices) {
                    if (firstSirutaCode == sirutaList[i].parentCode) {
                        localityList.add(sirutaData[i])
                    }
                }
                if (localityList.isNotEmpty()) {
                    localityList.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
                    localitys.setText(localityList[0].name)
                }
            }

            progressbar?.dismissPopup()

        }

        viewModel.licenseCategoryData.observe(viewLifecycleOwner) { licenseCategoryData ->
            this.licenseCatList = licenseCategoryData
            drivLicenseCateg.setOnClickListener {
                val dialog = DrivingCategoriesDialogFragment()
                dialog.listener = this
                dialog.catelogList = licenseCategoryData
                dialog.show(childFragmentManager, DrivingCategoriesDialogFragment.TAG)
            }

            if (isEdit && naturalPersonDetails != null) {
                val drivingLicense = naturalPersonDetails?.drivingLicense
                val categorylist = ArrayList<String>()
                if (drivingLicense?.vehicleCategories != null && drivingLicense.vehicleCategories.isNotEmpty()) {
                    for (i in drivingLicense.vehicleCategories.indices) {
                        val catelog = CatalogUtils.findById(
                            licenseCategoryData,
                            drivingLicense.vehicleCategories[i].toLong()
                        )
                        catelog?.name?.let { categorylist.add(it) }
                        if (catelog != null) {
                            drivingCat.add(catelog)
                        }
                    }
                }

                categorylist.let { drivLicenseCateg.setText(it.joinToString(", ")) }
            }
        }

        viewModel.idTypeData.observe(viewLifecycleOwner) { idTypeData ->
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
                override fun OnSelection(model: Int, id: Int) {
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

        viewModel.streetTypeData.observe(viewLifecycleOwner) { streetTypeData ->
            this.streetTypeList = streetTypeData
            val arryadapter =
                ArrayAdapter(requireContext(), R.layout.drop_down_list, streetTypeList)
            sp_quata.adapter = arryadapter

            if (isEdit && naturalPersonDetails != null) {
                address = naturalPersonDetails?.address
                address?.streetType?.id?.let {
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


        viewModel.actionStream.observe(viewLifecycleOwner) {
            when (it) {
                is AddNewNaturalPersonViewModel.ACTION.ShowDatePicker -> showDatePicker(
                    it.view,
                    it.dateInMillis
                )
                is AddNewNaturalPersonViewModel.ACTION.onDeleteSuccess -> onDeleteSuccess(it.attachType)
                is AddNewNaturalPersonViewModel.ACTION.onUploadSuccess -> onUploadSuccess(
                    it.attachType,
                    it.attachment
                )
            }
        }

    }

    fun saveFile(body: ResponseBody?, pathWhereYouWantToSaveFile: String): String {
        if (body == null)
            return ""
        var input: InputStream? = null
        try {
            input = body.byteStream()
            //val file = File(getCacheDir(), "cacheFileAppeal.srl")
            val fos = FileOutputStream(pathWhereYouWantToSaveFile)
            fos.use { output ->
                val buffer = ByteArray(4 * 1024) // or other buffer size
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

        /* dpd!!.datePicker.maxDate = System.currentTimeMillis() - 1000*/
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
                    btnDeleteCertificate.visibility = View.VISIBLE
                    val dlFile = File(selectedFile)
                    naturalPersonDetails?.id?.let {
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
                    naturalPersonDetails?.id?.let {
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    callFileManagerForID()
                }
            }
        }
        if (requestCode == 2297) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
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
                val READ_EXTERNAL_STORAGE = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val WRITE_EXTERNAL_STORAGE = grantResults[1] == PackageManager.PERMISSION_GRANTED
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


    /*override fun onOccupationUpdated(occupationItem1: CatalogItem) {
        this.occupationItem = occupationItem1
        occupation.text = occupationItem1.name
    }*/

    fun onDeleteSuccess(attachType: String) {
        if (attachType.equals("DRIVING_LICENSE")) {
            lblCertificate.visibility = View.GONE
            btnDeleteCertificate.visibility = View.GONE
        } else {
            lblId.visibility = View.GONE
            btnDeleteId.visibility = View.GONE
        }
    }

    fun onUploadSuccess(attachType: String, attachments: Attachments) {
        if (attachType.equals("DRIVING_LICENSE")) {
            lblCertificate.visibility = View.VISIBLE
            btnDeleteCertificate.visibility = View.VISIBLE
            lblCertificate.text = attachments.name
        } else {
            lblId.visibility = View.VISIBLE
            btnDeleteId.visibility = View.VISIBLE
            lblId.text = attachments.name
        }
    }

    companion object {

        var countryItem: Country? = null
        var selectedPhoneCode: String? = null
        const val ARG_IS_EDIT = "arg_is_edit"
        const val ARG_NATURAL_PERSON = "arg_natural_person"

        @SuppressLint("StaticFieldLeak")
        var cna: TextView? = null

        @SuppressLint("StaticFieldLeak")
        var flgs: ImageView? = null

        @SuppressLint("StaticFieldLeak")
        private var in_filleds_county: TextInputLayout? = null

        @SuppressLint("StaticFieldLeak")
        private var in_filleds_locality: TextInputLayout? = null

        @SuppressLint("StaticFieldLeak")
        private var spinnerCity1: TextInputLayout? = null

        @SuppressLint("StaticFieldLeak")
        private var spinnerCounty1: TextInputLayout? = null

        @SuppressLint("StaticFieldLeak")
        lateinit var contexts: Context

        @SuppressLint("StaticFieldLeak")
        var ccna: TextView? = null

        @SuppressLint("StaticFieldLeak")
        var cflgs: ImageView? = null


        fun getFlg(item: Country, flagDrawableResId: Int) {
            this.countryItem = item

            cna?.text = item.name
            flgs?.setImageResource(flagDrawableResId)

            if (item.code == "ROU") {
                spinnerCity1?.isVisible = true
                spinnerCounty1?.isVisible = true
                in_filleds_county?.visibility = View.GONE
                in_filleds_locality?.visibility = View.GONE

            } else {
                spinnerCity1?.isVisible = false
                spinnerCounty1?.isVisible = false
                in_filleds_county?.visibility = View.VISIBLE
                in_filleds_locality?.visibility = View.VISIBLE
            }
        }

        fun getcountrycode(countries: PhoneCodeModel) {
            ccna?.text = "+ ${countries.value}"
            selectedPhoneCode = countries.key
            cflgs?.setImageResource(
                CountryCityUtils.getFlagDrawableResId(
                    CountryCityUtils.firstTwo(
                        countries.key?.lowercase(Locale.getDefault()).toString()
                    ).toString()
                )
            )
        }
    }

    fun findPositionForSirutaCode(countries: List<Siruta>, code: Int): String? {
        for (c in countries.withIndex()) {
            if (c.value.code == code) {
                return c.value.name
            }
        }
        return null
    }


}