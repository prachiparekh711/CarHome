package ro.westaco.carhome.presentation.screens.reminder

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem
import ro.westaco.carhome.data.sources.remote.responses.models.Reminder
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
class ReminderViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {

    val remindersLiveData = MutableLiveData<ArrayList<Reminder>?>()
    val remindersTabData = MutableLiveData<ArrayList<CatalogItem>?>()

    override fun onFragmentCreated() {
        fetchRemoteData()
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

    }

    fun fetchReminderList() {
        api.getReminders()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                remindersLiveData.value = resp?.data
            }, {
                it.printStackTrace()
                remindersLiveData.value = null
            })
    }

    /*
    ** User Interaction
    */
    internal fun onFabClicked() {
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.AddReminder, null, true))
    }

    internal fun onNotificationsClicked() {
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.Notifications))
    }

    internal fun onDelete(item: Reminder) {
        if (!DeviceUtils.isOnline(app)) {
            uiEventStream.value = UiEvent.ShowToast(R.string.int_not_connect)
            return
        }


        item.id?.let {
            api.deleteReminder(it)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    uiEventStream.value = UiEvent.ShowToast(R.string.delete_success_msg)
                    fetchReminderList()
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
            }, true))
    }
}