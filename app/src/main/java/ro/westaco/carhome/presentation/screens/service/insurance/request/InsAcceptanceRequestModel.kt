package ro.westaco.carhome.presentation.screens.service.insurance.request

import android.app.Application
import android.os.Bundle
import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.requests.TermsRequestItem
import ro.westaco.carhome.data.sources.remote.responses.models.RcaCarIdentifyResponse
import ro.westaco.carhome.data.sources.remote.responses.models.TermsResponseItem
import ro.westaco.carhome.data.sources.remote.responses.models.Vehicle
import ro.westaco.carhome.navigation.BundleProvider
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.SingleLiveEvent
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.service.insurance.car_selection.InsCarEditFragment
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class InsAcceptanceRequestModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi,
) : BaseViewModel() {

    private val validationLivedata = MutableLiveData<RcaCarIdentifyResponse?>()
    var termsLiveData = MutableLiveData<ArrayList<TermsResponseItem>?>()

    val actionStream: SingleLiveEvent<ACTION> = SingleLiveEvent()

    sealed class ACTION {
        object TermsSuccess : ACTION()
        class GetTerms(val termRequired: Boolean) : ACTION()
    }

    override fun onFragmentCreated() {
        super.onFragmentCreated()
        checkTermsForCurrentUser()
    }

    private fun checkTermsForCurrentUser() {
        api.getAllTermsForCurrentUserAndScope("USE_WBA")
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                if (resp.data?.isNotEmpty() == true) {
                    getWBATerms()
                    actionStream.value = ACTION.GetTerms(termRequired = true)
                } else {
                    actionStream.value = ACTION.GetTerms(termRequired = false)
                }

            }) {
                actionStream.value = ACTION.GetTerms(termRequired = false)
            }
    }

    private fun getWBATerms() {
        api.getAllTermsForScope("USE_WBA")
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                termsLiveData.value = resp.data
            }) {
            }
    }

    fun saveTerms(termsResponseList: ArrayList<TermsResponseItem>?) {

        if (termsResponseList?.isNotEmpty() == true) {
            for (i in termsResponseList.indices) {
                if (!termsResponseList[i].allowed && termsResponseList[i].mandatory == true) {
                    uiEventStream.value = UiEvent.ShowToast(R.string.terms_info)
                    return
                }
            }
        }

        val requestList: ArrayList<TermsRequestItem> = ArrayList()
        for (i in termsResponseList?.indices!!) {
            val item =
                TermsRequestItem(
                    termsResponseList[i].versionId,
                    termsResponseList[i].allowed
                )
            requestList.add(item)
        }

        api.saveUserTermResolutions(requestList)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                actionStream.value = ACTION.TermsSuccess
            }, {
            })

    }

    internal fun onBack() {
        uiEventStream.value = UiEvent.NavBack
    }

    internal fun onMain() {
        uiEventStream.value = UiEvent.GoToMain
    }

    internal fun onStart() {
        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.SelectInsuranceCar, addToBackStack = false))
    }

    internal fun identifyVehicle(vehicle: Vehicle?) {

        vehicle?.guid?.let {
            api.identifyVehicle(it)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({ resp ->
                    validationLivedata.value = resp?.data

                    uiEventStream.value = UiEvent.Navigation(
                        NavAttribs(
                            Screen.InsuranceEditCar,
                            object : BundleProvider() {
                                override fun onAddArgs(bundle: Bundle?): Bundle {
                                    return Bundle().apply {
                                        putParcelableArrayList(
                                            InsCarEditFragment.ARG_CAR_WARNING,
                                            resp?.data?.warnings as java.util.ArrayList<out Parcelable>
                                        )

                                        putBoolean(
                                            InsCarEditFragment.ARG_IS_EDIT,
                                            true
                                        )

                                        vehicle.id?.let { it1 ->
                                            putInt(
                                                InsCarEditFragment.ARG_CAR_ID,
                                                it1
                                            )
                                        }
                                    }
                                }
                            }, false
                        )
                    )
                }, {
                })
        }

    }

}