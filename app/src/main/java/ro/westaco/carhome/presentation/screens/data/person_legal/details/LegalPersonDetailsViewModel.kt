package ro.westaco.carhome.presentation.screens.data.person_legal.details

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem
import ro.westaco.carhome.data.sources.remote.responses.models.Country
import ro.westaco.carhome.data.sources.remote.responses.models.LegalPersonDetails
import ro.westaco.carhome.navigation.BundleProvider
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.SingleLiveEvent
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.data.person_legal.add_new.AddNewLegalPersonFragment
import ro.westaco.carhome.utils.DeviceUtils
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File
import javax.inject.Inject


@HiltViewModel
class LegalPersonDetailsViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {

    val legalPersDetailsLiveData: MutableLiveData<LegalPersonDetails?>? = MutableLiveData()

    var countryData = MutableLiveData<ArrayList<Country>?>()
    var streetTypeData = MutableLiveData<ArrayList<CatalogItem>?>()

    val actionStream: SingleLiveEvent<ACTION> = SingleLiveEvent()

    sealed class ACTION {
        object OnDeleteLogo : ACTION()
    }

    internal fun onBack() {
        uiEventStream.value = UiEvent.NavBack
    }

    internal fun onMain() {
        uiEventStream.value = UiEvent.GoToMain
    }

    fun fetchDefaultData() {
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

    internal fun getLegalPersonDetails(id: Long) {
        api.getLegalPersonDetails(id)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                legalPersDetailsLiveData?.value = it?.data
            }, {
                //   it.printStackTrace()
                legalPersDetailsLiveData?.value = null
                uiEventStream.value = UiEvent.ShowToast(R.string.general_server_error)
            })

    }

    internal fun onEdit(legalPersonDetails: LegalPersonDetails?) {
        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.AddLegalPerson, object : BundleProvider() {
                override fun onAddArgs(bundle: Bundle?): Bundle {
                    return Bundle().apply {
                        putSerializable(AddNewLegalPersonFragment.ARG_IS_EDIT, true)
                        putSerializable(
                            AddNewLegalPersonFragment.ARG_LEGAL_PERSON,
                            legalPersonDetails
                        )
                    }
                }
            }))
    }

    internal fun onDelete(id: Long) {
        if (!DeviceUtils.isOnline(app)) {
            uiEventStream.value = UiEvent.ShowToast(R.string.int_not_connect)
            return
        }

        api.deleteLegalPerson(id)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                uiEventStream.value = UiEvent.ShowToast(R.string.delete_success_msg)
                uiEventStream.value = UiEvent.NavBack
            }, {
                //   it.printStackTrace()
                uiEventStream.value = UiEvent.ShowToast(R.string.general_server_error)
            })
    }

    internal fun onAddLogo(
        id: Int,
        logoFile: File
    ) {
        val requestFile: RequestBody =
            logoFile.asRequestBody("multipart/form-data".toMediaTypeOrNull())

        val body: MultipartBody.Part =
            MultipartBody.Part.createFormData("attachment", logoFile.name, requestFile)

        api.addPersonLogo(id, body)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                uiEventStream.value = UiEvent.ShowToast(R.string.logo_success_msg)
                getLegalPersonDetails(id.toLong())
            }, {
                uiEventStream.value =
                    UiEvent.ShowToast(R.string.server_saving_error)
            })
    }

    internal fun deleteLogo(id: Int) {
        api.deletePersonLogo(id)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                actionStream.value = ACTION.OnDeleteLogo
                getLegalPersonDetails(id.toLong())
            }, {
                uiEventStream.value = UiEvent.ShowToast(R.string.general_server_error)
            })
    }
}