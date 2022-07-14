package ro.westaco.carhome.presentation.screens.splash

import android.app.Application
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.viewModelScope
import com.scottyab.rootbeer.RootBeer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.auth.AuthActivity
import ro.westaco.carhome.presentation.screens.onboarding.OnboardingActivity
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {

    private val appPreferences = AppPreferencesDelegates.get()

    companion object {
        const val DEFAULT_SPLASH_TIME = 3000L
    }

    override fun onActivityCreated() {
        val rootBeer = RootBeer(app)
        if (rootBeer.isRooted) {
            viewModelScope.launch {
                uiEventStream.postValue(
                    UiEvent.ShowToast(R.string.phone_rooted)
                )
                delay(3000)
                android.os.Process.killProcess(android.os.Process.myPid())
            }

        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                if (appPreferences.wasOnboardingSeen) {
                    appPreferences.wasOnboardingSeen = true

                    uiEventStream.postValue(
                        UiEvent.OpenIntent(
                            Intent(app, AuthActivity::class.java),
                            finishSourceActivity = true
                        )
                    )
                } else {
                    uiEventStream.postValue(
                        UiEvent.OpenIntent(
                            Intent(app, OnboardingActivity::class.java),
                            finishSourceActivity = true
                        )
                    )
                }
            }, DEFAULT_SPLASH_TIME)
        }
    }
}