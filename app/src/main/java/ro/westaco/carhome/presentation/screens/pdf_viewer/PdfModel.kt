package ro.westaco.carhome.presentation.screens.pdf_viewer

import android.app.Application
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.ResponseBody
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.presentation.base.BaseViewModel
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

        api.getDocumentData(href)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                documentData?.value = it
            }, {
            })
    }

    internal fun onViewPID(insurer: String, type: String) {
        api.getInsurancePID(insurer, type)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                documentData?.value = it
            }, {
            })
    }

}