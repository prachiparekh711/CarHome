package ro.westaco.carhome.presentation.screens.documents

import android.app.Application
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.requests.DocumentCategoryRequest
import ro.westaco.carhome.data.sources.remote.responses.models.Categories
import ro.westaco.carhome.navigation.SingleLiveEvent
import ro.westaco.carhome.presentation.base.BaseViewModel
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject


@HiltViewModel
class DocumentOperationViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {

    val actionStream: SingleLiveEvent<ACTION> = SingleLiveEvent()

    sealed class ACTION {

        object OnBackOfSuccess : ACTION()
    }

    val categoriesLiveData = MutableLiveData<ArrayList<Categories>?>()

    override fun onFragmentCreated() {
        fetchCategories(null)
    }

    fun fetchCategories(parentID: Int?) {

        api.getDocumentCategories(parentID)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                categoriesLiveData.value = resp?.data
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
            })
    }

    fun copyDocumentandCategory(parentId: Int, docList: ArrayList<Int>?, catList: ArrayList<Int>?) {


        val req = DocumentCategoryRequest(catList, parentId, docList)
        api.copyDocumentandCategory(req)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                actionStream.value = ACTION.OnBackOfSuccess
            }, {
            })
    }

    fun moveDocumentandCategory(parentId: Int, docList: ArrayList<Int>?, catList: ArrayList<Int>?) {


        val req = DocumentCategoryRequest(catList, parentId, docList)
        api.moveDocumentandCategory(req)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                actionStream.value = ACTION.OnBackOfSuccess
            }, {
            })
    }

}
