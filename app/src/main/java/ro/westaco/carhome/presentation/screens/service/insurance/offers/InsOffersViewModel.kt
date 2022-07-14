package ro.westaco.carhome.presentation.screens.service.insurance.offers

import android.annotation.SuppressLint
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.requests.RcaOfferRequest
import ro.westaco.carhome.data.sources.remote.responses.models.RcaDurationItem
import ro.westaco.carhome.data.sources.remote.responses.models.RcaOfferResponse
import ro.westaco.carhome.navigation.BundleProvider
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.SingleLiveEvent
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.service.insurance.offers.InsOfferDetailsFragment.Companion.ARG_OFFERDETAIL
import ro.westaco.carhome.presentation.screens.service.insurance.summary.SummaryFragment
import ro.westaco.carhome.utils.FirebaseAnalyticsList
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class InsOffersViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {

    /*
   ** User Interaction
   */
    val stateStream: SingleLiveEvent<STATE> = SingleLiveEvent()

    enum class STATE {
        IN_PROGESS
    }

    var durationData = MutableLiveData<ArrayList<RcaDurationItem>>()
    var rcaOfferResponseData = MutableLiveData<RcaOfferResponse>()
    var mFirebaseAnalytics = FirebaseAnalytics.getInstance(app)

    override fun onFragmentCreated() {
        super.onFragmentCreated()
        fetchDefaultData()
    }

    internal fun onBack() {
        uiEventStream.value = UiEvent.NavBack
    }

    internal fun onMain() {
        uiEventStream.value = UiEvent.GoToMain
    }

    internal fun onRootClicked() {
        uiEventStream.value = UiEvent.HideKeyboard
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun fetchDefaultData() {
        api.getRcaDuration()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { resp ->
                    durationData.value = resp?.data
                },
                {

                }
            )
    }

    @SuppressLint("NullSafeMutableLiveData")
    internal fun onChangeDuration(request: RcaOfferRequest) {

        api.getRcaOffers(request)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                rcaOfferResponseData.value = resp.data
            }, {
                //   it.printStackTrace()
            })
    }

    internal fun onViewOffer(offerCode: String, insurerCode: String) {
        val params = Bundle()
        mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.INSURANCE_GET_OFFER_DETAILS, params)
        api.getRcaOfferDetails(offerCode, insurerCode)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                stateStream.value = STATE.IN_PROGESS
                uiEventStream.value =
                    UiEvent.Navigation(
                        NavAttribs(
                            Screen.InsuranceOfferDetail,
                            object : BundleProvider() {
                                override fun onAddArgs(bundle: Bundle?): Bundle {
                                    return Bundle().apply {
                                        putSerializable(ARG_OFFERDETAIL, resp.data)
                                    }
                                }
                            })
                    )
            }, {
                //   it.printStackTrace()
            })
    }

    internal fun onViewSummary(
        offerCode: String,
        insurerCode: String,
        ds: Boolean,
    ) {
        api.getRcaOfferDetails(offerCode, insurerCode)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                stateStream.value = STATE.IN_PROGESS
                uiEventStream.value =
                    UiEvent.Navigation(
                        NavAttribs(
                            Screen.InsuranceSummary,
                            object : BundleProvider() {
                                override fun onAddArgs(bundle: Bundle?): Bundle {
                                    return Bundle().apply {
                                        putSerializable(SummaryFragment.ARG_OFFERDETAIL, resp.data)
                                        putBoolean(SummaryFragment.ARG_DS, ds)
                                    }
                                }
                            })
                    )
            }, {
                //   it.printStackTrace()
            })
    }


}