package ro.westaco.carhome.presentation.screens.service.insurance.init

import android.annotation.SuppressLint
import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.requests.RcaOfferRequest
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem
import ro.westaco.carhome.data.sources.remote.responses.models.RcaDurationItem
import ro.westaco.carhome.navigation.BundleProvider
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.SingleLiveEvent
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.service.insurance.offers.FetchScreenFragment
import ro.westaco.carhome.presentation.screens.service.insurance.offers.InsOffersFragment.Companion.ARG_REQUEST
import ro.westaco.carhome.presentation.screens.service.insurance.offers.InsOffersFragment.Companion.ARG_RESPONSE
import ro.westaco.carhome.utils.DateTimeUtils
import ro.westaco.carhome.utils.FirebaseAnalyticsList
import ro.westaco.carhome.utils.default
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

@HiltViewModel
class InsuranceStep2ViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi,

    ) : BaseViewModel() {

    var durationData = MutableLiveData<ArrayList<RcaDurationItem>>()
    var usageData = MutableLiveData<ArrayList<CatalogItem>>()
    val startDateLiveData = MutableLiveData<HashMap<View, Long>>().default(hashMapOf())
    val actionStream: SingleLiveEvent<ACTION> = SingleLiveEvent()
    var mFirebaseAnalytics = FirebaseAnalytics.getInstance(app)


    sealed class ACTION {
        class ShowDatePicker(val view: View, val dateInMillis: Long) : ACTION()
    }

    internal fun onBack() {
        uiEventStream.value = UiEvent.NavBack
    }

    internal fun onDirectClaim() {
        uiEventStream.value = UiEvent.Navigation(
            NavAttribs(
                Screen.DirectClaimInfoScreen
            )
        )
    }


    internal fun convertFromServerDate(date: String?) =
        DateTimeUtils.convertFromServerDate(app, date)

    internal fun convertToServerDate(date: String?) =
        DateTimeUtils.convertToServerDate(app, date)

    override fun onFragmentCreated() {
        super.onFragmentCreated()
        fetchDefaultData()
    }

    internal fun onDateClicked(view: View, dateInMillis: Long?) {
        uiEventStream.value = UiEvent.HideKeyboard

        val date = startDateLiveData.value?.get(view)
        actionStream.value = ACTION.ShowDatePicker(view, dateInMillis ?: Date().time)
    }

    internal fun onDatePicked(view: View, dateInMillis: Long) {
        val datesMap = startDateLiveData.value
        datesMap?.put(view, dateInMillis)
        startDateLiveData.value = datesMap
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
        api.getVehicleUsage()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                usageData.value = resp?.data
            }, {
                //   it.printStackTrace()
            })

    }

    internal fun onCta(request: RcaOfferRequest) {
        val params = Bundle()
        mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.INSURANCE_GET_OFFERS, params)
        uiEventStream.value = UiEvent.Navigation(
            NavAttribs(
                Screen.FetchOfferScreen,
                object : BundleProvider() {
                    override fun onAddArgs(bundle: Bundle?): Bundle {
                        return Bundle().apply {
                            putSerializable(FetchScreenFragment.ARG_REQUEST, request)
                        }
                    }
                }
            )
        )

    }

    internal fun navigateToOffers(request: RcaOfferRequest) {

        api.getRcaOffers(request)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->


                uiEventStream.value =
                    UiEvent.Navigation(
                        NavAttribs(
                            Screen.InsuranceOffers,
                            object : BundleProvider() {
                                override fun onAddArgs(bundle: Bundle?): Bundle {
                                    return Bundle().apply {
                                        putSerializable(ARG_RESPONSE, resp.data)
                                        putSerializable(ARG_REQUEST, request)
                                    }
                                }
                            }, false
                        )
                    )

            }, {

                uiEventStream.value = UiEvent.ShowToast(R.string.fill_all_fields)
                onBack()
            })


    }

}