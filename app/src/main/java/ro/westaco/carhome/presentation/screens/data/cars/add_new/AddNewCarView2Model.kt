package ro.westaco.carhome.presentation.screens.data.cars.add_new

import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.GsonBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.requests.AddVehicleRequest
import ro.westaco.carhome.data.sources.remote.requests.VehicleEvent
import ro.westaco.carhome.data.sources.remote.responses.ApiResponse
import ro.westaco.carhome.navigation.SingleLiveEvent
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.utils.DateTimeUtils
import ro.westaco.carhome.utils.FirebaseAnalyticsList
import ro.westaco.carhome.utils.default
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
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
        isChecked: Boolean,
        vehicleDetails: AddVehicleRequest,
        vehicleEventList: ArrayList<VehicleEvent?>
    ) {
        if (!isChecked) {
            uiEventStream.value = UiEvent.ShowToast(R.string.confirm_details)
            return
        }

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
                api.editVehicle(it, addCarRequest).enqueue(object :
                    Callback<ApiResponse<Nothing>> {
                    override fun onResponse(
                        call: Call<ApiResponse<Nothing>>,
                        response: Response<ApiResponse<Nothing>>,
                    ) {
                        if (response.isSuccessful) {
                            uiEventStream.value = UiEvent.ShowToast(R.string.edit_success_msg)
                            Observable
                                .just(true)
                                .delay(1, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                                .subscribe {
                                    uiEventStream.value =
                                        UiEvent.NavBack
                                }
                        } else {
                            val gson = GsonBuilder().create()
                            try {
                                val pojo = gson.fromJson(
                                    response.errorBody()?.string(),
                                    ApiResponse::class.java
                                )

                                actionStream.value =
                                    pojo.errorMessage?.let { it1 ->
                                        ACTION.ShowError(
                                            it1
                                        )
                                    }

                            } catch (e: IOException) {
                            }
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse<Nothing>>,
                        t: Throwable,
                    ) {
                        uiEventStream.value =
                            UiEvent.ShowToast(R.string.server_saving_error)
                    }
                })
            }
        } else {

            api.createVehicle(addCarRequest)
                .enqueue(object :
                    Callback<ApiResponse<Long>> {
                    override fun onResponse(
                        call: Call<ApiResponse<Long>>,
                        response: Response<ApiResponse<Long>>,
                    ) {
                        if (response.isSuccessful) {
                            val params = Bundle()
                            mFirebaseAnalytics.logEvent(
                                FirebaseAnalyticsList.NEW_CAR_ADDED_ANDROID,
                                params
                            )
                            uiEventStream.value = UiEvent.ShowToast(R.string.save_success_msg)
                            Observable
                                .just(true)
                                .delay(1, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                                .subscribe {
                                    uiEventStream.value =
                                        UiEvent.NavBack
                                }
                        } else {
                            val gson = GsonBuilder().create()
                            try {
                                val pojo = gson.fromJson(
                                    response.errorBody()?.string(),
                                    ApiResponse::class.java
                                )


                                when (pojo.errorCode) {

                                    "VEHICLE_INVALID_LPN" -> actionStream.value =
                                        pojo.errorMessage?.let {
                                            ACTION.ShowError(
                                                it
                                            )
                                        }
                                    "VEHICLE_WITH_SAME_LICENSE_PLATE_ALREADY_EXISTS" -> actionStream.value =
                                        pojo.errorMessage?.let {
                                            ACTION.ShowError(
                                                it
                                            )
                                        }

                                    "VEHICLE_WITH_SAME_VIN_ALREADY_EXISTED" -> actionStream.value =
                                        pojo.errorMessage?.let {
                                            ACTION.ShowError(
                                                it
                                            )
                                        }

                                }
                            } catch (e: IOException) {
                            }
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse<Long>>,
                        t: Throwable,
                    ) {
                        uiEventStream.value = UiEvent.ShowToast(R.string.server_saving_error)
                    }
                })
        }
    }


}
