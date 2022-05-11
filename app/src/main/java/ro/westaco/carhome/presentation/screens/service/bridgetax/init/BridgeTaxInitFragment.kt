package ro.westaco.carhome.presentation.screens.service.bridgetax.init

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_bridge_tax_init.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.*
import ro.westaco.carhome.databinding.BottomSheetNumberPassesBinding
import ro.westaco.carhome.databinding.BottomSheetVignetteBinding
import ro.westaco.carhome.databinding.DifferentCategoryLayoutBinding
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.utils.FirebaseAnalyticsList

@AndroidEntryPoint
class BridgeTaxInitFragment : BaseFragment<BridgeTaxInitViewModel>(),
    PassTaxAdapter.OnItemInteractionListener {
    companion object {
        const val ARG_CAR = "arg_car"
    }

    var categoryList: ArrayList<ServiceCategory> = ArrayList()
    var objectiveList: ArrayList<ObjectiveItem> = ArrayList()
    var countries: ArrayList<Country> = ArrayList()
    var categoryPos = 0
    var objectiveItem = 0
    var passTextItem = 0
    private lateinit var adapter: CategoryAdapter
    private lateinit var objectiveAdapter: ObjectiveAdapter
    private var vehicle: Vehicle? = null
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)

    override fun getContentView() = R.layout.fragment_bridge_tax_init
    var bottomSheet: BottomSheetDialog? = null
    var numberpassbottomSheet: BottomSheetDialog? = null
    var categoryBottomSheet: BottomSheetDialog? = null
    var priceModel: BridgeTaxPrices? = null


    @SuppressLint("UseCompatLoadingForDrawables")
    override fun initUi() {
        back.setOnClickListener {
            viewModel.onBack()
        }

        home.setOnClickListener {
            viewModel.onMain()
        }


        bottomSheet = BottomSheetDialog(requireContext())
        numberpassbottomSheet = BottomSheetDialog(requireContext())

        categoryBottomSheet = BottomSheetDialog(requireContext())

        val bindingSheet = DataBindingUtil.inflate<DifferentCategoryLayoutBinding>(
            layoutInflater,
            R.layout.different_category_layout,
            null,
            false
        )
        categoryBottomSheet?.setContentView(bindingSheet.root)

        bindingSheet?.cta?.setOnClickListener { categoryBottomSheet?.dismiss() }

        bridge_tax_passes.setOnClickListener {
            viewModel.fetchPrices(categoryList[categoryPos].code, objectiveList[objectiveItem].code)
            numberpassbottomSheet?.show()
        }


        cta.setOnClickListener {

            if (vinLL.isVisible) {
                vinET.background =
                    requireContext().resources.getDrawable(R.drawable.auth_text_input_background)
                vin_label.setTextColor(requireContext().resources.getColor(R.color.textOnWhiteAccentGray))
                vin_error.isVisible = false
            }

            licensePlate.background =
                requireContext().resources.getDrawable(R.drawable.auth_text_input_background)
            number_plate_Label.setTextColor(requireContext().resources.getColor(R.color.textOnWhiteAccentGray))
            number_plate_error.isVisible = false

            priceModel?.let { it2 ->

                viewModel.onSave(
                    vehicle = vehicle,
                    registrationCountryCode = countries[regCountry.selectedItemPosition].code,
                    licensePlate = licensePlate.text.toString(),
                    vin = if (vinLL.isVisible) vinET.text.toString() else null,
                    price = it2,
                    startDate = null,
                    lowerCategoryReason = bindingSheet?.categoryET?.text.toString(),
                    checked = check.isChecked
                )

            }
        }
    }

    override fun setObservers() {
        viewModel.vehicleDetailsLivedata.observe(viewLifecycleOwner) { vehicleDetails ->
            licensePlate.setText(vehicleDetails.licensePlate)
            if (vehicleDetails.vehicleIdentificationNumber != null) {
                vinET.setText(vehicleDetails.vehicleIdentificationNumber)
                vinLL.isVisible = true
            } else {
                vinLL.isVisible = false
            }
        }

        viewModel.pricesLivedata.observe(viewLifecycleOwner) {
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
            numberpassbottomSheet?.setContentView(bindingSheet.root)

        }

        viewModel.countryData.observe(viewLifecycleOwner) { countryData ->
            this.countries = countryData
            ArrayAdapter(requireContext(), R.layout.spinner_item, countries).also { adapter ->
                regCountry.adapter = adapter
            }

            regCountry.setSelection(Country.findPositionForCode(countries), false)
        }

        viewModel.bridgeTaxCategories.observe(viewLifecycleOwner) { bridgeTaxCategories ->
            this.categoryList = bridgeTaxCategories

            if (categoryList.isNotEmpty())
                bridge_tax_categories.text = categoryList[0].shortDescription

            bridge_tax_categories.setOnClickListener {
                bridgetaxCategory()
            }
        }

        viewModel.bridgeTaxObjectives.observe(viewLifecycleOwner) { bridgeTaxObjectives ->
            this.objectiveList = bridgeTaxObjectives
            val objective = object : ObjectiveAdapter.OnItemInteractionListener {
                override fun onItemClick(item: ObjectiveItem, position: Int) {
                    objectiveItem = position
                }
            }

            objectiveRV.layoutManager =
                LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            objectiveRV.adapter =
                ObjectiveAdapter(requireContext(), objectiveItem, objective, objectiveList)
        }

        viewModel.stateStream.observe(viewLifecycleOwner) { state ->

            activity?.window?.statusBarColor =
                ContextCompat.getColor(requireContext(), R.color.white)

            when (state) {
                BridgeTaxInitViewModel.STATE.EnterLpn -> {
                    licensePlate.background =
                        requireContext().resources.getDrawable(R.drawable.auth_text_error_background)
                    number_plate_Label.setTextColor(requireContext().resources.getColor(R.color.orangeExpired))
                    number_plate_error.isVisible = true
                }

                BridgeTaxInitViewModel.STATE.EnterVin -> {
                    vinLL.isVisible = true
                }

                BridgeTaxInitViewModel.STATE.ErrorVin -> {
                    vinET.background =
                        requireContext().resources.getDrawable(R.drawable.auth_text_error_background)
                    vin_label.setTextColor(requireContext().resources.getColor(R.color.orangeExpired))
                    vin_error.isVisible = true
                }

                BridgeTaxInitViewModel.STATE.EnterCategory -> {
                    categoryBottomSheet?.show()
                }
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            vehicle = it.getSerializable(ARG_CAR) as? Vehicle?
            vehicle?.let { v -> viewModel.onVehicle(v) }
        }
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())
        val params = Bundle()
        mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.ACCESS_BRIDGE_TAX_ANDROID, params)
    }

    private fun bridgetaxCategory() {

        val repeatInterface = object : CategoryAdapter.OnItemInteractionListener {
            override fun onItemClick(position: Int, mText: TextView) {
                categoryPos = position
                bridge_tax_categories.text = categoryList[position].shortDescription
                context?.resources?.getColor(R.color.white)?.let { mText.setTextColor(it) }
                bottomSheet?.dismiss()
            }
        }

        val bindingSheet = DataBindingUtil.inflate<BottomSheetVignetteBinding>(
            layoutInflater, R.layout.bottom_sheet_vignette, null, false
        )

        bottomSheet?.setContentView(bindingSheet.root)
        bindingSheet.mRecycler.layoutManager = GridLayoutManager(requireActivity(), 2)
        adapter = CategoryAdapter(requireContext(), categoryPos, repeatInterface, categoryList)
        bindingSheet.mRecycler.adapter = adapter
        bindingSheet.title.text = requireContext().resources.getString(R.string.bridge_tax_cat)
        bottomSheet?.show()

    }

    override fun onItemClick(model: BridgeTaxPrices, position: Int) {
        passTextItem = position
        priceModel = model
        bridge_tax_passes.text = model.description
        numberpassbottomSheet?.dismiss()
    }

}