package ro.westaco.carhome.presentation.screens.onboarding

import android.app.Application
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.auth.AuthActivity
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(

    private val app: Application
) : BaseViewModel() {


    private val appPreferences = AppPreferencesDelegates.get()
    val pagerItems = MutableLiveData<List<OnboardingItem>>()

    init {
        pagerItems.value = getOnboardingItems()
    }

    override fun onActivityCreated() {
        if (appPreferences.wasOnboardingSeen) {
            onSkip()
        }
    }

    private fun getOnboardingItems() = arrayListOf<OnboardingItem>().apply {
        if (AppPreferencesDelegates.get().language == "en-US") {
            add(OnboardingItem(R.string.onb_title2, R.drawable.ben1))
            add(OnboardingItem(R.string.onb_title1, R.drawable.ben2))
            add(OnboardingItem(R.string.onb_title3, R.drawable.ben3))
        } else {
            add(OnboardingItem(R.string.onb_title2, R.drawable.bro1))
            add(OnboardingItem(R.string.onb_title1, R.drawable.bro2))
            add(OnboardingItem(R.string.onb_title3, R.drawable.bro3))
        }
    }

    /*
    ** User Interaction
    */
    fun onSkip() {
        appPreferences.wasOnboardingSeen = true

        uiEventStream.postValue(
            UiEvent.OpenIntent(
                Intent(app, AuthActivity::class.java),
                finishSourceActivity = true
            )
        )
    }

}