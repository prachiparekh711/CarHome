package ro.westaco.carhome.presentation.screens.service.person.legal.addlegal

import android.app.Application
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.requests.AddLegalPersonRequest
import ro.westaco.carhome.data.sources.remote.requests.Address
import ro.westaco.carhome.data.sources.remote.responses.models.Caen
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem
import ro.westaco.carhome.data.sources.remote.responses.models.Country
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.utils.DeviceUtils
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class AddBillLegalViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {

    override fun onFragmentCreated() {
        super.onFragmentCreated()
        fetchDefaultData()
    }

    var caenData = MutableLiveData<ArrayList<Caen>?>()
    var streetTypeData = MutableLiveData<ArrayList<CatalogItem>?>()
    var activityTypeData = MutableLiveData<ArrayList<CatalogItem>?>()
    var countryData = MutableLiveData<ArrayList<Country>?>()


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


        api.getCaen()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                caenData.value = it.data
            }, {
                //   it.printStackTrace()
            })

        api.getActivityType()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                activityTypeData.value = it.data
            }, {
            })
    }


    internal fun onSave(
        id: Long?,
        companyName: String,
        cui: String,
        noReg: String,
        address: Address?,
        isChecked: Boolean,
        caen: Caen?,
        activityType: CatalogItem?
    ) {
        uiEventStream.value = UiEvent.HideKeyboard

        if (!isChecked) {
            uiEventStream.value = UiEvent.ShowToast(R.string.confirm_details)
            return
        }


        if (!DeviceUtils.isOnline(app)) {
            uiEventStream.value = UiEvent.ShowToast(R.string.int_not_connect)
            return
        }


        val vatPayer = cui.contains("ro", true)
        val legalPerson = AddLegalPersonRequest(
            noReg,
            vatPayer,
            address,
            cui,
            companyName,
            caen,
            id,
            activityType
        )

        val request = api.createLegalPerson(legalPerson)

        request
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                uiEventStream.value = UiEvent.ShowToast(
                    R.string.save_success_msg

                )

                uiEventStream.value = UiEvent.NavBack
            }, {
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


    fun hideKeyboard() {
        uiEventStream.value = UiEvent.HideKeyboard
    }

}