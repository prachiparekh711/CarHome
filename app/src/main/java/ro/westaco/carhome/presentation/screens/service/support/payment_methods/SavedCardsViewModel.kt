package ro.westaco.carhome.presentation.screens.service.support.payment_methods

import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.data.sources.local.dummy.Card
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import javax.inject.Inject


@HiltViewModel
class SavedCardsViewModel @Inject constructor(
) : BaseViewModel() {
    val cardsLiveData = MutableLiveData<ArrayList<Card>>()

    init {
        populateCardsListFakeItems()
    }

    private fun populateCardsListFakeItems() {
        val fakeCards = arrayListOf<Card>()

        val fakeCard1 = Card(
            "********3300",
            "Ion Andrei",
            "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5a/Visa_2014.svg/1200px-Visa_2014.svg.png"
        )
        fakeCards.add(fakeCard1)

        cardsLiveData.value = fakeCards
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

    internal fun onAddCard() {
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.AddPaymentMethod))
    }
}