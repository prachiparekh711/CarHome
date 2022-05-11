package ro.westaco.carhome.presentation.screens.signup_methods.biometric_setup

import android.app.Application
import android.content.Intent
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.signup_methods.profile_progress.ProfileProgressActivity
import javax.inject.Inject

@HiltViewModel
class BiometricSetupModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {
    internal fun navigateToProgress() {

        uiEventStream.postValue(
            UiEvent.OpenIntent(
                Intent(app, ProfileProgressActivity::class.java),
                true
            )
        )
    }
}