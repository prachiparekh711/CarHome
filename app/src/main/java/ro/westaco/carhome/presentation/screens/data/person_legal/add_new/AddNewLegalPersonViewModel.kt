package ro.westaco.carhome.presentation.screens.data.person_legal.add_new

import android.annotation.SuppressLint
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
class AddNewLegalPersonViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi,
) : BaseViewModel() {

    override fun onFragmentCreated() {
        super.onFragmentCreated()
        fetchDefaultData()
    }

    var caenData = MutableLiveData<ArrayList<Caen>>()
    var streetTypeData = MutableLiveData<ArrayList<CatalogItem>>()
    var activityTypeData = MutableLiveData<ArrayList<CatalogItem>>()
    var countryData = MutableLiveData<ArrayList<Country>>()

    @SuppressLint("NullSafeMutableLiveData")
    private fun fetchDefaultData() {
        api.getCountries()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { resp ->
                    countryData.value = resp.data
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

    internal fun onSave(
        id: Long?,
        companyName: String,
        cui: String,
        noReg: String,
        address: Address?,
        isChecked: Boolean,
        caen: Caen?,
        activityType: CatalogItem?,
        isEdit: Boolean,
        phoneId: String,
        phoneCountryCodeId: String?,
        emailId: String,
        type: String
    ) {
        uiEventStream.value = UiEvent.HideKeyboard

        if (companyName.isEmpty()) {
            uiEventStream.value = UiEvent.ShowToast(R.string.company_empty)
        }
        if (cui.isEmpty()) {
            uiEventStream.value = UiEvent.ShowToast(R.string.cui_empty)
        }
        if (cui.length >= 10) {
            uiEventStream.value = UiEvent.ShowToast(R.string.invalid_cui)
        }

//        if (noReg.isEmpty()){
//            uiEventStream.value = UiEvent.ShowToast(R.string.caen_empty)
//        }

        if (type.isEmpty()) {
            uiEventStream.value = UiEvent.ShowToast(R.string.Activity_type_empty)
        }


        if (!isChecked) {
            uiEventStream.value = UiEvent.ShowToast(R.string.confirm_details)
            return
        }

        if (!DeviceUtils.isOnline(app)) {
            uiEventStream.value = UiEvent.ShowToast(R.string.int_not_connect)
            return
        }

        /*if (!validateFields(address)
        ) {
            return
        }*/


        val vatPayer = cui.contains("ro", true)

        val legalPerson = AddLegalPersonRequest(
            noRegistration = noReg,
            vatPayer = vatPayer,
            address = address,
            cui = cui,
            companyName = companyName,
            caen = caen,
            id = id,
            activityType = activityType,
            phone = phoneId,
            phoneCountryCode = phoneCountryCodeId,
            email = emailId
        )

        val request = if (isEdit) {
            id?.let { api.editLegalPerson(it, legalPerson) }
        } else {
            api.createLegalPerson(legalPerson)
        }

        request?.subscribeOn(Schedulers.io())?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                uiEventStream.value = UiEvent.ShowToast(
                    if (isEdit)
                        R.string.edit_success_msg
                    else
                        R.string.save_success_msg
                )
                uiEventStream.value = UiEvent.NavBack
            }, {
                uiEventStream.value = UiEvent.ShowToast(R.string.server_saving_error)
            })
    }


    fun hideKeyboard() {
        uiEventStream.value = UiEvent.HideKeyboard
    }
}