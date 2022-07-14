package ro.westaco.carhome.presentation.screens.data.cars.details

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
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem
import ro.westaco.carhome.data.sources.remote.responses.models.Country
import ro.westaco.carhome.data.sources.remote.responses.models.EventType
import ro.westaco.carhome.data.sources.remote.responses.models.VehicleDetails
import ro.westaco.carhome.navigation.BundleProvider
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.SingleLiveEvent
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.data.cars.add_new.AddNewCar2Fragment
import ro.westaco.carhome.presentation.screens.data.cars.add_new.AddNewCarFragment
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


@HiltViewModel
class CarDetailsViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {

    val vehicleDetailsLivedata: MutableLiveData<VehicleDetails> = MutableLiveData()
    var vehicleSubCategoryData = MutableLiveData<ArrayList<CatalogItem>?>()
    var vehicleUsageData = MutableLiveData<ArrayList<CatalogItem>?>()
    var vehicleCategoryData = MutableLiveData<ArrayList<CatalogItem>?>()
    var vehicleBrandData = MutableLiveData<ArrayList<CatalogItem>?>()
    var fuelTypeData = MutableLiveData<ArrayList<CatalogItem>?>()
    var countryData = MutableLiveData<ArrayList<Country>?>()

    val actionStream: SingleLiveEvent<ACTION> = SingleLiveEvent()

    internal fun onVehicle(vehicleId: Int) {

        api.getVehicle(vehicleId)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.success && it.data != null) {
                    vehicleDetailsLivedata.value = it.data
                }
            }, {
                it.printStackTrace()
                vehicleDetailsLivedata.value = null
            })

    }

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

    fun fetchVehicleSubCategory(category: Int) {
        api.getVehicleSubCategory(category)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                vehicleSubCategoryData.value = resp?.data
            }, {
                //   it.printStackTrace()
            })
    }

    internal fun getVehicleEventType() {

        api.getVehicleEventType()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                if (resp.success && resp.data != null) {
                    actionStream.value = resp.data?.let { it1 -> ACTION.OnEventsFetched(it1) }
                }
            }, {
                it.printStackTrace()
            })

    }

    sealed class ACTION {
        class OnEventsFetched(val eventsTypelist: ArrayList<EventType>) : ACTION()
        object OnAttachSuccess : ACTION()
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
                actionStream.value = ACTION.OnAttachSuccess
            }, {
                it.printStackTrace()

            })
    }

    internal fun onDeleteCertificateAttachment(vehicleId: Int, attachId: Int?) {
        if (attachId != null) {
            api.deleteCertificateAttachment(vehicleId, attachId)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    onVehicle(vehicleId)
                }, {
                    it.printStackTrace()
                })
        }
    }

    internal fun onBack() {
        uiEventStream.value = UiEvent.NavBack
    }

    internal fun onMain() {
        uiEventStream.value = UiEvent.GoToMain
    }

    internal fun onAddReminders(vehicleDetails: VehicleDetails?) {

        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.AddCar2, object : BundleProvider() {
                override fun onAddArgs(bundle: Bundle?): Bundle {
                    return Bundle().apply {
                        putSerializable(AddNewCar2Fragment.ARG_IS_EDIT, true)
                        putSerializable(AddNewCar2Fragment.ARG_CAR, getCarRequest(vehicleDetails))
                    }
                }
            }))
    }

    internal fun onEdit(vehicleDetails: VehicleDetails?) {
        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.AddCar, object : BundleProvider() {
                override fun onAddArgs(bundle: Bundle?): Bundle {
                    return Bundle().apply {
                        putSerializable(AddNewCarFragment.ARG_IS_EDIT, true)
                        putSerializable(AddNewCarFragment.ARG_CAR, vehicleDetails)
                    }
                }
            }))
    }

    internal fun onDelete(id: Long) {

        api.deleteVehicle(id)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                uiEventStream.value = UiEvent.NavBack
            }, {
                it.printStackTrace()
            })
    }


    internal fun getCarRequest(vehicleDetails: VehicleDetails?): AddVehicleRequest {
        return AddVehicleRequest(
            vehicleDetails?.vehicleIdentityCard,
            vehicleDetails?.manufacturingYear,
            vehicleDetails?.vehicleIdentificationNumber,
            vehicleDetails?.leasingCompany,
            vehicleDetails?.vehicleUsageType?.toInt(),
            vehicleDetails?.enginePower,
            vehicleDetails?.vehicleCategory?.toInt(),
            vehicleDetails?.registrationCountryCode,
            vehicleDetails?.vehicleBrand?.toInt(),
            vehicleDetails?.maxAllowableMass,
            vehicleDetails?.licensePlate,
            vehicleDetails?.noOfSeats,
            vehicleDetails?.fuelTypeId?.toInt(),
            vehicleDetails?.engineSize,
            vehicleDetails?.vehicleSubCategoryId?.toInt(),
            vehicleDetails?.model,
            vehicleDetails?.id?.toInt(),
            vehicleDetails?.vehicleEvents
        )
    }

    val originalFormat = SimpleDateFormat(
        app.getString(R.string.server_standard_datetime_format_template),
        Locale.US
    )




}