package ro.westaco.carhome.presentation.screens.service.insurance.car_selection

import android.app.Application
import android.os.Bundle
import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.responses.models.RcaCarIdentifyResponse
import ro.westaco.carhome.data.sources.remote.responses.models.Vehicle
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
class InsuranceCarViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi,

    ) : BaseViewModel() {

    val carsLivedata = MutableLiveData<ArrayList<Vehicle>?>()
    private val validationLivedata = MutableLiveData<RcaCarIdentifyResponse?>()

    val actionStream: SingleLiveEvent<ACTION> = SingleLiveEvent()

    sealed class ACTION {
        object onSuccess : ACTION()
    }

    override fun onFragmentCreated() {
        fetchRemoteData()
    }

    internal fun onBack() {
        uiEventStream.value = UiEvent.NavBack
    }

    internal fun onMain() {
        uiEventStream.value = UiEvent.GoToMain
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

    fun identifyVehicle(selectedVehicle: Vehicle?) {

        selectedVehicle?.guid?.let {
            api.identifyVehicle(it)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({ resp ->
                    validationLivedata.value = resp?.data
                    uiEventStream.value = UiEvent.Navigation(
                        NavAttribs(
                            Screen.InsuranceEditCar,
                            object : BundleProvider() {
                                override fun onAddArgs(bundle: Bundle?): Bundle {
                                    return Bundle().apply {
                                        actionStream.value = ACTION.onSuccess
                                        putParcelableArrayList(
                                            InsCarEditFragment.ARG_CAR_WARNING,
                                            resp?.data?.warnings as java.util.ArrayList<out Parcelable>
                                        )

                                        putBoolean(
                                            InsCarEditFragment.ARG_IS_EDIT,
                                            true
                                        )

                                        selectedVehicle.id?.let { it1 ->
                                            putInt(
                                                InsCarEditFragment.ARG_CAR_ID,
                                                it1
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    )
                }, {
                })
        }
    }

    fun onAddNew() {
        uiEventStream.value = UiEvent.Navigation(
            NavAttribs(
                Screen.InsuranceAddCar
            )
        )
    }
}