package ro.westaco.carhome.presentation.screens.service.help

import android.app.Application
import android.content.Intent
import android.net.Uri
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.presentation.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class TransactionHelpViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {

    internal fun onBack() {
        uiEventStream.value = UiEvent.NavBack
    }

    internal fun openDialPad(item: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:${item}")
        intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        uiEventStream.postValue(
            UiEvent.OpenIntent(
                intent,
                false
            )
        )
    }

    internal fun openComposedMail(item: String) {
        val intent = Intent(
            Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", item, null
            )
        )
        intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        uiEventStream.postValue(
            UiEvent.OpenIntent(
                intent,
                false
            )
        )
    }

}