package ro.westaco.carhome.presentation.screens.auth

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.navigation.SingleLiveEvent
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.presentation.base.BaseViewModel
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject


@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {

    private var firebaseAuth = FirebaseAuth.getInstance()
    private val appPreferences = AppPreferencesDelegates.get()

    val actionStream: SingleLiveEvent<ACTION> = SingleLiveEvent()

    sealed class ACTION {
        object ShowSuccessState : ACTION()
        object LoginSuccess : ACTION()
    }

    /*
    ** User Interaction
    */
    internal fun onBack() {
        uiEventStream.value = UiEvent.NavBack
    }

    internal fun onSubmit(email: String) {
        if (email.isEmpty()) {
            uiEventStream.value = UiEvent.ShowToast(R.string.email_required)
            return
        }

        uiEventStream.value = UiEvent.HideKeyboard

        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    actionStream.value = ACTION.ShowSuccessState
                } else {
                    uiEventStream.value = UiEvent.ShowToast(R.string.reset_failed)
                }
            }
    }

    fun login() {
        firebaseAuth.currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // save Firebase token
                appPreferences.token = task.result.token.toString()
                api.login()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        actionStream.value = ACTION.LoginSuccess

                    }, {
                        uiEventStream.value = UiEvent.ShowToast(R.string.failed_server)
                    })
            }
        }
    }

    internal fun onResend(email: String) {
        if (email.isEmpty()) {
            uiEventStream.value = UiEvent.ShowToast(R.string.email_required)
            return
        }

        uiEventStream.value = UiEvent.HideKeyboard

        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    actionStream.value = ACTION.ShowSuccessState
                    uiEventStream.value = UiEvent.ShowToast(R.string.email_sent)
                } else {
                    uiEventStream.value = UiEvent.ShowToast(R.string.reset_failed)
                }
            }
    }

}