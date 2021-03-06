package ro.westaco.carhome.presentation.screens.dashboard.profile.edit

import android.app.Application
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
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.requests.*
import ro.westaco.carhome.data.sources.remote.responses.models.Attachments
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem
import ro.westaco.carhome.data.sources.remote.responses.models.Country
import ro.westaco.carhome.navigation.SingleLiveEvent
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.main.MainActivity
import ro.westaco.carhome.utils.DateTimeUtils
import ro.westaco.carhome.utils.default
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
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

    var occupationData = MutableLiveData<ArrayList<CatalogItem>?>()
    var idTypeData = MutableLiveData<ArrayList<CatalogItem>?>()
    var countryData = MutableLiveData<ArrayList<Country>?>()
    var licenseCategoryData = MutableLiveData<ArrayList<CatalogItem>?>()
    var streetTypeData = MutableLiveData<ArrayList<CatalogItem>?>()


    override fun onFragmentCreated() {
        super.onFragmentCreated()
        fetchDefaultData()
    }

    sealed class ACTION {
        class ShowDatePicker(val view: View, val dateInMillis: Long) : ACTION()
        class OnDeleteSuccess(val attachType: String) : ACTION()
        class OnUploadSuccess(val attachType: String, val attachment: Attachments) : ACTION()
    }

    init {
        userLiveData.value = FirebaseAuth.getInstance().currentUser
    }

    internal fun fetchDefaultData() {

        api.getSimpleCatalog("NOM_STREET_TYPE")
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { streetTypeData.value = it.data }, {

                })

        api.getCountries()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { resp ->
                    countryData.value = resp?.data
                },
                {

                }
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
        phoneCountryCode: String?,
        drivLicenseId: String?,
        drivLicenseIssueDate: String?,
        drivLicenseExpDate: String?,
        drivingLicenseCateg: ArrayList<Int>?,
        email: String?
    ) {
        uiEventStream.value = UiEvent.HideKeyboard


        if (firstName.isNullOrEmpty()) {
            uiEventStream.value = UiEvent.ShowToast(R.string.first_name_r)
            return
        }

        if (lastname.isNullOrEmpty()) {
            uiEventStream.value = UiEvent.ShowToast(R.string.last_name_r)
            return
        }

        if (firstName.length < 2 || firstName.length > 50) {
            uiEventStream.value = UiEvent.ShowToast(R.string.first_name_len)
            return
        }

        if (lastname.length < 2 || lastname.length > 50) {
            uiEventStream.value = UiEvent.ShowToast(R.string.last_name_len)
            return
        }

        var drivingLicense: DrivingLicense? = null
        if (drivLicenseId != null) {
            drivingLicense = DrivingLicense(
                drivLicenseId,
                DateTimeUtils.convertToServerDate(app, drivLicenseIssueDate),
                DateTimeUtils.convertToServerDate(app, drivLicenseExpDate),
                drivingLicenseCateg
            )
        }

        var identityDocument: IdentityDocument? = null
        if (docType != null) {
            identityDocument =
                IdentityDocument(
                    number,
                    DocumentType(docType, docID),
                    series,
                    DateTimeUtils.convertToServerDate(app, expirationDate)
                )
        }

        val addProfileDataRequest = AddProfileDataRequest(
            firstName,
            lastname,
            occupationCorIsco08,
            address,
            identityDocument,
            cnp,
            phone,
            phoneCountryCode,
            drivingLicense,
            employerName,
            DateTimeUtils.convertToServerDate(app, dateOfBirth),
            null,
            email
        )

        api.editProfile(addProfileDataRequest)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                getProfileData()
                uiEventStream.value = UiEvent.NavBack
            }, {

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
                if (attachType == "DRIVING_LICENSE") {
                    uiEventStream.value = UiEvent.ShowToast(R.string.dlUpload_success)
                } else {
                    uiEventStream.value = UiEvent.ShowToast(R.string.idUpload_success)
                }
                actionStream.value = it.data?.let { it1 -> ACTION.OnUploadSuccess(attachType, it1) }
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

    fun hideKeyboard() {
        uiEventStream.value = UiEvent.HideKeyboard
    }

    private fun getProfileData() {
        api.getProfile()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.success && it.data != null) {
                    MainActivity.profileItem = it.data
                    MainActivity.activeUser = MainActivity.profileItem?.firstName ?: " "
                    MainActivity.activeId = MainActivity.profileItem?.id
                }
            }, {

            })

    }

}