package ro.westaco.carhome.presentation.screens.auth

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.requests.DeviceTokenRequest
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.SingleLiveEvent
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.auth.AuthActivity.Companion.BIOMETRICS
import ro.westaco.carhome.presentation.screens.auth.AuthActivity.Companion.BIOMETRICS_SETUP
import ro.westaco.carhome.presentation.screens.main.MainActivity
import ro.westaco.carhome.presentation.screens.signup_methods.biometric_setup.SetUpBiometricActivity
import ro.westaco.carhome.presentation.screens.signup_methods.email_verification.EmailVerificationActivity
import ro.westaco.carhome.presentation.screens.signup_methods.profile_progress.ProfileProgressActivity
import ro.westaco.carhome.utils.DeviceUtils
import ro.westaco.carhome.utils.FirebaseAnalyticsList
import ro.westaco.carhome.utils.default
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@HiltViewModel
class AuthModel @Inject constructor(

    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {

    companion object {
        const val ELAPSED_MIN_RECENT_AUTH = 0
    }

    private var firebaseAuth = FirebaseAuth.getInstance()
    private var mFirebaseAnalytics = FirebaseAnalytics.getInstance(app)
    private val appPreferences = AppPreferencesDelegates.get()

    var authStateLiveData = MutableLiveData<STATE>().default(STATE.Login)

    sealed class STATE {
        object Login : STATE()
        object SignUp : STATE()
    }

    val actionStream: SingleLiveEvent<ACTION> = SingleLiveEvent()

    sealed class ACTION {

        class LaunchSignInWithEmailAndPassword(val email: String, val pass: String) : ACTION()
        class CreateUserWithEmailAndPassword(val email: String, val pass: String) : ACTION()
        object LaunchSignInWithGoogle : ACTION()
        object LaunchSignInWithFacebook : ACTION()
        class ChangePasswordState(val isHidden: Boolean) : ACTION()
        class ChangeConfirmPasswordState(val isHidden: Boolean) : ACTION()
        object AuthenticateViaBiometrics : ACTION()
        object UserSuccess : ACTION()
        object UserFailure : ACTION()
        object SetSharedPrefrences : ACTION()
        object UserRetrievalSuccess : ACTION()

    }

    private var isPasswordHidden = true
    private var isConfirmPasswordHidden = true

    override fun onActivityCreated() {

        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            actionStream.value = ACTION.UserSuccess
            onUserRetrieved(currentUser)
        } else {
            actionStream.value = ACTION.UserFailure
        }
    }

    private fun isLastLoginRecent(): Boolean {
        val nowMillis = System.currentTimeMillis()
        val lastLoginMillis = appPreferences.lastLoginMillis
        val diffInMillis = nowMillis - lastLoginMillis

        return TimeUnit.MINUTES.convert(
            diffInMillis,
            TimeUnit.MILLISECONDS
        ) < ELAPSED_MIN_RECENT_AUTH
    }

    private fun onUserRetrieved(user: FirebaseUser) {
        if (isLastLoginRecent()) {
            if (BIOMETRICS) {
                actionStream.value = ACTION.UserFailure
                actionStream.value = ACTION.AuthenticateViaBiometrics
            } else {

                actionStream.value = ACTION.UserSuccess
//                Do not uncomment
//                uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.Dashboard))
                uiEventStream.postValue(
                    UiEvent.OpenIntent(
                        Intent(app, MainActivity::class.java),
                        true
                    )
                )
            }
            return
        }

        if (!DeviceUtils.isOnline(app)) {
            actionStream.value = ACTION.UserFailure
            uiEventStream.value = UiEvent.ShowToast(R.string.int_not_connect)
            return
        }

        user.getIdToken(true).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // save Firebase token
                appPreferences.token = task.result.token.toString()
                Log.e("token", appPreferences.token)
                if (firebaseAuth.currentUser?.isEmailVerified == true) {
                    if (BIOMETRICS_SETUP) {
//*             Authenticate via biometrics (S4)

                        api.login()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({

                                if (BIOMETRICS) {
                                    actionStream.value = ACTION.UserFailure
                                    actionStream.value = ACTION.AuthenticateViaBiometrics
                                } else {
                                    actionStream.value = ACTION.UserSuccess
                                    appPreferences.lastLoginMillis = System.currentTimeMillis()
                                    uiEventStream.postValue(
                                        UiEvent.OpenIntent(
                                            Intent(
                                                app,
                                                MainActivity::class.java
                                            ), finishSourceActivity = true
                                        )
                                    )
                                }
                            }, {
                                actionStream.value = ACTION.UserFailure
                                uiEventStream.value =
                                    UiEvent.ShowToast(R.string.failed_server)
                            })
                    } else {
                        actionStream.value = ACTION.UserRetrievalSuccess
                    }

                } else {
                    uiEventStream.postValue(

                        UiEvent.OpenIntent(
                            Intent(app, EmailVerificationActivity::class.java),
                            false
                        )
                    )
                }
            } else {
                actionStream.value = ACTION.UserFailure
                uiEventStream.value = UiEvent.ShowToast(R.string.user_retrieval_failed)
            }
        }

    }

    fun onUserSuccess() {

        api.login()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                actionStream.value = ACTION.UserSuccess
                appPreferences.lastLoginMillis = System.currentTimeMillis()
//                uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.Dashboard))
                uiEventStream.postValue(
                    UiEvent.OpenIntent(
                        Intent(app, MainActivity::class.java),
                        finishSourceActivity = true
                    )
                )

            }, {
                actionStream.value = ACTION.UserFailure
                uiEventStream.value = UiEvent.ShowToast(R.string.failed_server)
            })
    }

    internal fun onLogin(email: String, password: String) {
        uiEventStream.value = UiEvent.HideKeyboard

        if (email.isEmpty()) {
            uiEventStream.value = UiEvent.ShowToast(R.string.email_required)
            return
        }
        if (password.isEmpty()) {
            uiEventStream.value = UiEvent.ShowToast(R.string.pass_required)
            return
        }

        actionStream.value = ACTION.LaunchSignInWithEmailAndPassword(email, password)
    }

    internal fun onLoginTaskCompleted(task: Task<AuthResult>) {
        if (task.isSuccessful) {
            onLoginSucceded()
        } else {
            actionStream.value = ACTION.UserFailure
            if (task.exception?.message.isNullOrEmpty()) {
                uiEventStream.value = UiEvent.ShowToast(R.string.login_failed)
            } else {
                uiEventStream.value = task.exception?.message?.let { UiEvent.ShowToastMsg(it) }
            }
        }
    }

    internal fun onLoginSucceded() {

        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            actionStream.value = ACTION.UserSuccess
            registerDevice()
            onUserRetrieved(currentUser)

            val params = Bundle()
            mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.LOGIN_ANDROID, params)

