package ro.westaco.carhome.presentation.screens.home

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.requests.AddVehicleRequest
import ro.westaco.carhome.data.sources.remote.responses.models.*
import ro.westaco.carhome.navigation.BundleProvider
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.dashboard.profile.edit.EditProfileFragment
import ro.westaco.carhome.presentation.screens.data.DataFragment
import ro.westaco.carhome.presentation.screens.data.cars.add_new.AddNewCar2Fragment
import ro.westaco.carhome.presentation.screens.data.cars.details.CarDetailsFragment
import ro.westaco.carhome.presentation.screens.reminder.add_new.AddNewReminderFragment
import ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.bridge_tax_init.PassTaxInitFragment
import ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.rovignette_init.BuyVignetteFragment
import ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.select_car.SelectCarFragment
import ro.westaco.carhome.presentation.screens.service.insurance.request.InsAcceptanceRequestFragment
import ro.westaco.carhome.presentation.screens.service.support.transaction_details.TransactionDetailsFragment
import ro.westaco.carhome.utils.DeviceUtils
import ro.westaco.carhome.utils.FirebaseAnalyticsList
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi,
) : BaseViewModel() {
    var userLiveData = MutableLiveData<FirebaseUser>()
    var profileLogoData: MutableLiveData<Bitmap>? = MutableLiveData()
    var progressData: MutableLiveData<ProgressItem> = MutableLiveData()
    val carsLivedata: MutableLiveData<ArrayList<Vehicle>> = MutableLiveData()
    val documentLivedata: MutableLiveData<ArrayList<RowsItem>> = MutableLiveData()
    val remindersLiveData = MutableLiveData<ArrayList<Reminder>?>()
    val remindersTabData = MutableLiveData<ArrayList<CatalogItem>?>()
    var historyLiveData = MutableLiveData<ArrayList<HistoryItem>?>()
    var mFirebaseAnalytics = FirebaseAnalytics.getInstance(app)

    override fun onFragmentCreated() {
        super.onFragmentCreated()
        fetchRemoteData()

    }

    internal fun getProfileImage(context: Context, user: FirebaseUser?) =
        DeviceUtils.getProfileImage(context, user)

    fun getUserLivedata() {
        userLiveData.value = FirebaseAuth.getInstance().currentUser
    }

    internal fun onAvatarClicked() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.ACCESS_PROFILE_HOME, params)
        mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.ACCESS_PROFILE_COMPLETE_CARD, params)
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.Profile))
    }

    internal fun onAddNewCar() {
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.QueryCar))
    }

    internal fun onServiceClicked(enter: String) {
        val params = Bundle()
        mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.ACCESS_ROVINIETA_HOME, params)
        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.SelectCarForService, object : BundleProvider() {
                override fun onAddArgs(bundle: Bundle?): Bundle {
                    return Bundle().apply {
                        putString(SelectCarFragment.ARG_ENTER_VALUE, enter)
                    }
                }
            }))
    }

    internal fun onInsurance() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.ACCESS_INSURANCE_HOME, params)
