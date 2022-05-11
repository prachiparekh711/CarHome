package ro.westaco.carhome.presentation.screens.service.vignette.buy

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_buy_vignette.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.*
import ro.westaco.carhome.databinding.BottomSheetDurationBinding
import ro.westaco.carhome.databinding.BottomSheetVignetteBinding
import ro.westaco.carhome.databinding.DifferentCategoryLayoutBinding
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.service.vignette.buy.BuyVignetteViewModel.ACTION
import ro.westaco.carhome.utils.FirebaseAnalyticsList
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class BuyVignetteFragment : BaseFragment<BuyVignetteViewModel>() {

    private var vehicle: Vehicle? = null
    private lateinit var adapter: BuyVignetteAdapter
    private lateinit var adapterDuration: BuyDurationAdapter
    lateinit var bottomSheetVignette: BottomSheetDialog
    lateinit var bottomSheetDuration: BottomSheetDialog
    var vignetteList: ArrayList<ServiceCategory> = ArrayList()
    var durationList: ArrayList<RovignetteDuration> = ArrayList()
    var priceListNew: ArrayList<VignettePrice> = ArrayList()
    var countries: ArrayList<Country> = ArrayList()
    var priceList: ArrayList<VignettePrice> = ArrayList()
    var priceModel: VignettePrice? = null
    var categoryBottomSheet: BottomSheetDialog? = null
    var mVignette: TextView? = null

    var repeatVignettePos = 0
    var repeatDurationPos = 0
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

    companion object {
        const val ARG_CAR = "arg_car"

        @SuppressLint("StaticFieldLeak")
        var mDurationText: TextView? = null
    }

    override fun getContentView() = R.layout.fragment_buy_vignette

    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        arguments?.let {
            vehicle = it.getSerializable(ARG_CAR) as? Vehicle?

            vehicle?.let { v -> viewModel.onVehicle(v) }

        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())

        val params = Bundle()
        mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.ACCESS_ROVINIETA_ANDROID, params)

        bottomSheetVignette = BottomSheetDialog(requireContext())
        bottomSheetDuration = BottomSheetDialog(requireContext())

    }

    override fun initUi() {

        viewModel.fetchPrices()

        mDurationText = activity?.findViewById(R.id.mDuration)
        mVignette = activity?.findViewById(R.id.mVignette)

        back.setOnClickListener {
            viewModel.onBack()
        }

        home.setOnClickListener {
            viewModel.onMain()
        }

        val ctx = requireContext()

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


        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrow = calendar.time

        val formatter: DateFormat = SimpleDateFormat("dd/MM/yyyy")
//        val currentTime = Calendar.getInstance().time

        val currentDate: String = formatter.format(tomorrow)

        startDate.text = currentDate

        startDate.setOnClickListener { viewModel.onDateClicked() }


        cta.setOnClickListener {

            if (licensePlate.text.isNullOrEmpty()) {
                mVin.visibility = View.GONE
            } else {
                licensePlate.background =
                    requireContext().resources.getDrawable(R.drawable.auth_text_input_background)
                licensePlateLabel.setTextColor(requireContext().resources.getColor(R.color.textOnWhiteAccentGray))
                licenseEnter.setTextColor(requireContext().resources.getColor(R.color.textOnWhiteAccentGray))
                licenseEnter.isVisible = false
                mVin.visibility = View.VISIBLE
            }

            if (vin.text.isNullOrEmpty()) {
                vin.setBackgroundResource(R.drawable.auth_text_error_background)
                vinLabel.setTextColor(resources.getColor(R.color.orangeExpired))
                mEnter.setTextColor(resources.getColor(R.color.orangeExpired))
                mEnter.visibility = View.VISIBLE
            } else {
                vin.setBackgroundResource(R.drawable.auth_text_input_background)
                vinLabel.setTextColor(resources.getColor(R.color.textOnWhiteAccentGray))
                mEnter.visibility = View.GONE
            }

            if (startDateError.isVisible) {
                startDate.background =
                    requireContext().resources.getDrawable(R.drawable.auth_text_input_background)
                startDateLabel.setTextColor(requireContext().resources.getColor(R.color.textOnWhiteAccentGray))
                startDateError.isVisible = false
            }

            if (priceModel == null) {
                Toast.makeText(
                    activity,
                    requireContext().resources.getString(R.string.duration_required),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
            priceModel?.let { it1 ->
                viewModel.onCta(
                    vehicle,
                    mVignette?.text.toString(),
                    startDate.text.toString(),
                    regCountry.selectedItemPosition,
                    licensePlate.text.toString(),
                    vin.text.toString(),
                    it1,
                    check.isChecked,
                )
            }
        }
    }

    private var dpd: DatePickerDialog? = null

    private fun showDatePicker(dateInMillis: Long) {
        val c = Calendar.getInstance().apply {
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
                    }.timeInMillis
                )
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
        )
        dpd?.datePicker?.minDate = System.currentTimeMillis() + (1000 * 24 * 60 * 60)
        dpd?.show()

    }

    override fun setObservers() {
        viewModel.rovignetteCategories.observe(viewLifecycleOwner) { rovignetteCategories ->
            this.vignetteList = rovignetteCategories

            if (vignetteList.isNotEmpty())
                mVignette?.text = vignetteList[0].shortDescription

            mVignette?.setOnClickListener {
                vignetteCategory()
            }
        }

        viewModel.rovignetteDurations.observe(viewLifecycleOwner) { rovignetteDurations ->
            this.durationList = rovignetteDurations
            mDurationText?.setOnClickListener {
                durationVignette()
            }
        }

        viewModel.countryData.observe(viewLifecycleOwner) { countryData ->

            this.countries = countryData
            ArrayAdapter(requireContext(), R.layout.spinner_item, countries).also { adapter ->
                regCountry.adapter = adapter
            }

            regCountry.setSelection(Country.findPositionForCode(countries), false)
        }

        viewModel.vignettePricesLivedata.observe(requireActivity()) {
            for (vp in it) {

                priceList.add(vp)
                if (durationList.isNotEmpty()) {
                    priceModel = priceList[0]
                    mDurationText?.text =
                        "${durationList[1].timeUnitCount} " +
                                "${durationList[1].timeUnit.lowercase()} " +
                                "( ${priceList[0].paymentValue}" +
                                " ${priceList[0].paymentCurrency?.lowercase()} )"
                }
            }
        }

        viewModel.vehicleDetailsLivedata.observe(viewLifecycleOwner) { vehicleDetails ->

            licensePlate.setText(vehicleDetails.licensePlate)
            vin.setText(vehicleDetails.vehicleIdentificationNumber)

        }

        viewModel.dateLiveData.observe(viewLifecycleOwner) { dateMillis ->
            startDate.visibility = View.VISIBLE
            startDate.text = SimpleDateFormat(
                getString(R.string.date_format_template), Locale.getDefault()
            ).format(
                Date(dateMillis)
            )
        }

        viewModel.stateStream.observe(viewLifecycleOwner) { state ->

            activity?.window?.statusBarColor =
                ContextCompat.getColor(requireContext(), R.color.white)

            when (state) {
                BuyVignetteViewModel.STATE.EnterLpn -> {
                    licensePlate.background =
                        requireContext().resources.getDrawable(R.drawable.auth_text_error_background)
                    licensePlateLabel.setTextColor(requireContext().resources.getColor(R.color.orangeExpired))
                    licenseEnter.setTextColor(requireContext().resources.getColor(R.color.orangeExpired))
                    licenseEnter.isVisible = true
                }

                BuyVignetteViewModel.STATE.EnterVin -> {
                    mVin.isVisible = true
                }

                BuyVignetteViewModel.STATE.ErrorVin -> {
                    vin.background =
                        requireContext().resources.getDrawable(R.drawable.auth_text_error_background)
                    vinLabel.setTextColor(requireContext().resources.getColor(R.color.orangeExpired))
                    mEnter.isVisible = true
                }

                BuyVignetteViewModel.STATE.EnterCategory -> {
                    categoryBottomSheet?.show()
                }

                BuyVignetteViewModel.STATE.EnterCategory -> {
                    categoryBottomSheet?.show()
                }

            }
        }

        viewModel.actionStream.observe(viewLifecycleOwner) {
            when (it) {
                is ACTION.ShowDatePicker -> showDatePicker(it.dateInMillis)
                is ACTION.ShowError ->
                    Toast.makeText(activity, it.error, Toast.LENGTH_SHORT)
                        .show()
                is ACTION.ShowDateError -> {
                    startDate.background =
                        requireContext().resources.getDrawable(R.drawable.ic_error_bg)
                    startDateLabel.setTextColor(requireContext().resources.getColor(R.color.orangeExpired))
                    startDateError.isVisible = true
                    startDateError.text = it.error
                }
            }
        }

    }

    private fun vignetteCategory() {

        val repeatInterface = object : BuyVignetteAdapter.OnItemInteractionListener {
            @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
            override fun onItemClick(position: Int, mText: TextView) {
                priceListNew.clear()
                repeatVignettePos = position
                mVignette?.text = vignetteList[position].shortDescription
                requireContext().resources?.getColor(R.color.white)?.let { mText.setTextColor(it) }
                viewModel.vignettePricesLivedata.observe(requireActivity(),
                    androidx.lifecycle.Observer {
                        priceListNew.clear()
                        for (vp in it) {

                            if (vp.vignetteCategoryCode.equals(vignetteList[position].code)) {
                                priceListNew.add(vp)
                            }
                        }
                    })
                if (durationList.isNotEmpty()) {
                    if (position == 0 || position == 1) {
                        mDurationText?.text =
                            durationList[1].timeUnitCount.toString() + " " + durationList[1].timeUnit.lowercase() + " (" + priceListNew[0].paymentValue + " " + priceListNew[0].paymentCurrency.toString()
                                .lowercase() + ")"
                    } else {
                        mDurationText?.text =
                            durationList[0].timeUnitCount.toString() + " " + durationList[0].timeUnit.lowercase() + " (" + priceListNew[0].paymentValue + " " + priceListNew[0].paymentCurrency.toString()
                                .lowercase() + ")"
                    }
                    priceModel = priceListNew[0]
                }
                bottomSheetVignette.dismiss()
            }
        }

        val bindingSheet = DataBindingUtil.inflate<BottomSheetVignetteBinding>(
            layoutInflater,
            R.layout.bottom_sheet_vignette,
            null,
            false
        )
        bottomSheetVignette.setContentView(bindingSheet.root)
        bindingSheet.mRecycler.layoutManager = GridLayoutManager(requireActivity(), 2)
        adapter =
            BuyVignetteAdapter(requireContext(), repeatVignettePos, repeatInterface, vignetteList)
        bindingSheet.mRecycler.adapter = adapter

        bottomSheetVignette.show()


    }

    private fun durationVignette() {

        val repeatInterfaceDuration = object : BuyDurationAdapter.OnItemInteractionListener {
            override fun onItemClick(position: Int, model: VignettePrice) {
                priceModel = model
                repeatDurationPos = position
                bottomSheetDuration.dismiss()
            }
        }

        if (priceListNew.isNullOrEmpty()) {

            viewModel.vignettePricesLivedata.observe(requireActivity(),
                androidx.lifecycle.Observer {
                    priceListNew.clear()
                    for (vp in it) {
                        if (vp.vignetteCategoryCode.equals(vignetteList[0].code)) {
                            priceListNew.add(vp)
                        }
                    }
                })
        }
        val bindingSheet = DataBindingUtil.inflate<BottomSheetDurationBinding>(
            layoutInflater,
            R.layout.bottom_sheet_duration,
            null,
            false
        )
        bottomSheetDuration.setContentView(bindingSheet.root)
        bindingSheet.mRecycler.layoutManager = LinearLayoutManager(context)
        adapterDuration = BuyDurationAdapter(
            requireContext(),
            repeatDurationPos,
            repeatInterfaceDuration,
            durationList,
            priceListNew
        )
        bindingSheet.mRecycler.adapter = adapterDuration
        bottomSheetDuration.show()

    }

    override fun onResume() {
        super.onResume()
        bottomSheetDuration.dismiss()
    }

}