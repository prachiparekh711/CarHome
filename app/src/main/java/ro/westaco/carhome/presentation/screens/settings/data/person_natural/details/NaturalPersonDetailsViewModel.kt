package ro.westaco.carhome.presentation.screens.settings.data.person_natural.details

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.responses.models.*
import ro.westaco.carhome.navigation.BundleProvider
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.SingleLiveEvent
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.home.PdfActivity
import ro.westaco.carhome.presentation.screens.settings.data.person_natural.add_new.AddNewNaturalPersonFragment
import ro.westaco.carhome.utils.DateTimeUtils
import ro.westaco.carhome.utils.DeviceUtils
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject


@HiltViewModel
class NaturalPersonDetailsViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {
    val naturalPersDetailsLiveData: MutableLiveData<NaturalPersonDetails?> = MutableLiveData()
    var naturalLogoData: MutableLiveData<Bitmap>? = MutableLiveData()
    val actionStream: SingleLiveEvent<ACTION> = SingleLiveEvent()

    var streetTypeData = MutableLiveData<ArrayList<CatalogItem>>()
    var sirutaData = MutableLiveData<ArrayList<Siruta>>()
    var countryData = MutableLiveData<ArrayList<Country>>()
    var licenseCategoryData = MutableLiveData<ArrayList<CatalogItem>>()

    sealed class ACTION {
        class onDeleteSuccess(val attachType: String) : ACTION()
        class onUploadSuccess(val attachType: String, val attachment: Attachments) : ACTION()
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

        api.getSiruta()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    sirutaData.value = it.data
                }, {}
            )

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

            api.getPersonLogo(id.toLong())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                    try {
                        val byteArray = it.source().readByteArray()
                        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                        naturalLogoData?.value = bitmap
                    } catch (e: Exception) {
                    }
                }, {
                    naturalLogoData?.value = null
//                //   it.printStackTrace()
//                uiEventStream.value = UiEvent.ShowToast(R.string.failed_server)
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
        if (!DeviceUtils.isOnline(app)) {
            uiEventStream.value = UiEvent.ShowToast(R.string.int_not_connect)
            return
        }

        api.deleteNaturalPerson(id)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                uiEventStream.value = UiEvent.ShowToast(R.string.delete_success_msg)
                uiEventStream.value = UiEvent.NavBack
            }, {
                //   it.printStackTrace()
                uiEventStream.value = UiEvent.ShowToast(R.string.general_server_error)
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
                if (attachType.equals("DRIVING_LICENSE")) {
                    uiEventStream.value = UiEvent.ShowToast(R.string.dlUpload_success)
                } else {
                    uiEventStream.value = UiEvent.ShowToast(R.string.idUpload_success)
                }
                actionStream.value =
                    it.data?.let { it1 -> ACTION.onUploadSuccess("DRIVING_LICENSE", it1) }
            }, {
                uiEventStream.value =
                    UiEvent.ShowToast(R.string.server_saving_error)
            })
    }

    internal fun onDeleteAttachment(
        id: Int, attachID: Int, attachType: String,
    ) {
        api.deleteAttachmentToNaturalPerson(id.toLong(), attachID.toLong())
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                uiEventStream.value = UiEvent.ShowToast(R.string.delete_success_msg)
                if (attachType.equals("DRIVING_LICENSE")) {
                    actionStream.value = ACTION.onDeleteSuccess("DRIVING_LICENSE")
                } else {
                    actionStream.value = ACTION.onDeleteSuccess("IDENTITY_DOCUMENT")
                }
            }, {
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
                onReceivedPerson(id)
            }, {
                uiEventStream.value =
                    UiEvent.ShowToast(R.string.server_saving_error)
            })
    }

    var attachmentData: MutableLiveData<ResponseBody> = MutableLiveData()
    internal fun fetchData(href: String) {
        if (!DeviceUtils.isOnline(app)) {
            uiEventStream.value = UiEvent.ShowToast(R.string.int_not_connect)
            return
        }
        api.getAttachmentData(href)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                attachmentData.value = it
                if (attachmentData.value != null) {
                    val buffer = ByteArray(8192)
                    var bytesRead: Int? = null
                    val output = ByteArrayOutputStream()
                    while (attachmentData.value?.byteStream()?.read(buffer).also {
                            if (it != null) {
                                bytesRead = it
                            }
                        } != -1) {
                        bytesRead?.let { it1 -> output.write(buffer, 0, it1) }
                    }
                    openPDF(output.toByteArray())
                }
            }, {
                attachmentData.value = null
            })
    }

    internal fun openPDF(data: ByteArray) {

        val intent = Intent(app, PdfActivity::class.java)
        intent.putExtra(PdfActivity.ARG_DATA, data)
        intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        uiEventStream.postValue(
            UiEvent.OpenIntent(
                intent,
                false
            )
        )
    }
}