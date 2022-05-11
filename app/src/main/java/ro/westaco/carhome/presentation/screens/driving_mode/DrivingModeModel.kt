package ro.westaco.carhome.presentation.screens.driving_mode

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem
import ro.westaco.carhome.data.sources.remote.responses.models.ProgressItem
import ro.westaco.carhome.data.sources.remote.responses.models.Reminder
import ro.westaco.carhome.data.sources.remote.responses.models.Vehicle
import ro.westaco.carhome.navigation.BundleProvider
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.reminder.add_new.AddNewReminderFragment
import ro.westaco.carhome.utils.DeviceUtils
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class DrivingModeModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {
    var userLiveData = MutableLiveData<FirebaseUser>()
    var progressData: MutableLiveData<ProgressItem> = MutableLiveData()
    val carsLivedata: MutableLiveData<ArrayList<Vehicle>> = MutableLiveData()
    val remindersLiveData = MutableLiveData<ArrayList<Reminder>>()
    val remindersTabData = MutableLiveData<ArrayList<CatalogItem>>()

    init {
        userLiveData.value = FirebaseAuth.getInstance().currentUser
    }

    override fun onFragmentCreated() {
        super.onFragmentCreated()
        fetchRemoteData()
    }

    internal fun getProfileImage(context: Context, user: FirebaseUser?) =
        DeviceUtils.getProfileImage(context, user)


    internal fun onAvatarClicked() {
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.Profile))
    }


    private fun fetchRemoteData() {
        if (!DeviceUtils.isOnline(app)) {
            uiEventStream.value = UiEvent.ShowToast(R.string.int_not_connect)
            return
        }
        api.getSimpleCatalog("NOM_REMINDER_TAG")
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                remindersTabData.value = resp?.data
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

        api.getVehicles()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                carsLivedata.value = resp?.data
            }, {
                it.printStackTrace()
                carsLivedata.value = null
            })

        api.getReminders()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                remindersLiveData.value = resp?.data
            }, {
                //   it.printStackTrace()
            })

    }

    internal fun onDelete(item: Reminder) {
        if (!DeviceUtils.isOnline(app)) {
            uiEventStream.value = UiEvent.ShowToast(R.string.int_not_connect)
            return
        }

        val reminders = remindersLiveData.value

        item.id?.let {
            api.deleteReminder(it)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    uiEventStream.value = UiEvent.ShowToast(R.string.delete_success_msg)

                    reminders?.remove(item)
                    remindersLiveData.value = reminders
                }, {
                    //   it.printStackTrace()
                    uiEventStream.value = UiEvent.ShowToast(R.string.general_server_error)
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
}