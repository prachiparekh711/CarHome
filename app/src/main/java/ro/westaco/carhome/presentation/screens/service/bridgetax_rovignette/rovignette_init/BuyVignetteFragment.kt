package ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.rovignette_init

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_buy_vignette.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.*
import ro.westaco.carhome.databinding.BottomSheetDurationBinding
import ro.westaco.carhome.databinding.DifferentCategoryLayoutBinding
import ro.westaco.carhome.databinding.VehicleCatLayoutBinding
import ro.westaco.carhome.di.ApiModule
import ro.westaco.carhome.dialog.DialogUtils.Companion.showErrorInfo
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.data.commen.CountryCodeDialog
import ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.adapter.BuyDurationAdapter
import ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.adapter.CategoryAdapter
import ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.rovignette_init.BuyVignetteViewModel.ACTION
import ro.westaco.carhome.utils.FirebaseAnalyticsList
import ro.westaco.carhome.utils.RegexData
import ro.westaco.carhome.views.Progressbar
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class BuyVignetteFragment : BaseFragment<BuyVignetteViewModel>(),
    CountryCodeDialog.CountryCodePicker,
    BuyDurationAdapter.OnItemInteractionListener,
    CategoryAdapter.OnItemInteractionListener {

    private var vehicle: Vehicle? = null
    private lateinit var adapter: CategoryAdapter
    private lateinit var adapterDuration: BuyDurationAdapter
    lateinit var bottomSheetCategory: BottomSheetDialog
    lateinit var bottomSheetDuration: BottomSheetDialog
    var bindingCategory: VehicleCatLayoutBinding? = null
    var bindingDuration: BottomSheetDurationBinding? = null
    var vignetteList: ArrayList<ServiceCategory> = ArrayList()
    var durationList: ArrayList<RovignetteDuration> = ArrayList()
    var priceListNew: ArrayList<VignettePrice> = ArrayList()
    var countries: ArrayList<Country> = ArrayList()
    var priceList: ArrayList<VignettePrice> = ArrayList()
    var priceModel: VignettePrice? = null
    var categoryBottomSheet: BottomSheetDialog? = null
    var countryItem: Country? = null
    var activeService: String = ""
    var repeatVignettePos = 0
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
    private var vehicleDetail: VehicleDetails? = null
    var dataCompleted = false
    var progressbar: Progressbar? = null
    var vignetteExpirationDate: String? = null
    lateinit var xStr: String   //    X   =   Start date
    lateinit var yStr: String   //    Y   =   End date
    lateinit var x: Date   //    X   =   Start date
    lateinit var y: Date   //    Y   =   End date
    var isValidRange = false

    companion object {
        const val ARG_CAR = "arg_car"
        const val ARG_ENTER_VALUE = "arg_enter_value"
    }

    override fun getContentView() = R.layout.fragment_buy_vignette

    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        arguments?.let {
            vehicle = it.getSerializable(ARG_CAR) as? Vehicle?
            activeService = it.getString(ARG_ENTER_VALUE).toString()
            vehicle?.let { v -> viewModel.onVehicle(v) }

        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())

        val params = Bundle()
        mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.ACCESS_ROVINIETA_ANDROID, params)

        bottomSheetCategory = BottomSheetDialog(requireContext())
        bottomSheetDuration = BottomSheetDialog(requireContext())

    }

    override fun initUi() {
        progressbar = Progressbar(requireContext())

        back.setOnClickListener {
            viewModel.onBack()
        }

        mPrevious.setOnClickListener {

            viewModel.onBack()
        }

        categoryBottomSheet = BottomSheetDialog(requireContext())
        val bindingSheet = DataBindingUtil.inflate<DifferentCategoryLayoutBinding>(
            layoutInflater,
            R.layout.different_category_layout,
            null,
            false
        )
        categoryBottomSheet?.setContentView(bindingSheet.root)
        bindingSheet?.cta?.setOnClickListener { categoryBottomSheet?.dismiss() }

        priceList.clear()

        vignetteExpirationDate = vehicle?.vignetteExpirationDate
        isValidRange = if (vignetteExpirationDate == null) {
            dateDefaultCase(isActive = false, policyDate = null)
        } else {
            val dateFormat: DateFormat =
                SimpleDateFormat(requireContext().getString(R.string.server_standard_datetime_format_template1))
            val policyDate = dateFormat.parse(vignetteExpirationDate)

            if (System.currentTimeMillis() > policyDate.time) {
                //               Expires
                dateDefaultCase(isActive = false, policyDate = null)
            } else {
                //               Active
                dateDefaultCase(isActive = true, policyDate = policyDate)
            }
        }

        mStartDateText.setOnClickListener {
            if (isValidRange)
                viewModel.onDateClicked(dateToMilis(mStartDateText.text.toString()))

        }

//        *******
//        val calendar = Calendar.getInstance()
//        calendar.add(Calendar.DAY_OF_YEAR, 1)
//        val tomorrow = calendar.time
//        val formatter: DateFormat = SimpleDateFormat("dd/MM/yyyy")
//        val currentDate: String = formatter.format(tomorrow)
//        mStartDateText.setText(currentDate)
//        changeTint(mStartDateText)
//        mStartDateText.setOnClickListener { viewModel.onDateClicked() }
//        ************

        licensePlate.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.toString()?.let { checkData() }
            }
        })

        check.setOnCheckedChangeListener { _, _ ->
            checkData()
        }

        cta.setOnClickListener {

            if (licensePlate.text?.isNotEmpty() == true) {
                var valid = true
                when (countryItem?.code) {

                    "ROU" -> {
                        if (!RegexData.checkNumberPlateROU(licensePlate.text.toString())) {
                            valid = false
                        }
                    }

                    "QAT" -> {
                        if (!RegexData.checkNumberPlateQAT(licensePlate.text.toString())) {
                            valid = false
                        }
                    }

                    "UKR" -> {
                        if (!RegexData.checkNumberPlateUKR(licensePlate.text.toString())) {
                            valid = false
                        }
                    }

                    "BGR" -> {
                        if (!RegexData.checkNumberPlateBGR(licensePlate.text.toString())) {
                            valid = false
                        }
                    }

                }

                if (!valid) {
                    licensePlate.setTextColor(requireContext().resources.getColor(R.color.delete_dialog_color))
                    licensePlate.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.error_icon,
                        0
                    )
                    number_plate_error.isVisible = true
                    return@setOnClickListener
                }
            }

            if (dataCompleted) {
                number_plate_error.isVisible = licensePlate.text.isNullOrEmpty()
                if (vin_error.isVisible) {
                    vinET.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    vin_error.isVisible = false
                }

                if (number_plate_error.isVisible) {
                    licensePlate.setTextColor(requireContext().resources.getColor(R.color.service_text_color))
                    licensePlate.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    number_plate_error.isVisible = false
                }

                if (startDateError.isVisible) {
                    mStartDateText.background =
                        requireContext().resources.getDrawable(R.drawable.auth_text_input_background)
                    mStartDateText.setTextColor(requireContext().resources.getColor(R.color.textOnWhiteAccentGray))
                    startDateError.isVisible = false
                }

                if (priceModel == null) {

                    showErrorInfo(requireContext(), getString(R.string.duration_required))

                }
                var vinStr = if (vinET.text.isNullOrEmpty()) {
                    null
                } else {
                    vinET.text.toString()
                }

                if (vinET.text?.isNotEmpty() == true) {

                    if (vinET.text?.length != 17) {
                        showErrorInfo(requireContext(), getString(R.string.error_vin))
                        return@setOnClickListener
                    }
                    vinStr = vinET.text.toString()

                }
                priceModel?.let { it1 ->
                    countryItem?.code?.let { it2 ->
                        progressbar?.showPopup()
                        viewModel.onCta(
                            vehicle,
                            rovignette_categories?.text.toString(),
                            mStartDateText.text.toString(),
                            registrationCountryCode = it2,
                            licensePlate.text.toString(),
                            vinStr,
                            it1,
                            check.isChecked,
                            activeService
                        )
                    }
                }
            }
        }

        carHeader.setOnClickListener {
            carInfoBlock.isVisible = !carInfoBlock.isVisible
            if (carInfoBlock.isVisible) {
                carHeader.setBackgroundResource(R.color.expande_colore)
                carInfoArrowIcon.setImageDrawable(requireContext().resources.getDrawable(R.drawable.ic_arrow_circle_up))
            } else {
                carHeader.setBackgroundResource(R.color.white)
                carInfoArrowIcon.setImageDrawable(requireContext().resources.getDrawable(R.drawable.ic_arrow_circle_down))
            }
        }

        serviceHeader.setOnClickListener {
            rovinietaTaxServiceBlock.isVisible = !rovinietaTaxServiceBlock.isVisible
            if (rovinietaTaxServiceBlock.isVisible) {
                serviceHeader.setBackgroundResource(R.color.expande_colore)
                serviceInfoArrowIcon.setImageDrawable(requireContext().resources.getDrawable(R.drawable.ic_arrow_circle_up))
            } else {
                serviceHeader.setBackgroundResource(R.color.white)
                serviceInfoArrowIcon.setImageDrawable(requireContext().resources.getDrawable(R.drawable.ic_arrow_circle_down))
            }
        }


        mDuration?.setOnClickListener {
            bottomSheetDuration.show()
        }

    }

    private var dpd: DatePickerDialog? = null
    fun showDatePicker(dateInMillis: Long) {
        val mCalendar = Calendar.getInstance()
        val c = mCalendar.apply {
            timeInMillis = dateInMillis
        }

        dpd?.cancel()
        dpd = DatePickerDialog(
            requireContext(), R.style.DialogTheme, { _, year, monthOfYear, dayOfMonth ->
                viewModel.onDatePicked(
                    Calendar.getInstance().apply {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, monthOfYear)
                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
//                         set(Calendar.HOUR,get(Calendar.HOUR))
//                         set(Calendar.MINUTE,get(Calendar.MINUTE))
//                         set(Calendar.SECOND,get(Calendar.SECOND))
                    }.timeInMillis
                )
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
        )

        val xMillis = dateToMilis(xStr) //    X   =   Start date
        val yMillis = dateToMilis(yStr) //    Y   =   End date

        dpd?.datePicker?.minDate = xMillis
        dpd?.datePicker?.maxDate = yMillis

        dpd?.show()
    }

    override fun setObservers() {
        viewModel.countryData.observe(viewLifecycleOwner) { countryData ->

            this.countries = countryData

            if (vehicleDetail != null) {

                val pos = vehicleDetail?.registrationCountryCode?.let {
                    Country.findPositionForCode(
                        countries,
                        it
                    )
                }
                this.countryItem = pos?.let { countries[it] }
            } else {
                this.countryItem = countries[Country.findPositionForCode(countries)]
            }
            li_dialog.setOnClickListener {
                val countryCodeDialog = CountryCodeDialog(requireActivity(), countries, this)
                countryCodeDialog.show(requireActivity().supportFragmentManager, null)
            }

        }

        viewModel.rovignetteCategories.observe(viewLifecycleOwner) { rovignetteCategories ->
            this.vignetteList = rovignetteCategories
            vignetteCategory()

            if (vignetteList.isNotEmpty())
                rovignette_categories?.setText(vignetteList[0].shortDescription)

            rovignette_categories?.setOnClickListener {

                val metrics = resources.displayMetrics
                bottomSheetCategory.behavior.peekHeight = metrics.heightPixels / 1
                bottomSheetCategory.behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                bottomSheetCategory.show()
            }
        }

        viewModel.rovignetteDurations.observe(viewLifecycleOwner) { rovignetteDurations ->
            this.durationList = rovignetteDurations
        }

        viewModel.vignettePricesLivedata.observe(viewLifecycleOwner) {
            priceListNew.clear()
            if (it != null) {
                if (it.isNotEmpty()) {
                    priceList = it
                }
            }


            if (durationList.isNotEmpty()) {
                durationVignette(0)
            }
        }

        viewModel.vehicleDetailsLivedata.observe(viewLifecycleOwner) { vehicleDetails ->

            this.vehicleDetail = vehicleDetails
            if (vehicleDetails != null) {
                licensePlate.setText(vehicleDetails.licensePlate)

                mCarNumberText.setText(vehicleDetails.licensePlate)
                vinET.setText(vehicleDetails.vehicleIdentificationNumber)

                mVINNumberText.setText(vehicleDetails.vehicleIdentificationNumber)
                vinLabelI.isVisible = true

                val options = RequestOptions()
                carLogo.clipToOutline = true

                Glide.with(requireContext())
                    .load(ApiModule.BASE_URL_RESOURCES + vehicle?.vehicleBrandLogo)
                    .apply(
                        options.fitCenter()
                            .skipMemoryCache(true)
                            .priority(Priority.HIGH)
                            .format(DecodeFormat.PREFER_ARGB_8888)
                    )
                    .error(R.drawable.carhome_icon_roviii)
                    .into(carLogo)

                mCarName.text = vehicle?.vehicleBrand ?: ""

                mCarInformation.isVisible = false
                mCarSelected.isVisible = true
            }
        }

        viewModel.dateLiveData.observe(viewLifecycleOwner) { dateMillis ->

            mStartDateText.isVisible = true
            mStartDateText.setText(
                SimpleDateFormat(
                    getString(R.string.date_format_template),
                    Locale.getDefault()
                ).format(Date(dateMillis))
            )
            mStartDateText.setTextColor(requireContext().resources.getColor(R.color.textOnWhite))
            changeTint(mStartDateText)
            startDateError.isVisible = false
        }

        viewModel.stateStream.observe(viewLifecycleOwner) { state ->

            activity?.window?.statusBarColor =
                ContextCompat.getColor(requireContext(), R.color.white)

            when (state) {
                BuyVignetteViewModel.STATE.EnterLpn -> {
                    licensePlate.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_info,
                        0
                    )
                    number_plate_error.isVisible = true
                }

                BuyVignetteViewModel.STATE.EnterVin -> {
                    vinLabelI.isVisible = true
                }

                BuyVignetteViewModel.STATE.ErrorVin -> {
                    vinLabelI.isVisible = true
                    vinET.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_info, 0)
                    vin_error.isVisible = true
                }

                BuyVignetteViewModel.STATE.EnterCategory -> {
                    categoryBottomSheet?.show()
                }
                BuyVignetteViewModel.STATE.StopProgress -> {
                    progressbar?.dismissPopup()
                }

            }
        }

        viewModel.actionStream.observe(viewLifecycleOwner) {

            when (it) {
                is ACTION.ShowDatePicker -> showDatePicker(it.dateInMillis)
                is ACTION.ShowError -> showErrorInfo(requireContext(), it.error)

                is ACTION.ShowDateError -> {
                    showDateError(it.error)
                }
            }
        }

    }

    private fun showDateError(error: String?) {
        mStartDateText.setTextColor(requireContext().resources.getColor(R.color.orangeExpired))
        TextViewCompat.setCompoundDrawableTintList(
            mStartDateText,
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.orangeExpired
                )
            )
        )
        mStartDateText.setTextColor(requireContext().resources.getColor(R.color.orangeExpired))
        if (error != null) {
            startDateError.isVisible = true
            startDateError.text = error
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun vignetteCategory() {
        bindingCategory = DataBindingUtil.inflate<VehicleCatLayoutBinding>(
            layoutInflater,
            R.layout.vehicle_cat_layout,
            null,
            false
        )
        bindingCategory?.root?.let { bottomSheetCategory.setContentView(it) }
        bindingCategory?.mRecycler?.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        adapter =
            CategoryAdapter(requireContext(), repeatVignettePos, this, vignetteList)
        bindingCategory?.mRecycler?.adapter = adapter
        bindingCategory?.cancel?.setOnClickListener { bottomSheetCategory.dismiss() }
        bindingCategory?.mContinue?.setOnClickListener {
            vignetteCategory.endIconDrawable =
                requireContext().resources.getDrawable(R.drawable.ic_arrow_down_id)
            vignetteCategory.setEndIconTintList(
                ContextCompat.getColorStateList(
                    requireContext(),
                    R.color.appPrimary
                )
            )
            rovignette_categories.setText(vignetteList[repeatVignettePos].shortDescription)

            durationVignette(repeatVignettePos)
            bottomSheetCategory.dismiss()
        }
        bindingCategory?.mClose?.setOnClickListener { bottomSheetCategory.dismiss() }
    }

    private fun durationVignette(pos: Int) {

        bindingDuration = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.bottom_sheet_duration,
            null,
            false
        )
        for (vp in priceList) {
            if (vp.vignetteCategoryCode.equals(vignetteList[pos].code)) {
                priceListNew.add(vp)
            }
        }

        if (priceListNew.isNotEmpty()) {
            /*  priceModel = priceListNew[0]
              for (i in durationList.indices) {
                  if (durationList[i].code == priceModel?.vignetteDurationCode) {
                      mDuration?.setText(
                          "${durationList[i].timeUnitCount} ${durationList[i].timeUnit}" +
                                  " (${priceModel?.paymentValue} ${priceModel?.paymentCurrency})"
                      )
                      changeTint(mDuration)
                  }
              }*/

//            priceModel = priceListNew[0]

            for (j in priceListNew.indices) {
                if (priceListNew[j].vignetteDurationCode == durationList[durationList.size - 1].code &&
                    priceListNew[j].vignetteCategoryCode == vignetteList[pos].code
                ) {
                    priceModel = priceListNew[j]
                }
            }
            for (i in durationList.indices) {
                if (durationList[i].code == priceModel?.vignetteDurationCode) {
                    mDuration?.setText(
                        "${durationList[i].timeUnitCount} ${durationList[i].timeUnit}" +
                                " (${priceModel?.paymentValue} ${priceModel?.paymentCurrency})"
                    )
                    changeTint(mDuration)
                }
            }
            checkData()
        }

        bindingDuration?.root?.let { bottomSheetDuration.setContentView(it) }
        bindingDuration?.mRecycler?.layoutManager = LinearLayoutManager(context)
        adapterDuration = BuyDurationAdapter(
            requireContext(),
            this,
            durationList
        )
        adapterDuration.setItems(priceListNew)
        bindingDuration?.cancel?.setOnClickListener { bottomSheetDuration.dismiss() }
        bindingDuration?.mContinue?.setOnClickListener {
            for (i in durationList.indices) {
                if (priceModel?.vignetteDurationCode == durationList[i].code) {
                    mDuration?.setText(
                        "${durationList[i].timeUnitCount} ${durationList[i].timeUnit}" +
                                " (${priceModel?.paymentValue} ${priceModel?.paymentCurrency})"
                    )
                    changeTint(mDuration)
                }
            }

            bottomSheetDuration.dismiss()

        }
        bindingDuration?.mClose?.setOnClickListener { bottomSheetDuration.dismiss() }
        bindingDuration?.mRecycler?.adapter = adapterDuration
    }

    override fun onResume() {
        super.onResume()
        bottomSheetDuration.dismiss()
    }

    override fun OnCountryPick(item: Country, flagDrawableResId: String) {
        this.countryItem = item
        countryName.text = item.name
        countryFlag.text = flagDrawableResId
        checkData()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun checkData() {

        if (vehicleDetail == null) {

            if (countryItem != null && priceModel != null && licensePlate.text.toString() != ""
                && mStartDateText.text != null && check.isChecked
            ) {
                cta.background = requireContext().resources.getDrawable(R.drawable.save_background)
                dataCompleted = true
            } else {
                cta.background =
                    requireContext().resources.getDrawable(R.drawable.save_background_invisible)
                dataCompleted = false
            }

        } else {
            if (priceModel != null && check.isChecked && isValidRange) {
                cta.background = requireContext().resources.getDrawable(R.drawable.save_background)
                dataCompleted = true
            } else {
                cta.background =
                    requireContext().resources.getDrawable(R.drawable.save_background_invisible)
                dataCompleted = false
            }
        }
    }

    override fun onDurationClick(model: VignettePrice, rovignetteDuration: RovignetteDuration) {
        priceModel = model
        changeTint(mDuration)
        checkData()
    }

    override fun onCategoryClick(position: Int) {
        priceListNew.clear()
        repeatVignettePos = position
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


    private fun dateDefaultCase(isActive: Boolean, policyDate: Date?): Boolean {
        var isValidRange = false
        val calendar1 = Calendar.getInstance()
        x = calendar1.time
        val calendar2 = Calendar.getInstance()
        calendar2.add(Calendar.DATE, 30)
        y = calendar2.time

        val dateFormat: DateFormat = SimpleDateFormat("dd/MM/yyyy")
        xStr = dateFormat.format(x)

        yStr = dateFormat.format(y)
        // ( when No Active Rovignette / Rovignette Expired)

        if (isActive && policyDate != null) {
            // ( when Rovignette Active)
            if (policyDate.after(x) && policyDate.before(y)) {
                xStr = dateFormat.format(policyDate)
                val c = Calendar.getInstance()
                c.time = dateFormat.parse(xStr)
                c.add(Calendar.DATE, 1)
                x = c.time
                xStr = dateFormat.format(x)

                val calendar2 = Calendar.getInstance()
                calendar2.add(Calendar.DATE, 29)
                y = calendar2.time
                yStr = dateFormat.format(y)

                isValidRange = y.after(x)
            } else {
                xStr = dateFormat.format(policyDate)
                val c = Calendar.getInstance()
                c.time = dateFormat.parse(xStr)
                c.add(Calendar.DATE, 1)
                x = c.time
                xStr = dateFormat.format(x)
                isValidRange = false
                // Condition Not satisfy So user not able to purchase
            }
        } else {
            isValidRange = true
        }

        mStartDateText.setText(xStr)

        if (!isValidRange)
            showDateError(null)
        return isValidRange
    }


    @SuppressLint("SimpleDateFormat")
    fun dateToMilis(str: String): Long {
        val sdf = SimpleDateFormat(getString(R.string.date_format_template))
        val mDate = sdf.parse(str)
        return mDate.time
    }

}