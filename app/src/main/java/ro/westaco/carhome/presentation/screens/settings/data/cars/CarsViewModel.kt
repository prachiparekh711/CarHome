package ro.westaco.carhome.presentation.screens.settings.data.cars

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.ResponseBody
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.responses.models.Vehicle
import ro.westaco.carhome.navigation.BundleProvider
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.SingleLiveEvent
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.home.PdfActivity
import ro.westaco.carhome.presentation.screens.service.bridgetax.init.BridgeTaxInitFragment
import ro.westaco.carhome.presentation.screens.service.vignette.buy.BuyVignetteFragment
import ro.westaco.carhome.presentation.screens.settings.data.cars.details.CarDetailsFragment
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import javax.inject.Inject


@HiltViewModel
class CarsViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {

    val carsLivedata: MutableLiveData<ArrayList<Vehicle>> = MutableLiveData()

    override fun onFragmentCreated() {
        fetchRemoteData()
    }

    val stateStream: SingleLiveEvent<STATE> = SingleLiveEvent()

    enum class STATE {
        DOCUMENT_NOT_FOUND
    }

    private fun fetchRemoteData() {
        api.getVehicles()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                carsLivedata.value = resp?.data
            }, {
                it.printStackTrace()
                carsLivedata.value = null
            })
    }

    internal fun onItemClick(item: Vehicle) {
        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.CarDetails, object : BundleProvider() {
                override fun onAddArgs(bundle: Bundle?): Bundle {
                    return Bundle().apply {
                        putSerializable(CarDetailsFragment.ARG_CAR, item)
                    }
                }
            }))
    }

    internal fun onAddNew() {
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.QueryCar))
    }

    internal fun onBuyPassTax(vehicle: Vehicle) {
        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.BridgeTaxInit, object : BundleProvider() {
                override fun onAddArgs(bundle: Bundle?): Bundle {
                    return Bundle().apply {

                        putSerializable(BridgeTaxInitFragment.ARG_CAR, vehicle)
                    }
                }
            }))
    }

    internal fun onBuyVignette(vehicle: Vehicle) {
        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.BuyVignette, object : BundleProvider() {
                override fun onAddArgs(bundle: Bundle?): Bundle {
                    return Bundle().apply {

                        putSerializable(BuyVignetteFragment.ARG_CAR, vehicle)
                    }
                }
            }))
    }

    internal var attachmentData: MutableLiveData<ResponseBody> = MutableLiveData()
    internal fun fetchData(href: String) {

        api.getAttachmentData(href)
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

                stateStream.value = STATE.DOCUMENT_NOT_FOUND
            })

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