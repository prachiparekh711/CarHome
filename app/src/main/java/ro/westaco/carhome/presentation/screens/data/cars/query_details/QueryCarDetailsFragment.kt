package ro.westaco.carhome.presentation.screens.data.cars.query_details

import android.view.View
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_query_car_details.*
import kotlinx.android.synthetic.main.layout_add_car_enter_vin.*
import kotlinx.android.synthetic.main.layout_add_car_error.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.Country
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.data.cars.query_details.QueryCarDetailsViewModel.STATE
import ro.westaco.carhome.utils.DialogUtils.Companion.showErrorInfo
import ro.westaco.carhome.utils.Progressbar
import ro.westaco.carhome.utils.RegexData

@AndroidEntryPoint
class QueryCarDetailsFragment : BaseFragment<QueryCarDetailsViewModel>() {

    var countryListItems: ArrayList<Country> = ArrayList()
    private var progressbar: Progressbar? = null

    override fun getContentView() = R.layout.fragment_query_car_details

    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)

    override fun initUi() {

        progressbar = Progressbar(requireContext())
        progressbar?.showPopup()

        back.setOnClickListener {
            viewModel.onBack()
        }

        home.setOnClickListener {
            viewModel.onMain()
        }

        root.setOnClickListener {
            viewModel.onRootClicked()
        }

        cta.setOnClickListener {

            if (registrationNumber.text?.isNotEmpty() == true) {

                when (countryListItems[registrationCountry.selectedItemPosition].code) {

                    "ROU" -> {
                        if (!RegexData.checkNumberPlateROU(registrationNumber.text.toString())) {
                            showErrorInfo(
                                requireContext(),
                                getString(R.string.license_error)
                            )
                            return@setOnClickListener
                        }
                    }

                    "QAT" -> {
                        if (!RegexData.checkNumberPlateQAT(registrationNumber.text.toString())) {
                            showErrorInfo(
                                requireContext(),
                                getString(R.string.license_error)
                            )
                            return@setOnClickListener
                        }
                    }

                    "UKR" -> {
                        if (!RegexData.checkNumberPlateUKR(registrationNumber.text.toString())) {
                            showErrorInfo(
                                requireContext(),
                                getString(R.string.license_error)
                            )
                            return@setOnClickListener
                        }
                    }

                    "BGR" -> {
                        if (!RegexData.checkNumberPlateBGR(registrationNumber.text.toString())) {
                            showErrorInfo(
                                requireContext(),
                                getString(R.string.license_error)
                            )
                            return@setOnClickListener
                        }
                    }

                }
            }

            viewModel.onCta(
                registrationCountry.selectedItemPosition,
                vin.text.toString(),
                registrationNumber.text.toString()
            )
        }

        addCarManually.setOnClickListener {
            viewModel.onAddCarManually(vin.text.toString(), registrationNumber.text.toString())
        }

        tryAgain.setOnClickListener {
            viewModel.onTryAgain()
        }
    }

    override fun setObservers() {

        viewModel.countryData.observe(viewLifecycleOwner) { countryList ->

            if (countryList != null) {
                countryListItems = countryList
                ArrayAdapter(
                    requireContext(),
                    R.layout.spinner_item,
                    countryList
                ).also { adapter ->
                    registrationCountry.adapter = adapter
                }
                registrationCountry.setSelection(Country.findPositionForCode(countryList), false)
            }
            progressbar?.dismissPopup()
        }

        viewModel.stateStream.observe(viewLifecycleOwner) { state ->
            enterVinState.visibility = View.GONE
            generateProfileState.visibility = View.GONE
            successState.visibility = View.GONE
            errorState.visibility = View.GONE
            activity?.window?.statusBarColor =
                ContextCompat.getColor(requireContext(), R.color.white)

            when (state) {
                STATE.EnterVin -> enterVinState.visibility = View.VISIBLE
                STATE.GeneratingProfile -> {
                    generateProfileState.visibility = View.VISIBLE
                    activity?.window?.statusBarColor =
                        ContextCompat.getColor(requireContext(), R.color.appPrimary)
                }
                STATE.Success -> successState.visibility = View.VISIBLE
                STATE.Error -> errorState.visibility = View.VISIBLE
            }
        }
    }
}