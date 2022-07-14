package ro.westaco.carhome.presentation.screens.settings.contact_us

import android.app.Application
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.responses.models.Categories
import ro.westaco.carhome.navigation.SingleLiveEvent
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.utils.DateTimeUtils
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File
import javax.inject.Inject


@HiltViewModel
class ContactViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {

    var reasonLiveData: MutableLiveData<ArrayList<Categories>>? = MutableLiveData()

    override fun onFragmentCreated() {
        super.onFragmentCreated()
        fetchReasonsData()
    }

    val actionStream: SingleLiveEvent<ACTION> = SingleLiveEvent()

    sealed class ACTION {
        object OnSubmit : ACTION()
    }


    private fun fetchReasonsData() {

        api.getContactReasons()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.success && it.data != null) {
                    reasonLiveData?.value = it.data
                }
            }, {
                it.printStackTrace()
            })
    }

    internal fun onBack() {
        uiEventStream.value = UiEvent.NavBack
    }

    internal fun onMain() {
        uiEventStream.value = UiEvent.GoToMain
    }

    internal fun convertFromServerDate(date: String?) =
        DateTimeUtils.convertFromServerDate(app, date)


    internal fun onSubmit(
        reasonId: Int,
        message: String,
        attachmentFileList: ArrayList<File>?
    ) {


        val attachmentBodyList: ArrayList<MultipartBody.Part> = ArrayList()
        if (attachmentFileList != null) {

            for (i in 0 until attachmentFileList.size) {
                val requestList: RequestBody =
                    attachmentFileList[i].asRequestBody("multipart/form-data".toMediaTypeOrNull())

                val attachmentBody =
                    MultipartBody.Part.createFormData(
                        "attachments",
                        attachmentFileList[i].name,
                        requestList
                    )

                attachmentBodyList.add(attachmentBody)
            }

        }

        val reasonBody: RequestBody =
            reasonId.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val messageBody: RequestBody =
            message.toRequestBody("multipart/form-data".toMediaTypeOrNull())

        api.submitForm(reasonBody, messageBody, attachmentBodyList)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                actionStream.value = ACTION.OnSubmit

            }, {

            })
    }

}