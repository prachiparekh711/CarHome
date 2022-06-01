package ro.westaco.carhome.presentation.screens.service.person.legal

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.responses.models.LegalPerson
import ro.westaco.carhome.data.sources.remote.responses.models.LegalPersonDetails
import ro.westaco.carhome.navigation.BundleProvider
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.data.person_legal.add_new.AddNewLegalPersonFragment
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class BillingLegalViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi,
) : BaseViewModel() {

    val legalPersonsLiveData = MutableLiveData<ArrayList<LegalPerson>?>()
    val legalPersonsDetailsLiveData = MutableLiveData<LegalPersonDetails?>()


    override fun onFragmentCreated() {
        fetchRemoteData()
    }


    private fun fetchRemoteData() {
        api.getLegalPersons()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                legalPersonsLiveData.value = resp?.data
            }, {
                //   it.printStackTrace()
            })
    }

    fun fetchLegalDetails(id: Long) {

        id.let {
            api.getLegalPersonDetails(it)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({ resp ->
                    legalPersonsDetailsLiveData.value = resp?.data
                }, {
                })
        }
    }

    internal fun onAddNew() {
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.AddBillLegalPerson))
    }

    internal fun onEdit(personDetail: LegalPersonDetails) {

        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.AddLegalPerson, object : BundleProvider() {
                override fun onAddArgs(bundle: Bundle?): Bundle {
                    return Bundle().apply {
                        putSerializable(AddNewLegalPersonFragment.ARG_IS_EDIT, true)
                        putSerializable(
                            AddNewLegalPersonFragment.ARG_LEGAL_PERSON,
                            personDetail
                        )
                    }
                }
            }))

    }

}

