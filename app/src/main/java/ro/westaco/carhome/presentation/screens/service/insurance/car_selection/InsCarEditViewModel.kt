package ro.westaco.carhome.presentation.screens.service.insurance.car_selection

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.requests.AddVehicleRequest
import ro.westaco.carhome.data.sources.remote.requests.VehicleEvent
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem
import ro.westaco.carhome.data.sources.remote.responses.models.Country
import ro.westaco.carhome.data.sources.remote.responses.models.LeasingCompany
import ro.westaco.carhome.data.sources.remote.responses.models.VehicleDetails
import ro.westaco.carhome.navigation.BundleProvider
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.service.insurance.init.InsuranceFragment
import ro.westaco.carhome.utils.FirebaseAnalyticsList
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class InsCarEditViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {

    var vehicleSubCategoryData = MutableLiveData<ArrayList<CatalogItem>?>()
    var vehicleData: MutableLiveData<VehicleDetails> = MutableLiveData()
    var vehicleUsageData = MutableLiveData<ArrayList<CatalogItem>?>()
    var vehicleCategoryData = MutableLiveData<ArrayList<CatalogItem>?>()
    var vehicleBrandData = MutableLiveData<ArrayList<CatalogItem>?>()
    var fuelTypeData = MutableLiveData<ArrayList<CatalogItem>?>()
    var countryData = MutableLiveData<ArrayList<Country>?>()
    var leasingCompaniesData = MutableLiveData<ArrayList<LeasingCompany>?>()
    private var mFirebaseAnalytics = FirebaseAnalytics.getInstance(app)


    internal fun onBack() {
        uiEventStream.value = UiEvent.NavBack
    }

    internal fun onMain() {
        uiEventStream.value = UiEvent.GoToMain
    }

    fun fetchVehicleDetail(vehicleId: Int) {
        api.getVehicle(vehicleId)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                if (resp.success && resp.data != null) {
                    vehicleData.value = resp?.data
                }
            }, {
            })
    }


    fun onSave(
        vehicleId: Int?,
        registrationCountryPos: Int,
        licensePlate: String,
        vehicleCategoryPos: Int?,
        vehicleSubCategoryPos: Int?,
        vehicleUsageType: Int?,
        manufacturerPos: Int?,
        model: String?,
        vin: String?,
        year: String?,
        maxAllowableMass: String?,
        engineSize: String?,
        power: String?,
        fuelTypePos: Int?,
        noSeats: String?,
        civ: String?,
        leasingCompanyID: Int?,
        vehicleEvents: ArrayList<VehicleEvent>?,
        isEdit: Boolean
    ) {
        uiEventStream.value = UiEvent.HideKeyboard

        val addCarRequest = AddVehicleRequest(
            civ,
            year?.toIntOrNull(),
            vin,
            leasingCompanyID,
            vehicleUsageType?.let { vehicleUsageData.value?.get(it)?.id?.toInt() },
            power?.toIntOrNull(),
            vehicleCategoryPos?.let { vehicleCategoryData.value?.get(it)?.id?.toInt() },
            countryData.value?.get(registrationCountryPos)?.code,
            manufacturerPos?.let { vehicleBrandData.value?.get(it)?.id?.toInt() },
            maxAllowableMass?.toIntOrNull(),
            licensePlate,
            noSeats?.toIntOrNull(),
            fuelTypePos?.let { fuelTypeData.value?.get(it)?.id?.toInt() },
            engineSize?.toIntOrNull(),
            vehicleSubCategoryPos?.let { vehicleSubCategoryData.value?.get(it)?.id?.toInt() },
            model,
            vehicleId,
            vehicleEvents
        )

        if (isEdit) {
            vehicleId?.toLong()?.let {
                api.editVehicle(it, addCarRequest)
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        uiEventStream.value =
                            UiEvent.Navigation(
                                NavAttribs(
                                    Screen.Insurance,
                                    object : BundleProvider() {
                                        override fun onAddArgs(bundle: Bundle?): Bundle {
                                            return Bundle().apply {
                                                putSerializable(
                                                    InsuranceFragment.ARG_CAR_ID,
                                                    vehicleId
                                                )
                                            }
                                        }
                                    },
                                    false
                                )
                            )
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
                        UiEvent.Navigation(
                            NavAttribs(
                                Screen.Insurance,
                                object : BundleProvider() {
                                    override fun onAddArgs(bundle: Bundle?): Bundle {
                                        return Bundle().apply {
                                            resp.data?.let {
                                                putInt(
                                                    InsuranceFragment.ARG_CAR_ID,
                                                    it.toInt()
                                                )
                                            }
                                        }
                                    }
                                },
                                false
                            )
                        )
                }, {

                })

        }
    }

    fun fetchVehicleSubCategory(category: Int) {
        api.getVehicleSubCategory(category)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                vehicleSubCategoryData.value = resp?.data
            }, {
                //   it.printStackTrace()
            })
    }

    fun fetchDefaultData() {
        api.getCountries()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { resp ->
                    countryData.value = resp?.data
                },
                Throwable::printStackTrace
            )


        api.getVehicleUsage()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                vehicleUsageData.value = resp?.data
            }, {
                //   it.printStackTrace()
            })

        api.getSimpleCatalog("NOM_VEHICLE_CATEGORY_TYPE")
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                vehicleCategoryData.value = it.data
            }, { })


        api.getSimpleCatalog("NOM_VEHICLE_BRAND_TYPE")
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    vehicleBrandData.value = it.data
                },
                {

                }
            )

        api.getSimpleCatalog("NOM_VEHICLE_FUEL_TYPE")
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { fuelTypeData.value = it.data },
                {

                }
            )
    }

    fun fetchLeasingCompanies(countryCode: String) {
        api.getLeasingCompanies(countryCode)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                leasingCompaniesData.value = resp.data
            }, {
            })
    }

}