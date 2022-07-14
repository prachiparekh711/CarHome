package ro.westaco.carhome.presentation.screens.signup_methods.email_verification

import android.app.Application
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.navigation.SingleLiveEvent
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.signup_methods.biometric_setup.SetUpBiometricActivity
import ro.westaco.carhome.presentation.screens.signup_methods.profile_progress.ProfileProgressActivity
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class EmailVerificationModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {

    private var firebaseAuth = FirebaseAuth.getInstance()
    private val appPreferences = AppPreferencesDelegates.get()
    val actionStream: SingleLiveEvent<ACTION> = SingleLiveEvent()

    sealed class ACTION {

        object LoginSuccess : ACTION()
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
                    })
            }
        }
    }


    internal fun navigateToProgress() {

        uiEventStream.postValue(
            UiEvent.OpenIntent(
                Intent(app, ProfileProgressActivity::class.java),
                true
            )
        )
    }

    internal fun navigateToBiometric() {

        uiEventStream.postValue(
            UiEvent.OpenIntent(
                Intent(app, SetUpBiometricActivity::class.java),
                true
            )
        )
    }
}