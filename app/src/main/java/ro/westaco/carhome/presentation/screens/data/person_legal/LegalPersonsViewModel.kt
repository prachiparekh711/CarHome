package ro.westaco.carhome.presentation.screens.data.person_legal

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.responses.models.LegalPerson
import ro.westaco.carhome.navigation.BundleProvider
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.data.person_legal.details.LegalPersonDetailsFragment
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject


@HiltViewModel
class LegalPersonsViewModel @Inject constructor(
    private val api: CarHomeApi
) : BaseViewModel() {

    val legalPersonsLiveData = MutableLiveData<ArrayList<LegalPerson>>()


    override fun onFragmentCreated() {
        fetchRemoteData()
    }


    @SuppressLint("NullSafeMutableLiveData")
    private fun fetchRemoteData() {
        api.getLegalPersons()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                legalPersonsLiveData.value = resp?.data
            }, {
                //   it.printStackTrace()
            })
    }

    /*
    ** User Interaction
    */
    internal fun onItemClick(item: LegalPerson) {
        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.LegalPersonDetails, object : BundleProvider() {
                override fun onAddArgs(bundle: Bundle?): Bundle {
                    return Bundle().apply {
                        putSerializable(LegalPersonDetailsFragment.ARG_LEGAL_PERSON, item)
                    }
                }
            }))
    }


    internal fun onAddNew() {
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.AddLegalPerson))
    }
}