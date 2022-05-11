package ro.westaco.carhome.presentation.screens.service.insurance.summary

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.requests.PaymentRequest
import ro.westaco.carhome.data.sources.remote.requests.RcaInitRequest
import ro.westaco.carhome.data.sources.remote.responses.models.*
import ro.westaco.carhome.navigation.BundleProvider
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.service.transaction_details.TransactionDetailsFragment
import ro.westaco.carhome.utils.FirebaseAnalyticsList
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {

    /*
   ** User Interaction
   */

    var durationData = MutableLiveData<ArrayList<RcaDurationItem>>()
    var rcaInitData = MutableLiveData<RcaInitResponse>()
    var rcaTransaction = MutableLiveData<TransactionData>()
    var initTransactionDataItems = MutableLiveData<PaymentResponse>()
    var mFirebaseAnalytics = FirebaseAnalytics.getInstance(app)

    internal fun onBack() {
        uiEventStream.value = UiEvent.NavBack
    }

    internal fun onMain() {
        uiEventStream.value = UiEvent.GoToMain
    }

    internal fun onRootClicked() {
        uiEventStream.value = UiEvent.HideKeyboard
    }

    override fun onFragmentCreated() {
        super.onFragmentCreated()
        fetchDefaultData()
    }

    fun fetchDefaultData() {
        api.getRcaDuration()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { resp ->
                    durationData.value = resp?.data
                },
                {}
            )
    }

    internal fun onCtaItems(items: OffersItem, checked: Boolean, ds: Boolean) {

        if (!checked) {
            uiEventStream.value = UiEvent.ShowToast(R.string.confirm_details)
            return
        }

        val params = Bundle()
        mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.PURCHASE_INSURANCE, params)

        val dataItems = RcaInitRequest(items, null, ds)
        api.initRcaTransactions(dataItems)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                rcaInitData.value = it.data
            }, {})

    }

    internal fun paymentStart(transactionGuidId: String, invoicePersonGuidId: String) {

        val request = PaymentRequest(
            transactionGuid = transactionGuidId,
            invoicePersonGuid = invoicePersonGuidId
        )
        api.initPayment(request)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.success && it.data != null)
                    initTransactionDataItems.value = it.data
            }, {})

    }

    internal fun onPaymentSuccessful(model: PaymentResponse) {

        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.TransactionDetails, object : BundleProvider() {
                override fun onAddArgs(bundle: Bundle?): Bundle {
                    return Bundle().apply {
                        putString(TransactionDetailsFragment.ARG_TRANSACTION_GUID, model.guid)
                        putString(TransactionDetailsFragment.ARG_OF, "RO_RCA")
                    }
                }
            }))
    }

}