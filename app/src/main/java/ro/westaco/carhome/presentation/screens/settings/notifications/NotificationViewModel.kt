package ro.westaco.carhome.presentation.screens.settings.notifications

import android.app.Application
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.requests.MarkSeenRequest
import ro.westaco.carhome.data.sources.remote.responses.models.Notification
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject


@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {

    val notificationLivedata: MutableLiveData<ArrayList<Notification>> = MutableLiveData()
    override fun onFragmentCreated() {
        fetchRemoteData()
    }

    internal fun onBack() {
        uiEventStream.value = UiEvent.NavBack
    }

    internal fun onMain() {
        uiEventStream.value = UiEvent.GoToMain
    }

    private fun fetchRemoteData() {
        api.getNotifications()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                notificationLivedata.value = resp?.data
            }, {
                it.printStackTrace()
                notificationLivedata.value = null
            })
    }

    internal fun markAsSeen(ids: ArrayList<Int>) {

        val req = MarkSeenRequest(ids)

        api.markAsSeen(req)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
            }, {
            })
    }

    internal fun onSettingClick() {
        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.AllNotificationSetting))
    }

    internal fun onItemClick(item: Notification) {
//        uiEventStream.value =
//            UiEvent.Navigation(NavAttribs(Screen.NotificationSetting, object : BundleProvider() {
//                override fun onAddArgs(bundle: Bundle?): Bundle {
//                    return Bundle().apply {
//                        putSerializable(NotificationSettingFragment.ARG_NOTIFICATION, item)
//                    }
//                }
//            }))
    }
}