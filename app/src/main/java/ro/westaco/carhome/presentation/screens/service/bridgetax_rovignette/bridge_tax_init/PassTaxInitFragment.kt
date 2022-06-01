package ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.bridge_tax_init

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_pass_tax_init.*
import kotlinx.android.synthetic.main.fragment_pass_tax_init.licensePlate
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.*
import ro.westaco.carhome.databinding.BottomSheetNumberPassesBinding
import ro.westaco.carhome.databinding.DifferentCategoryLayoutBinding
import ro.westaco.carhome.databinding.VehicleCatLayoutBinding
import ro.westaco.carhome.di.ApiModule
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.data.commen.CountryCodeDialog
import ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.adapter.CategoryAdapter
import ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.adapter.PassTaxAdapter
import ro.westaco.carhome.utils.DialogUtils.Companion.showErrorInfo
import ro.westaco.carhome.utils.FirebaseAnalyticsList
import ro.westaco.carhome.utils.Progressbar
import ro.westaco.carhome.utils.RegexData

@AndroidEntryPoint
class PassTaxInitFragment : BaseFragment<BridgeTaxInitViewModel>(),
    PassTaxAdapter.OnItemInteractionListener,
    CountryCodeDialog.CountryCodePicker {

    companion object {
        const val ARG_CAR = "arg_car"
        const val ARG_ENTER_VALUE = "arg_enter_value"
    }

    var categoryList: ArrayList<ServiceCategory> = ArrayList()
    var objectiveList: ArrayList<ObjectiveItem> = ArrayList()
    var countries: ArrayList<Country> = ArrayList()
    var categoryPos = 0
    var objectiveItem = 0
    var passTextItem = 0
    private lateinit var adapter: CategoryAdapter
    private var vehicle: Vehicle? = null
    private var vehicleDetail: VehicleDetails? = null
    var progressbar: Progressbar? = null
    var countryItem: Country? = null
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)
    override fun getContentView() = R.layout.fragment_pass_tax_init
    var bottomSheet: BottomSheetDialog? = null
    var numberPassBottomSheet: BottomSheetDialog? = null
    var categoryBottomSheet: BottomSheetDialog? = null
    var priceModel: BridgeTaxPrices? = null
    var activeService: String = ""
    var dataCompleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            vehicle = it.getSerializable(ARG_CAR) as? Vehicle?
            activeService = it.getString(ARG_ENTER_VALUE).toString()
            vehicle?.let { v -> viewModel.onVehicle(v) }
        }
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())
        val params = Bundle()
        mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.ACCESS_BRIDGE_TAX_ANDROID, params)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun initUi() {

        progressbar = Progressbar(requireContext())
        progressbar?.showPopup()

        back.setOnClickListener {
            viewModel.onBack()
        }

        mDismiss.setOnClickListener {
            viewModel.onBack()
        }

        mMenu.setOnClickListener {
        }

        carHeader.setOnClickListener {
            carInfoBlock.isVisible = !carInfoBlock.isVisible
            if (carInfoBlock.isVisible) {
                carInfoArrow.setImageDrawable(requireContext().resources.getDrawable(R.drawable.ic_arrow_circle_up))
            } else {
                carInfoArrow.setImageDrawable(requireContext().resources.getDrawable(R.drawable.ic_arrow_circle_down))
            }
        }
        serviceHeader.setOnClickListener {
            bridgeTaxServiceBlock.isVisible = !bridgeTaxServiceBlock.isVisible
            if (bridgeTaxServiceBlock.isVisible) {
                serviceInfoArrow.setImageDrawable(requireContext().resources.getDrawable(R.drawable.ic_arrow_circle_up))
            } else {
                serviceInfoArrow.setImageDrawable(requireContext().resources.getDrawable(R.drawable.ic_arrow_circle_down))
            }
        }
        bottomSheet = BottomSheetDialog(requireContext())
        numberPassBottomSheet = BottomSheetDialog(requireContext())
        categoryBottomSheet = BottomSheetDialog(requireContext())
        val bindingSheet = DataBindingUtil.inflate<DifferentCategoryLayoutBinding>(
            layoutInflater,
            R.layout.different_category_layout,
            null,
            false
        )

        categoryBottomSheet?.setContentView(bindingSheet.root)
        bindingSheet?.cta?.setOnClickListener { categoryBottomSheet?.dismiss() }


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
                if (vin_error.isVisible) {
                    vinET.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    vin_error.isVisible = false
                }
                if (number_plate_error.isVisible) {
                    licensePlate.setTextColor(requireContext().resources.getColor(R.color.service_text_color))
                    licensePlate.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    number_plate_error.isVisible = false
                }

                var vinStr = if (vinET.text.isNullOrEmpty()) {
                    null
                } else {
                    vinET.text.toString()
                }

                if (mVINNumberText.text?.isNotEmpty() == true) {

                    if (mVINNumberText.text?.length != 17) {
                        showErrorInfo(requireContext(), getString(R.string.error_vin))
                        return@setOnClickListener
                    }
                    vinStr = mVINNumberText.text.toString()

                }

                countryItem?.let { it2 ->
                    priceModel?.let { it1 ->
                        viewModel.onSave(
                            vehicle = vehicle,
                            registrationCountryCode = it2.code,
                            licensePlate = licensePlate.text.toString(),
                            vin = vinStr,
                            price = it1,
                            startDate = null,
                            lowerCategoryReason = bindingSheet?.categoryET?.text.toString(),
                            checked = check.isChecked,
                            activeService
                        )
                    } ?: showErrorInfo(requireContext(), getString(R.string.number_passes_info))
                } ?: showErrorInfo(requireContext(), getString(R.string.country_info))
            }

        }

        bridge_tax_categories.setOnClickListener {
            bridgeTaxCategory()
        }

        check.setOnCheckedChangeListener { _, _ ->
            checkData()
        }

        licensePlate.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.toString()?.let { checkData() }
            }
        })
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun checkData() {
        if (vehicleDetail == null) {

            if (countryItem != null && priceModel != null && licensePlate.text.toString() != "" && check.isChecked) {
                cta.background = requireContext().resources.getDrawable(R.drawable.save_background)
                dataCompleted = true
            } else {
                cta.background =
                    requireContext().resources.getDrawable(R.drawable.save_background_invisible)
                dataCompleted = false
            }
        } else {
            if (priceModel != null && check.isChecked) {
                cta.background = requireContext().resources.getDrawable(R.drawable.save_background)
                dataCompleted = true
            } else {
                cta.background =
                    requireContext().resources.getDrawable(R.drawable.save_background_invisible)
                dataCompleted = false
            }
        }
    }

    override fun setObservers() {

        viewModel.vehicleDetailsLivedata.observe(viewLifecycleOwner) { vehicleDetails ->
            if (vehicleDetails != null) {
                this.vehicleDetail = vehicleDetails

                licensePlate.setText(vehicleDetails.licensePlate)
                mCarNumberText.setText(vehicleDetails.licensePlate)
                vinET.setText(vehicleDetails.vehicleIdentificationNumber)

                if (!vehicleDetails.vehicleIdentificationNumber.isNullOrEmpty()) {
                    mVINNumberText.setText(vehicleDetails.vehicleIdentificationNumber)
                }

                vinLabel.isVisible = true

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

        viewModel.pricesLivedata.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                priceModel = it[0]

                bridge_tax_passes.setText(priceModel?.description)

                val bindingSheet = DataBindingUtil.inflate<BottomSheetNumberPassesBinding>(
                    layoutInflater,
                    R.layout.bottom_sheet_number_passes,
                    null,
                    false
                )
                val adapter = PassTaxAdapter(requireContext(), it, this, passTextItem)
                bindingSheet.rvPasses.layoutManager =
                    LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                bindingSheet.rvPasses.adapter = adapter
                numberPassBottomSheet?.setContentView(bindingSheet.root)

                bindingSheet.cancel.setOnClickListener { numberPassBottomSheet?.dismiss() }
                bindingSheet.mContinue.setOnClickListener { numberPassBottomSheet?.dismiss() }
                bindingSheet.mClose.setOnClickListener { numberPassBottomSheet?.dismiss() }

                bridge_tax_passes.setOnClickListener {
                    numberPassBottomSheet?.show()
                }

                checkData()
            }
        }

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

            progressbar?.dismissPopup()
        }

        viewModel.vehicleCategories.observe(viewLifecycleOwner) { bridgeTaxCategories ->
            if (bridgeTaxCategories != null) {
                this.categoryList = bridgeTaxCategories
                if (categoryList.isNotEmpty()) {
                    bridge_tax_categories.setText(categoryList[categoryPos].shortDescription)
                    viewModel.fetchPrices(
                        categoryList[categoryPos].code,
                        objectiveList[objectiveItem].code
                    )
                }
            }
        }

        viewModel.bridgeTaxObjectives.observe(viewLifecycleOwner) { bridgeTaxObjectives ->
            this.objectiveList = bridgeTaxObjectives
            viewModel.getCategories()

            /*  val objective = object : ObjectiveAdapter.OnItemInteractionListener {
                  override fun onItemClick(item: ObjectiveItem, position: Int) {
                      objectiveItem = position
                  }
              }
              objectiveRV.layoutManager =
                  LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
              objectiveRV.adapter =
                  ObjectiveAdapter(requireContext(), objectiveItem, objective, objectiveList)*/

        }

        viewModel.stateStream.observe(viewLifecycleOwner) { state ->
            activity?.window?.statusBarColor =
                ContextCompat.getColor(requireContext(), R.color.white)

            when (state) {

                BridgeTaxInitViewModel.STATE.EnterLpn -> {
                    licensePlate.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_info,
                        0
                    )
                    number_plate_error.isVisible = true
                }

                BridgeTaxInitViewModel.STATE.EnterVin -> {
//                    setTextInputLayoutHintColor(mVINNumber,requireContext(),R.color.delete_dialog_color)
                    mVINNumberText.isClickable = true
                    mVINNumberText.isCursorVisible = true
                    mVINNumberText.isFocusable = true
                    mVINNumberText.isFocusableInTouchMode = true
                    vinLabel.isVisible = true
                }

                BridgeTaxInitViewModel.STATE.ErrorVin -> {

                    vinET.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_info, 0)
                    vin_error.isVisible = true
                }

                BridgeTaxInitViewModel.STATE.EnterCategory -> {
                    categoryBottomSheet?.show()
                }

            }
        }

    }

    private fun bridgeTaxCategory() {

        val repeatInterface = object : CategoryAdapter.OnItemInteractionListener {
            override fun onCategoryClick(position: Int) {
                categoryPos = position
                bridge_tax_categories.setText(categoryList[position].shortDescription)
                viewModel.fetchPrices(
                    categoryList[categoryPos].code,
                    objectiveList[objectiveItem].code
                )
                checkData()
            }
        }
        val bindingSheet = DataBindingUtil.inflate<VehicleCatLayoutBinding>(
            layoutInflater, R.layout.vehicle_cat_layout, null, false
        )

        bottomSheet?.setContentView(bindingSheet.root)
        bindingSheet.mRecycler.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        adapter = CategoryAdapter(requireContext(), categoryPos, repeatInterface, categoryList)
        bindingSheet.mRecycler.adapter = adapter
        bindingSheet.cancel.setOnClickListener { bottomSheet?.dismiss() }
        bindingSheet.mContinue.setOnClickListener { bottomSheet?.dismiss() }
        bindingSheet.mClose.setOnClickListener { bottomSheet?.dismiss() }
        bottomSheet?.show()

    }

    override fun onItemClick(model: BridgeTaxPrices, position: Int) {
        passTextItem = position
        priceModel = model
        bridge_tax_passes.setText(model.description)
        checkData()
    }

    override fun OnCountryPick(item: Country, flagDrawableResId: String) {

        this.countryItem = item
        countryName.text = item.name
        countryFlag.text = flagDrawableResId
        checkData()
    }

    private fun setTextInputLayoutHintColor(
        textInputLayout: TextInputLayout,
        context: Context,
        @ColorRes colorIdRes: Int
    ) {
        textInputLayout.defaultHintTextColor =
            ColorStateList.valueOf(ContextCompat.getColor(context, colorIdRes))
    }

}