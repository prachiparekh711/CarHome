package ro.westaco.carhome.presentation.screens.service.insurance

import android.annotation.SuppressLint
import android.os.Build
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_insurance.*
import kotlinx.android.synthetic.main.item_in_cars.view.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import ro.westaco.carhome.data.sources.remote.requests.RcaOfferRequest
import ro.westaco.carhome.data.sources.remote.responses.models.*
import ro.westaco.carhome.di.ApiModule
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.service.insurance.SelectUserFragment.Companion.driverNaturalItem
import ro.westaco.carhome.presentation.screens.service.insurance.SelectUserFragment.Companion.ownerLegalItem
import ro.westaco.carhome.presentation.screens.service.insurance.SelectUserFragment.Companion.ownerNaturalItem
import ro.westaco.carhome.presentation.screens.service.insurance.SelectUserFragment.Companion.userLegalItem
import ro.westaco.carhome.presentation.screens.service.insurance.SelectUserFragment.Companion.userNaturalItem
import ro.westaco.carhome.presentation.screens.service.insurance.SelectUserFragment.Companion.verifyLegalList
import ro.westaco.carhome.presentation.screens.service.insurance.SelectUserFragment.Companion.verifyNaturalList
import ro.westaco.carhome.presentation.screens.service.insurance.adapter.DriverAdapter
import ro.westaco.carhome.presentation.screens.service.insurance.leasing_c.LeasingInFragment
import ro.westaco.carhome.presentation.screens.service.insurance.selectcar.SelectCarsFragment
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class InsuranceFragment : BaseFragment<InsuranceViewModel>(),
    LeasingInFragment.OnDialogInteractionListener,
    SelectCarsFragment.OnCarSelectionSetListener,
    DriverAdapter.OnRemoveListener,
    SelectUserFragment.OnOwnerSelectionListener,
    SelectUserFragment.OnUserSelectionListener,
    SelectUserFragment.OnDriverSelectionListener,
    SelectUserFragment.OnNewDriverSelectionListener,
    SelectUserFragment.AddNewUserView {

    var mView: View? = null
    private var selectCarsFragment: SelectCarsFragment? = null
    lateinit var driverAdapter: DriverAdapter
    var driverList = ArrayList<NaturalPersonForOffer>()
    val itemsDriverList = ArrayList<NaturalPerson>()
    var carList: ArrayList<Vehicle>? = null
    var leasingCompanyList = ArrayList<LeasingCompany>()
    var selectedVehicle: VehicleDetails? = null
    var vehicleItem: Vehicle? = null
    var vehicleOwnerLegalPerson: LegalPersonDetails? = null
    var vehicleOwnerNaturalPerson: NaturalPersonForOffer? = null
    var vehicleUserNaturalPerson: NaturalPersonForOffer? = null
    var vehicleUserLegalPerson: LegalPersonDetails? = null
    var bottomSheet: SelectUserFragment? = null

    companion object {
        const val TAG = "Insurance"
        var addNew = false
        var addNewType = ""
    }

    override fun getContentView() = R.layout.fragment_insurance

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun initUi() {

        requireView().isFocusableInTouchMode = true
        requireView().requestFocus()
        requireView().setOnKeyListener { v, keyCode, event ->
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

        if (vehicleItem != null) {
            cars_list.visibility = View.VISIBLE
            setCarDetail(vehicleItem!!)
        }

        selectCarLL.setOnClickListener {
            if (mFirst.isChecked) {
                selectCarsFragment = carList?.let { it1 -> SelectCarsFragment(it1) }
                selectCarsFragment.let {
                    if (it != null) {
                        childFragmentManager.beginTransaction()
                            .replace(R.id.selectCarFL, it)
                            .commit()
                    }
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    requireContext().resources.getString(R.string.select_car_info),
                    Toast.LENGTH_SHORT
                ).show()
            }
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

        setOwner()
        userCheckBox.isVisible = !leasingCheckbox.isChecked
        driverCheckBox.isVisible = !leasingCheckbox.isChecked
        leasingCheckbox.setOnCheckedChangeListener { _, isChecked ->
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
        }

        userCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                setUserSameAsOwner()
            } else {
                setUserAsBlank()
            }
        }

        driverCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                setDriverSameAsOwner()
            } else {
                setDriverAsBlank()
            }
        }


        driverAdapter = DriverAdapter(requireContext(), this)

        mContinue.setOnClickListener {


            if (selectedVehicle != null) {
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
                viewModel.onCta(request)
            } else {
                Toast.makeText(
                    requireContext(),
                    requireContext().resources.getString(R.string.select_car_info),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    fun onBackPress() {
        addNew = false
        ownerLegalItem = null
        ownerNaturalItem = null
        userNaturalItem = null
        userLegalItem = null
        driverNaturalItem = null
        verifyNaturalList = null
        verifyLegalList = null
        viewModel.onBack()
    }

    fun setUserAsBlank() {
        mSelectUserName.text = requireContext().resources.getString(R.string.select_user)
        mSelectUserEmail.isVisible = false
        mSelectUserImage.setImageDrawable(requireContext().resources.getDrawable(R.drawable.ic_profile_picture))
    }

    fun setDriverAsBlank() {
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

    private fun setOwner() {

        when {
            ownerLegalItem != null -> {
                mSelectOwnerName.text = ownerLegalItem?.companyName
                mSelectOwnerEmail.isVisible = false
                ownerLegalItem?.logoHref?.let { setImage(it, mSelectOwnerImage) }
                ownerLegalItem?.id?.toLong().let {
                    if (it != null) {
                        viewModel.getLegalPersonDetails(it, "OWNER")
                    }
                }
            }
            ownerNaturalItem != null -> {
                mSelectOwnerName.text =
                    "${ownerNaturalItem?.firstName} ${ownerNaturalItem?.lastName}"
                mSelectOwnerEmail.isVisible = true
                mSelectOwnerEmail.text = ownerNaturalItem?.email
                ownerNaturalItem?.logoHref?.let { setImage(it, mSelectOwnerImage) }
                ownerNaturalItem?.id?.let { viewModel.getNaturalPersonDetails(it, "OWNER") }
            }
            else -> {
                mSelectOwnerName.text = requireContext().resources.getString(R.string.select_owner)
                mSelectOwnerEmail.isVisible = false
                mSelectOwnerImage.setImageDrawable(requireContext().resources.getDrawable(R.drawable.ic_profile_picture))
            }
        }
    }

    fun setUserSameAsOwner() {
        when {
            ownerLegalItem != null -> {
                userLegalItem = ownerLegalItem
                mSelectUserName.text = userLegalItem?.companyName
                mSelectUserEmail.isVisible = false
                ownerLegalItem?.logoHref?.let { setImage(it, mSelectUserImage) }
                userLegalItem?.id?.toLong().let {
                    if (it != null) {
                        viewModel.getLegalPersonDetails(it, "USER")
                    }
                }
            }
            ownerNaturalItem != null -> {
                userNaturalItem = ownerNaturalItem
                mSelectUserName.text =
                    "${userNaturalItem?.firstName} ${userNaturalItem?.lastName}"
                mSelectUserEmail.isVisible = true
                mSelectUserEmail.text = userNaturalItem?.email
                userNaturalItem?.logoHref?.let { setImage(it, mSelectUserImage) }
                userNaturalItem?.id?.let { viewModel.getNaturalPersonDetails(it, "USER") }
            }
            else -> {
                mSelectUserName.text = requireContext().resources.getString(R.string.select_user)
                mSelectUserEmail.isVisible = false
                mSelectUserImage.setImageDrawable(requireContext().resources.getDrawable(R.drawable.ic_profile_picture))
            }
        }
    }

    fun setDriverSameAsOwner() {
        when {
            ownerNaturalItem != null -> {
                driverNaturalItem = ownerNaturalItem
                mSelectDriverName.text =
                    "${driverNaturalItem?.firstName} ${driverNaturalItem?.lastName}"
                mSelectDriverEmail.isVisible = true
                mSelectDriverEmail.text = driverNaturalItem?.email
                driverNaturalItem?.logoHref?.let { setImage(it, mSelectDriverImage) }
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

    override fun setObservers() {
        viewModel.carsLivedata.observe(viewLifecycleOwner) { cars ->
            this.carList = cars
        }

        viewModel.leasingCompaniesData.observe(viewLifecycleOwner) { leasingData ->
            this.leasingCompanyList = leasingData

            mSelectLeasing.setOnClickListener {

                if (!leasingCompanyList.isNullOrEmpty()) {
                    val dialog = LeasingInFragment(leasingCompanyList)
                    dialog.listener = this
                    dialog.show(childFragmentManager, LeasingInFragment.TAG)
                } else {
                    Toast.makeText(
                        requireContext(),
                        requireContext().resources.getString(R.string.no_companies_found),
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
        }

        viewModel.currentRcaData.observe(viewLifecycleOwner) { rca ->
            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                selectCarsFragment?.let {
                    childFragmentManager.beginTransaction().remove(it).commit()

                    cars_list.visibility = View.VISIBLE
                    this.selectedVehicle = rca.vehicle

                    /*  if (rca.vehicleOwnerLegalPerson != null) {
                          rca.vehicleOwnerLegalPerson.logoHref?.let { it1 ->
                              setImage(
                                  it1,
                                  mSelectOwnerImage
                              )
                          }
                          mSelectOwnerName.text = rca.vehicleOwnerLegalPerson.companyName
                      }

                      if (rca.vehicleOwnerNaturalPerson != null) {
                          rca.vehicleOwnerNaturalPerson.logoHref?.let { it1 ->
                              setImage(
                                  it1,
                                  mSelectOwnerImage
                              )
                          }
                          mSelectOwnerName.text =
                              "${rca.vehicleOwnerNaturalPerson.firstName ?: ""} ${rca.vehicleOwnerNaturalPerson.lastName ?: ""}"

                          if (rca.vehicleOwnerNaturalPerson.email?.isNotEmpty() == true) {
                              mSelectOwnerEmail.isVisible = true
                              mSelectOwnerEmail.text = rca.vehicleOwnerNaturalPerson.email
                          }
                      }

                      if (rca.vehicleUserLegalPerson != null) {
                          rca.vehicleUserLegalPerson.logoHref?.let { it1 ->
                              setImage(
                                  it1,
                                  mSelectUserImage
                              )
                          }
                          mSelectOwnerName.text = rca.vehicleUserLegalPerson.companyName
                      }

                      if (rca.vehicleUserNaturalPerson != null) {
                          rca.vehicleUserNaturalPerson.logoHref?.let { it1 ->
                              setImage(
                                  it1,
                                  mSelectUserImage
                              )
                          }
                          mSelectOwnerName.text =
                              "${rca.vehicleUserNaturalPerson.firstName} ${rca.vehicleUserNaturalPerson.lastName}"

                          if (rca.vehicleUserNaturalPerson.email?.isNotEmpty() == true) {
                              mSelectOwnerEmail.isVisible = true
                              mSelectOwnerEmail.text = rca.vehicleUserNaturalPerson.email
                          }
                      }

                      userCheckBox.isChecked = rca.vehicleUserSameAsOwner == true
                      driverCheckBox.isChecked = rca.driverSameAsOwner == true

                      driverList = rca.drivers as ArrayList<NaturalPersonForOffer>
                      if (!driverList.isNullOrEmpty()) {
                          if (driverList.size > 1) {
                              driverAdapter.setItems(driverList)
                          } else {
                              val driverDetail = driverList[0]
  //                        driverDetail.logoHref?.let { it1 -> setImage(it1, mSelectDriverImage) }
                              mSelectDriverName.text =
                                  "${driverDetail.firstName} ${driverDetail.lastName}"
                              if (driverDetail.email?.isNotEmpty() == true) {
                                  mSelectDriverEmail.isVisible = true
                                  mSelectDriverEmail.text = driverDetail.email
                              }
                          }
                      }*/
                }
            }
        }


        viewModel.actionStream.observe(viewLifecycleOwner) {
            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                when (it) {
                    is InsuranceViewModel.ACTION.onGetLegalDetails -> {
                        when (it.personType) {
                            "OWNER" -> vehicleOwnerLegalPerson = it.item
                            "USER" -> vehicleUserLegalPerson = it.item
                        }
                    }
                    is InsuranceViewModel.ACTION.onGetNaturalDetails -> {
                        when (it.personType) {
                            "OWNER" -> vehicleOwnerNaturalPerson = it.item
                            "USER" -> vehicleUserNaturalPerson = it.item
                            "DRIVER" -> driverList.add(it.item)
                        }
                    }
                }
            }
        }

    }

    private fun setImage(href: String, view: ImageView) {
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
                    .skipMemoryCache(true)
                    .priority(Priority.HIGH)
                    .format(DecodeFormat.PREFER_ARGB_8888)
            )
            .into(view)
    }

    override fun onCompanyUpdated(company: LeasingCompany) {

        val typeface = ResourcesCompat.getFont(requireContext(), R.font.inter_semibold_600)
        tv_companyName.typeface = typeface

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
    }

    override fun onCarSelectionSet(item: Vehicle) {
        vehicleItem = item
        setCarDetail(item)
        viewModel.identifyVehicle(item)
    }

    private fun setCarDetail(item: Vehicle) {
        val options = RequestOptions()
        cars_list.logo.clipToOutline = true
        Glide.with(requireActivity())
            .load(ApiModule.BASE_URL_RESOURCES + item.vehicleBrandLogo)
            .apply(
                options.fitCenter()
                    .skipMemoryCache(true)
                    .priority(Priority.HIGH)
                    .format(DecodeFormat.PREFER_ARGB_8888)
            )
            .error(R.drawable.carhome_icon_roviii)
            .into(cars_list.logo)

        cars_list.makeAndModel.text = "${item.vehicleBrand ?: ""} ${item.model ?: ""}"
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
                    SimpleDateFormat("dd-MM-yyyy")
                val dateStr: String =
                    formatter.format(date!!)
                cars_list.policyExpiry.text = dateStr
                cars_list.policyExpiry.setTextColor(
                    requireContext().resources.getColor(
                        R.color.text_color
                    )
                )

                val sdf = SimpleDateFormat("dd-MM-yyyy")
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

    override fun onBackFromCarList() {
        selectCarsFragment?.let { childFragmentManager.beginTransaction().remove(it).commit() }
    }

    override fun onRemoveClick(position: Int) {
        driverList.removeAt(position)
        itemsDriverList.removeAt(position)
    }


    override fun onContinueOwner(ownerNaturalItem: NaturalPerson?, ownerLegalItem: LegalPerson?) {

        when {
            ownerNaturalItem != null -> {
                ownerNaturalItem.logoHref?.let { it1 -> setImage(it1, mSelectOwnerImage) }
                mSelectOwnerName.text = "${ownerNaturalItem.firstName} ${ownerNaturalItem.lastName}"

                if (ownerNaturalItem.email?.isNotEmpty() == true) {
                    mSelectOwnerEmail.isVisible = true
                    mSelectOwnerEmail.text = ownerNaturalItem.email
                }
                driverCheckBox.isVisible = true
                ownerNaturalItem.id?.let { viewModel.getNaturalPersonDetails(it, "OWNER") }
            }
            ownerLegalItem != null -> {
                ownerLegalItem.logoHref?.let { it1 -> setImage(it1, mSelectOwnerImage) }
                mSelectOwnerName.text = ownerLegalItem.companyName
                driverCheckBox.isVisible = false
                ownerLegalItem.id?.toLong().let {
                    if (it != null) {
                        viewModel.getLegalPersonDetails(it, "OWNER")
                    }
                }
            }
        }

        if (userCheckBox.isChecked)
            setUserSameAsOwner()
    }

    override fun onContinueUser(userNaturalItem: NaturalPerson?, userLegalItem: LegalPerson?) {

        when {
            userNaturalItem != null -> {
                userNaturalItem.logoHref?.let { it1 -> setImage(it1, mSelectUserImage) }
                mSelectUserName.text = "${userNaturalItem.firstName} ${userNaturalItem.lastName}"
                if (userNaturalItem.email != null) {
                    mSelectUserEmail.text = userNaturalItem.email
                    mSelectUserEmail.isVisible = true
                } else {
                    mSelectUserEmail.isVisible = false
                }
                userNaturalItem.id?.let { viewModel.getNaturalPersonDetails(it, "USER") }
            }
            userLegalItem != null -> {
                val singlechar = "${userLegalItem.companyName}"

                if (userLegalItem.logoHref != null) {

                    tv_text.visibility = View.INVISIBLE
                    mSelectUserImage.visibility = View.VISIBLE
                    setImage(userLegalItem.logoHref, mSelectUserImage)
                } else {
                    tv_text.visibility = View.VISIBLE
                    mSelectUserImage.visibility = View.INVISIBLE
                    tv_text.text = singlechar.replace(
                        "^\\s*([a-zA-Z]).*\\s+([a-zA-Z])\\S+$".toRegex(),
                        "$1$2"
                    ).uppercase(Locale.getDefault())
                }
                mSelectUserName.text = userLegalItem.companyName
                mSelectUserEmail.isVisible = false
                userLegalItem.id?.toLong().let {
                    if (it != null) {
                        viewModel.getLegalPersonDetails(it, "USER")
                    }
                }
            }
        }
    }

    override fun onContinueDriver(driverNaturalItem: NaturalPerson?) {
        if (driverNaturalItem != null) {
            driverNaturalItem.logoHref?.let { it1 -> setImage(it1, mSelectDriverImage) }
            mSelectDriverName.text =
                "${driverNaturalItem.firstName} ${driverNaturalItem.lastName}"
            mSelectDriverEmail.isVisible = true
            if (driverNaturalItem.email != null) {
                mSelectDriverEmail.text = driverNaturalItem.email
            } else {
                mSelectDriverEmail.isVisible = false
            }
            driverNaturalItem.id?.let { viewModel.getNaturalPersonDetails(it, "DRIVER") }
        }
    }

    override fun onResume() {

        super.onResume()
        if (addNew) {
            bottomSheet = SelectUserFragment(this, null, null, this, null, addNewType)
            bottomSheet?.show(requireActivity().supportFragmentManager, null)
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

}