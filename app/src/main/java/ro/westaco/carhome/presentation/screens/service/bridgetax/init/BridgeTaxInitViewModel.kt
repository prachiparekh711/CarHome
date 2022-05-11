package ro.westaco.carhome.presentation.screens.service.bridgetax.init

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
import ro.westaco.carhome.data.sources.remote.requests.PassTaxInitRequest
import ro.westaco.carhome.data.sources.remote.responses.ApiResponse
import ro.westaco.carhome.data.sources.remote.responses.models.*
import ro.westaco.carhome.navigation.BundleProvider
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.SingleLiveEvent
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.service.person.BillingInformationFragment
import ro.westaco.carhome.presentation.screens.service.person.BillingInformationFragment.Companion.ARG_CAR
import ro.westaco.carhome.presentation.screens.service.person.BillingInformationFragment.Companion.ARG_GUID
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
    internal val vehicleDetailsLivedata = MutableLiveData<VehicleDetails>()
    var pricesLivedata = MutableLiveData<ArrayList<BridgeTaxPrices>>()
    var countryData = MutableLiveData<java.util.ArrayList<Country>>()
    var bridgeTaxCategories = MutableLiveData<java.util.ArrayList<ServiceCategory>>()
    var bridgeTaxObjectives = MutableLiveData<java.util.ArrayList<ObjectiveItem>>()

    val stateStream: SingleLiveEvent<STATE> = SingleLiveEvent()

    enum class STATE {
        EnterLpn, EnterVin, ErrorVin, EnterCategory
    }

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

        api.getBridgeTaxCategories()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                bridgeTaxCategories.value = resp?.data
            },
                {

                })

        api.getObjectives()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                bridgeTaxObjectives.value = resp?.data
            },
                {

                })
    }

    internal fun onBack() {
        uiEventStream.value = UiEvent.NavBack
    }

    internal fun onMain() {
        uiEventStream.value = UiEvent.GoToMain
    }

    override fun onFragmentCreated() {
        fetchDefaultData()
    }

    internal fun onVehicle(vehicle: Vehicle) {
        this.vehicle = vehicle
        vehicle.id?.let {
            api.getVehicle(it)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.success && it.data != null) {
                        vehicleDetailsLivedata.value = it.data
                    }
                }, {
                })
        }
    }

    internal fun fetchPrices(code: String, code1: String?) {

        val req = BridgeTaxPrices(code, null, null, null, code1, null, null, null)

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
        checked: Boolean
    ) {

        uiEventStream.value = UiEvent.HideKeyboard

        if (licensePlate?.isNotEmpty() == true) {
            if (!checked) {
                uiEventStream.value =
                    UiEvent.ShowToast(R.string.confirm_details)
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
            vehicle?.guid,
            startDate
        )

        api.initPassTax(request)
            .enqueue(object : Callback<ApiResponse<PaymentResponse>> {

                override fun onFailure(
                    call: Call<ApiResponse<PaymentResponse>>,
                    t: Throwable
                ) {
                }

                override fun onResponse(
                    call: Call<ApiResponse<PaymentResponse>>,
                    response: Response<ApiResponse<PaymentResponse>>
                ) {
                    if (response.isSuccessful) {
//                        initTransectionData.value = response.body()?.data
                        response.body()?.data?.let {
                            onSuccess(it, vehicle)
                        }
                    } else {
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
                        } catch (e: IOException) {
                        }
                    }
                }
            })

    }


    internal fun onSuccess(model: PaymentResponse, vehicle: Vehicle?) {

        val params = Bundle()
        mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.PURCHASE_BRIDGE_TAX_ANDROID, params)

        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.BridgeTaxBillingInfo, object : BundleProvider() {
                override fun onAddArgs(bundle: Bundle?): Bundle {
                    return Bundle().apply {

                        putSerializable(ARG_GUID, model)
                        putSerializable(ARG_CAR, vehicle)
                        putString(BillingInformationFragment.ARG_OF, "RO_PASS_TAX")
                    }
                }
            }))
    }


}