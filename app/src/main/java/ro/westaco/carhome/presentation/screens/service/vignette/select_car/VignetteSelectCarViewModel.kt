package ro.westaco.carhome.presentation.screens.service.vignette.select_car

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.responses.models.Vehicle
import ro.westaco.carhome.navigation.BundleProvider
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.service.vignette.buy.BuyVignetteFragment
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject


@HiltViewModel
class VignetteSelectCarViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {

    val carsLivedata = MutableLiveData<ArrayList<Vehicle>>()

    override fun onFragmentCreated() {
        fetchRemoteData()
    }

    private fun fetchRemoteData() {
        api.getVehicles()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                carsLivedata.value = resp?.data
            }, {
                //   it.printStackTrace()
            })
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

    internal fun onStartWithNew() {
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.BuyVignette))
    }

    internal fun onCta(selectedCar: Vehicle?) {

        if (selectedCar == null) {

            uiEventStream.value = UiEvent.ShowToast(R.string.vignette_select_car)
            return

        }

        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.BuyVignette, object : BundleProvider() {
                override fun onAddArgs(bundle: Bundle?): Bundle {
                    return Bundle().apply {

                        putSerializable(BuyVignetteFragment.ARG_CAR, selectedCar)
                    }
                }
            }))

    }

}