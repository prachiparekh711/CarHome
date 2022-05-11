package ro.westaco.carhome.presentation.screens.home.purchases

import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.responses.models.PurchaseCategory
import ro.westaco.carhome.presentation.base.BaseViewModel
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject


@HiltViewModel
class PurchasesViewModel @Inject constructor(
    private val api: CarHomeApi
) : BaseViewModel() {
    val purchasesWithCategoriesLiveData = MutableLiveData<ArrayList<PurchaseCategory>>()

    override fun onFragmentCreated() {
        fetchRemoteData()
    }

    private fun fetchRemoteData() {
        api.getPurchases()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                purchasesWithCategoriesLiveData.value = resp?.data
            }, {
                //   it.printStackTrace()
            })
    }
}