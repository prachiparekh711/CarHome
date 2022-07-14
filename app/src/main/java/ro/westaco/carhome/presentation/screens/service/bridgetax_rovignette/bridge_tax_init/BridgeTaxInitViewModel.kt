package ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.bridge_tax_init

import android.annotation.SuppressLint
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.GsonBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.requests.PassTaxInitRequest
import ro.westaco.carhome.data.sources.remote.responses.ApiResponse
import ro.westaco.carhome.data.sources.remote.responses.models.*
import ro.westaco.carhome.dialog.NoServerDialog
import ro.westaco.carhome.navigation.BundleProvider
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.SingleLiveEvent
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseActivity
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.summary.BridgeTaxSummaryFragment.Companion.ARG_CAR
import ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.summary.BridgeTaxSummaryFragment.Companion.ARG_ENTER_VALUE
import ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.summary.BridgeTaxSummaryFragment.Companion.ARG_PASS_TAX_REQUEST
import ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.summary.BridgeTaxSummaryFragment.Companion.ARG_PAYMENT_RESPONSE
import ro.westaco.carhome.utils.FirebaseAnalyticsList
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.IOException
import javax.inject.Inject


@HiltViewModel
class BridgeTaxInitViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {

    private var vehicle: Vehicle? = null
    private var mFirebaseAnalytics = FirebaseAnalytics.getInstance(app)
    val vehicleDetailsLivedata = MutableLiveData<VehicleDetails?>()
    var pricesLivedata = MutableLiveData<ArrayList<BridgeTaxPrices>>()
    var countryData = MutableLiveData<ArrayList<Country>>()
    var vehicleCategories = MutableLiveData<ArrayList<ServiceCategory>?>()
    var bridgeTaxObjectives = MutableLiveData<ArrayList<ObjectiveItem>>()

    val stateStream: SingleLiveEvent<STATE> = SingleLiveEvent()

    enum class STATE {
        EnterLpn, EnterVin, ErrorVin, EnterCategory, StopProgress
    }

    @SuppressLint("NullSafeMutableLiveData")
    internal fun fetchDefaultData() {
        api.getCountries()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { resp ->
                    countryData.value = resp?.data
                },
                {

                }
            )

        api.getObjectives()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                bridgeTaxObjectives.value = resp?.data
            },
                {

                })


    }

    fun getCategories() {
        api.getBridgeTaxCategories()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                vehicleCategories.value = resp?.data
            },
                {

                })

    }

    internal fun onBack() {
        uiEventStream.value = UiEvent.NavBack
    }


    override fun onFragmentCreated() {
        fetchDefaultData()
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

    @SuppressLint("NullSafeMutableLiveData")
    internal fun fetchPrices(catCode: String, objCode: String?) {

        val req = BridgeTaxPrices(passTaxCategoryCode = catCode, objectiveCode = objCode)
        api.getPasstaxPrices(req)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                if (resp.success && resp.data != null) {
                    pricesLivedata.value = resp.data
                }
            }, {
            })
    }

    internal fun onSave(
        vehicle: Vehicle?,
        registrationCountryCode: String,
        licensePlate: String?,
        vin: String?,
        price: BridgeTaxPrices,
        startDate: String?,
        lowerCategoryReason: String?,
        checked: Boolean,
        type: String
    ) {

        uiEventStream.value = UiEvent.HideKeyboard

        if (licensePlate?.isNotEmpty() == true) {
            if (!checked) {
                uiEventStream.value =
                    UiEvent.ShowToast(R.string.check_info)
                return
            }
        } else {
            uiEventStream.value = UiEvent.ShowToast(R.string.liecence_plate_number_empty)
            return
        }


        if (!vin.isNullOrEmpty()) {
            if (vin.length != 17) {
                stateStream.value = STATE.ErrorVin
                return
            }
        }

        val request = PassTaxInitRequest(
            registrationCountryCode,
            licensePlate,
            lowerCategoryReason,
            price,
            vin,
            vehicleGuid = vehicle?.guid,
            startDate
        )

        api.initPassTax(request)
            .enqueue(object : Callback<ApiResponse<PaymentResponse>> {

                override fun onFailure(
                    call: Call<ApiResponse<PaymentResponse>>,
                    t: Throwable
                ) {
                    stateStream.value = STATE.StopProgress
                }

                override fun onResponse(
                    call: Call<ApiResponse<PaymentResponse>>,
                    response: Response<ApiResponse<PaymentResponse>>
                ) {
                    if (response.isSuccessful) {
//                        initTransectionData.value = response.body()?.data
                        response.body()?.data?.let {
                            onSuccess(request, it, type, vehicle)
                        }
                    } else {
                        stateStream.value = STATE.StopProgress
                        val gson = GsonBuilder().create()
                        try {
                            val pojo = gson.fromJson(
                                response.errorBody()?.string(),
                                ApiResponse::class.java
                            )

                            when (pojo.errorCode) {
                                "VEHICLE_INVALID_LPN" -> stateStream.value = STATE.EnterLpn
                                "TRANSACTION_VIN_REQUIRED" -> stateStream.value = STATE.EnterVin
                                "TRANSACTION_PASS_TAX_VIN_REQUIRED" -> stateStream.value =
                                    STATE.ErrorVin
                                "TRANSACTION_LOWER_CATEGORY_REASON_REQUIRED" -> stateStream.value =
                                    STATE.EnterCategory
                            }
                        } catch (e: Exception) {
                            if (e is IOException) {
                                Handler(Looper.getMainLooper()).post {
                                    BaseActivity.instance?.let {
                                        NoServerDialog.showServerErrorInfo(
                                            it
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            })

    }


    internal fun onSuccess(
        request: PassTaxInitRequest,
        response: PaymentResponse,
        type: String,
        vehicle: Vehicle?
    ) {
        val params = Bundle()
        mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.PURCHASE_BRIDGE_TAX_ANDROID, params)

        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.BridgeTaxSummary, object : BundleProvider() {
                override fun onAddArgs(bundle: Bundle?): Bundle {
                    return Bundle().apply {
                        stateStream.value = STATE.StopProgress
                        putSerializable(ARG_PAYMENT_RESPONSE, response)
                        putSerializable(ARG_PASS_TAX_REQUEST, request)
                        putString(ARG_ENTER_VALUE, type)
                        putSerializable(ARG_CAR, vehicle)
                    }
                }
            }))
    }

}