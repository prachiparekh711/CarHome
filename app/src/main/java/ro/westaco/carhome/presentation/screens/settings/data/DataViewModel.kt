package ro.westaco.carhome.presentation.screens.settings.data

import android.app.Application
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.R
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.presentation.base.BaseViewModel
import java.util.*
import javax.inject.Inject


@HiltViewModel
class DataViewModel @Inject constructor(
    private val app: Application
) : BaseViewModel() {

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

    internal fun onCloseAccount(confirmation: String) {
        uiEventStream.value = UiEvent.HideKeyboard

        val verify = app.getString(R.string.close_confirm_txt)
        if (confirmation.isEmpty()
            || confirmation.lowercase(Locale.getDefault()) != verify.lowercase(Locale.getDefault())
        ) {
            uiEventStream.value = UiEvent.ShowToast(R.string.user_retrieval_failed)
            return
        }
    }

}