package ro.westaco.carhome.presentation.screens.signup_methods.profile_progress

import android.app.Application
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.responses.models.ProgressItem
import ro.westaco.carhome.presentation.base.BaseViewModel
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject


@HiltViewModel
class ProfileProgressModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {
    var progressData: MutableLiveData<ProgressItem> = MutableLiveData()

    override fun onActivityCreated() {
        super.onActivityCreated()
        fetchRemoteData()
    }

    private fun fetchRemoteData() {

        api.getProgress()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.success && it.data != null) {
                    progressData.value = it.data
                }
            }, {
                it.printStackTrace()
                progressData.value = null
            })

    }
}