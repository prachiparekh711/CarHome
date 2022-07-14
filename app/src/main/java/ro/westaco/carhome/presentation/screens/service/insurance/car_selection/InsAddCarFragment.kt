package ro.westaco.carhome.presentation.screens.service.insurance.car_selection

import android.view.KeyEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_fetch_screen.*
import kotlinx.android.synthetic.main.fragment_ins_add_car.*
import kotlinx.android.synthetic.main.ins_add_car_layout.*
import kotlinx.android.synthetic.main.layout_add_car_error.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.Country
import ro.westaco.carhome.dialog.DialogUtils
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.data.commen.CountryCodeDialog
import ro.westaco.carhome.utils.RegexData


@AndroidEntryPoint
class InsAddCarFragment : BaseFragment<InsAddCarViewModel>(),
    CountryCodeDialog.CountryCodePicker {

    var countryItem: Country? = null
    var countries: ArrayList<Country> = ArrayList()
    var dataCompleted = false


    override fun getContentView() = R.layout.fragment_ins_add_car
    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)

    override fun initUi() {


        back.setOnClickListener {
            onBackPress()
        }

        requireView().isFocusableInTouchMode = true
        requireView().requestFocus()
        requireView().setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                onBackPress()
                true
            } else false
        }

        mDismiss.setOnClickListener {
            viewModel.onBack()
        }

        radioGroup.setOnCheckedChangeListener { radioGroup, i ->
            licensePlate.text = null
            when (i) {
                R.id.mFirst -> {
                    licenseLabel.isEnabled = true
                    licenseLabel.alpha = 1F
                    licenseLabel.setBackgroundColor(requireContext().resources.getColor(R.color.white))
                }
                R.id.mSecond -> {
                    licenseLabel.isEnabled = false
                    licenseLabel.alpha = 0.3F
                    licenseLabel.setBackgroundColor(requireContext().resources.getColor(R.color.expande_colore))
                }
            }
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
                } else {
                    licensePlate.setTextColor(requireContext().resources.getColor(R.color.service_text_color))
                    licensePlate.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        0,
                        0
                    )
                    number_plate_error.isVisible = false
                }
            }

            if (dataCompleted) {

                if (countryName.text.isEmpty() && mFirst.isChecked) {
                    licensePlate.setTextColor(requireContext().resources.getColor(R.color.delete_dialog_color))
                    number_plate_error.text =
                        requireContext().resources.getString(R.string.enter_register_num)
                    number_plate_error.isVisible = true
                } else {
                    licensePlate.setTextColor(requireContext().resources.getColor(R.color.service_text_color))
                    number_plate_error.isVisible = false
                }

                if (vinET.text.isNullOrEmpty() && mSecond.isChecked) {
                    vinET.setTextColor(requireContext().resources.getColor(R.color.delete_dialog_color))
                    vin_error.isVisible = true
                } else {
                    vinET.setTextColor(requireContext().resources.getColor(R.color.service_text_color))
                    vin_error.isVisible = false
                }

                if (check.isChecked) {
                    countryItem?.let { it1 ->
                        viewModel.onCta(
                            it1,
                            vinET.text.toString().ifBlank { null },
                            licensePlate.text.toString()
                        )
                    }
                } else {
                    DialogUtils.showErrorInfo(requireContext(), getString(R.string.check_info))
                }
            }
        }

        addCarManually.setOnClickListener {
            viewModel.gotToEditPage(vinET.text.toString(), licensePlate.text.toString())
        }

        tryAgain.setOnClickListener {
            viewModel.onTryAgain()
        }

        check.setOnCheckedChangeListener { _, _ ->
            checkData()
        }

        licensePlate.addTextChangedListener {
            checkData()
        }

        vinET.addTextChangedListener {
            checkData()
        }
    }

    private fun onBackPress() {
        if (!enterVinState.isVisible) {
            enterVinState.isVisible = true
            generateProfileState.visibility = View.GONE
            successState.visibility = View.GONE
            errorState.visibility = View.GONE
        } else {
            viewModel.onBack()
        }
    }

    private fun checkData() {
        when {
            mFirst.isChecked -> {
                dataCompleted =
                    (!licensePlate.text.isNullOrEmpty() && countryItem != null && check.isChecked)
            }
            mSecond.isChecked -> {
                dataCompleted =
                    (!vinET.text.isNullOrEmpty() && countryItem != null && check.isChecked)
            }
        }

        if (dataCompleted)
            cta.background = requireContext().resources.getDrawable(R.drawable.save_background)
        else
            cta.background =
                requireContext().resources.getDrawable(R.drawable.save_background_invisible)
    }

    override fun setObservers() {
        viewModel.countryData.observe(viewLifecycleOwner) { countryData ->

            if (countryData != null) {
                this.countries = countryData
                this.countryItem = countries[Country.findPositionForCode(countries)]

                li_dialog.setOnClickListener {
                    val countryCodeDialog = CountryCodeDialog(requireActivity(), countries, this)
                    countryCodeDialog.show(requireActivity().supportFragmentManager, null)
                }
            }
        }

        viewModel.stateStream.observe(viewLifecycleOwner) { state ->
            enterVinState.visibility = View.GONE
            generateProfileState.visibility = View.GONE
            successState.visibility = View.GONE
            errorState.visibility = View.GONE
            activity?.window?.statusBarColor =
                ContextCompat.getColor(requireContext(), R.color.white)

            when (state) {
                InsAddCarViewModel.STATE.EnterVin -> enterVinState.visibility = View.VISIBLE
                InsAddCarViewModel.STATE.GeneratingProfile -> {
                    generateProfileState.visibility = View.VISIBLE
                    activity?.window?.statusBarColor =
                        ContextCompat.getColor(requireContext(), R.color.offer_color)
                    titleStep1.text =
                        requireContext().resources.getString(R.string.generate_carprofile)
                    titleStep2.text =
                        "${requireContext().resources.getString(R.string.generate_rar)} ${'\n'} ${
                            requireContext().resources.getString(R.string.rar_helping)
                        }"
                }
                InsAddCarViewModel.STATE.Success -> successState.visibility = View.VISIBLE
                InsAddCarViewModel.STATE.Error -> errorState.visibility = View.VISIBLE
            }
        }
    }

    override fun OnCountryPick(item: Country, flagDrawableResId: String) {
        this.countryItem = item
        countryName.text = item.name
        countryFlag.text = flagDrawableResId
    }

}