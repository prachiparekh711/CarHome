package ro.westaco.carhome.presentation.screens.reminder.add_new

import android.app.Application
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.requests.AddReminderRequest
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem
import ro.westaco.carhome.data.sources.remote.responses.models.LocationV2Item
import ro.westaco.carhome.data.sources.remote.responses.models.Reminder
import ro.westaco.carhome.navigation.SingleLiveEvent
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.utils.DateTimeUtils
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.*
import javax.inject.Inject


@HiltViewModel
class AddNewReminderViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {
    internal var dueDateLiveData = MutableLiveData<Long>()
    internal var dueTimeLiveData = MutableLiveData<Long>()
    val remindersLiveData = MutableLiveData<Reminder?>()
    val repeatLiveData = MutableLiveData<ArrayList<CatalogItem>?>()
    val durationData = MutableLiveData<ArrayList<CatalogItem>?>()
    val remindersTabData = MutableLiveData<ArrayList<CatalogItem>?>()

    internal val actionStream: SingleLiveEvent<ACTION> = SingleLiveEvent()

    override fun onFragmentCreated() {
        super.onFragmentCreated()
        getDefaultData()
    }

    sealed class ACTION {
        class ShowDatePicker(val dateInMillis: Long) : ACTION()
        class ShowTimePicker(val dateTimeInMillis: Long) : ACTION()
    }

    fun fetchRemoteData(id: Long) {


        api.getRemindersDetail(id)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                remindersLiveData.value = resp?.data
            }, {
            })
    }

    private fun getDefaultData() {
        api.getSimpleCatalog("NOM_REMINDER_TAG")
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                remindersTabData.value = resp?.data
            }, {
            }
            )

        api.getSimpleCatalog("NOM_REMINDER_REPEAT")
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { resp ->
                    repeatLiveData.value = resp?.data
                },
                {
                }
            )

        api.getSimpleCatalog("NOM_NOTIFICATION_DURATION_UNIT")
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                durationData.value = resp?.data
            }, {
            })
    }

    /*
    ** User Interaction
    */
    internal fun onBack() {
        uiEventStream.value = UiEvent.NavBack
    }

    internal fun onRootClicked() {
        uiEventStream.value = UiEvent.HideKeyboard
    }

    internal fun onDueDateClicked() {
        uiEventStream.value = UiEvent.HideKeyboard

        val date = dueDateLiveData.value
        actionStream.value = ACTION.ShowDatePicker(date ?: Date().time)
    }

    internal fun onDueDatePicked(dateInMillis: Long) {
        dueDateLiveData.value = dateInMillis
    }

    internal fun onDueTimeClicked() {
        uiEventStream.value = UiEvent.HideKeyboard

        val datetime = dueTimeLiveData.value

        if (datetime != null)
            actionStream.value = ACTION.ShowTimePicker(datetime)
        else
            actionStream.value = ACTION.ShowTimePicker(Date().time)
    }


    internal fun onDueTimePicked(dateTimeInMillis: Long) {
        dueTimeLiveData.value = dateTimeInMillis
    }

    //    (R6)
    internal fun onSave(
        title: String,
        notes: String,
        dueDate: String,
        dueTime: String,
        notifications: ArrayList<NotificationWithUnit>,
        selectedTag: List<CatalogItem>?,
        repeatPos: Int,
        locationItem: LocationV2Item?,
        isEdit: Boolean,
        reminder: Reminder?
    ) {
        uiEventStream.value = UiEvent.HideKeyboard

        if (!validateFields(title, dueDate, dueTime, notifications)) {
            return
        }


        val formattedDueDate = DateTimeUtils.convertToServerDate(app, dueDate)

        val tagIds = selectedTag?.map {
            it.id
        }

        val repeatId = repeatLiveData.value?.get(repeatPos)?.id
        val reminderNotifications = notifications.map { it.reminderNotification }

        val addReminderRequest =
            AddReminderRequest(
                title,
                notes,
                formattedDueDate,
                dueTime,
                reminderNotifications,
                tagIds,
                repeatId,
                locationItem?.guid
            )

        if (isEdit) {
            if (reminder != null)
                addReminderRequest.let {
                    reminder.id?.let { it1 ->
                        api.editReminder(it1, it)
                            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                uiEventStream.value = UiEvent.NavBack
                            }, {

                            })
                    }
                }
        } else {
            api.createReminder(addReminderRequest)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    uiEventStream.value = UiEvent.NavBack
                }, {

                })
        }
    }


    internal fun onDelete(item: Reminder) {

        item.id?.let {
            api.deleteReminder(it)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                }, {
                    //   it.printStackTrace()
                })
        }
    }

    private fun validateFields(
        title: String,
        dueDate: String,
        dueTime: String,
        notifications: ArrayList<NotificationWithUnit>
    ): Boolean {
        if (title.isEmpty()) {
            uiEventStream.value = UiEvent.ShowToast(R.string.err_no_title)
            return false
        }
        if (dueDate.isEmpty() || dueTime.isEmpty()) {
            uiEventStream.value = UiEvent.ShowToast(R.string.err_no_duedatetime)
            return false
        }
        if (notifications.isNotEmpty()) {
            val notificationWithUnit = notifications.first()
            val dueDateTime = "$dueDate $dueTime"
            val nowDate = DateTimeUtils.getNowDate()
            val dueDateTimeDate = DateTimeUtils.stringDatetimeToDate(dueDateTime)
            val notificationDate = DateTimeUtils.addSelectedPeriodToDate(
                nowDate, notificationWithUnit.reminderNotification.duration!!.toInt(),
                notificationWithUnit.unitName
            )
            if (DateTimeUtils.firstDateIsBeforeSecondDate(dueDateTimeDate, notificationDate)) {
                uiEventStream.value = UiEvent.ShowToast(R.string.err_notification_after_duedate)
                return false
            }
        }
        return true
    }
}