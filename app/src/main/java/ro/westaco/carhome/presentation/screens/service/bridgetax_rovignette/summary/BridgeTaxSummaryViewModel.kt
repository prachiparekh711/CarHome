package ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.summary

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.requests.*
import ro.westaco.carhome.data.sources.remote.responses.models.*
import ro.westaco.carhome.navigation.BundleProvider
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.service.transaction_details.TransactionDetailsFragment
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
    internal val vehicleDetailsLivedata = MutableLiveData<VehicleDetails>()
    var vignetteDurations = MutableLiveData<java.util.ArrayList<RovignetteDuration>>()
    var profileLogoData: MutableLiveData<Bitmap>? = MutableLiveData()
    val legalPersonDetailsLiveDataList: MutableLiveData<LegalPersonDetails?> = MutableLiveData()
    val naturalPersonDetailsLiveDataList: MutableLiveData<NaturalPersonDetails?> = MutableLiveData()
    var streetTypeData = MutableLiveData<ArrayList<CatalogItem>?>()


    internal fun onBack() {
        uiEventStream.value = UiEvent.NavBack
    }

    init {
        userLiveData.value = FirebaseAuth.getInstance().currentUser
    }

    override fun onFragmentCreated() {
        super.onFragmentCreated()
        fetchDefaultData()
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

    internal fun onSaveNaturalPerson(
        id: Long,
        firstName: String?,
        lastname: String?,
        address: Address?,
        cnp: String?,
        phone: String?,
        phoneCountryCode: String?,
        dateOfBirth: String?,
        email: String?,
        drivingLicenseCateg: ArrayList<Int>?,
        drivLicenseId: String?,
        drivLicenseIssueDate: String?,
        drivLicenseExpDate: String?,
        idDoc: IdentityDocument?,
        guid: String,
        personGUID: String,
        check: Boolean
    ) {

        /*val drivingLicenseCategory = java.util.ArrayList<Int>()

        drivingLicenseCateg?.indices?.forEach { i ->
            drivingLicenseCategory.add(drivingLicenseCateg[i].id.toInt())
        }*/

        if (!check) {

            uiEventStream.value = UiEvent.ShowToast(R.string.confirm_details)
            return

        } else {

            val drivingLicense = DrivingLicense(
                drivLicenseId,
                drivLicenseIssueDate,
                drivLicenseExpDate,
                drivingLicenseCateg
            )

            val naturalPersonReq = AddNaturalPersonRequest(
                firstName,
                lastname,
                null,
                address,
                idDoc,
                cnp,
                phone,
                phoneCountryCode,
                drivingLicense,
                null,
                dateOfBirth,
                id.toInt(),
                email,

                )

//        api.editNaturalPerson(id, naturalPersonReq)

//        val request = api.editNaturalPerson(id, naturalPersonReq)
            val request = api.editNaturalPerson(id, naturalPersonReq)
//        api.editNaturalPerson(id, naturalPersonReq)

            request.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                    uiEventStream.value = UiEvent.ShowToast(R.string.edit_success_msg)
                    onNextClick(guid, personGUID)

                }, {})

        }

    }

    internal fun onSaveLegalPerson(
        noReg: String?,
        address: Address?,
        cui: String?,
        companyName: String?,
        caen: Caen?,
        id: Long,
        activityType: CatalogItem?,
        phoneId: String?,
        phoneCountryCodeId: String?,
        emailId: String?,
        guid: String,
        personGUID: String,
        check: Boolean
    ) {

        if (!check) {

            uiEventStream.value = UiEvent.ShowToast(R.string.confirm_details)
            return

        } else {
            val vatPayer = cui?.contains("ro", true)

            val legalPerson = AddLegalPersonRequest(
                noRegistration = noReg,
                vatPayer = vatPayer,
                address = address,
                cui = cui,
                companyName = companyName,
                caen = caen,
                id = id,
                activityType = activityType,
                phone = phoneId,
                phoneCountryCode = phoneCountryCodeId,
                email = emailId
            )

            val request = api.editLegalPerson(id, legalPerson)

            request.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    uiEventStream.value = UiEvent.ShowToast(R.string.edit_success_msg)
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