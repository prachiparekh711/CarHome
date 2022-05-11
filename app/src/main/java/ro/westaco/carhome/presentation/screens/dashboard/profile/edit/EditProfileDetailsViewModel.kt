package ro.westaco.carhome.presentation.screens.dashboard.profile.edit

import android.app.Application
import android.content.Intent
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem
import ro.westaco.carhome.data.sources.remote.responses.models.Country
import ro.westaco.carhome.data.sources.remote.responses.models.Siruta
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.SingleLiveEvent
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
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
class EditProfileDetailsViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {
    var userLiveData = MutableLiveData<FirebaseUser>()
    val profileDateLiveData = MutableLiveData<HashMap<View, Long>>().default(hashMapOf())
    val actionStream: SingleLiveEvent<ACTION> = SingleLiveEvent()

    var occupationData = MutableLiveData<ArrayList<CatalogItem>>()
    var idTypeData = MutableLiveData<ArrayList<CatalogItem>>()
    var sirutaData = MutableLiveData<ArrayList<Siruta>>()
    var countryData = MutableLiveData<ArrayList<Country>>()
    var licenseCategoryData = MutableLiveData<ArrayList<CatalogItem>>()


    override fun onFragmentCreated() {
        super.onFragmentCreated()
        fetchDefaultData()
    }

    sealed class ACTION {
        class ShowDatePicker(val view: View, val dateInMillis: Long) : ACTION()
        class onDeleteSuccess(val attachType: String) : ACTION()
        class onUploadSuccess(val attachType: String, val name: String) : ACTION()
    }

    init {
        userLiveData.value = FirebaseAuth.getInstance().currentUser
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

        api.getSiruta()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    sirutaData.value = it.data
                }, {}
            )

        api.getOccupation()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                occupationData.value = resp.data
            }, {
                //   it.printStackTrace()
            })

        api.getSimpleCatalog("NOM_IDENTITY_DOCUMENT_TYPE")
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { idTypeData.value = it.data },
                {

                }
            )


        api.getSimpleCatalog("NOM_DRIVING_LICENSE_CATEGORY_TYPE")
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                licenseCategoryData.value = it.data
            }, {

            })
    }

    internal fun onDateClicked(view: View, dateInMillis: Long?) {
        uiEventStream.value = UiEvent.HideKeyboard

        val date = profileDateLiveData.value?.get(view)
        actionStream.value = ACTION.ShowDatePicker(view, dateInMillis ?: Date().time)
    }

    internal fun onDatePicked(view: View, dateInMillis: Long) {
        val datesMap = profileDateLiveData.value
        datesMap?.put(view, dateInMillis)
        profileDateLiveData.value = datesMap
    }

    internal fun onBack() {
        uiEventStream.value = UiEvent.NavBack
    }

    internal fun onMain() {
        uiEventStream.value = UiEvent.GoToMain
    }

    internal fun onCta(
        lastname: String?,
        address: Address?,
        docType: String?,
        docID: Int?,
        series: String?,
        number: String?,
        expirationDate: String?,
        cnp: String?,
        employerName: String?,
        dateOfBirth: String?,
        firstName: String?,
        occupationCorIsco08: CatalogItem?,
        phone: String?,
        drivLicenseId: String?,
        drivLicenseIssueDate: String?,
        drivLicenseExpDate: String?,
        drivingLicenseCateg: ArrayList<Int>?,
        email: String?,
        isChecked: Boolean
    ) {
        uiEventStream.value = UiEvent.HideKeyboard
        if (!isChecked) {
            uiEventStream.value = UiEvent.ShowToast(R.string.confirm_details)
            return
        }
        if (!validateFields(address)
        ) {
            return
        }


        val drivingLicense = DrivingLicense(
            drivLicenseId,
            DateTimeUtils.convertToServerDate(app, drivLicenseIssueDate),
            DateTimeUtils.convertToServerDate(app, drivLicenseExpDate),
            drivingLicenseCateg
        )

        val identityDocument =
            IdentityDocument(
                number,
                DocumentType(docType, docID),
                series,
                DateTimeUtils.convertToServerDate(app, expirationDate)
            )

        val addProfileDataRequest = AddProfileDataRequest(
            firstName,
            lastname,
            occupationCorIsco08,
            address,
            identityDocument,
            cnp,
            phone,
            drivingLicense,
            employerName,
            DateTimeUtils.convertToServerDate(app, dateOfBirth),
            null,
            email
        )

        api.editProfile(addProfileDataRequest)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                uiEventStream.value = UiEvent.ShowToast(R.string.edit_success_msg)
                uiEventStream.value = UiEvent.NavBack
            }, {

                uiEventStream.value =
                    UiEvent.ShowToast(R.string.server_saving_error)
            })
    }

    internal fun onCloseAccount() {
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.CloseAccount))
    }

    internal fun convertFromServerDate(date: String?) =
        DateTimeUtils.convertFromServerDate(app, date)

    internal fun onAttach(
        attachType: String,
        attachmentFile: File
    ) {

        val requestFile: RequestBody =
            attachmentFile.asRequestBody("multipart/form-data".toMediaTypeOrNull())

        val body: MultipartBody.Part =
            MultipartBody.Part.createFormData("attachment", attachmentFile.name, requestFile)

        val fullName: RequestBody =
            attachType.toRequestBody("multipart/form-data".toMediaTypeOrNull())

        api.attachDocumentToProfile(fullName, body)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (attachType.equals("DRIVING_LICENSE")) {
                    uiEventStream.value = UiEvent.ShowToast(R.string.dlUpload_success)
                    actionStream.value =
                        ACTION.onUploadSuccess("DRIVING_LICENSE", attachmentFile.name)
                } else {
                    uiEventStream.value = UiEvent.ShowToast(R.string.idUpload_success)
                    actionStream.value =
                        ACTION.onUploadSuccess("IDENTITY_DOCUMENT", attachmentFile.name)
                }
            }, {
                uiEventStream.value =
                    UiEvent.ShowToast(R.string.server_saving_error)
            })
    }

    internal fun onDeleteAttachment(
        attachID: Int, attachType: String,
    ) {
        api.deleteAttachment(attachID)
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
        address: Address?
    ): Boolean {
        if (address?.streetName?.isEmpty() == true ||
            address?.countryCode?.isEmpty() == true ||
            address?.region?.isEmpty() == true ||
            address?.zipCode?.isEmpty() == true
        ) {
            uiEventStream.value = UiEvent.ShowToast(R.string.address_require)
            return false
        }
        return true
    }

    fun hideKeyboard() {
        uiEventStream.value = UiEvent.HideKeyboard
    }

}