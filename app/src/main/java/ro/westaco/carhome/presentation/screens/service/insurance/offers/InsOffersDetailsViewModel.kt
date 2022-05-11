package ro.westaco.carhome.presentation.screens.service.insurance.offers

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.ResponseBody
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem
import ro.westaco.carhome.data.sources.remote.responses.models.RcaDurationItem
import ro.westaco.carhome.navigation.BundleProvider
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.home.PdfActivity
import ro.westaco.carhome.presentation.screens.service.insurance.summary.SummaryFragment
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class InsOffersDetailsViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {

    /*
   ** User Interaction
   */

    var durationData = MutableLiveData<ArrayList<RcaDurationItem>>()
    var rcaOfferPID: MutableLiveData<ResponseBody> = MutableLiveData()
    var fuelTypeData = MutableLiveData<ArrayList<CatalogItem>>()
    var usageTypeData = MutableLiveData<ArrayList<CatalogItem>>()

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
        fetchDefaultDataDuration()
    }

    fun fetchDefaultDataDuration() {
        api.getRcaDuration()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { resp ->
                    durationData.value = resp?.data
                },
                {}
            )

        api.getSimpleCatalog("NOM_VEHICLE_FUEL_TYPE")
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { fuelTypeData.value = it.data },
                {
                }
            )

        api.getSimpleCatalog("NOM_VEHICLE_USAGE_TYPE")
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { usageTypeData.value = it.data },
                {
                }
            )
    }

    internal fun onViewSummaryFragment(
        offerCode: String,
        insurerCode: String,
        ds: Boolean,
    ) {

        api.getRcaOfferDetails(offerCode, insurerCode)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                uiEventStream.value = UiEvent.Navigation(
                    NavAttribs(Screen.InsuranceSummary,
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


    internal fun onViewPID(insurer: String, type: String) {
        api.getInsurancePID(insurer, type)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe { new ->
                rcaOfferPID.value = new
                if (rcaOfferPID.value != null) {
                    val buffer = ByteArray(8192)
                    var bytesRead: Int? = null
                    val output = ByteArrayOutputStream()
                    while (rcaOfferPID.value?.byteStream()?.read(buffer).also {
                            if (it != null) {
                                bytesRead = it
                            }
                        } != -1) {
                        bytesRead?.let { it1 -> output.write(buffer, 0, it1) }
                    }
                    openPDF(output.toByteArray())
                }
            }
    }

    internal fun openPDF(data: ByteArray) {

        val intent = Intent(app, PdfActivity::class.java)
        intent.putExtra(PdfActivity.ARG_DATA, data)
        intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        uiEventStream.postValue(
            UiEvent.OpenIntent(intent, false)
        )
    }

}