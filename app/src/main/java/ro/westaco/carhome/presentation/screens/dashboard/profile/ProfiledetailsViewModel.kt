package ro.westaco.carhome.presentation.screens.dashboard.profile

import android.app.Application
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
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem
import ro.westaco.carhome.data.sources.remote.responses.models.Country
import ro.westaco.carhome.data.sources.remote.responses.models.ProfileItem
import ro.westaco.carhome.navigation.BundleProvider
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.SingleLiveEvent
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.dashboard.profile.edit.EditProfileFragment
import ro.westaco.carhome.utils.DateTimeUtils
import ro.westaco.carhome.utils.DeviceUtils
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File
import javax.inject.Inject


@HiltViewModel
class ProfileDetailsViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {

    var profileLiveData: MutableLiveData<ProfileItem>? = MutableLiveData()
    var profileLogoData: MutableLiveData<Bitmap>? = MutableLiveData()
    val actionStream: SingleLiveEvent<ACTION> = SingleLiveEvent()

    var countryData = MutableLiveData<ArrayList<Country>?>()
    var licenseCategoryData = MutableLiveData<ArrayList<CatalogItem>?>()

    sealed class ACTION {
        class OnDeleteSuccess(val attachType: String) : ACTION()
        object OnDeleteLogo : ACTION()
    }

    override fun onFragmentCreated() {
        super.onFragmentCreated()
        fetchRemoteData()
        fetchProfileData()

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

        api.getSimpleCatalog("NOM_DRIVING_LICENSE_CATEGORY_TYPE")
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                licenseCategoryData.value = it.data
            }, {

            })
    }


    private fun fetchRemoteData() {
        if (!DeviceUtils.isOnline(app)) {
            uiEventStream.value = UiEvent.ShowToast(R.string.int_not_connect)
            return
        }
        api.getProfile()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.success && it.data != null) {
                    profileLiveData?.value = it.data
                    fetchDefaultData()
                }
            }, {
                it.printStackTrace()
                profileLiveData?.value = null
            })
    }

    private fun fetchProfileData() {
        api.getProfileLogo()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                try {
                    val byteArray = it.source().readByteArray()
                    val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                    profileLogoData?.value = bitmap
                } catch (e: Exception) {
                }
            }, {
                profileLogoData?.value = null
            })
    }


    internal fun onCloseAccount() {
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.CloseAccount))
    }

    internal fun onBack() {
        uiEventStream.value = UiEvent.NavBack
    }

    internal fun onMain() {
        uiEventStream.value = UiEvent.GoToMain
    }

    internal fun onEditAccount(profileItem: ProfileItem) {
        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.EditAccount, object : BundleProvider() {
                override fun onAddArgs(bundle: Bundle?): Bundle {
                    return Bundle().apply {
                        putSerializable(EditProfileFragment.ARG_PROFILE, profileItem)
                    }
                }
            }))
    }

    internal fun convertFromServerDate(date: String?) =
        DateTimeUtils.convertFromServerDate(app, date)

    internal fun onAddLogo(
        logoFile: File,
    ) {

        val requestFile: RequestBody =
            logoFile.asRequestBody("multipart/form-data".toMediaTypeOrNull())

        val body: MultipartBody.Part =
            MultipartBody.Part.createFormData("attachment", logoFile.name, requestFile)

        api.addProfileLogo(body)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                fetchProfileData()
            }, {
                it.printStackTrace()
                uiEventStream.value =
                    UiEvent.ShowToast(R.string.server_saving_error)
            })
    }

    internal fun deleteLogo() {
        api.deleteProfileLogo()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                actionStream.value = ACTION.OnDeleteLogo
                fetchProfileData()
            }, {
                uiEventStream.value = UiEvent.ShowToast(R.string.general_server_error)
            })
    }

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
                if (attachType == "DRIVING_LICENSE") {
                    uiEventStream.value = UiEvent.ShowToast(R.string.dlUpload_success)
                } else {
                    uiEventStream.value = UiEvent.ShowToast(R.string.idUpload_success)
                }
                fetchRemoteData()
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
                    actionStream.value = ACTION.OnDeleteSuccess("DRIVING_LICENSE")
                } else {
                    actionStream.value = ACTION.OnDeleteSuccess("IDENTITY_DOCUMENT")
                }
                fetchRemoteData()
            }, {
                uiEventStream.value = UiEvent.ShowToast(R.string.general_server_error)
            })
    }



}