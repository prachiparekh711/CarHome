package ro.westaco.carhome.presentation.screens.settings

import android.app.Application
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.SingleLiveEvent
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.dashboard.DashboardViewModel
import ro.westaco.carhome.utils.DeviceUtils
import ro.westaco.carhome.utils.FirebaseAnalyticsList
import javax.inject.Inject


@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val app: Application,
) : BaseViewModel() {

    private val appPreferences = AppPreferencesDelegates.get()
    private var firebaseAuth = FirebaseAuth.getInstance()
    private var mFirebaseAnalytics = FirebaseAnalytics.getInstance(app)


    val actionStream: SingleLiveEvent<ACTION> = SingleLiveEvent()

    sealed class ACTION {
        object onLogout : ACTION()
    }

    /*
    ** User Interaction
    */
    internal fun onProfileClicked() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.ACCESS_PROFILE, params)
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.Profile))
    }

    internal fun onPaymentMethodsClicked() {

        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.PaymentMethods))
    }

    internal fun onSecurityClicked() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.ACCESS_SECURITY, params)
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.Security))
    }

    internal fun onHistoryClicked() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.ACCESS_PURCHASE_HISTORY, params)
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.History))
    }

    internal fun onDataClicked() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.ACCESS_MY_DATA, params)
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.Data))
    }

    internal fun onNotificationsClicked() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.ACCESS_NOTIFICATIONS, params)
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.Notifications))
    }

    internal fun onDocumentsClicked() {

    }

    internal fun onAboutUsClicked() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.ACCESS_ABOUT_US, params)
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.AboutUs))
    }

    internal fun onFaqClicked() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.ACCESS_HELP, params)
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.Faq))
    }

    internal fun onTermsClicked() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.ACCESS_TERMS_AND_CONDITIONS, params)
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.TermsAndCond))
    }

    internal fun onGdprClicked() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.ACCESS_GDPR, params)
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.GDPR))
    }

    internal fun onShareAppClicked() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.ACCESS_SHARE_THE_APP, params)
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.Share))
    }

    internal fun onContactUsClicked() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.ACCESS_CONTACT_US, params)
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.ContactUs))
    }

    internal fun onSocialClicked() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.ACCESS_SOCIALS, params)
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.Social))
    }

    internal fun onLanguageClicked() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.ACCESS_CHANGE_LANGUAGE, params)
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.Language))
    }

    internal fun onLogout() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.ACCESS_LOG_OUT, params)
    }

    internal fun onLogoutClicked() {
        actionStream.value = ACTION.onLogout
        DashboardViewModel.selectedMenuItem = null
        appPreferences.token = ""
        firebaseAuth.signOut()
        val params = Bundle()
        mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.LOGOUT_ANDROID, params)
        uiEventStream.value = UiEvent.ShowToast(R.string.logout_success)
        DeviceUtils.logoutApp(app)
    }
}