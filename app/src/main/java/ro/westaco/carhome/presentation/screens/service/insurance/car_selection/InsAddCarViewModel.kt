package ro.westaco.carhome.presentation.screens.service.insurance.car_selection

import android.app.Application
import android.os.Bundle
import android.os.Parcelable
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
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class InsAddCarViewModel @Inject constructor(
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

    internal fun onBack() {
        uiEventStream.value = UiEvent.NavBack
    }

    internal fun onMain() {
        uiEventStream.value = UiEvent.GoToMain
    }

    internal fun onRootClicked() {
        uiEventStream.value = UiEvent.HideKeyboard
    }

    internal fun onCta(country: Country, vin: String?, registrationNumber: String?) {
        uiEventStream.value = UiEvent.HideKeyboard

        if (vin?.isNotEmpty() == true && vin.length != 17) {
            uiEventStream.value = UiEvent.ShowToast(R.string.should_vin_num)
            return
        }


        stateStream.value = STATE.GeneratingProfile

        val queryCarInfoRequest = QueryVehicleInfoRequest(
            country.code,
            vin,
            registrationNumber
        )

        api.queryVehicleInfo(queryCarInfoRequest)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                if (resp.success && resp.data != null) {
                    stateStream.value = STATE.Success
                    if (vin != null) {
                        if (registrationNumber != null) {
                            gotToEditPage(vin, registrationNumber)
                        }
                    }
                } else {
                    stateStream.value = STATE.Error
                }
            }, {
                stateStream.value = STATE.Error
            })
    }


    internal fun gotToEditPage(vin: String, registrationNumber: String) {
        api.validateVehicle(false)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                uiEventStream.value = UiEvent.Navigation(
                    NavAttribs(
                        Screen.InsuranceEditCar,
                        object : BundleProvider() {
                            override fun onAddArgs(bundle: Bundle?): Bundle {
                                return Bundle().apply {
                                    putParcelableArrayList(
                                        InsCarEditFragment.ARG_CAR_WARNING,
                                        resp?.data?.warnings as java.util.ArrayList<out Parcelable>
                                    )
                                    putBoolean(
                                        InsCarEditFragment.ARG_IS_EDIT,
                                        false
                                    )
                                    putString(
                                        InsCarEditFragment.ARG_VIN,
                                        vin
                                    )
                                    putString(
                                        InsCarEditFragment.ARG_REG_NUMBER,
                                        registrationNumber
                                    )
                                }
                            }
                        }, false
                    )
                )
            }, {
            })
    }

    internal fun onTryAgain() {
        stateStream.value = STATE.EnterVin
    }
}