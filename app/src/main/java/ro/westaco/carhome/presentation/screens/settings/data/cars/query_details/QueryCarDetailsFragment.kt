package ro.westaco.carhome.presentation.screens.settings.data.cars.query_details

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
import ro.westaco.carhome.presentation.screens.settings.data.cars.query_details.QueryCarDetailsViewModel.STATE

@AndroidEntryPoint
class QueryCarDetailsFragment : BaseFragment<QueryCarDetailsViewModel>() {

    override fun getContentView() = R.layout.fragment_query_car_details

    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)

    override fun initUi() {
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
            ArrayAdapter(
                requireContext(),
                R.layout.spinner_item,
                countryList
            ).also { adapter ->
                registrationCountry.adapter = adapter
            }

            registrationCountry.setSelection(Country.findPositionForCode(countryList), false)
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