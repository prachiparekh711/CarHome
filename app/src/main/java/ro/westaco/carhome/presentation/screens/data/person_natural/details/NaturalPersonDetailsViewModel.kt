package ro.westaco.carhome.presentation.screens.data.person_natural.details

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.responses.models.Attachments
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem
import ro.westaco.carhome.data.sources.remote.responses.models.Country
import ro.westaco.carhome.data.sources.remote.responses.models.NaturalPersonDetails
import ro.westaco.carhome.navigation.BundleProvider
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.SingleLiveEvent
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.data.person_natural.add_new.AddNewNaturalPersonFragment
import ro.westaco.carhome.utils.DateTimeUtils
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File
import javax.inject.Inject


@HiltViewModel
class NaturalPersonDetailsViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {
    val naturalPersDetailsLiveData: MutableLiveData<NaturalPersonDetails?> = MutableLiveData()
    val actionStream: SingleLiveEvent<ACTION> = SingleLiveEvent()

    var streetTypeData = MutableLiveData<ArrayList<CatalogItem>?>()
    var countryData = MutableLiveData<ArrayList<Country>?>()
    var licenseCategoryData = MutableLiveData<ArrayList<CatalogItem>?>()

    sealed class ACTION {
        class OnDeleteSuccess(val attachType: String) : ACTION()
        class OnUploadSuccess(val attachType: String, val attachment: Attachments) : ACTION()
        object OnDeleteLogo : ACTION()
        object InProgress : ACTION()
    }

    internal fun convertFromServerDate(date: String?) =
        DateTimeUtils.convertFromServerDate(app, date)

    internal fun fetchDefaultData() {
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


        api.getSimpleCatalog("NOM_DRIVING_LICENSE_CATEGORY_TYPE")
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                licenseCategoryData.value = it.data
            }, {

            })
    }

    /*
    ** User Interaction
    */

    internal fun onReceivedPerson(id: Int?) {
        if (id != null) {
            api.getNaturalPerson(id.toLong())
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({ resp ->
                    naturalPersDetailsLiveData.value = resp?.data
                }, {
                    naturalPersDetailsLiveData.value = null
//                    //   it.printStackTrace()
                })

        }
    }

    internal fun onBack() {
        uiEventStream.value = UiEvent.NavBack
    }

    internal fun onMain() {
        uiEventStream.value = UiEvent.GoToMain
    }

    internal fun onEdit() {
        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.AddNaturalPerson, object : BundleProvider() {
                override fun onAddArgs(bundle: Bundle?): Bundle {
                    return Bundle().apply {
                        putSerializable(AddNewNaturalPersonFragment.ARG_IS_EDIT, true)
                        putSerializable(
                            AddNewNaturalPersonFragment.ARG_NATURAL_PERSON,
                            naturalPersDetailsLiveData.value
                        )
                    }
                }
            }))
    }

    internal fun onDelete(id: Long) {

        api.deleteNaturalPerson(id)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                uiEventStream.value = UiEvent.NavBack
            }, {
                //   it.printStackTrace()
            })
    }


    internal fun onAttach(
        id: Int,
        attachType: String,
        attachmentFile: File
    ) {

        val requestFile: RequestBody =
            attachmentFile.asRequestBody("multipart/form-data".toMediaTypeOrNull())

        val body: MultipartBody.Part =
            MultipartBody.Part.createFormData("attachment", attachmentFile.name, requestFile)

        val fullName: RequestBody =
            attachType.toRequestBody("multipart/form-data".toMediaTypeOrNull())

        api.attachDocumentToNaturalPerson(id, fullName, body)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (attachType == "DRIVING_LICENSE") {
                    uiEventStream.value = UiEvent.ShowToast(R.string.dlUpload_success)
                } else {
                    uiEventStream.value = UiEvent.ShowToast(R.string.idUpload_success)
                }
                actionStream.value =
                    it.data?.let { it1 -> ACTION.OnUploadSuccess(attachType, it1) }
            }, {

            })
    }

    internal fun onDeleteAttachment(
        id: Int, attachID: Int, attachType: String,
    ) {
        api.deleteAttachmentToNaturalPerson(id.toLong(), attachID.toLong())
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (attachType == "DRIVING_LICENSE") {
                    actionStream.value = ACTION.OnDeleteSuccess("DRIVING_LICENSE")
                } else {
                    actionStream.value = ACTION.OnDeleteSuccess("IDENTITY_DOCUMENT")
                }
            }, {
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
                onReceivedPerson(id)
                uiEventStream.value = UiEvent.ShowToast(R.string.logo_success_msg)
            }, {

            })
    }

    internal fun deleteLogo(id: Int) {
        api.deletePersonLogo(id)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                actionStream.value = ACTION.OnDeleteLogo
                onReceivedPerson(id)
            }, {
            })
    }

    internal fun openDialPad(item: NaturalPersonDetails) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:${item.phone}")
        intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        uiEventStream.postValue(
            UiEvent.OpenIntent(
                intent,
                false
            )
        )
    }

    internal fun openComposedMail(item: NaturalPersonDetails) {
        val intent = Intent(
            Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", item.email, null
            )
        )
        intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        uiEventStream.postValue(
            UiEvent.OpenIntent(
                intent,
                false
            )
        )
    }
}