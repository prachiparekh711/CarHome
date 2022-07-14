package ro.westaco.carhome.presentation.screens.data.cars.add_new

import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.requests.AddVehicleRequest
import ro.westaco.carhome.data.sources.remote.requests.VehicleEvent
import ro.westaco.carhome.navigation.SingleLiveEvent
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.utils.DateTimeUtils
import ro.westaco.carhome.utils.FirebaseAnalyticsList
import ro.westaco.carhome.utils.default
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AddNewCarView2Model @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {

    val dateLiveData = MutableLiveData<HashMap<View, Long>>().default(hashMapOf())
    val actionStream: SingleLiveEvent<ACTION> = SingleLiveEvent()
    private var mFirebaseAnalytics = FirebaseAnalytics.getInstance(app)

    sealed class ACTION {
        class ShowDatePicker(val view: View, val dateInMillis: Long) : ACTION()
        class ShowError(val error: String) : ACTION()
    }


    internal fun onBack() {
        uiEventStream.value = UiEvent.NavBack
    }

    internal fun onMain() {
        uiEventStream.value = UiEvent.GoToMain
    }

    internal fun onDateClicked(view: View, dateInMillis: Long?) {
        uiEventStream.value = UiEvent.HideKeyboard

        val date = dateLiveData.value?.get(view)
        actionStream.value = ACTION.ShowDatePicker(view, dateInMillis ?: Date().time)
    }

    internal fun onDatePicked(view: View, dateInMillis: Long) {
        val datesMap = dateLiveData.value
        datesMap?.put(view, dateInMillis)
        dateLiveData.value = datesMap
    }

    internal fun convertFromServerDate(date: String?) =
        DateTimeUtils.convertFromServerDate(app, date)

    internal fun convertToServerDate(date: String?) =
        DateTimeUtils.convertToServerDate(app, date)

    internal fun onCta(
        isEdit: Boolean,
        vehicleDetails: AddVehicleRequest,
        vehicleEventList: ArrayList<VehicleEvent?>
    ) {


        val addCarRequest = AddVehicleRequest(
            vehicleDetails.vehicleIdentityCard,
            vehicleDetails.manufacturingYear,
            vehicleDetails.vehicleIdentificationNumber,
            vehicleDetails.leasingCompany,
            vehicleDetails.vehicleUsageType,
            vehicleDetails.enginePower,
            vehicleDetails.vehicleCategory,
            vehicleDetails.registrationCountryCode,
            vehicleDetails.vehicleBrand,
            vehicleDetails.maxAllowableMass,
            vehicleDetails.licensePlate,
            vehicleDetails.noOfSeats,
            vehicleDetails.fuelType,
            vehicleDetails.engineSize,
            vehicleDetails.vehicleSubCategory,
            vehicleDetails.model,
            vehicleDetails.id,
            vehicleEventList
        )


        if (isEdit) {
            vehicleDetails.id?.toLong()?.let {
                api.editVehicle(it, addCarRequest)
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ resp ->

                        uiEventStream.value =
                            UiEvent.NavBack

                    }, {

                    })
            }

        } else {
            api.createVehicle(addCarRequest)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({ resp ->
                    val params = Bundle()
                    mFirebaseAnalytics.logEvent(
                        FirebaseAnalyticsList.NEW_CAR_ADDED_ANDROID,
                        params
                    )

                    uiEventStream.value =
                        UiEvent.NavBack

                }, {

                })

        }
    }


}
