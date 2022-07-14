package ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.bill_user.natural

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.responses.models.NaturalPerson
import ro.westaco.carhome.navigation.BundleProvider
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.data.person_natural.details.NaturalPersonDetailsFragment
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class BillingNaturalViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {

    val naturalPersonsLiveData = MutableLiveData<ArrayList<NaturalPerson>?>()

    override fun onFragmentCreated() {
        fetchRemoteData()
    }

    private fun fetchRemoteData() {
        api.getNaturalPersons()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp -> naturalPersonsLiveData.value = resp?.data }, {})
    }

    internal fun onAddNew() {
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.AddBillNaturalPerson))
    }


    internal fun onItemClick(item: NaturalPerson) {
        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.NaturalPersonDetails, object : BundleProvider() {
                override fun onAddArgs(bundle: Bundle?): Bundle {
                    return Bundle().apply {
                        putSerializable(NaturalPersonDetailsFragment.ARG_NATURAL_PERSON, item)
                        putBoolean(NaturalPersonDetailsFragment.ARG_MENU, false)
                    }
                }
            }))
    }


}