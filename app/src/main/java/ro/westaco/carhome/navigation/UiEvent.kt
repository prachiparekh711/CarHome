package ro.westaco.carhome.navigation

import android.content.Intent
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import com.gumroad.creator.navigation.events.OpenUrlEvent
import ro.westaco.carhome.navigation.events.NavAttribs

sealed class UiEvent {

    class Navigation(val navAttribs: NavAttribs) : UiEvent()

    object NavBack : UiEvent()

    object GoToMain : UiEvent()

    class OpenUrl(val openUrlEvent: OpenUrlEvent) : UiEvent()

    class OpenIntent(val intent: Intent, val finishSourceActivity: Boolean = false) : UiEvent()

    class ShowToast(@StringRes val messageResId: Int) : UiEvent()

    class ShowToastMsg(val message: String) : UiEvent()

    class ShowPopup(val message: String, val title: String? = null) : UiEvent()

    class ShowBottomSheet(
        @StringRes val titleResId: Int,
        @StringRes val messageResId: Int,
        @LayoutRes val layoutResId: Int,
        val linkClicked: Boolean
    ) : UiEvent()

    object HideKeyboard : UiEvent()
    object ShowLoading : UiEvent()
    object HideLoading : UiEvent()

}