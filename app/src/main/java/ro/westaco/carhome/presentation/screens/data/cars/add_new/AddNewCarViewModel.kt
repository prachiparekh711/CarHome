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
import ro.westaco.carhome.utils.DeviceUtils
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File
import javax.inject.Inject


@HiltViewModel
class AddNewCarViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
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
    var leasingCompaniesData = ArrayList<LeasingCompany>()
    val actionStream: SingleLiveEvent<ACTION> = SingleLiveEvent()

    sealed class ACTION {

        class SetLeasingCompanyData(val leasingCompaniesData: ArrayList<LeasingCompany>) : ACTION()
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
        vehicleCategoryPos: Int,
        vehicleSubCategoryPos: Int,
        vehicleUsageType: Int,
        manufacturerPos: Int,
        model: String,
        vin: String,
        year: String,
        maxAllowableMass: String,
        engineSize: String,
        power: String,
        fuelTypePos: Int,
        noSeats: String,
        civ: String,
        leasingCompanyID: Int?,
        vehicleEvents: ArrayList<VehicleEvent>,
        isEdit: Boolean

    ) {
        uiEventStream.value = UiEvent.HideKeyboard

        if (!validateFields(
                licensePlate,
                model,
                vin,
                year,
                maxAllowableMass,
                engineSize,
                power,
                noSeats,
                civ
            )
        ) {
            return
        }

        if (!DeviceUtils.isOnline(app)) {
            uiEventStream.value = UiEvent.ShowToast(R.string.int_not_connect)
            return
        }

        val addCarRequest = AddVehicleRequest(
            civ,
            year.toInt(),
            vin,
            leasingCompanyID,
            vehicleUsageData.value?.get(vehicleUsageType)?.id?.toInt(),
            power.toInt(),
            vehicleCategoryData.value?.get(vehicleCategoryPos)?.id?.toInt(),
            countryData.value?.get(registrationCountryPos)?.code,
            vehicleBrandData.value?.get(manufacturerPos)?.id?.toInt(),
            maxAllowableMass.toInt(),
            licensePlate,
            noSeats.toInt(),
            fuelTypeData.value?.get(fuelTypePos)?.id?.toInt(),
            engineSize.toInt(),
            vehicleSubCategoryData.value?.get(vehicleSubCategoryPos)?.id?.toInt(),
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
                }
            )
        )
    }

    private fun validateFields(
        licensePlate: String,
        model: String,
        vin: String,
        year: String,
        maxAllowableMass: String,
        engineSize: String,
        power: String,
        noSeats: String,
        civ: String
    ): Boolean {
        if (licensePlate.isEmpty() || model.isEmpty() || vin.isEmpty() || year.isEmpty() || maxAllowableMass.isEmpty() || engineSize.isEmpty() || power.isEmpty() || noSeats.isEmpty() || civ.isEmpty()) {
            uiEventStream.value = UiEvent.ShowToast(R.string.fill_all_fields)
            return false
        }

        if (vin.length != 17) {
            uiEventStream.value = UiEvent.ShowToast(R.string.error_vin)
            return false
        }

        return true
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
                resp?.data?.let { leasingCompaniesData.addAll(it) }
                actionStream.value = ACTION.SetLeasingCompanyData(leasingCompaniesData)
            }, {
                leasingCompaniesData.clear()
            })
    }

    internal fun onDeleteCertificateAttachment(vehicleId: Int, attachId: Int?) {
        if (attachId != null) {
            api.deleteCertificateAttachment(vehicleId, attachId)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    onVehicle(vehicleId)
                }, {
                    uiEventStream.value = UiEvent.ShowToast(R.string.general_server_error)
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
                uiEventStream.value =
                    UiEvent.ShowToast(R.string.server_saving_error)
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