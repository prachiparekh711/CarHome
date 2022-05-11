package ro.westaco.carhome.presentation.screens.settings.notifications

import android.app.Application
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.responses.models.NotificationPrefrences
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.presentation.base.BaseViewModel
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class NotificationSettingModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {

    val notificationPrefLivedata: MutableLiveData<ArrayList<NotificationPrefrences>> =
        MutableLiveData()

    override fun onFragmentCreated() {
        fetchRemoteData()
    }

    private fun fetchRemoteData() {

        api.getNotificationPrefrences()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                notificationPrefLivedata.value = resp?.data
            }, {
                notificationPrefLivedata.value = null
            })
    }

    internal fun onChange(
        type: String,
        channel: String,
        active: Boolean
    ) {
        api.postNotificationPrefrences(type, channel, active)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
            }, {
            })
    }

    internal fun onBack() {
        uiEventStream.value = UiEvent.NavBack
    }

    internal fun onMain() {
        uiEventStream.value = UiEvent.GoToMain
    }
}