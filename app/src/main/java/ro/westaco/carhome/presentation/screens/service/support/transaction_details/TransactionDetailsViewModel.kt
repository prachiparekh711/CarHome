package ro.westaco.carhome.presentation.screens.service.support.transaction_details

import android.app.Application
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.responses.models.PaymentResponse
import ro.westaco.carhome.data.sources.remote.responses.models.TransactionData
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject


@HiltViewModel
class TransactionDetailsViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {

    var transactionLiveData = MutableLiveData<TransactionData?>()
    var initTransactionDataItems = MutableLiveData<PaymentResponse?>()


    fun onTransactionGuid(transactionGuid: String?, transactionOf: String) {

        if (transactionGuid == null) return

        when (transactionOf) {

            "RO_PASS_TAX" -> {
                api.getPassTaxTransaction(transactionGuid)
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ resp ->

                        transactionLiveData.value = resp.data

                    }, {
                        it.printStackTrace()
                        transactionLiveData.value = null
                    })
            }
            "RO_VIGNETTE" -> {
                api.getVignetteTransaction(transactionGuid)
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ resp ->

                        transactionLiveData.value = resp.data

                    }, {
                        it.printStackTrace()
                        transactionLiveData.value = null
                    })
            }
            "RO_RCA" -> {
                api.getRcaTransaction(transactionGuid)
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ resp ->

                        transactionLiveData.value = resp.data

                    }, {
                        it.printStackTrace()
                        transactionLiveData.value = null
                    })
            }

        }
    }

    /*
    ** User Interaction
    */
    internal fun onMain() {

        uiEventStream.value = UiEvent.GoToMain
    }

    internal fun onBack() {
        uiEventStream.value = UiEvent.NavBack
    }

    internal fun onHelpCenter() {
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.TransactionHelp))
    }


}