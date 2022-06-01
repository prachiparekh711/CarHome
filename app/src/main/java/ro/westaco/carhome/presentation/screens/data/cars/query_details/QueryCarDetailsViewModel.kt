package ro.westaco.carhome.presentation.screens.data.cars.query_details

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.requests.QueryVehicleInfoRequest
import ro.westaco.carhome.data.sources.remote.responses.models.Country
import ro.westaco.carhome.navigation.BundleProvider
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.SingleLiveEvent
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.data.cars.add_new.AddNewCarFragment
import ro.westaco.carhome.utils.DeviceUtils
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@HiltViewModel
class QueryCarDetailsViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {

    val stateStream: SingleLiveEvent<STATE> = SingleLiveEvent()


    enum class STATE {
        EnterVin, GeneratingProfile, Success, Error
    }

    override fun onFragmentCreated() {
        super.onFragmentCreated()
        fetchDefaultData()
    }

    var countryData = MutableLiveData<ArrayList<Country>?>()
    private fun fetchDefaultData() {
        api.getCountries()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { resp ->
                    countryData.value = resp?.data
                },
                Throwable::printStackTrace
            )
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

    internal fun onCta(countryPos: Int, vin: String, registrationNumber: String) {
        uiEventStream.value = UiEvent.HideKeyboard

        /*if (vin.isEmpty() || registrationNumber.isEmpty()) {
            uiEventStream.value = UiEvent.ShowToast(R.string.fill_all_fields)
            return
        }*/

        if (vin.length in 0..16) {

            uiEventStream.value = UiEvent.ShowToast(R.string.should_vin_num)
            return
        }

        if (registrationNumber.isEmpty()) {

            uiEventStream.value = UiEvent.ShowToast(R.string.enter_register_num)
            return
        }


        if (!DeviceUtils.isOnline(app)) {
            uiEventStream.value = UiEvent.ShowToast(R.string.int_not_connect)
            return
        }

        stateStream.value = STATE.GeneratingProfile

        val queryCarInfoRequest = QueryVehicleInfoRequest(
            countryData.value?.get(countryPos)?.code,
            vin,
            registrationNumber
        )

        Observable
            .just(true).delay(1, TimeUnit.SECONDS)
            .flatMap { api.queryVehicleInfo(queryCarInfoRequest) }
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                if (resp.success && resp.data != null) {
                    stateStream.value = STATE.Success
                    val vehicleDetails = resp.data

                    Observable
                        .just(true)
                        .delay(1, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                        .subscribe {
                            /* uiEventStream.value = UiEvent.NavBack*/
                            uiEventStream.value = UiEvent.Navigation(
                                NavAttribs(
                                    Screen.AddCar, object : BundleProvider() {
                                        override fun onAddArgs(bundle: Bundle?): Bundle {
                                            return Bundle().apply {
                                                putSerializable(
                                                    AddNewCarFragment.ARG_IS_EDIT,
                                                    false
                                                )
                                                putSerializable(
                                                    AddNewCarFragment.ARG_CAR,
                                                    vehicleDetails
                                                )
                                            }
                                        }
                                    },
                                    false
                                )
                            )
                        }
                } else {
                    stateStream.value = STATE.Error
                }
            }, {
                //   it.printStackTrace()
                stateStream.value = STATE.Error
            })
    }

    internal fun onAddCarManually(vin: String, registrationNumber: String) {
        uiEventStream.value = UiEvent.NavBack
        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.AddCar, object : BundleProvider() {
                override fun onAddArgs(bundle: Bundle?): Bundle {
                    return Bundle().apply {

                        putSerializable(
                            AddNewCarFragment.ARG_QUERY_VEHICLE,
                            vin
                        )
                        putSerializable(
                            AddNewCarFragment.Regi_Number,
                            registrationNumber
                        )
                    }
                }
            }))
    }

    internal fun onTryAgain() {
        stateStream.value = STATE.EnterVin
    }
}