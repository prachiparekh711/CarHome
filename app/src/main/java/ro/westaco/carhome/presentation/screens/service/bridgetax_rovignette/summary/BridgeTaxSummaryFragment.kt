package ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.summary

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.AlertDialog
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.bridge_tax_summary_fragment.*
import kotlinx.android.synthetic.main.bridge_tax_summary_fragment.check
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import ro.westaco.carhome.data.sources.remote.requests.Address
import ro.westaco.carhome.data.sources.remote.requests.InitVignettePurchaseRequest
import ro.westaco.carhome.data.sources.remote.requests.PassTaxInitRequest
import ro.westaco.carhome.data.sources.remote.responses.models.*
import ro.westaco.carhome.di.ApiModule
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.interfaceitem.CountyListClick
import ro.westaco.carhome.presentation.screens.data.commen.CountryCodeDialog
import ro.westaco.carhome.presentation.screens.data.commen.CountyAdapter
import ro.westaco.carhome.presentation.screens.data.commen.LocalityAdapter
import ro.westaco.carhome.presentation.screens.main.MainActivity.Companion.profileItem
import ro.westaco.carhome.presentation.screens.service.person.BillingInformationFragment
import ro.westaco.carhome.utils.CatalogUtils
import ro.westaco.carhome.utils.Progressbar
import ro.westaco.carhome.utils.SirutaUtil
import ro.westaco.carhome.utils.SirutaUtil.Companion.countyList
import ro.westaco.carhome.utils.ViewUtils
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class BridgeTaxSummaryFragment : BaseFragment<BridgeTaxSummaryViewModel>(),
    BillingInformationFragment.OnServicePersonListener,
    BillingInformationFragment.AddNewPersonList,
    CountryCodeDialog.CountryCodePicker {

    override fun getContentView() = R.layout.bridge_tax_summary_fragment

    companion object {

        const val ARG_PAYMENT_RESPONSE = "arg_payment_response"
        const val ARG_PASS_TAX_REQUEST = "arg_pass_tax_request"
        const val ARG_ENTER_VALUE = "arg_enter_value"
        const val ARG_CAR = "arg_car"
        var addNewPerson = ""

    }

    var paymentResponse: PaymentResponse? = null
    var passTaxRequest: PassTaxInitRequest? = null
    var initTaxRequest: InitVignettePurchaseRequest? = null
    var legalPersonItemsDetails: LegalPersonDetails? = null
    var naturalPersonItemsDetails: NaturalPersonDetails? = null
    var progressbar: Progressbar? = null
    private var address: Address? = null
    private var vehicleDetail: VehicleDetails? = null
    var vehicle: Vehicle? = null
    var activeService: String = ""
    var personGuid: String = ""
    var bottomSheetDialog: BillingInformationFragment? = null
    var streetTypeList: ArrayList<CatalogItem> = ArrayList()

    var countriesList: ArrayList<Country> = ArrayList()
    var cityList: ArrayList<Siruta> = ArrayList()
    var countryItem: Country? = null
    var countyPosition: Int? = null
    var localityPosition: Int? = null
    var countyDialog: BottomSheetDialog? = null
    var localityDialog: BottomSheetDialog? = null

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun initUi() {

        progressbar = Progressbar(requireContext())
        progressbar?.showPopup()

        arguments?.let {
            paymentResponse = it.getSerializable(ARG_PAYMENT_RESPONSE) as? PaymentResponse?
            passTaxRequest = it.getSerializable(ARG_PASS_TAX_REQUEST) as? PassTaxInitRequest?
            initTaxRequest =
                it.getSerializable(ARG_PASS_TAX_REQUEST) as? InitVignettePurchaseRequest?
            vehicle = it.getSerializable(ARG_CAR) as? Vehicle?
            vehicle?.let { it1 -> viewModel.onVehicle(it1) }
            activeService = it.getString(ARG_ENTER_VALUE).toString()

            if (vehicle != null) {
                mCarName.text = vehicle?.vehicleBrand ?: getString(R.string.car_home)
                val options = RequestOptions()
                carLogo.clipToOutline = true
                val url = "${ApiModule.BASE_URL_RESOURCES}${vehicle?.vehicleBrandLogo}"
                val glideUrl = GlideUrl(
                    url,
                    LazyHeaders.Builder()
                        .addHeader("Authorization", "Bearer ${AppPreferencesDelegates.get().token}")
                        .build()
                )

                Glide.with(requireContext())
                    .load(glideUrl)
                    .apply(
                        options.centerCrop()
                            .skipMemoryCache(true)
                            .priority(Priority.HIGH)
                            .format(DecodeFormat.PREFER_ARGB_8888)
                    )
                    .into(carLogo)
            }

            mOwnerName.setText("${profileItem?.firstName} ${profileItem?.lastName}")
            profileItem?.id?.toLong()?.let { it1 -> viewModel.getNaturalPerson(it1) }
            mNumberOfPassesText.setText(passTaxRequest?.price?.description)

            personGuid = profileItem?.guid.toString()

            when (activeService) {
                "RO_PASS_TAX" -> {
                    chargeLabel.text = requireContext().resources.getString(R.string.bridge_tax)
                    mCategoryLabel.hint =
                        requireContext().resources.getString(R.string.bridge_tax_cat_)
                }
                "RO_VIGNETTE" -> {
                    chargeLabel.text = requireContext().resources.getString(R.string.rovinieta)
                    mCategoryLabel.hint =
                        requireContext().resources.getString(R.string.vignette_category)
                }
            }

            mPriceText.text =
                "${passTaxRequest?.price?.paymentValue ?: initTaxRequest?.price?.paymentValue} ${passTaxRequest?.price?.paymentCurrency ?: initTaxRequest?.price?.paymentCurrency}"

            if (initTaxRequest?.startDate != null) {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                val outputFormat = SimpleDateFormat("dd-MM-yyyy")
                val date: Date =
                    initTaxRequest?.startDate?.let { it1 -> inputFormat.parse(it1) } as Date
                val formattedDate = outputFormat.format(date)
                mVignetteDateText.setText(formattedDate)
            }

        }

        toolbar.setNavigationOnClickListener {
            viewModel.onBack()
        }

        mPrevious.setOnClickListener {

            viewModel.onBack()
        }

        mChange.setOnClickListener {
            bottomSheetDialog = BillingInformationFragment(this, this, addNewPerson)
            bottomSheetDialog?.show(requireActivity().supportFragmentManager, null)
        }

        check.setOnCheckedChangeListener { buttonView, isChecked ->

            if (isChecked) {
                mContinue.setBackgroundResource(R.drawable.save_background)
            } else {
                mContinue.setBackgroundResource(R.drawable.save_background_invisible)
            }
        }

        countySpinnerText.setOnClickListener {
            openCountyDialog()
        }

        mContinue.setOnClickListener {

            var editMode = false
            if (check.isChecked) {

                when {

                    naturalPersonItemsDetails != null -> {
                        editMode = naturalPersonItemsDetails?.address?.streetName.isNullOrEmpty() ||
                                naturalPersonItemsDetails?.address?.buildingNo.isNullOrEmpty() ||
                                naturalPersonItemsDetails?.address?.countryCode.isNullOrEmpty() ||
                                naturalPersonItemsDetails?.address?.region.isNullOrEmpty() ||
                                naturalPersonItemsDetails?.address?.locality.isNullOrEmpty()
                    }

                    legalPersonItemsDetails != null -> {
                        editMode =
                            legalPersonItemsDetails?.address?.streetName.isNullOrEmpty() ||
                                    legalPersonItemsDetails?.address?.buildingNo.isNullOrEmpty() ||
                                    legalPersonItemsDetails?.address?.countryCode.isNullOrEmpty() ||
                                    legalPersonItemsDetails?.address?.region.isNullOrEmpty() ||
                                    legalPersonItemsDetails?.address?.locality.isNullOrEmpty()

                    }
                }

                mAddressLayout.isVisible = editMode
                if (editMode) {

                    val streetTypeItem =
                        sp_quata?.selectedItemPosition?.let { it1 -> streetTypeList.get(it1).id }
                            ?.let { it2 ->
                                CatalogUtils.findById(streetTypeList, it2)
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
                            regionStr = SirutaUtil.defaultCounty?.name
                            sirutaCode = SirutaUtil.defaultCity?.code
                            localityStr = SirutaUtil.defaultCity?.name
                        }
                    } else {
                        regionStr = stateProvinceText.text.toString()
                        sirutaCode = null
                        localityStr = localityAreaText.text.toString()
                    }

                    var addressItem: Address? = null
                    addressItem = Address(
                        zipCode = mZipCodeNameText.text.toString(),
                        streetType = streetTypeItem,
                        sirutaCode = sirutaCode,
                        locality = localityStr,
                        streetName = mStreetNameText.text.toString(),
                        addressDetail = null,
                        buildingNo = mNumberText.text.toString(),
                        countryCode = countryItem?.code,
                        block = mBlockNameText.text.toString(),
                        region = regionStr,
                        entrance = mEntranceNameText.text.toString(),
                        floor = mFloorNameText.text.toString(),
                        apartment = mApartmentNameText.text.toString()
                    )

                    when {
                        naturalPersonItemsDetails != null -> {

                            naturalPersonItemsDetails?.id?.toLong()?.let { it1 ->

                                paymentResponse?.guid?.let { it2 ->

                                    personGuid.let { it3 ->
                                        viewModel.onSaveNaturalPerson(
                                            id = it1,
                                            firstName = naturalPersonItemsDetails?.firstName,
                                            lastname = naturalPersonItemsDetails?.lastName,
                                            address = addressItem,
                                            cnp = naturalPersonItemsDetails?.cnp,
                                            phone = naturalPersonItemsDetails?.phone,
                                            phoneCountryCode = naturalPersonItemsDetails?.phoneCountryCode,
                                            dateOfBirth = naturalPersonItemsDetails?.dateOfBirth,
                                            email = naturalPersonItemsDetails?.email,
                                            drivingLicenseCateg = naturalPersonItemsDetails?.drivingLicense?.vehicleCategories as ArrayList<Int>?,
                                            drivLicenseId = naturalPersonItemsDetails?.drivingLicense?.licenseId,
                                            drivLicenseIssueDate = naturalPersonItemsDetails?.drivingLicense?.issueDate,
                                            drivLicenseExpDate = naturalPersonItemsDetails?.drivingLicense?.expirationDate,
                                            idDoc = naturalPersonItemsDetails?.identityDocument,
                                            guid = it2,
                                            personGUID = it3,
                                            check = check.isChecked
                                        )
                                    }
                                }

                            }

                        }

                        legalPersonItemsDetails != null -> {

                            legalPersonItemsDetails?.id?.let { it1 ->

                                paymentResponse?.guid?.let { it2 ->
                                    personGuid.let { it3 ->
                                        viewModel.onSaveLegalPerson(
                                            noReg = legalPersonItemsDetails?.noRegistration,
                                            address = addressItem,
                                            cui = legalPersonItemsDetails?.cui,
                                            companyName = legalPersonItemsDetails?.companyName,
                                            caen = legalPersonItemsDetails?.caen,
                                            id = it1,
                                            activityType = legalPersonItemsDetails?.activityType,
                                            phoneId = legalPersonItemsDetails?.phone,
                                            phoneCountryCodeId = legalPersonItemsDetails?.phoneCountryCode,
                                            emailId = legalPersonItemsDetails?.email,
                                            guid = it2,
                                            personGUID = it3,
                                            check = check.isChecked
                                        )
                                    }
                                }

                            }
                        }
                    }
                } else {
                    paymentResponse?.guid?.let { it1 -> viewModel.onNextClick(it1, personGuid) }
                }
            }

        }

    }

    override fun setObservers() {

        viewModel.profileLogoData?.observe(viewLifecycleOwner) { profileLogo ->
            if (profileLogo != null) {
                val options = RequestOptions()
                mSelectOwnerImage.clipToOutline = true
                Glide.with(requireContext())
                    .load(profileLogo)
                    .error(requireContext().resources.getDrawable(R.drawable.ic_profile_picture))
                    .apply(
                        options.centerCrop()
                            .skipMemoryCache(true)
                            .priority(Priority.HIGH)
                            .format(DecodeFormat.PREFER_ARGB_8888)
                    )
                    .into(mSelectOwnerImage)
            } else {
                viewModel.userLiveData.observe(viewLifecycleOwner) { user ->
                    if (user != null) {
                        val imageUrl = viewModel.getProfileImage(requireContext(), user)

                        val options = RequestOptions()
                        mSelectOwnerImage.clipToOutline = true
                        context?.let {
                            Glide.with(it)
                                .load(imageUrl)
                                .apply(
                                    options.fitCenter()
                                        .skipMemoryCache(true)
                                        .priority(Priority.HIGH)
                                        .format(DecodeFormat.PREFER_ARGB_8888)
                                )
                                .into(mSelectOwnerImage)

                        }
                    }
                }
            }

            viewModel.vehicleCategories.observe(viewLifecycleOwner) { catList ->
                for (i in catList.indices) {
                    if (catList[i].code == passTaxRequest?.price?.passTaxCategoryCode) {
                        mCategoryText.setText(catList[i].description)
                        bridgeTaxInformation.isVisible = true
                        vignetteInformation.isVisible = false
                    }
                    if (catList[i].code == initTaxRequest?.price?.vignetteCategoryCode) {
                        mVignetteText.setText(catList[i].description)
                        bridgeTaxInformation.isVisible = false
                        vignetteInformation.isVisible = true
                    }
                }
            }

            viewModel.bridgeTaxObjectives.observe(viewLifecycleOwner) { objectiveList ->
                for (i in objectiveList.indices) {
                    if (objectiveList[i].code == passTaxRequest?.price?.objectiveCode) {
                        mObjectiveText.setText(objectiveList[i].description)
                    }
                }
            }

            viewModel.vignetteDurations.observe(viewLifecycleOwner) { vehicleDuration ->
                for (i in vehicleDuration.indices) {
                    if (initTaxRequest?.price?.vignetteDurationCode == vehicleDuration[i].code) {
                        val textDuration: String =
                            vehicleDuration[i].timeUnitCount.toString() + " " + vehicleDuration[i].timeUnit
                        mVignetteDurationText.setText(textDuration)
                    }
                }
            }

            viewModel.initTransectionData.observe(viewLifecycleOwner) { model ->

                if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {

                    if (model != null) {
                        if (model.warnings.isNullOrEmpty()) {
                            model.html?.let {

                                showPurchaseBottomSheetDialog(
                                    model
                                )
                            }
                        } else {
                            var warning = ""
                            for (i in model.warnings) {
                                warning += i + "\n"
                            }

                            showPurchaseWarningsDialog(
                                model, warning
                            )

                        }
                    }
                }

            }

            viewModel.naturalPersonDetailsLiveDataList.observe(viewLifecycleOwner) { naturalPerson ->
                this.naturalPersonItemsDetails = naturalPerson

                progressbar?.dismissPopup()
                naturalPerson.let {
                    if (it != null) {
                        val addressNatural = it.address
                        if (addressNatural != null) {
                            mStreetNameText.setText(addressNatural.streetName)
                            mNumberText.setText(addressNatural.buildingNo)
                            mBlockNameText.setText(addressNatural.block)
                            mEntranceNameText.setText(addressNatural.entrance)
                            mFloorNameText.setText(addressNatural.floor)
                            mApartmentNameText.setText(addressNatural.apartment)
                            mZipCodeNameText.setText(addressNatural.zipCode)

                            addressNatural.countryCode?.let { it1 -> changeCountryState(it1) }
                            if (addressNatural.countryCode == "ROU") {
                                countySpinnerText.setText(addressNatural.region)
                                localitySpinnerText.setText(addressNatural.locality)
                            } else {
                                stateProvinceText.setText(addressNatural.region)
                                localityAreaText.setText(addressNatural.locality)
                            }
                        } else {
                            changeCountryState("ROU")
                            countySpinnerText.setText(SirutaUtil.defaultCounty?.name)
                            localitySpinnerText.setText(SirutaUtil.defaultCity?.name)
                        }
                    }
                }
            }

            viewModel.legalPersonDetailsLiveDataList.observe(viewLifecycleOwner) { legalPerson ->
                this.legalPersonItemsDetails = legalPerson

                progressbar?.dismissPopup()
                legalPerson.let {
                    if (it != null) {
                        val addressLegal = it.address
                        if (addressLegal != null) {
                            mStreetNameText.setText(addressLegal.streetName)
                            mNumberText.setText(addressLegal.buildingNo)
                            mBlockNameText.setText(addressLegal.block)
                            mEntranceNameText.setText(addressLegal.entrance)
                            mFloorNameText.setText(addressLegal.floor)
                            mApartmentNameText.setText(addressLegal.apartment)
                            mZipCodeNameText.setText(addressLegal.zipCode)

                            if (addressLegal.countryCode == "ROU") {
                                countySpinnerText.setText(addressLegal.region)
                                localitySpinnerText.setText(addressLegal.locality)
                            } else {
                                stateProvinceText.setText(addressLegal.region)
                                localityAreaText.setText(addressLegal.locality)
                            }
                        } else {
                            changeCountryState("ROU")
                            countySpinnerText.setText(SirutaUtil.defaultCounty?.name)
                            localitySpinnerText.setText(SirutaUtil.defaultCity?.name)
                        }
                    }

                }

            }

            viewModel.vehicleDetailsLivedata.observe(viewLifecycleOwner) { vehicleDetailsItems ->
                vehicleDetail = vehicleDetailsItems
            }

            viewModel.countryData.observe(viewLifecycleOwner) { countryData ->

                this.countriesList = countryData

                if (vehicleDetail != null) {

                    val pos = vehicleDetail?.registrationCountryCode?.let {
                        Country.findPositionForCode(
                            countriesList,
                            it
                        )
                    }

                    this.countryItem = pos?.let { countriesList[it] }

                } else {

                    this.countryItem = countriesList[Country.findPositionForCode(countriesList)]

                }

                val countryCodeDialog = CountryCodeDialog(requireActivity(), countriesList, this)

                mCountry_NameText.setOnClickListener {
                    countryCodeDialog.show(requireActivity().supportFragmentManager, null)
                }
                progressbar?.dismissPopup()

            }


            viewModel.streetTypeData.observe(viewLifecycleOwner) { streetTypeData ->

                if (streetTypeData != null) {

                    this.streetTypeList = streetTypeData
                    val arryadapter =
                        ArrayAdapter(requireContext(), R.layout.drop_down_list, streetTypeList)
                    sp_quata.adapter = arryadapter

                    if (naturalPersonItemsDetails != null) {

                        address = naturalPersonItemsDetails?.address
                        address?.streetType?.id?.let {
                            CatalogUtils.findPosById(
                                streetTypeList,
                                it
                            )
                        }
                            ?.let { sp_quata?.setSelection(it) }

                    }

                    if (legalPersonItemsDetails != null) {

                        address = legalPersonItemsDetails?.address
                        address?.streetType?.id?.let {
                            CatalogUtils.findPosById(streetTypeList, it)
                        }?.let {
                            sp_quata?.setSelection(it)
                        }

                    }

                }

            }
        }
    }

    private var purchaseDialog: AlertDialog? = null

    private fun showPurchaseWarningsDialog(model: PaymentResponse, warning: String) {

        purchaseDialog?.dismiss()
        purchaseDialog = AlertDialog.Builder(requireContext(), R.style.DialogTheme).create()
        purchaseDialog?.setMessage(warning)
        purchaseDialog?.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        purchaseDialog?.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { dialog, _ ->
            dialog.dismiss()
            vehicle?.let { showPurchaseBottomSheetDialog(model) }
        }
        purchaseDialog?.show()

    }

    private fun showPurchaseBottomSheetDialog(model: PaymentResponse) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_purchase)
        var navigated = false

        val webView = bottomSheetDialog.findViewById<WebView>(R.id.webView)
        webView?.clearHistory()

        webView?.settings?.javaScriptEnabled = true
        webView?.settings?.javaScriptCanOpenWindowsAutomatically = true
        webView?.settings?.loadsImagesAutomatically = true

        webView?.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(webView: WebView, url: String): Boolean {
                return shouldOverrideUrlLoading(url)
            }

            @TargetApi(Build.VERSION_CODES.N)
            override fun shouldOverrideUrlLoading(
                webView: WebView,
                request: WebResourceRequest,
            ): Boolean {
                val uri = request.url
                return shouldOverrideUrlLoading(uri.toString())
            }

            private fun shouldOverrideUrlLoading(url: String): Boolean {
                return if (url.contains("payment-done")) {
                    if (!navigated) {
                        navigated = true
                        activeService.let { viewModel.onPaymentSuccess(model, arg_of = it) }
                    }
                    bottomSheetDialog.dismiss()
                    true
                } else {
                    false
                }
            }

        }

        model.html?.replace("&#39;", "")
            ?.let { webView?.loadData(it, "text/html; charset=UTF-8", null) }

        bottomSheetDialog.findViewById<View>(R.id.dismiss)?.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        val parentLayout =
            bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        parentLayout?.let { it ->
            val behaviour = BottomSheetBehavior.from(it)
            ViewUtils.setViewHeightAsWindowPercent(requireContext(), it, 85)
            behaviour.state = BottomSheetBehavior.STATE_EXPANDED
        }

        bottomSheetDialog.show()

    }

    override fun onPersonChange(naturalItem: NaturalPerson?, legalItem: LegalPerson?) {
        var logoHref: String? = null
        var singlechar: String? = null
        when {

            naturalItem != null -> {
                mOwnerName.setText("${naturalItem.firstName} ${naturalItem.lastName}")
                personGuid = naturalItem.guid.toString()
                logoHref = naturalItem.logoHref
                singlechar = "${naturalItem.firstName} ${naturalItem.lastName}"
                setImage(logoHref, singlechar)
                naturalItem.id?.let { viewModel.getNaturalPerson(it) }
                mAddressLayout.isVisible = false
                progressbar?.showPopup()
            }

            legalItem != null -> {
                mOwnerName.setText("${legalItem.companyName}")
                personGuid = legalItem.guid.toString()
                logoHref = legalItem.logoHref
                val one = legalItem.companyName?.substring(0, 1)
                val two = legalItem.companyName?.substring(1, 2)
                val single = one + two
                setImage(logoHref, single)
                legalItem.id?.toLong()?.let { viewModel.getLegalPerson(it) }
                mAddressLayout.isVisible = false
                progressbar?.showPopup()

            }

        }

    }

    private fun setImage(logoHref: String?, singlechar: String?) {
        if (logoHref != null) {

            val url = "${ApiModule.BASE_URL_RESOURCES}${logoHref}"
            val glideUrl = GlideUrl(
                url,
                LazyHeaders.Builder()
                    .addHeader("Authorization", "Bearer ${AppPreferencesDelegates.get().token}")
                    .build()
            )

            textLogo.isVisible = false
            mSelectOwnerImage.isVisible = true
            val options = RequestOptions()
            mSelectOwnerImage.clipToOutline = true

            Glide.with(requireContext())
                .load(glideUrl)
                .apply(
                    options.centerCrop()
                        .skipMemoryCache(true)
                        .priority(Priority.HIGH)
                        .format(DecodeFormat.PREFER_ARGB_8888)
                )
                .into(mSelectOwnerImage)
        } else {
            textLogo.isVisible = true
            mSelectOwnerImage.isVisible = false

            textLogo.text = singlechar?.replace(
                "^\\s*([a-zA-Z]).*\\s+([a-zA-Z])\\S+$".toRegex(),
                "$1$2"
            )?.uppercase(Locale.getDefault())
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
                    countyCode = SirutaUtil.fetchCounty(countySpinnerText.text.toString())?.code
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
                    after: Int
                ) {

                }

                @SuppressLint("NotifyDataSetChanged")
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
                after: Int
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

    @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
    override fun OnCountryPick(item: Country, flagDrawableResId: String) {
        this.countryItem = item
        changeCountryState(item.code)
        mCountry_NameText.setText(flagDrawableResId + " " + item.name)

        if (item.name == "Romania") {
            spinnerCounty.setEndIconTintList(
                ContextCompat.getColorStateList(
                    requireContext(),
                    R.color.skip
                )
            )
            spinnerLocality.setEndIconTintList(
                ContextCompat.getColorStateList(
                    requireContext(),
                    R.color.skip
                )
            )
        }

        mCountry_Name.endIconDrawable =
            requireContext().resources.getDrawable(R.drawable.ic_arrow_down_id)
        mCountry_Name.setEndIconTintList(
            ContextCompat.getColorStateList(
                requireContext(),
                R.color.appPrimary
            )
        )

    }

    override fun openNewPerson(type: String?) {
        if (type != null) {
            addNewPerson = type
            bottomSheetDialog?.dismiss()
        }
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

}