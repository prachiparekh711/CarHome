package ro.westaco.carhome.presentation.screens.settings.data.person_natural.add_new

import android.app.Application
import android.content.Intent
import android.view.View
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
import ro.westaco.carhome.data.sources.remote.requests.*
import ro.westaco.carhome.data.sources.remote.responses.models.Attachments
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem
import ro.westaco.carhome.data.sources.remote.responses.models.Country
import ro.westaco.carhome.data.sources.remote.responses.models.Siruta
import ro.westaco.carhome.navigation.SingleLiveEvent
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.home.PdfActivity
import ro.westaco.carhome.utils.DateTimeUtils
import ro.westaco.carhome.utils.DeviceUtils
import ro.westaco.carhome.utils.default
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import javax.inject.Inject


@HiltViewModel
class AddNewNaturalPersonViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {
    val datesMapLiveData = MutableLiveData<HashMap<View, Long>>().default(hashMapOf())
    var occupationData = ArrayList<CatalogItem>()
    val actionStream: SingleLiveEvent<ACTION> = SingleLiveEvent()

    var streetTypeData = MutableLiveData<ArrayList<CatalogItem>>()
    var idTypeData = MutableLiveData<ArrayList<CatalogItem>>()
    var sirutaData = MutableLiveData<ArrayList<Siruta>>()
    var countryData = MutableLiveData<ArrayList<Country>>()
    var licenseCategoryData = MutableLiveData<ArrayList<CatalogItem>>()

    sealed class ACTION {

        class ShowDatePicker(val view: View, val dateInMillis: Long) : ACTION()
        class onDeleteSuccess(val attachType: String) : ACTION()
        class onUploadSuccess(val attachType: String, val attachment: Attachments) : ACTION()
    }

    override fun onFragmentCreated() {
        super.onFragmentCreated()
        fetchDefaultData()
    }


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

        api.getSimpleCatalog("NOM_IDENTITY_DOCUMENT_TYPE")
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { idTypeData.value = it.data },
                {

                }
            )

        api.getOccupation()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                resp?.data?.let { occupationData.addAll(it) }
            }, {
                //   it.printStackTrace()
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
    internal fun onBack() {
        uiEventStream.value = UiEvent.NavBack
    }

    internal fun onMain() {
        uiEventStream.value = UiEvent.GoToMain
    }

    internal fun onRootClicked() {
        uiEventStream.value = UiEvent.HideKeyboard
    }

    internal fun onDateClicked(view: View, dateInMillis: Long?) {
        uiEventStream.value = UiEvent.HideKeyboard

        val date = datesMapLiveData.value?.get(view)
        actionStream.value = ACTION.ShowDatePicker(view, dateInMillis ?: Date().time)
    }

    internal fun onDatePicked(view: View, dateInMillis: Long) {
        val datesMap = datesMapLiveData.value
        datesMap?.put(view, dateInMillis)
        datesMapLiveData.value = datesMap
    }

    internal fun onSave(
        id: Long?,
        lastname: String?,
        address: Address?,
        docType: String?,
        docID: Int?,
        series: String?,
        number: String?,
        expirationDate: String?,
        cnp: String?,
        dateOfBirth: String?,
        firstName: String?,
        phone: String?,
        phoneCountryCode: String?,
        drivLicenseId: String?,
        drivLicenseIssueDate: String?,
        drivLicenseExpDate: String?,
        drivingLicenseCateg: ArrayList<CatalogItem>?,
        email: String?,
        isChecked: Boolean,
        isEdit: Boolean
    ) {

        uiEventStream.value = UiEvent.HideKeyboard

        if (firstName?.isNotEmpty() == true) {

            if (lastname?.isNotEmpty() == true) {

                if (firstName.length in 2..50) {

                    if (lastname.length in 2..50) {

                        if (address?.streetName?.isNotEmpty() == true) {


                            if (address.buildingNo?.isNotEmpty() == true) {

                                if (!isChecked) {
                                    uiEventStream.value =
                                        UiEvent.ShowToast(R.string.confirm_details)
                                    return
                                }

                            } else {

                                uiEventStream.value = UiEvent.ShowToast(R.string.number_empty)
                                return
                            }


                        } else {


                            uiEventStream.value = UiEvent.ShowToast(R.string.street_empty)
                            return

                        }

                    } else {

                        uiEventStream.value = UiEvent.ShowToast(R.string.last_name_len)
                        return
                    }

                } else {

                    uiEventStream.value = UiEvent.ShowToast(R.string.first_name_len)
                    return
                }

            } else {

                uiEventStream.value = UiEvent.ShowToast(R.string.last_name_r)
                return
            }


        } else {

            uiEventStream.value = UiEvent.ShowToast(R.string.first_name_r)
            return
        }

        val drivingLicenseCategory = ArrayList<Int>()
        drivingLicenseCateg?.indices?.forEach { i ->
            drivingLicenseCategory.add(drivingLicenseCateg[i].id.toInt())
        }

        val drivingLicense = DrivingLicense(
            drivLicenseId,
            DateTimeUtils.convertToServerDate(app, drivLicenseIssueDate),
            DateTimeUtils.convertToServerDate(app, drivLicenseExpDate),
            drivingLicenseCategory
        )

        val doc: DocumentType?
        if (docType.isNullOrEmpty()) {

            doc =
                DocumentType(idTypeData.value?.get(0)?.name, idTypeData.value?.get(0)?.id?.toInt())

        } else {

            doc = DocumentType(docType, docID)

        }

        val identityDocument = IdentityDocument(
            number,
            doc,
            series,
            DateTimeUtils.convertToServerDate(app, expirationDate)
        )

        val naturalPersonReq = AddNaturalPersonRequest(
            firstName,
            lastname,
            null,
            address,
            identityDocument,
            cnp,
            phone,
            phoneCountryCode,
            drivingLicense,
            null,
            DateTimeUtils.convertToServerDate(app, dateOfBirth),
            null,
            email
        )

        val request =
            if (isEdit) {
                if (id != null)
                    api.editNaturalPerson(id, naturalPersonReq)
                else
                    null
            } else {
                api.createNaturalPerson(naturalPersonReq)
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
                //   it.printStackTrace()
                uiEventStream.value = UiEvent.ShowToast(R.string.server_saving_error)
            })
    }

    internal fun convertFromServerDate(date: String?) =
        DateTimeUtils.convertFromServerDate(app, date)

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
                actionStream.value = it.data?.let { it1 -> ACTION.onUploadSuccess(attachType, it1) }
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
                if (attachType == "DRIVING_LICENSE") {
                    actionStream.value = ACTION.onDeleteSuccess("DRIVING_LICENSE")
                } else {
                    actionStream.value = ACTION.onDeleteSuccess("IDENTITY_DOCUMENT")
                }
            }, {
                uiEventStream.value = UiEvent.ShowToast(R.string.general_server_error)
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

    private fun validateFields(
        address: Address?,
        firstName: String?,
        lastname: String?
    ): Boolean {

        if (address?.streetName?.isEmpty() == true || address?.countryCode?.isEmpty() == true || address?.region?.isEmpty() == true || address?.zipCode?.isEmpty() == true) {
            uiEventStream.value = UiEvent.ShowToast(R.string.address_require)
            return false
        }


        return true
    }

    fun hideKeyboard() {
        uiEventStream.value = UiEvent.HideKeyboard
    }
}