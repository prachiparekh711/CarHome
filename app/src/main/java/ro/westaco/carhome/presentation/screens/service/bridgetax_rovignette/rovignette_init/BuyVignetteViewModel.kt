package ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.rovignette_init

import android.annotation.SuppressLint
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.GsonBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.requests.InitVignettePurchaseRequest
import ro.westaco.carhome.data.sources.remote.responses.ApiResponse
import ro.westaco.carhome.data.sources.remote.responses.models.*
import ro.westaco.carhome.navigation.BundleProvider
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.SingleLiveEvent
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.summary.BridgeTaxSummaryFragment.Companion.ARG_CAR
import ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.summary.BridgeTaxSummaryFragment.Companion.ARG_ENTER_VALUE
import ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.summary.BridgeTaxSummaryFragment.Companion.ARG_PASS_TAX_REQUEST
import ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.summary.BridgeTaxSummaryFragment.Companion.ARG_PAYMENT_RESPONSE
import ro.westaco.carhome.utils.DateTimeUtils
import ro.westaco.carhome.utils.DeviceUtils
import ro.westaco.carhome.utils.FirebaseAnalyticsList
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


@HiltViewModel
class BuyVignetteViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi,
) : BaseViewModel() {

    private var mFirebaseAnalytics = FirebaseAnalytics.getInstance(app)
    internal val dateLiveData = MutableLiveData<Long>()
    internal val vehicleDetailsLivedata = MutableLiveData<VehicleDetails>()
    var vignettePricesLivedata = MutableLiveData<ArrayList<VignettePrice>?>()
    private var vignetteDurationPosLivedata = MutableLiveData<Int>()

    val stateStream: SingleLiveEvent<STATE> = SingleLiveEvent()

    enum class STATE {
        EnterLpn, EnterVin, ErrorVin, EnterCategory
    }

    var rovignetteCategories = MutableLiveData<ArrayList<ServiceCategory>>()
    var rovignetteDurations = MutableLiveData<ArrayList<RovignetteDuration>>()
    var countryData = MutableLiveData<ArrayList<Country>>()

    private var vehicle: Vehicle? = null

    internal val actionStream: SingleLiveEvent<ACTION> = SingleLiveEvent()

    sealed class ACTION {
        class ShowDatePicker(val dateInMillis: Long) : ACTION()
        class ShowError(val error: String) : ACTION()
        class ShowDateError(val error: String) : ACTION()
    }

    override fun onFragmentCreated() {
        fetchDefaultData()
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun fetchDefaultData() {
        api.getCountries()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { resp ->
                    countryData.value = resp?.data
                },
                {

                }
            )


        api.getRovignetteDurations()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                rovignetteDurations.value = it.data
                fetchPrices()
            }, {


            })

        api.getRovignetteCategories()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                rovignetteCategories.value = it.data
            }, {

            })

    }

    @SuppressLint("NullSafeMutableLiveData")
    fun fetchPrices() {
        api.getVignettePrices()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                if (resp.success && resp.data != null) {
                    vignettePricesLivedata.value = resp.data

                }
            }, {
            })

    }

    internal fun onVehicle(vehicle: Vehicle) {
        this.vehicle = vehicle
        vehicle.id?.let {
            api.getVehicle(it)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({ resp ->
                    if (resp.success && resp.data != null) {
                        vehicleDetailsLivedata.value = resp.data
                    }
                }, {
                })
        }
    }

    internal fun onBack() {
        uiEventStream.value = UiEvent.NavBack
    }

    internal fun onMain() {
        uiEventStream.value = UiEvent.GoToMain
    }

    internal fun onDateClicked() {
        uiEventStream.value = UiEvent.HideKeyboard

        val date = dateLiveData.value
        actionStream.value = ACTION.ShowDatePicker(date ?: Date().time)
    }

    internal fun onDatePicked(dateInMillis: Long) {
        dateLiveData.value = dateInMillis
    }

    internal fun onCta(
        vehicle: Vehicle?,
        vignetteCategoryPos: String,
        startDate: String,
        registrationCountryCode: String,
        licensePlate: String,
        vin: String?,
        model: VignettePrice,
        isChecked: Boolean,
        type: String,
    ) {
        uiEventStream.value = UiEvent.HideKeyboard

        if (licensePlate.isEmpty() || startDate.isEmpty() || vignetteCategoryPos.isEmpty()) {
            uiEventStream.value = UiEvent.ShowToast(R.string.fill_all_fields)
            return
        }

        if (!isChecked) {
            uiEventStream.value = UiEvent.ShowToast(R.string.confirm_details)
            return
        }

        if (licensePlate.length >= 11) {
            uiEventStream.value = UiEvent.ShowToast(R.string.license_error)
            return
        }

        if (!DeviceUtils.isOnline(app)) {
            uiEventStream.value = UiEvent.ShowToast(R.string.int_not_connect)
            return
        }

        val request = InitVignettePurchaseRequest(
            vehicleGuid = vehicle?.guid,
            registrationCountryCode = registrationCountryCode,
            licensePlate = licensePlate,
            vin = vin,
            price = model,
            startDate =
            DateTimeUtils.convertDate(
                startDate,
                SimpleDateFormat(app.getString(R.string.date_format_template), Locale.US),
                SimpleDateFormat(
                    app.getString(R.string.server_standard_datetime_format_template),
                    Locale.US
                )
            )
        )

        api.initVignettePurchaseModel(request)
            .enqueue(object : Callback<ApiResponse<PaymentResponse>> {
                override fun onResponse(
                    call: Call<ApiResponse<PaymentResponse>>,
                    response: Response<ApiResponse<PaymentResponse>>,
                ) {

                    if (response.isSuccessful) {
                        val data = response.body()?.data

                        if (data != null) {

                            onSuccess(request, data, vehicle, type)

                        }

                    } else {

                        val gson = GsonBuilder().create()
                        try {
                            val pojo = gson.fromJson(
                                response.errorBody()?.string(),
                                ApiResponse::class.java
                            )


                            when (pojo.errorCode) {

                                "VEHICLE_NOT_FOUND" ->
                                    actionStream.value = pojo.errorMessage?.let {
                                        ACTION.ShowError(
                                            it
                                        )
                                    }

                                "VEHICLE_INVALID_LPN" ->
                                    stateStream.value = STATE.EnterLpn

                                "TRANSACTION_VIN_REQUIRED" ->
                                    stateStream.value = STATE.EnterVin

                                "TRANSACTION_VIGNETTE_COUNTRY_NOT_ACCEPTED" ->
                                    actionStream.value = pojo.errorMessage?.let {
                                        ACTION.ShowError(
                                            it
                                        )
                                    }

                                "TRANSACTION_LOWER_CATEGORY_REASON_REQUIRED" ->
                                    stateStream.value = STATE.EnterCategory

                                "TRANSACTION_VIGNETTE_INTERVAL_OVERLAP" ->
                                    actionStream.value = pojo.errorMessage?.let {
                                        ACTION.ShowDateError(
                                            it
                                        )
                                    }

                            }

                        } catch (e: IOException) {
                        }

                    }

                }

                override fun onFailure(
                    call: Call<ApiResponse<PaymentResponse>>,
                    t: Throwable,
                ) {


                }
            })
    }

    internal fun onSuccess(
        request: InitVignettePurchaseRequest,
        model: PaymentResponse,
        vehicle: Vehicle?,
        type: String,
    ) {

        val params = Bundle()
        mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.PURCHASE_ROVINIETA_ANDROID, params)

        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.BridgeTaxSummary, object : BundleProvider() {

                override fun onAddArgs(bundle: Bundle?): Bundle {

                    return Bundle().apply {

                        putSerializable(ARG_PAYMENT_RESPONSE, model)
                        putSerializable(ARG_PASS_TAX_REQUEST, request)
                        putString(ARG_ENTER_VALUE, type)
                        putSerializable(ARG_CAR, vehicle)

                    }
                }
            }))

    }

}