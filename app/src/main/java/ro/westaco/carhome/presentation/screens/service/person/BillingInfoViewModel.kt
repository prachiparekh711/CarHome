package ro.westaco.carhome.presentation.screens.service.person

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.requests.PaymentRequest
import ro.westaco.carhome.data.sources.remote.responses.models.PaymentResponse
import ro.westaco.carhome.navigation.BundleProvider
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.service.transaction_details.TransactionDetailsFragment
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class BillingInfoViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {
    var initTransectionData = MutableLiveData<PaymentResponse>()

    companion object {
        var personID: String? = null
    }

    /*
   ** User Interaction
   */
    internal fun onBack() {
        uiEventStream.value = UiEvent.NavBack
    }

    internal fun onMain() {
        uiEventStream.value = UiEvent.GoToMain
    }

    internal fun onRootClicked() {
        uiEventStream.value = UiEvent.HideKeyboard
    }

    internal fun onNextClick(guid: String) {
        if (personID != null) {
            val request = personID?.let { PaymentRequest(it, transactionGuid = guid) }
            if (request != null) {
                api.initPayment(request)
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ resp ->
                        if (resp.success && resp.data != null) {
                            initTransectionData.value = resp.data
                        }
                    }, {
//                        Toast.makeText(app, "Error ->" + it.localizedMessage, Toast.LENGTH_SHORT).show()
                    })
            }
        } else {
            uiEventStream.value = UiEvent.ShowToast(R.string.select_person)
            return
        }
    }

    internal fun onPaymentSuccess(
        model: PaymentResponse,
        arg_of: String
    ) {
        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.TransactionDetails, object : BundleProvider() {
                override fun onAddArgs(bundle: Bundle?): Bundle {
                    return Bundle().apply {
                        putString(TransactionDetailsFragment.ARG_OF, arg_of)
                        putString(TransactionDetailsFragment.ARG_TRANSACTION_GUID, model.guid)
                    }
                }
            }))
    }
}