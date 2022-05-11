package ro.westaco.carhome.presentation.screens.settings.security

import android.app.Application
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class ChangePasswordModel @Inject constructor(
    private val app: Application
) : BaseViewModel() {


    internal fun onBack() {
        uiEventStream.value = UiEvent.NavBack
    }

    internal fun onMain() {
        uiEventStream.value = UiEvent.GoToMain
    }


    internal fun OnPasswordChangeSuccess() {
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.PasswordChangeSuccess))
    }


    internal fun onRootClicked() {
        uiEventStream.value = UiEvent.HideKeyboard
    }


}