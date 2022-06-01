package ro.westaco.carhome.presentation.screens.documents

import android.app.Application
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.requests.DocumentCategoryRequest
import ro.westaco.carhome.data.sources.remote.responses.models.Categories
import ro.westaco.carhome.data.sources.remote.responses.models.Documents
import ro.westaco.carhome.data.sources.remote.responses.models.RowsItem
import ro.westaco.carhome.navigation.SingleLiveEvent
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.utils.DeviceUtils
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File
import javax.inject.Inject


@HiltViewModel
class DocumentsViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {
    val actionStream: SingleLiveEvent<ACTION> = SingleLiveEvent()

    sealed class ACTION {

        object OnBackOfSuccess : ACTION()
    }

    internal fun onBack() {
        uiEventStream.value = UiEvent.NavBack
    }

    internal fun onMain() {
        uiEventStream.value = UiEvent.GoToMain
    }

    val categoriesLiveData = MutableLiveData<ArrayList<Categories>?>()
    val documentsLiveData = MutableLiveData<Documents?>()
    val categoryDetailData = MutableLiveData<Categories?>()
    val documentsDetailData = MutableLiveData<RowsItem?>()

    override fun onFragmentCreated() {
        fetchCategories(null)
    }

    fun fetchCategories(parentID: Int?) {
        if (!DeviceUtils.isOnline(app)) {
            uiEventStream.value = UiEvent.ShowToast(R.string.int_not_connect)
            return
        }


        api.getDocumentCategories(parentID)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                categoriesLiveData.value = resp?.data
            }, {

            })
    }

    fun fetchDocuments(categoryId: Int) {

        api.getDocuments(categoryId)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                documentsLiveData.value = resp?.data
            }, {

            })
    }

    fun addCategory(parentId: Int?, name: String) {

        val request = Categories(name, null, parentId)
        api.addCategory(request)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                fetchCategories(parentId)
            }, {
                uiEventStream.value = UiEvent.ShowToast(R.string.general_server_error)
            })
    }


    internal fun addDocument(
        categoryId: Int,
        fileName: String,
        mergeAs: String?,
        attachment: ArrayList<File>
    ) {
        if (!DeviceUtils.isOnline(app)) {
            uiEventStream.value = UiEvent.ShowToast(R.string.int_not_connect)
            return
        }

        val attachmentBodyList: ArrayList<MultipartBody.Part> = ArrayList()
        var type: String? = null
        when (mergeAs) {
            "IMG", "PDF" ->
                type = "image/*"
            null -> type = "multipart/form-data"
        }
        for (i in 0 until attachment.size) {
            val requestList: RequestBody =
                attachment[i].asRequestBody(type?.toMediaTypeOrNull())

            val attachmentBody =
                MultipartBody.Part.createFormData(
                    "attachment",
                    attachment[i].name,
                    requestList
                )

            attachmentBodyList.add(attachmentBody)
        }

        val catIDBody: RequestBody =
            categoryId.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val fileNameBody: RequestBody =
            fileName.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val mergeAsBody: RequestBody? =
            mergeAs?.toRequestBody("multipart/form-data".toMediaTypeOrNull())

        api.addDocument(catIDBody, fileNameBody, mergeAsBody, attachmentBodyList)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                actionStream.value = ACTION.OnBackOfSuccess
            }, {
                uiEventStream.value = UiEvent.ShowToast(R.string.general_server_error)
            })
    }

    fun editDocument(parentId: Int, name: String, id: Int) {
        if (!DeviceUtils.isOnline(app)) {
            uiEventStream.value = UiEvent.ShowToast(R.string.int_not_connect)
            return
        }


        val nameAsBody: RequestBody =
            name.toRequestBody("text/plain".toMediaTypeOrNull())

        api.editDocument(id.toString(), nameAsBody)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                fetchDocuments(parentId)
            }, {
                uiEventStream.value = UiEvent.ShowToast(R.string.general_server_error)
            })
    }

    fun editCategory(parentId: Int?, name: String, id: Int) {

        val request = Categories(name, id, parentId)
        api.editCategory(id.toString(), request)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                fetchCategories(parentId)
            }, {
                uiEventStream.value = UiEvent.ShowToast(R.string.general_server_error)
            })
    }


    fun deleteDocument(parentId: Int, id: Int) {

        api.deleteDocument(id.toString())
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                fetchDocuments(parentId)
            }, {
                uiEventStream.value = UiEvent.ShowToast(R.string.general_server_error)
            })
    }

    fun deleteCategory(parentId: Int?, id: Int) {


        api.deleteCategory(id.toString())
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                fetchCategories(parentId)
            }, {
                uiEventStream.value = UiEvent.ShowToast(R.string.general_server_error)
            })
    }


    fun deleteDocumentandCategory(
        parentId: Int?,
        docList: ArrayList<Int>?,
        catList: ArrayList<Int>?
    ) {


        val req = DocumentCategoryRequest(catList, null, docList)
        api.deleteDocumentandCategory(req)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                fetchCategories(parentId)

                if (parentId != null) {
                    fetchDocuments(parentId)
                }
            }, {
                uiEventStream.value = UiEvent.ShowToast(R.string.general_server_error)
            })
    }

    fun getCategoryDetail(id: Int) {


        api.getCategoryDetail(id.toString())
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                categoryDetailData.value = resp?.data
            }, {
            })
    }

    fun getDocumentDetail(id: Int) {


        api.getDocumentDetail(id.toString())
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                documentsDetailData.value = resp.data
            }, {
            })
    }

}