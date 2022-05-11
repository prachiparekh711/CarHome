package ro.westaco.carhome.presentation.screens.service.transaction_details

import android.app.Application
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.ResponseBody
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.responses.models.TransactionData
import ro.westaco.carhome.di.ApiModule
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.home.PdfActivity
import ro.westaco.carhome.utils.DeviceUtils
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import javax.inject.Inject


@HiltViewModel
class TransactionDetailsViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {

    internal val transactionLiveData = MutableLiveData<TransactionData>()

    fun onTransactionGuid(transactionGuid: String?, transactionOf: String) {
        if (transactionGuid == null) return

        when (transactionOf) {
            "RO_PASS_TAX" -> {
                api.getPassTaxTransaction(transactionGuid)
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        if (it.success && it.data != null) {
                            transactionLiveData.value = it.data
                        }
                    }, {
                        //   it.printStackTrace()
                    })
            }
            "RO_VIGNETTE" -> {
                api.getVignetteTransaction(transactionGuid)
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        if (it.success && it.data != null) {
                            transactionLiveData.value = it.data
                        }
                    }, {
                        //   it.printStackTrace()
                    })
            }
            "RO_RCA" -> {
                api.getRcaTransaction(transactionGuid)
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        if (it.success && it.data != null) {
                            transactionLiveData.value = it.data
                        }
                    }, {
                        //   it.printStackTrace()
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

    internal fun onHistory() {
        uiEventStream.value = UiEvent.NavBack
    }


    var attachmentData: MutableLiveData<ResponseBody> = MutableLiveData()
    internal fun fetchData() {

        if (!DeviceUtils.isOnline(app)) {
            uiEventStream.value = UiEvent.ShowToast(R.string.int_not_connect)
            return
        }

        val vignetteTransaction = transactionLiveData.value
        val href = ApiModule.BASE_URL_RESOURCES + vignetteTransaction?.ticket?.href
        href.let {
            api.getAttachmentData(it)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    attachmentData.value = it
                    if (attachmentData.value != null) {
                        val buffer = ByteArray(8192)
                        var bytesRead: Int? = null
                        val output = ByteArrayOutputStream()
                        while (attachmentData.value?.byteStream()?.read(buffer).also {
                                if (it != null) {
                                    bytesRead = it
                                }
                            } != -1) {
                            bytesRead?.let { it1 -> output.write(buffer, 0, it1) }
                        }
                        openPDF(output.toByteArray())
                    }

                }, {
                })
        }
    }

    private fun openPDF(data: ByteArray) {

        val intent = Intent(app, PdfActivity::class.java)
        intent.putExtra(PdfActivity.ARG_DATA, data)
        intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        uiEventStream.postValue(
            UiEvent.OpenIntent(
                intent,
                false
            )
        )
    }

}