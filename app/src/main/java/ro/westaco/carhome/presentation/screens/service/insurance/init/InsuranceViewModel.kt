package ro.westaco.carhome.presentation.screens.service.insurance.init

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.requests.RcaOfferRequest
import ro.westaco.carhome.data.sources.remote.responses.models.*
import ro.westaco.carhome.navigation.BundleProvider
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.SingleLiveEvent
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class InsuranceViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi,

    ) : BaseViewModel() {

    val currentRcaData = MutableLiveData<RcaResponse?>()
    val carsLivedata = MutableLiveData<ArrayList<Vehicle>?>()
    val leasingCompaniesData = MutableLiveData<ArrayList<LeasingCompany>?>()
    val vehicleDetailsLivedata: MutableLiveData<VehicleDetailsForOffer> = MutableLiveData()
    var verifyNaturalPerson = MutableLiveData<ArrayList<VerifyRcaPerson>?>()
    var verifyLegalPerson = MutableLiveData<ArrayList<VerifyRcaPerson>?>()

    val actionStream: SingleLiveEvent<ACTION> = SingleLiveEvent()

    sealed class ACTION {
        class OnGetNaturalDetails(val personType: String, val item: NaturalPersonForOffer) :
            ACTION()

        class OnGetLegalDetails(val personType: String, val item: LegalPersonDetails) : ACTION()
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

    override fun onFragmentCreated() {
        super.onFragmentCreated()
        fetchDefaultData()
    }


    fun fetchDefaultData() {
        api.getVehicles()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                carsLivedata.value = resp?.data
            }, {
            })

        api.getLeasingCompanies("ROU")
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                leasingCompaniesData.value = resp?.data
            }, {
                //   it.printStackTrace()
            })
    }


    fun identifyVehicle(selectedVehicle: Vehicle) {

        selectedVehicle.guid?.let {
            api.identifyVehicle(it)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({ resp ->
                    currentRcaData.value = resp?.data
                }, {
                })
        }
    }

    fun fetchCarDetails(vehicleId: Int) {
        api.getVehicleForOffer(vehicleId)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->

                vehicleDetailsLivedata.value = resp.data
            }, {
                it.printStackTrace()
            })

    }


    internal fun onCta(
        request: RcaOfferRequest,
        vehicle: Vehicle,
    ) {


        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.InsuranceStep2, object : BundleProvider() {
                override fun onAddArgs(bundle: Bundle?): Bundle {
                    return Bundle().apply {
                        putSerializable(InsuranceStep2Fragment.ARG_REQUEST, request)
                        putSerializable(InsuranceStep2Fragment.ARG_CAR, vehicle)
                    }
                }
            }))
    }

    fun verifyNaturalPerson(personRole: String) {
        api.verifyNaturalPersonForRCA(personRole)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                verifyNaturalPerson.value = resp.data
            }, {

                //   it.printStackTrace()
            })
    }

    fun verifyLegalPerson(legalPerson: String) {
        api.verifyLegalPersonForRCA(legalPerson)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                verifyLegalPerson.value = resp.data
            }, {

                //   it.printStackTrace()
            })
    }

    fun getNaturalPersonDetails(naturalId: Long, personType: String) {

        api.getNaturalPersonOffer(naturalId)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                actionStream.value =
                    response.data?.let { ACTION.OnGetNaturalDetails(personType, it) }
            }, {
                //   it.printStackTrace()
            })
    }

    fun getLegalPersonDetails(legalId: Long, personType: String) {

        api.getLegalPersonDetails(legalId)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                actionStream.value = response.data?.let { ACTION.OnGetLegalDetails(personType, it) }
            }, {
                //   it.printStackTrace()
            })
    }

}