package ro.westaco.carhome.presentation.base

import androidx.lifecycle.ViewModel
import ro.westaco.carhome.navigation.SingleLiveEvent
import ro.westaco.carhome.navigation.UiEvent

open class BaseViewModel : ViewModel() {

    val uiEventStream: SingleLiveEvent<UiEvent> = SingleLiveEvent()

    open fun onActivityCreated() {

    }

    open fun onFragmentCreated() {

    }

}