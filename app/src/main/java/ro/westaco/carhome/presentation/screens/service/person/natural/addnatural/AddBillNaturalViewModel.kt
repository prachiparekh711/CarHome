package ro.westaco.carhome.presentation.screens.service.person.natural.addnatural

import android.app.Application
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.requests.AddNaturalPersonRequest
import ro.westaco.carhome.data.sources.remote.requests.Address
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem
import ro.westaco.carhome.data.sources.remote.responses.models.Country
import ro.westaco.carhome.data.sources.remote.responses.models.Siruta
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


    var streetTypeData = MutableLiveData<ArrayList<CatalogItem>>()
    var sirutaData = MutableLiveData<ArrayList<Siruta>>()
    var countryData = MutableLiveData<ArrayList<Country>>()

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

        api.getSiruta()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    sirutaData.value = it.data
                }, {}
            )

    }

    fun onSave(
        firstName: String,
        lastName: String,
        checked: Boolean,
        id: Long?,
        address: Address?,

        ) {
        if (!checked) {
            uiEventStream.value = UiEvent.ShowToast(R.string.confirm_details)
            return
        }

        val naturalPersonReq = AddNaturalPersonRequest(firstName, lastName, null, address)


        val request = api.createNaturalPerson(naturalPersonReq)

        request.subscribeOn(Schedulers.io())?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                uiEventStream.value = UiEvent.ShowToast(
                    R.string.save_success_msg

                )

                uiEventStream.value = UiEvent.NavBack
            }, {
                //   it.printStackTrace()
                uiEventStream.value = UiEvent.ShowToast(R.string.server_saving_error)
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