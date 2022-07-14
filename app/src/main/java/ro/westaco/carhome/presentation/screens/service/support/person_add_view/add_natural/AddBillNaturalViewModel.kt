package ro.westaco.carhome.presentation.screens.service.support.person_add_view.add_natural

import android.app.Application
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.requests.AddNaturalPersonRequest
import ro.westaco.carhome.data.sources.remote.requests.Address
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem
import ro.westaco.carhome.data.sources.remote.responses.models.Country
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.presentation.base.BaseViewModel
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class AddBillNaturalViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {


    var streetTypeData = MutableLiveData<ArrayList<CatalogItem>?>()
    var countryData = MutableLiveData<ArrayList<Country>?>()

    override fun onFragmentCreated() {
        super.onFragmentCreated()
        fetchDefaultData()
    }

    private fun fetchDefaultData() {
        api.getCountries()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { resp ->
                    countryData.value = resp?.data
                },
                {

                }
            )

        api.getSimpleCatalog("NOM_STREET_TYPE")
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { streetTypeData.value = it.data }, {

                })

    }

    fun onSave(
        firstName: String,
        lastName: String,
        address: Address?,

        ) {

        if (firstName.isNullOrEmpty()) {
            uiEventStream.value = UiEvent.ShowToast(R.string.first_name_r)
            return
        }

        if (lastName.isNullOrEmpty()) {
            uiEventStream.value = UiEvent.ShowToast(R.string.last_name_r)
            return
        }

        if (firstName.length < 2 || firstName.length > 50) {
            uiEventStream.value = UiEvent.ShowToast(R.string.first_name_len)
            return
        }

        if (lastName.length < 2 || lastName.length > 50) {
            uiEventStream.value = UiEvent.ShowToast(R.string.last_name_len)
            return
        }
        val naturalPersonReq = AddNaturalPersonRequest(firstName, lastName, null, address)

        api.createNaturalPerson(naturalPersonReq)
            .subscribeOn(Schedulers.io())?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({

                uiEventStream.value = UiEvent.NavBack
            }, {

            })

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


}