//        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.Insurance))
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.InsuranceRequest))
    }


    internal fun onNewDocument() {
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.Document))
    }

    internal fun onNewReminder() {
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.AddReminder))
    }

    internal fun onDataClicked(index: Int) {
        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.Data, object : BundleProvider() {
                override fun onAddArgs(bundle: Bundle?): Bundle {
                    return Bundle().apply {
                        putInt(DataFragment.INDEX, index)
                    }
                }
            }))
    }

    internal fun onEditCar(itemID: Int) {
        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.CarDetails, object : BundleProvider() {
                override fun onAddArgs(bundle: Bundle?): Bundle {
                    return Bundle().apply {
                        putInt(CarDetailsFragment.ARG_CAR_ID, itemID)
                    }
                }
            }))
    }

    internal fun onEditProfile(profileItem: ProfileItem) {
        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.EditAccount, object : BundleProvider() {
                override fun onAddArgs(bundle: Bundle?): Bundle {
                    return Bundle().apply {
                        putSerializable(EditProfileFragment.ARG_PROFILE, profileItem)
                    }
                }
            }))
    }

    internal fun onHistoryDetail(item: HistoryItem) {
        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.TransactionDetails, object : BundleProvider() {
                override fun onAddArgs(bundle: Bundle?): Bundle {
                    return Bundle().apply {
                        putString(
                            TransactionDetailsFragment.ARG_TRANSACTION_GUID,
                            item.transactionGuid
                        )
                        putString(
                            TransactionDetailsFragment.ARG_OF,
                            item.service
                        )
                    }
                }
            }))
    }

    internal fun onNotificationClick(item: Vehicle) {

        item.id?.let {
            api.getVehicle(it)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.success && it.data != null) {
                        it.data.let {
                            if (it != null) {
                                onCheck(it)
                            }
                        }
                    }
                }, {
                })
        }

    }

    internal fun onCheck(vehicleDetails: VehicleDetails) {

        val addCarRequest = AddVehicleRequest(
            vehicleDetails.vehicleIdentityCard,
            vehicleDetails.manufacturingYear,
            vehicleDetails.vehicleIdentificationNumber,
            vehicleDetails.leasingCompany,
            vehicleDetails.vehicleUsageType?.toInt(),
            vehicleDetails.enginePower,
            vehicleDetails.vehicleCategory?.toInt(),
            vehicleDetails.registrationCountryCode,
            vehicleDetails.vehicleBrand?.toInt(),
            vehicleDetails.maxAllowableMass,
            vehicleDetails.licensePlate,
            vehicleDetails.noOfSeats,
            vehicleDetails.fuelTypeId?.toInt(),
            vehicleDetails.engineSize,
            vehicleDetails.vehicleSubCategoryId?.toInt(),
            vehicleDetails.model,
            vehicleDetails.id.toInt(),
            vehicleDetails.vehicleEvents
        )

        uiEventStream.value = UiEvent.Navigation(
            NavAttribs(
                Screen.AddCar2,
                object : BundleProvider() {
                    override fun onAddArgs(bundle: Bundle?): Bundle {
                        return Bundle().apply {
                            putSerializable(
                                AddNewCar2Fragment.ARG_IS_EDIT,
                                true
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


    internal fun onHistoryClicked() {
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.History))
    }

    internal fun onDocumentClicked() {
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.Document))
    }

    internal fun onContactUsClicked() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.ACCESS_EXCITINGOFFERS_HOME, params)
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.ContactUs))
    }

    private fun fetchRemoteData() {


        api.getSimpleCatalog("NOM_REMINDER_TAG")
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                remindersTabData.value = resp?.data
                fetchReminders()
            }, {
            }
            )

        api.getProgress()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.success && it.data != null) {
                    progressData.value = it.data
                }
            }, {
                it.printStackTrace()
                progressData.value = null
            })

        api.getRecentDocuments()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                documentLivedata.value = resp?.data
            }, {
                it.printStackTrace()
                documentLivedata.value = null
            })


        api.getHistory()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                historyLiveData.value = resp?.data
            }, {

            })
    }

    fun fetchVehicles() {
        api.getVehicles()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                carsLivedata.value = resp?.data
            }, {
                it.printStackTrace()
                carsLivedata.value = null
            })
    }

    private fun fetchReminders() {
        api.getReminders(false)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                remindersLiveData.value = resp?.data
            }, {
            })
    }

    internal fun onDelete(item: Reminder) {

        item.id?.let {
            api.deleteReminder(it)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    fetchReminders()
                }, {

                })
        }
    }

    //    (R11)
    internal fun onUpdate(item: Reminder) {

        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.AddReminder, object : BundleProvider() {
                override fun onAddArgs(bundle: Bundle?): Bundle {
                    return Bundle().apply {
                        putSerializable(AddNewReminderFragment.ARG_IS_EDIT, true)
                        putSerializable(AddNewReminderFragment.ARG_REMINDER, item)
                    }
                }
            }))
    }

    internal fun onMarkAsCompleted(item: Reminder) {
        item.id?.let {
            api.markAsCompleted(it)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    fetchReminders()
                }, {
                })
        }
    }

    fun fetchProfileData() {
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
            })
    }

    internal fun onBuyPassTax(vehicle: Vehicle) {
        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.BridgeTaxInit, object : BundleProvider() {
                override fun onAddArgs(bundle: Bundle?): Bundle {
                    return Bundle().apply {

                        putSerializable(PassTaxInitFragment.ARG_CAR, vehicle)
                    }
                }
            }))
    }

    internal fun onBuyInsurance(vehicle: Vehicle) {
        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.InsuranceRequest, object : BundleProvider() {
                override fun onAddArgs(bundle: Bundle?): Bundle {
                    return Bundle().apply {
                        putSerializable(InsAcceptanceRequestFragment.ARG_CAR, vehicle)
                    }
                }
            }))
    }

    internal fun onBuyVignette(vehicle: Vehicle) {
        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.BuyVignette, object : BundleProvider() {
                override fun onAddArgs(bundle: Bundle?): Bundle {
                    return Bundle().apply {
                        putSerializable(BuyVignetteFragment.ARG_CAR, vehicle)
                    }
                }
            }))
    }

}