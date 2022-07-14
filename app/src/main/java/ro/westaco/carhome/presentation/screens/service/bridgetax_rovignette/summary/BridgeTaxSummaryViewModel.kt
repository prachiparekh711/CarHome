package ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.summary

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.requests.AddLegalPersonRequest
import ro.westaco.carhome.data.sources.remote.requests.AddNaturalPersonRequest
import ro.westaco.carhome.data.sources.remote.requests.Address
import ro.westaco.carhome.data.sources.remote.requests.PaymentRequest
import ro.westaco.carhome.data.sources.remote.responses.models.*
import ro.westaco.carhome.navigation.BundleProvider
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.main.MainActivity
import ro.westaco.carhome.presentation.screens.service.support.transaction_details.TransactionDetailsFragment
import ro.westaco.carhome.utils.DeviceUtils
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class BridgeTaxSummaryViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi,
) : BaseViewModel() {

    private var vehicle: Vehicle? = null
    var userLiveData = MutableLiveData<FirebaseUser>()
    var initTransectionData = MutableLiveData<PaymentResponse?>()
    var vehicleCategories = MutableLiveData<ArrayList<ServiceCategory>>()
    var bridgeTaxObjectives = MutableLiveData<ArrayList<ObjectiveItem>>()
    var countryData = MutableLiveData<ArrayList<Country>>()
    var streetTypeData = MutableLiveData<ArrayList<CatalogItem>?>()
    internal val vehicleDetailsLivedata = MutableLiveData<VehicleDetails>()
    var vignetteDurations = MutableLiveData<java.util.ArrayList<RovignetteDuration>>()
    var profileLogoData: MutableLiveData<Bitmap>? = MutableLiveData()
    val legalPersonDetailsLiveDataList: MutableLiveData<LegalPersonDetails?> = MutableLiveData()
    val naturalPersonDetailsLiveDataList: MutableLiveData<NaturalPersonDetails?> = MutableLiveData()

    internal fun onBack() {
        uiEventStream.value = UiEvent.NavBack
    }

    init {
        userLiveData.value = FirebaseAuth.getInstance().currentUser
    }

    override fun onFragmentCreated() {
        super.onFragmentCreated()
        Handler(Looper.getMainLooper()).postDelayed({
            fetchDefaultData()
        }, 500)

    }

    @SuppressLint("NullSafeMutableLiveData")
    internal fun fetchDefaultData() {
        api.getProfileLogo()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                try {
                    val byteArray = it.source().readByteArray()
                    val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                    profileLogoData?.value = bitmap
                } catch (e: Exception) {
                }
            }, {
                profileLogoData?.value = null
//                it.printStackTrace()
//                uiEventStream.value = UiEvent.ShowToast(R.string.failed_server)
            })

        api.getBridgeTaxCategories()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                vehicleCategories.value = resp?.data
            },
                {

                })

        api.getRovignetteCategories()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                vehicleCategories.value = resp?.data
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

        api.getRovignetteDurations()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                vignetteDurations.value = it.data
            }, {

            })

        api.getCountries()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { resp ->
                    countryData.value = resp?.data
                },
                {

                }
            )


        api.getSimpleCatalog("NOM_STREET_TYPE")
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { streetTypeData.value = it.data }, {

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
                }, {})
        }
    }

    fun getNaturalPerson(id: Long) {
        api.getNaturalPerson(id)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                naturalPersonDetailsLiveDataList.value = it?.data
            }, {})
    }

    fun getLegalPerson(id: Long) {
        api.getLegalPersonDetails(id)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                legalPersonDetailsLiveDataList.value = it?.data
            }, {})
    }


    internal fun getProfileImage(context: Context, user: FirebaseUser?) =
        DeviceUtils.getProfileImage(context, user)

    fun refreshProfile() {
        api.getProfile()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.success && it.data != null) {
                    MainActivity.profileItem = it.data
                    MainActivity.activeUser = MainActivity.profileItem?.firstName ?: " "
                    MainActivity.activeId = MainActivity.profileItem?.id
                }
            }, {

            })
    }

    internal fun onSaveNaturalPerson(
        item: NaturalPersonDetails?,
        address: Address?,
        firstName: String?,
        lastName: String?,
        personGUID: String,
        guid: String
    ) {

        val naturalPersonReq = AddNaturalPersonRequest(
            firstName,
            lastName,
            item?.occupationCorIsco08,
            address,
            item?.identityDocument,
            item?.cnp,
            item?.phone,
            item?.phoneCountryCode,
            item?.drivingLicense,
            item?.employerName,
            item?.dateOfBirth,
            item?.id,
            item?.email
        )

        item?.id?.toLong()?.let {
            api.editNaturalPerson(it, naturalPersonReq)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (item.profile == true) {
                        refreshProfile()
                    }
                    onNextClick(guid, personGUID)

                }, {})
        }


    }

    internal fun onSaveLegalPerson(
        item: LegalPersonDetails?,
        address: Address?,
        personGUID: String,
        guid: String
    ) {


        val legalPerson = AddLegalPersonRequest(
            noRegistration = item?.noRegistration,
            vatPayer = item?.vatPayer,
            address = address,
            cui = item?.cui,
            companyName = item?.companyName,
            caen = item?.caen,
            id = item?.id,
            activityType = item?.activityType,
            phone = item?.phone,
            phoneCountryCode = item?.phoneCountryCode,
            email = item?.email
        )

        item?.id?.let {
            api.editLegalPerson(it, legalPerson)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    onNextClick(guid, personGUID)
                }, {})
        }


    }


    //    internal fun onNextClick(guid: String, personID: Int) {
    internal fun onNextClick(guid: String, personGUID: String) {

        val request = PaymentRequest(invoicePersonGuid = personGUID, transactionGuid = guid)

        api.initPayment(request)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                if (resp.success && resp.data != null) {
                    initTransectionData.value = resp?.data
                }
            }, {
            })
    }

    internal fun onPaymentSuccess(
        model: PaymentResponse,
        arg_of: String
    ) {
        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.TransactionDetails, object : BundleProvider() {
                override fun onAddArgs(bundle: Bundle?): Bundle {
                    return Bundle().apply {
                        putString(TransactionDetailsFragment.ARG_OF, arg_of)
                        putString(TransactionDetailsFragment.ARG_TRANSACTION_GUID, model.guid)
                    }
                }
            }))
    }

}