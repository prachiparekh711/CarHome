package ro.westaco.carhome.presentation.screens.reminder

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem
import ro.westaco.carhome.data.sources.remote.responses.models.Reminder
import ro.westaco.carhome.navigation.BundleProvider
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.reminder.add_new.AddNewReminderFragment
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

        api.getSimpleCatalog("NOM_REMINDER_TAG")
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                remindersTabData.value = resp?.data
            }, {
            }
            )

    }

    fun fetchReminderList() {
        api.getReminders(true)
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
    internal fun onFabClicked(selectedTags: ArrayList<CatalogItem>) {
        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.AddReminder, object : BundleProvider() {
                override fun onAddArgs(bundle: Bundle?): Bundle {
                    return Bundle().apply {
                        putSerializable(AddNewReminderFragment.ARG_IS_EDIT, false)
                        putSerializable(AddNewReminderFragment.ARG_SELECTED_TAGS, selectedTags)
                    }
                }
            }, true))
    }

    internal fun onNotificationsClicked() {
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.Notifications))
    }

    internal fun onDelete(item: Reminder) {

        item.id?.let {
            api.deleteReminder(it)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    fetchReminderList()
                }, {
                    //   it.printStackTrace()
                })
        }
    }

    internal fun onMarkAsCompleted(item: Reminder) {

        item.id?.let {
            api.markAsCompleted(it)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    fetchReminderList()
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
            }, true))
    }
}