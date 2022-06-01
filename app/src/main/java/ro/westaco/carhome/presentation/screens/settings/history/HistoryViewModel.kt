package ro.westaco.carhome.presentation.screens.settings.history

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.responses.models.HistoryItem
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
class HistoryViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {

    var historyLiveData = MutableLiveData<ArrayList<HistoryItem>?>()

    override fun onFragmentCreated() {
        super.onFragmentCreated()
        fetchRemoteData()
    }

    fun fetchRemoteData() {
        api.getHistory()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                historyLiveData.value = resp?.data
            }, {

            })
    }

    internal fun onBack() {
        uiEventStream.value = UiEvent.NavBack
    }

    internal fun onMain() {
        uiEventStream.value = UiEvent.GoToMain
    }

    internal fun onHistoryDetail(item: HistoryItem) {
        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.TransactionDetails, object : BundleProvider() {
                override fun onAddArgs(bundle: Bundle?): Bundle {
                    return Bundle().apply {
                        putString(
                            TransactionDetailsFragment.ARG_TRANSACTION_GUID,
                            item.transactionGuid
                        )
                        putString(
                            TransactionDetailsFragment.ARG_OF,
                            item.service
                        )
                        putBoolean(
                            TransactionDetailsFragment.ARG_HISTORY,
                            true
                        )
                    }
                }
            }))
    }
}