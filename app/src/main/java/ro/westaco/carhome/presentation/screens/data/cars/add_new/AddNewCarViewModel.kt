package ro.westaco.carhome.presentation.screens.data.cars.add_new

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.requests.AddVehicleRequest
import ro.westaco.carhome.data.sources.remote.requests.VehicleEvent
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem
import ro.westaco.carhome.data.sources.remote.responses.models.Country
import ro.westaco.carhome.data.sources.remote.responses.models.LeasingCompany
import ro.westaco.carhome.data.sources.remote.responses.models.VehicleDetails
import ro.westaco.carhome.navigation.BundleProvider
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.SingleLiveEvent
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File
import javax.inject.Inject


@HiltViewModel
class AddNewCarViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi,
) : BaseViewModel() {
    override fun onFragmentCreated() {
        super.onFragmentCreated()
        fetchDefaultData()
    }

    var vehicleSubCategoryData = MutableLiveData<ArrayList<CatalogItem>?>()
    var vehicleUsageData = MutableLiveData<ArrayList<CatalogItem>?>()
    var vehicleCategoryData = MutableLiveData<ArrayList<CatalogItem>?>()
    var vehicleBrandData = MutableLiveData<ArrayList<CatalogItem>?>()
    var fuelTypeData = MutableLiveData<ArrayList<CatalogItem>?>()
    var countryData = MutableLiveData<ArrayList<Country>?>()
    var leasingCompaniesData = MutableLiveData<ArrayList<LeasingCompany>?>()
    val actionStream: SingleLiveEvent<ACTION> = SingleLiveEvent()

    sealed class ACTION {
        class OnRefresh(val vehicleDetails: VehicleDetails?) : ACTION()
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
        isEdit: Boolean,
    ) {

        uiEventStream.value = UiEvent.HideKeyboard

        if (licensePlate.isEmpty()) {
            uiEventStream.value = UiEvent.ShowToast(R.string.license_hint)
            return
        }

        if (model.isNullOrEmpty()) {
            uiEventStream.value = UiEvent.ShowToast(R.string.carmodel_cannot_be_empty)
            return
        }

        if (year.isNullOrEmpty()) {
            uiEventStream.value = UiEvent.ShowToast(R.string.invalid_year)
            return
        }

        if (maxAllowableMass.isNullOrEmpty()) {
            uiEventStream.value = UiEvent.ShowToast(R.string.invalid_mass)
            return
        }

        if (engineSize.isNullOrEmpty()) {
            uiEventStream.value = UiEvent.ShowToast(R.string.invalid_engine_size)
            return
        }

        if (engineSize.isNullOrEmpty()) {
            uiEventStream.value = UiEvent.ShowToast(R.string.invalid_engine_power)
            return
        }

        if (noSeats.isNullOrEmpty()) {
            uiEventStream.value = UiEvent.ShowToast(R.string.invalid_number_of_seats)
            return
        }

        val addCarRequest = AddVehicleRequest(
            civ,
            year.toIntOrNull(),
            vin,
            leasingCompanyID,
            vehicleUsageType?.let { vehicleUsageData.value?.get(it)?.id?.toInt() },
            power?.toIntOrNull(),
            vehicleCategoryPos?.let { vehicleCategoryData.value?.get(it)?.id?.toInt() },
            countryData.value?.get(registrationCountryPos)?.code,
            manufacturerPos?.let { vehicleBrandData.value?.get(it)?.id?.toInt() },
            maxAllowableMass.toIntOrNull(),
            licensePlate,
            noSeats.toIntOrNull(),
            fuelTypePos?.let { fuelTypeData.value?.get(it)?.id?.toInt() },
            engineSize.toIntOrNull(),
            vehicleSubCategoryPos?.let { vehicleSubCategoryData.value?.get(it)?.id?.toInt() },
            model,
            vehicleId,
            vehicleEvents
        )

        uiEventStream.value = UiEvent.Navigation(
            NavAttribs(
                Screen.AddCar2,
                object : BundleProvider() {
                    override fun onAddArgs(bundle: Bundle?): Bundle {
                        return Bundle().apply {
                            putSerializable(
                                AddNewCar2Fragment.ARG_IS_EDIT,
                                isEdit
                            )
                            putSerializable(
                                AddNewCar2Fragment.ARG_CAR,
                                addCarRequest
                            )
                        }
                    }
                }, false
            )
        )
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

    private fun fetchDefaultData() {
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

    internal fun onDeleteCertificateAttachment(vehicleId: Int, attachId: Int?) {
        if (attachId != null) {
            api.deleteCertificateAttachment(vehicleId, attachId)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    onVehicle(vehicleId)
                }, {
                })
        }
    }

    internal fun onAttach(vehicleId: Int, attachType: String, attachmentFile: File) {

        val requestFile: RequestBody =
            attachmentFile.asRequestBody("multipart/form-data".toMediaTypeOrNull())

        val body: MultipartBody.Part =
            MultipartBody.Part.createFormData("attachment", attachmentFile.name, requestFile)

        val fullName: RequestBody =
            attachType.toRequestBody("multipart/form-data".toMediaTypeOrNull())

        api.attachDocumentToVehicles(vehicleId, fullName, body)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                onVehicle(vehicleId)
            }, {

            })
    }

    private fun onVehicle(vehicleId: Int) {

        api.getVehicle(vehicleId)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.success && it.data != null) {
                    actionStream.value = ACTION.OnRefresh(it.data)
                }
            }, {
            })

    }


}