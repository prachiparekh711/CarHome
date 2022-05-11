package ro.westaco.carhome.presentation.screens.service.insurance.offers

import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.requests.RcaOfferRequest
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.service.insurance.InsuranceStep2ViewModel


@AndroidEntryPoint
class FetchScreenFragment : BaseFragment<InsuranceStep2ViewModel>() {

    companion object {
        const val ARG_REQUEST = "arg_request"
    }

    override fun getStatusBarColor() =
        ContextCompat.getColor(requireContext(), R.color.offer_color)

    var rcaOfferRequest: RcaOfferRequest? = null

    override fun getContentView() = R.layout.fragment_fetch_screen

    override fun initUi() {
        arguments?.let {
            rcaOfferRequest = it.getSerializable(InsOffersFragment.ARG_REQUEST) as? RcaOfferRequest?
            rcaOfferRequest?.let { it1 -> viewModel.navigateToOffers(request = it1) }
        }
    }

    override fun setObservers() {

    }


}