//            uiEventStream.value = UiEvent.ShowToast(R.string.new_car_error)

        } else {
            actionStream.value = ACTION.UserFailure
            uiEventStream.value = UiEvent.ShowToast(R.string.err_user_null)
        }

    }

    private fun registerDevice() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("TAG", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            val request = DeviceTokenRequest(token)
            api.registerDevice(request)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                }, {
                })
            // Log and toast
        })

    }

    internal fun onSignup(email: String, password: String, confirmPassword: String) {
        if (email.isEmpty()) {
            uiEventStream.value = UiEvent.ShowToast(R.string.email_required)
            return
        }
        if (password.isEmpty()) {
            uiEventStream.value = UiEvent.ShowToast(R.string.pass_required)
            return
        }
        if (confirmPassword.isEmpty() || password != confirmPassword) {
            uiEventStream.value = UiEvent.ShowToast(R.string.pass_different)
            return
        }

        actionStream.value = ACTION.CreateUserWithEmailAndPassword(email, password)

    }

    internal fun onSignupTaskCompleted(task: Task<AuthResult>) {
        if (task.isSuccessful) {
            actionStream.value = ACTION.UserSuccess
            registerDevice()
            actionStream.value = ACTION.SetSharedPrefrences

            val params = Bundle()
            mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.SIGNUP_ANDROID, params)

//            uiEventStream.value = UiEvent.ShowToast(R.string.new_car_error)

            firebaseAuth.currentUser?.getIdToken(true)?.addOnCompleteListener { taskResult ->
                if (taskResult.isSuccessful) {
                    // save Firebase token
                    appPreferences.token = taskResult.result.token.toString()
                    uiEventStream.postValue(
                        UiEvent.OpenIntent(
                            Intent(app, EmailVerificationActivity::class.java), false
                        )
                    )
                }
            }
        } else {
            actionStream.value = ACTION.UserFailure
            if (task.exception?.message.isNullOrEmpty()) {
                uiEventStream.value = UiEvent.ShowToast(R.string.signup_failed)
            } else {
                uiEventStream.value = task.exception?.message?.let { UiEvent.ShowToastMsg(it) }
            }
        }
    }

    internal fun onGoogleAuth() {
        actionStream.value = ACTION.LaunchSignInWithGoogle
    }

    internal fun onGoogleLoginFailed() {
        actionStream.value = ACTION.UserFailure
        uiEventStream.value = UiEvent.ShowToast(R.string.g_login_failed)
    }

    internal fun onFacebookAuth() {
        actionStream.value = ACTION.LaunchSignInWithFacebook
    }

    internal fun onFacebookLoginFailed() {
        actionStream.value = ACTION.UserFailure
        uiEventStream.value = UiEvent.ShowToast(R.string.f_login_failed)
    }

    internal fun onForgotPassword() {
        actionStream.value = ACTION.UserFailure
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.ForgotPassword))
    }

    internal fun onRevealPasswordClicked() {
        isPasswordHidden = !isPasswordHidden
        actionStream.value = ACTION.ChangePasswordState(isPasswordHidden)
    }

    internal fun onRevealConfirmPasswordClicked() {
        isConfirmPasswordHidden = !isConfirmPasswordHidden
        actionStream.value = ACTION.ChangeConfirmPasswordState(isConfirmPasswordHidden)
    }

    internal fun onSwitchAuth() {
        val authState = authStateLiveData.value
        authStateLiveData.value = if (authState == STATE.Login) STATE.SignUp else STATE.Login
    }

    internal fun onTermsClicked(linkClicked: Boolean) {
        uiEventStream.value = UiEvent.ShowBottomSheet(
            R.string.terms_tos,
            R.string.tc,
            R.layout.layout_bottom_sheet,
            linkClicked
        )
    }

    internal fun onGdprClicked(linkClicked: Boolean) {
        uiEventStream.value = UiEvent.ShowBottomSheet(
            R.string.gdpr_title,
            R.string.gdpr,
            R.layout.layout_bottom_sheet,
            linkClicked
        )
    }

    internal fun navigateToProgress() {

        uiEventStream.postValue(
            UiEvent.OpenIntent(
                Intent(app, ProfileProgressActivity::class.java),
                true
            )
        )
    }


    fun navigateToBiometric() {
        uiEventStream.postValue(
            UiEvent.OpenIntent(
                Intent(app, SetUpBiometricActivity::class.java),
                true
            )
        )
    }


    internal fun onBack() {
        uiEventStream.value = UiEvent.NavBack
    }

}