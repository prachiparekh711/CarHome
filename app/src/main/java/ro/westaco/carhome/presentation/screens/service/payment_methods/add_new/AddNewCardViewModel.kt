package ro.westaco.carhome.presentation.screens.service.payment_methods.add_new

import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.R
import ro.westaco.carhome.navigation.SingleLiveEvent
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.presentation.base.BaseViewModel
import java.util.*
import javax.inject.Inject


@HiltViewModel
class AddNewCardViewModel @Inject constructor(
) : BaseViewModel() {
    var cardExpirationDateLiveData = MutableLiveData<Long>()

    val actionStream: SingleLiveEvent<ACTION> = SingleLiveEvent()

    sealed class ACTION {
        class ShowDatePicker(val dateInMillis: Long) : ACTION()
    }

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

    internal fun onExpirationDateClicked() {
        uiEventStream.value = UiEvent.HideKeyboard

        val date = cardExpirationDateLiveData.value
        actionStream.value = ACTION.ShowDatePicker(date ?: Date().time)
    }

    internal fun onDatePicked(dateInMillis: Long) {
        cardExpirationDateLiveData.value = dateInMillis
    }

    internal fun onSave() {
        uiEventStream.value = UiEvent.HideKeyboard
        uiEventStream.value = UiEvent.ShowToast(R.string.save_success_msg)
    }
}