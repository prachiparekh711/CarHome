package ro.westaco.carhome.presentation.screens.dashboard.profile.close

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.navigation.SingleLiveEvent
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.utils.DeviceUtils
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject


@HiltViewModel
class CloseAccountViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {

    private var firebaseAuth = FirebaseAuth.getInstance()
    private val appPreferences = AppPreferencesDelegates.get()

    val isDeletionConfirmedLiveData = MutableLiveData(false)

    sealed class ACTION {
        object CloseAccount : ACTION()
    }

    val actionStream: SingleLiveEvent<ACTION> = SingleLiveEvent()

    /*
    ** User Interaction
    */
    internal fun onBack() {
        uiEventStream.value = UiEvent.NavBack
    }

    internal fun onMain() {
        uiEventStream.value = UiEvent.GoToMain
    }

    internal fun onRootClicked() {
        uiEventStream.value = UiEvent.HideKeyboard
    }

    private fun isDeletionConfirmed(confirmationText: String) =
        confirmationText.equals(app.getString(R.string.close_confirm_txt), true)


    internal fun onConfirmationTextChanged(confirmationText: String) {
        val isDeletionConfirmed = isDeletionConfirmed(confirmationText)
        if (isDeletionConfirmed) {
            uiEventStream.value = UiEvent.HideKeyboard
        }
        isDeletionConfirmedLiveData.value = isDeletionConfirmed
    }

    internal fun onCloseAccount(confirmationText: String) {
        uiEventStream.value = UiEvent.HideKeyboard
        if (!DeviceUtils.isOnline(app)) {
            uiEventStream.value = UiEvent.ShowToast(R.string.int_not_connect)
            return
        }

        if (isDeletionConfirmed(confirmationText)) {
            api.closeAccount()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    actionStream.value = ACTION.CloseAccount
                    appPreferences.token = ""
                    firebaseAuth.signOut()
                    DeviceUtils.restartApp(app)
                    uiEventStream.value = UiEvent.ShowToast(R.string.success_close_ac)
                }, {
                    uiEventStream.value = UiEvent.ShowToast(R.string.close_ac_server)
                })
        } else {
            uiEventStream.value =
                UiEvent.ShowToast(R.string.close_ac_incorrect)
        }
    }
}