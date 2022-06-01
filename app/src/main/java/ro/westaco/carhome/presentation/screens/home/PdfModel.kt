package ro.westaco.carhome.presentation.screens.home

import android.app.Application
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.ResponseBody
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.utils.DeviceUtils
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject


@HiltViewModel
class PdfModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {

    var documentData: MutableLiveData<ResponseBody>? = MutableLiveData()

    internal fun fetchDocumentData(href: String) {

        if (!DeviceUtils.isOnline(app)) {
            uiEventStream.value = UiEvent.ShowToast(R.string.int_not_connect)
            return
        }

        api.getDocumentData(href)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                documentData?.value = it
            }, {
                uiEventStream.value = UiEvent.ShowToast(R.string.general_server_error)
            })
    }

    internal fun onViewPID(insurer: String, type: String) {
        api.getInsurancePID(insurer, type)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                documentData?.value = it
            }, {
                uiEventStream.value = UiEvent.ShowToast(R.string.general_server_error)
            })
    }

}