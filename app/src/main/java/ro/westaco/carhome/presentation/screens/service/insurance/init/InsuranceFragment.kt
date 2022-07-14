package ro.westaco.carhome.presentation.screens.service.insurance.init

import android.annotation.SuppressLint
import android.os.Build
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_insurance.*
import kotlinx.android.synthetic.main.item_in_cars.view.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import ro.westaco.carhome.data.sources.remote.requests.RcaOfferRequest
import ro.westaco.carhome.data.sources.remote.responses.models.*
import ro.westaco.carhome.di.ApiModule
import ro.westaco.carhome.dialog.DialogUtils.Companion.showErrorInfo
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.main.MainActivity
import ro.westaco.carhome.presentation.screens.service.insurance.adapter.DriverAdapter
import ro.westaco.carhome.presentation.screens.service.insurance.ins_person.SelectUserFragment
import ro.westaco.carhome.utils.CountryCityUtils
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class InsuranceFragment : BaseFragment<InsuranceViewModel>(),
    LeasingInFragment.OnDialogInteractionListener,
    DriverAdapter.OnRemoveListener,
    SelectUserFragment.OnOwnerSelectionListener,
    SelectUserFragment.OnUserSelectionListener,
    SelectUserFragment.OnDriverSelectionListener,
    SelectUserFragment.OnNewDriverSelectionListener,
    SelectUserFragment.AddNewUserView {

    var mView: View? = null
    lateinit var driverAdapter: DriverAdapter
    private var driverList = ArrayList<NaturalPersonForOffer>()
    val itemsDriverList = ArrayList<NaturalPerson>()
    var carList: ArrayList<Vehicle>? = null
    var leasingCompanyList = ArrayList<LeasingCompany>()
    private var selectedVehicle: VehicleDetailsForOffer? = null
    var vehicleItem: VehicleDetails? = null
    private var vehicleOwnerLegalPerson: LegalPersonDetails? = null
    private var vehicleOwnerNaturalPerson: NaturalPersonForOffer? = null
    private var vehicleUserNaturalPerson: NaturalPersonForOffer? = null
    private var vehicleUserLegalPerson: LegalPersonDetails? = null
    var bottomSheet: SelectUserFragment? = null
    var personTypeToValidate = 0
    var mainWarningList: HashMap<String, ArrayList<WarningsItem>> = HashMap()
    var ownerNaturalItem: NaturalPerson? = null
    var ownerLegalItem: LegalPerson? = null
    var userNaturalItem: NaturalPerson? = null
    var userLegalItem: LegalPerson? = null
    var driverNaturalItem: NaturalPerson? = null
    var driverNewNaturalItem: NaturalPerson? = null

    companion object {
        var IS_PERSON_EDITABLE = true
        const val ARG_CAR_ID = "arg_car_id"
        var addNew = false
        var addNewType = ""
    }

    override fun getContentView() = R.layout.fragment_insurance

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun initUi() {
        arguments?.let {
            val vehicleId = it.getInt(ARG_CAR_ID)
            vehicleId.let { it1 -> viewModel.fetchCarDetails(it1) }
            vehicleId.let { it1 -> viewModel.fetchCarDetailsForOffer(it1) }
        }

        MainActivity.activeService = "RO_RCA"
        requireView().isFocusableInTouchMode = true
        requireView().requestFocus()
        requireView().setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                onBackPress()
                true
            } else false
        }

        back.setOnClickListener {
            onBackPress()
        }

        cancel.setOnClickListener {
            ownerLegalItem = null
            ownerNaturalItem = null
            viewModel.onBack()
        }

        mSelectOwner.setOnClickListener {
            bottomSheet = SelectUserFragment(this, null, null, this, null, "OWNER")
            bottomSheet?.show(requireActivity().supportFragmentManager, null)
        }

        mSelectUser.setOnClickListener {
            bottomSheet = SelectUserFragment(null, this, null, this, null, "USER")
            bottomSheet?.show(requireActivity().supportFragmentManager, null)
        }

        mSelectDriver.setOnClickListener {
            bottomSheet = SelectUserFragment(null, null, this, this, null, "DRIVER")
            bottomSheet?.show(requireActivity().supportFragmentManager, null)
        }

        mAddDriver.setOnClickListener {
            bottomSheet = SelectUserFragment(null, null, null, this, this, "DRIVER_NEW")
            bottomSheet?.show(requireActivity().supportFragmentManager, null)
        }

        setOwner(false)
        setUser()
        setDriver()

        leasingCheckbox.setOnCheckedChangeListener { btn, isChecked ->
            if (btn.isPressed) {
                userCheckBox.isVisible = !isChecked
                driverCheckBox.isVisible = !isChecked
                if (isChecked) {
                    mSelectLeasing.isVisible = true
                    mSelectOwner.isVisible = false
                    setUserAsBlank()
                    setDriverAsBlank()
                } else {
                    mSelectLeasing.isVisible = false
                    mSelectOwner.isVisible = true
                    if (userCheckBox.isChecked)
                        setUserSameAsOwner()
                    if (driverCheckBox.isChecked)
                        setDriverSameAsOwner()

                    driverCheckBox.isVisible = ownerNaturalItem != null

                }
                checkItems()
            }
        }

        userCheckBox.setOnCheckedChangeListener { btn, isChecked ->
            if (btn.isPressed) {
                if (isChecked) {
                    setUserSameAsOwner()
                } else {
                    if (vehicleUserNaturalPerson == null && vehicleUserLegalPerson == null)
                        setUserAsBlank()
                }
                checkItems()
            }
        }

        driverCheckBox.setOnCheckedChangeListener { btn, isChecked ->
            if (btn.isPressed) {
                if (isChecked) {
                    setDriverSameAsOwner()
                } else {
                    if (driverNaturalItem == null)
                        setDriverAsBlank()
                }
                checkItems()
            }
        }

        driverAdapter = DriverAdapter(requireContext(), this)

        mContinue.setOnClickListener {
            addNew = false
            val request = RcaOfferRequest(
                leasing = leasingCheckbox.isChecked,
                vehicleOwnerLegalPerson = vehicleOwnerLegalPerson,
                beginDate = null,
                vehicleOwnerNaturalPerson = vehicleOwnerNaturalPerson,
                rcaDurationId = null,
                vehicleUserNaturalPerson = vehicleUserNaturalPerson,
                vehicleRegistered = mFirst.isChecked,
                vehicleUserSameAsOwner = userCheckBox.isChecked,
                vehicleUserLegalPerson = vehicleUserLegalPerson,
                drivers = driverList,
                driverSameAsOwner = driverCheckBox.isChecked,
                vehicle = selectedVehicle
            )

            viewModel.onCta(request, vehicleItem?.policyExpirationDate)
        }
    }

    override fun onResume() {

        super.onResume()

        if (addNew) {
            bottomSheet = SelectUserFragment(this, null, null, this, null, addNewType)
            bottomSheet?.show(requireActivity().supportFragmentManager, null)
        }
        IS_PERSON_EDITABLE = true

    }

    private fun onBackPress() {
        addNew = false
        ownerLegalItem = null
        ownerNaturalItem = null
        userNaturalItem = null
        userLegalItem = null
        driverNaturalItem = null
        viewModel.onBack()
    }

    private fun setUserAsBlank() {
        vehicleUserNaturalPerson = null
        vehicleUserLegalPerson = null
        mSelectUserName.text = requireContext().resources.getString(R.string.select_user)
        mSelectUserEmail.isVisible = false
        mSelectUserImage.setImageDrawable(requireContext().resources.getDrawable(R.drawable.ic_profile_picture))
    }

    private fun setDriverAsBlank() {
        driverList.clear()
        itemsDriverList.clear()
        driverAdapter.clearItems()
        if (driverList.isNotEmpty()) {
            mRecycle.isVisible = false
        }
        mSelectDriverName.text = requireContext().resources.getString(R.string.select_driver)
        mSelectDriverEmail.isVisible = false
        mSelectDriverImage.setImageDrawable(requireContext().resources.getDrawable(R.drawable.ic_profile_picture))
    }


    private fun setUserSameAsOwner() {
        when {
            ownerLegalItem != null -> {
                userLegalItem = ownerLegalItem
                mSelectUserName.text = userLegalItem?.companyName
                mSelectUserEmail.isVisible = false
                if (userLegalItem?.logoHref != null) {
                    mSelectUserTv.isVisible = false
                    mSelectUserImage.isVisible = true
                    userLegalItem?.logoHref?.let { setImage(it, mSelectUserImage) }
                } else {
                    mSelectUserTv.isVisible = true
                    mSelectUserImage.isVisible = false
                    userLegalItem?.companyName?.let { setText(it, mSelectUserTv, false) }
                }

                userLegalItem?.id?.toLong().let {
                    if (it != null) {
                        viewModel.getLegalPersonDetails(it, "USER")
                    }
                }
                ownerNaturalItem = null
            }
            ownerNaturalItem != null -> {
                userNaturalItem = ownerNaturalItem
                mSelectUserName.text = "${userNaturalItem?.firstName} ${userNaturalItem?.lastName}"
                mSelectUserEmail.isVisible = true
                mSelectUserEmail.text = userNaturalItem?.email
                if (userNaturalItem?.logoHref != null) {
                    mSelectUserTv.isVisible = false
                    mSelectUserImage.isVisible = true
                    userNaturalItem?.logoHref?.let { setImage(it, mSelectUserImage) }
                } else {
                    mSelectUserTv.isVisible = true
                    mSelectUserImage.isVisible = false
                    setText(
                        "${userNaturalItem?.firstName} ${userNaturalItem?.lastName}",
                        mSelectUserTv, true
                    )
                }
                userNaturalItem?.id?.let { viewModel.getNaturalPersonDetails(it, "USER") }
                ownerLegalItem = null
            }
            else -> {
                setUserAsBlank()
            }
        }

    }

    private fun setDriverSameAsOwner() {
        when {
            ownerNaturalItem != null -> {
                driverNaturalItem = ownerNaturalItem
                mSelectDriverName.text =
                    "${driverNaturalItem?.firstName} ${driverNaturalItem?.lastName}"
                mSelectDriverEmail.isVisible = true
                mSelectDriverEmail.text = driverNaturalItem?.email

                if (driverNaturalItem?.logoHref != null) {
                    mSelectDriverTv.isVisible = false
                    mSelectDriverImage.isVisible = true
                    driverNaturalItem?.logoHref?.let { setImage(it, mSelectDriverImage) }
                } else {
                    mSelectDriverTv.isVisible = true
                    mSelectDriverImage.isVisible = false
                    setText(
                        "${driverNaturalItem?.firstName} ${driverNaturalItem?.lastName}",
                        mSelectDriverTv, true
                    )
                }
                driverNaturalItem?.id?.let { viewModel.getNaturalPersonDetails(it, "DRIVER") }
            }
            else -> {
                if (mSelectOwnerName.text.isBlank()) {
                    setDriverAsBlank()
                } else {
                    mSelectDriverName.text =
                        requireContext().resources.getString(R.string.select_driver)
                    mSelectDriverEmail.isVisible = false
                    mSelectDriverImage.setImageDrawable(requireActivity().resources.getDrawable(R.drawable.ic_profile_picture))
                }
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun setObservers() {

        viewModel.carsLivedata.observe(viewLifecycleOwner) { cars ->
            this.carList = cars
        }

        viewModel.vehicleDetailsLivedata.observe(viewLifecycleOwner) { car ->
            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                this.vehicleItem = car
                setCarDetail(car)
            }
            checkItems()
        }

        viewModel.vehicleForOfferLivedata.observe(viewLifecycleOwner) { car ->
            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                this.selectedVehicle = car
            }
            checkItems()
        }

        viewModel.leasingCompaniesData.observe(viewLifecycleOwner) { leasingData ->
            if (leasingData != null) {
                this.leasingCompanyList = leasingData
            }

            mSelectLeasing.setOnClickListener {

                if (leasingCompanyList.isNotEmpty()) {
                    val dialog = LeasingInFragment(leasingCompanyList)
                    dialog.listener = this
                    dialog.show(childFragmentManager, LeasingInFragment.TAG)
                } else {
                    showErrorInfo(requireContext(), getString(R.string.no_companies_found))

                }

            }
            checkItems()
        }

        viewModel.actionStream.observe(viewLifecycleOwner) {

            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                when (it) {
                    is InsuranceViewModel.ACTION.OnGetLegalDetails -> {
                        when (it.personType) {
                            "OWNER" -> {
                                vehicleOwnerNaturalPerson = null
                                vehicleOwnerLegalPerson = it.item
                            }
                            "USER" -> {
                                vehicleUserNaturalPerson = null
                                vehicleUserLegalPerson = it.item
                                userCheckBox.isChecked =
                                    vehicleUserLegalPerson?.id == ownerLegalItem?.id?.toLong()
                            }

                        }
                        val driverExist = driverList.find { it1 -> it1.id == it.item.id?.toInt() }
                        driverCheckBox.isChecked = driverExist != null

                        checkItems()
                    }
                    is InsuranceViewModel.ACTION.OnGetNaturalDetails -> {
                        when (it.personType) {
                            "OWNER" -> {
                                vehicleOwnerLegalPerson = null
                                vehicleOwnerNaturalPerson = it.item
                            }
                            "USER" -> {
                                vehicleUserLegalPerson = null
                                vehicleUserNaturalPerson = it.item
                                userCheckBox.isChecked =
                                    vehicleUserNaturalPerson?.id?.toLong() == ownerNaturalItem?.id
                            }
                            "DRIVER" -> {
                                if (!driverList.contains(it.item))
                                    driverList.add(it.item)
                                driverCheckBox.isChecked =
                                    it.item.id?.toLong() == ownerNaturalItem?.id

                            }
                        }

                        checkItems()
                    }
                }
            }
        }

        viewModel.verifyUser.observe(viewLifecycleOwner) { validationResult ->

            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                if (validationResult?.warnings?.isNotEmpty() == true) {
                    mainWarningList["User"] =
                        validationResult.warnings as java.util.ArrayList<WarningsItem>


                    userCheckBox.isChecked = false
                    setUserAsBlank()
                } else {

//                    userCheckBox.isChecked = true
                    setUserSameAsOwner()
                }
                isUserResponseAvailable = true
                responseDone()
            }
        }

        viewModel.verifyDriver.observe(viewLifecycleOwner) { validationResult ->
            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                if (validationResult?.warnings?.isNotEmpty() == true) {
                    mainWarningList["Driver"] =
                        validationResult.warnings as java.util.ArrayList<WarningsItem>
                    driverCheckBox.isChecked = false
                    setDriverAsBlank()
                } else {
//                    driverCheckBox.isChecked = true
                    setDriverSameAsOwner()
                }
                isDriverResponseAvailable = true
                responseDone()
            }
        }
    }

    private var isUserResponseAvailable = false
    private var isDriverResponseAvailable = false
    private fun responseDone() {
//        Log.e("mainWarningList", mainWarningList.toString())
        /*  if (mainWarningList.isNotEmpty()) {
              if (personTypeToValidate == 10010) {  //Natural person selected
                  if (isUserResponseAvailable && isDriverResponseAvailable) {
                      displayUserAndDriverWarning(mainWarningList)
                  }
              } else {
                  if (isUserResponseAvailable)
                      displayUserAndDriverWarning(mainWarningList)
              }
          }*/
    }

    private fun displayUserAndDriverWarning(mapList: HashMap<String, ArrayList<WarningsItem>>) {
        var dialogBody = ""
        if (mapList.isNotEmpty()) {

            for (i in mapList.keys) {
                var warningStr = ""
                dialogBody = "$dialogBody\n$i"
                val warningsItemList = mapList[i]
                if (!warningsItemList.isNullOrEmpty()) {

                    for (j in warningsItemList.indices) {
                        val field = requireContext().resources?.getIdentifier(
                            "${warningsItemList[j].field}",
                            "string",
                            requireContext().packageName
                        )
                            ?.let { requireContext().resources?.getString(it) }
                        warningStr =
                            "$warningStr${field} : ${warningsItemList[j].warning}\n"
                    }
                }
                dialogBody = "$dialogBody\n$warningStr"
            }
        }
        showErrorInfo(
            requireContext(),
            dialogBody
        )
    }

    private fun setImage(href: String?, view: ImageView) {
        val url = "${ApiModule.BASE_URL_RESOURCES}${href}"
        val glideUrl = GlideUrl(
            url,
            LazyHeaders.Builder()
                .addHeader("Authorization", "Bearer ${AppPreferencesDelegates.get().token}")
                .build()
        )
        val options = RequestOptions()
        view.clipToOutline = true
        Glide.with(requireContext())
            .load(glideUrl)
            .error(requireContext().resources.getDrawable(R.drawable.ic_profile_picture))
            .apply(
                options.centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .priority(Priority.HIGH)
                    .format(DecodeFormat.PREFER_ARGB_8888)
            )
            .into(view)

    }

    fun setText(singleChar: String, tv: TextView, isNatural: Boolean) {
        if (isNatural)
            tv.text = singleChar.replace(
                "^\\s*([a-zA-Z]).*\\s+([a-zA-Z])\\S+$".toRegex(),
                "$1$2"
            ).uppercase(Locale.getDefault())
        else
            tv.text = CountryCityUtils.firstTwo(singleChar.uppercase(Locale.getDefault()))
    }

    override fun onCompanyUpdated(company: LeasingCompany) {

        val typeface = ResourcesCompat.getFont(requireContext(), R.font.inter_semibold_600)
        tv_companyName.typeface = typeface

        val one = company.name.substring(0, 1)
        val two = company.name.substring(1, 2)
        val single = one + two
        companyIma.isVisible = false
        companyImage.isVisible = true
        companyImage.text = single.replace("^\\s*([a-zA-Z]).*\\s+([a-zA-Z])\\S+$".toRegex(), "$1$2")
            .uppercase(Locale.getDefault())

        address.visibility = View.VISIBLE
        tv_companyName.text = company.name
        address.text = company.address

        vehicleOwnerLegalPerson = LegalPersonDetails(
            noRegistration = "",
            vatPayer = false,
            address = company.address2,
            cui = company.pin,
            companyName = company.name,
            caen = null,
            id = null,
            activityType = null,
            logoHref = company.logoHref,
            guid = null,
            coverHref = null
        )

        checkItems()
    }

    private fun setCarDetail(item: VehicleDetails?) {

        if (item != null) {
            val options = RequestOptions()
            cars_list.logo.clipToOutline = true
            Glide.with(requireActivity())
                .load(ApiModule.BASE_URL_RESOURCES + item.brandLogo)
                .apply(
                    options.fitCenter()
                        .skipMemoryCache(true)
                        .priority(Priority.HIGH)
                        .format(DecodeFormat.PREFER_ARGB_8888)
                )
                .error(R.drawable.carhome_icon_roviii)
                .into(cars_list.logo)

            if (item.brandName.isNullOrEmpty() && item.model.isNullOrEmpty())
                cars_list.makeAndModel.text = "N/A"
            else
                cars_list.makeAndModel.text = "${item.brandName ?: ""} ${item.model ?: ""}"
            cars_list.carNumber.text = item.licensePlate

            item.let { v ->
                if (v.policyExpirationDate.isNullOrEmpty()) {
                    cars_list.policyExpiry.text = "N/A"
                    cars_list.status.text = "N/A"
                    cars_list.policyExpiry.setTextColor(
                        requireContext().resources.getColor(
                            R.color.unselected
                        )
                    )
                    cars_list.status.setTextColor(requireContext().resources.getColor(R.color.unselected))

                } else {

                    val dateFormat: DateFormat =
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    val date: Date? =
                        dateFormat.parse(v.policyExpirationDate)
                    val formatter: DateFormat =
                        SimpleDateFormat("dd/MM/yyyy")
                    val dateStr: String =
                        formatter.format(date)
                    cars_list.policyExpiry.text = dateStr
                    cars_list.policyExpiry.setTextColor(
                        requireContext().resources.getColor(
                            R.color.text_color
                        )
                    )

                    val sdf = SimpleDateFormat("dd/MM/yyyy")
                    val strDate = sdf.parse(dateStr)
                    if (System.currentTimeMillis() > strDate.time) {
                        cars_list.status.text =
                            requireActivity().getString(R.string.status_expires)
                        cars_list.status.setTextColor(requireContext().resources.getColor(R.color.redExpiredRovii))
                    } else {
                        cars_list.status.text =
                            requireActivity().getString(R.string.status_active)
                        cars_list.status.setTextColor(requireContext().resources.getColor(R.color.list_time))
                    }
                }
            }
        }
    }

    override fun onRemoveClick(position: Int) {
        driverList.removeAt(position)
        itemsDriverList.removeAt(position)
    }

    override fun onContinueOwner(ownerNaturalItem: NaturalPerson?, ownerLegalItem: LegalPerson?) {
        this.ownerLegalItem = ownerLegalItem
        this.ownerNaturalItem = ownerNaturalItem

        setOwner(true)
    }

    @SuppressLint("SetTextI18n")
    private fun setOwner(toVerify: Boolean) {

        when {
            ownerNaturalItem != null -> {
                if (ownerNaturalItem?.logoHref != null) {
                    mSelectOwnerTv.isVisible = false
                    mSelectOwnerImage.isVisible = true
                    setImage(ownerNaturalItem?.logoHref, mSelectOwnerImage)
                } else {
                    mSelectOwnerTv.isVisible = true
                    mSelectOwnerImage.isVisible = false
                    setText(
                        "${ownerNaturalItem?.firstName} ${ownerNaturalItem?.lastName}",
                        mSelectOwnerTv, true
                    )
                }

                mSelectOwnerName.text =
                    "${ownerNaturalItem?.firstName} ${ownerNaturalItem?.lastName}"

                if (ownerNaturalItem?.email?.isNotEmpty() == true) {
                    mSelectOwnerEmail.isVisible = true
                    mSelectOwnerEmail.text = ownerNaturalItem?.email
                }
                driverCheckBox.isVisible = true
                ownerNaturalItem?.id?.let { viewModel.getNaturalPersonDetails(it, "OWNER") }

//                Default selection of user & driver same as owner, but after validationg individual user & driver
                personTypeToValidate = 10010
                isUserResponseAvailable = false
                isDriverResponseAvailable = false
                if (toVerify) {
                    ownerNaturalItem?.guid?.let { viewModel.verifyUser(it, personTypeToValidate) }
                    ownerNaturalItem?.guid?.let { viewModel.verifyDriver(it) }
                }
                this.ownerLegalItem = null
            }
            ownerLegalItem != null -> {

                if (ownerLegalItem?.logoHref != null) {
                    mSelectOwnerTv.isVisible = false
                    mSelectOwnerImage.isVisible = true
                    setImage(ownerLegalItem?.logoHref, mSelectOwnerImage)
                } else {
                    mSelectOwnerTv.isVisible = true
                    mSelectOwnerImage.isVisible = false
                    ownerLegalItem?.companyName?.let { setText(it, mSelectOwnerTv, false) }
                }

                mSelectOwnerName.text = ownerLegalItem?.companyName
                driverCheckBox.isVisible = false
                ownerLegalItem?.id?.toLong().let {
                    if (it != null) {
                        viewModel.getLegalPersonDetails(it, "OWNER")
                    }
                }

//                Default selection of user & driver same as owner, but after validationg individual user & driver
                personTypeToValidate = 10020
                isUserResponseAvailable = false
                isDriverResponseAvailable = false
                if (toVerify) {
                    ownerLegalItem?.guid?.let { viewModel.verifyUser(it, personTypeToValidate) }
                }
                this.ownerNaturalItem = null
            }
        }
    }


    override fun onContinueUser(userNaturalItem: NaturalPerson?, userLegalItem: LegalPerson?) {
        this.userLegalItem = userLegalItem
        this.userNaturalItem = userNaturalItem
        setUser()
    }

    private fun setUser() {
        when {
            userNaturalItem != null -> {
                if (userNaturalItem?.logoHref != null) {
                    mSelectUserTv.isVisible = false
                    mSelectUserImage.isVisible = true
                    setImage(userNaturalItem?.logoHref, mSelectUserImage)
                } else {
                    mSelectUserTv.isVisible = true
                    mSelectUserImage.isVisible = false
                    setText(
                        "${userNaturalItem?.firstName} ${userNaturalItem?.lastName}",
                        mSelectUserTv, true
                    )
                }

                mSelectUserName.text = "${userNaturalItem?.firstName} ${userNaturalItem?.lastName}"
                if (userNaturalItem?.email != null) {
                    mSelectUserEmail.text = userNaturalItem?.email
                    mSelectUserEmail.isVisible = true
                } else {
                    mSelectUserEmail.isVisible = false
                }
                userNaturalItem?.id?.let { viewModel.getNaturalPersonDetails(it, "USER") }
                this.userLegalItem = null
            }
            userLegalItem != null -> {

                if (userLegalItem?.logoHref != null) {
                    mSelectUserTv.isVisible = false
                    mSelectUserImage.isVisible = true
                    setImage(userLegalItem?.logoHref, mSelectUserImage)
                } else {
                    mSelectUserTv.isVisible = true
                    mSelectUserImage.isVisible = false
                    userLegalItem?.companyName?.let { setText(it, mSelectUserTv, false) }
                }
                mSelectUserName.text = userLegalItem?.companyName
                mSelectUserEmail.isVisible = false
                userLegalItem?.id?.toLong().let {
                    if (it != null) {
                        viewModel.getLegalPersonDetails(it, "USER")
                    }
                }
                this.userNaturalItem = null
            }
        }
    }

    override fun onContinueDriver(driverNaturalItem: NaturalPerson?) {
        this.driverNaturalItem = driverNaturalItem
        setDriver()
    }

    private fun setDriver() {
        if (driverNaturalItem != null) {

            driverList.clear()
            if (driverNaturalItem?.logoHref != null) {
                mSelectDriverTv.isVisible = false
                mSelectDriverImage.isVisible = true
                setImage(driverNaturalItem?.logoHref, mSelectDriverImage)
            } else {
                mSelectDriverTv.isVisible = true
                mSelectDriverImage.isVisible = false
                setText(
                    "${driverNaturalItem?.firstName} ${driverNaturalItem?.lastName}",
                    mSelectDriverTv, true
                )
            }

            mSelectDriverName.text =
                "${driverNaturalItem?.firstName} ${driverNaturalItem?.lastName}"
            mSelectDriverEmail.isVisible = true
            if (driverNaturalItem?.email != null) {
                mSelectDriverEmail.text = driverNaturalItem?.email
            } else {
                mSelectDriverEmail.isVisible = false
            }
            driverNaturalItem?.id?.let { viewModel.getNaturalPersonDetails(it, "DRIVER") }
        }
    }


    override fun openNewUser(type: String?) {
        if (type != null) {
            addNew = true
            addNewType = type
            bottomSheet?.dismiss()
        }
    }

    override fun onContinueDriverNew(driverNewNaturalItem: NaturalPerson?) {
        this.driverNewNaturalItem = driverNewNaturalItem
        if (driverNewNaturalItem != null) {
            var exist = false
            for (i in driverList.indices) {
                if (driverNewNaturalItem.id == driverList[i].id?.toLong()) {
                    exist = true
                    break
                } else
                    exist = false
            }

            if (driverCheckBox.isChecked) {
                for (i in driverList.indices) {
                    if (driverNewNaturalItem.id == driverList[i].id?.toLong()) {
                        exist = true
                        break
                    } else
                        exist = false
                }

            }

            if (!exist) {
                itemsDriverList.add(driverNewNaturalItem)
                driverNewNaturalItem.id?.let { viewModel.getNaturalPersonDetails(it, "DRIVER") }
                mRecycle.layoutManager =
                    LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                driverAdapter = DriverAdapter(requireActivity(), this)
                mRecycle.adapter = driverAdapter
                driverAdapter.setItems(itemsDriverList)
            }

        }
    }

    private fun checkItems() {

        val dataCompleted =
            (vehicleOwnerLegalPerson != null || vehicleOwnerNaturalPerson != null) &&
                    (vehicleUserNaturalPerson != null || vehicleUserLegalPerson != null)
                    && selectedVehicle != null && driverList.isNotEmpty()

        if (dataCompleted) {
            mContinue.background =
                requireContext().resources.getDrawable(R.drawable.save_background)
            mContinue.isClickable = true
        } else {
            mContinue.background =
                requireContext().resources.getDrawable(R.drawable.save_background_invisible)
            mContinue.isClickable = false
        }

    }

}