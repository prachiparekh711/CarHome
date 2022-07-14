package ro.westaco.carhome.presentation.screens.dashboard

import android.app.Application
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.requests.TermsRequestItem
import ro.westaco.carhome.data.sources.remote.responses.models.TermsResponseItem
import ro.westaco.carhome.navigation.BundleProvider
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.SingleLiveEvent
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.dashboard.DashboardFragment.Companion.CAR_MODE
import ro.westaco.carhome.presentation.screens.data.DataFragment
import ro.westaco.carhome.presentation.screens.driving_mode.DrivingModeFragment
import ro.westaco.carhome.presentation.screens.home.HomeFragment
import ro.westaco.carhome.presentation.screens.maps.TopServicesMapFragment
import ro.westaco.carhome.presentation.screens.reminder.ReminderFragment
import ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.select_car.SelectCarFragment
import ro.westaco.carhome.presentation.screens.settings.SettingsFragment
import ro.westaco.carhome.utils.default
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject


@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {

    var servicesStateLiveData = MutableLiveData<STATE>().default(STATE.Collapsed)
    var termsLiveData = MutableLiveData<ArrayList<TermsResponseItem>?>()

    sealed class STATE {
        object Collapsed : STATE()
        object Expanded : STATE()
    }

    companion object {
        var selectedMenuItem: MenuItem? = null
        var serviceExpanded = false
    }

    val actionStream: SingleLiveEvent<ACTION> = SingleLiveEvent()

    sealed class ACTION {
        class OpenChildFragment(val fragment: Fragment, val tag: String?) : ACTION()
        class CheckMenuItem(val menuItem: MenuItem?) : ACTION()
    }

    override fun onFragmentCreated() {
        super.onFragmentCreated()
        checkTermsForCurrentUser()

        if (selectedMenuItem != null) {
            selectedMenuItem.let {
                if (it != null) {
                    onItemSelected(menuItem = it)
                }
            }
        } else {
            if (CAR_MODE == "DRIVING") {
                actionStream.value =
                    ACTION.OpenChildFragment(DrivingModeFragment(), DrivingModeFragment.TAG)
            } else {
                actionStream.value = ACTION.OpenChildFragment(HomeFragment(), HomeFragment.TAG)
            }
        }

        if (serviceExpanded) {
            serviceExpanded = false
            servicesStateLiveData.value = STATE.Expanded
        } else {
            servicesStateLiveData.value = STATE.Collapsed
        }
    }

    private fun checkTermsForCurrentUser() {
        api.getAllTermsForCurrentUserAndScope("USE_APP")
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                if (resp.data?.size != 0) {
                    getAPPTerms()
                }
            }) {
            }
    }


    private fun getAPPTerms() {
        api.getAllTermsForScope("USE_APP")
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
            }, {
            })

    }

    internal fun onCollapseServices() {
        serviceExpanded = false
        servicesStateLiveData.value = STATE.Collapsed
        actionStream.value = ACTION.CheckMenuItem(selectedMenuItem)
    }

    internal fun onServiceClicked(enter: String) {
        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.SelectCarForService, object : BundleProvider() {
                override fun onAddArgs(bundle: Bundle?): Bundle {
                    return Bundle().apply {
                        putString(SelectCarFragment.ARG_ENTER_VALUE, enter)
                    }
                }
            }))
    }

    internal fun onDataClicked(index: Int) {
        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.Data, object : BundleProvider() {
                override fun onAddArgs(bundle: Bundle?): Bundle {
                    return Bundle().apply {
                        putInt(DataFragment.INDEX, index)
                    }
                }
            }))
    }

    internal fun onNewDocument() {
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.Document))
    }

    internal fun onHistoryClicked() {
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.History))
    }


    internal fun onInsurance() {
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.InsuranceRequest))
    }

    fun onItemSelected(menuItem: MenuItem) {

        if (selectedMenuItem != menuItem) {
            when (menuItem.itemId) {
                R.id.home -> {
                    selectedMenuItem = menuItem
                    if (CAR_MODE == "DRIVING")
                        actionStream.value =
                            ACTION.OpenChildFragment(DrivingModeFragment(), DrivingModeFragment.TAG)
                    else
                        actionStream.value =
                            ACTION.OpenChildFragment(HomeFragment(), HomeFragment.TAG)
                    servicesStateLiveData.value = STATE.Collapsed
                }
                R.id.reminder -> {
                    selectedMenuItem = menuItem
                    actionStream.value =
                        ACTION.OpenChildFragment(ReminderFragment(), ReminderFragment.TAG)
                    servicesStateLiveData.value = STATE.Collapsed
                }
                R.id.services -> {
                    servicesStateLiveData.value = STATE.Expanded
                    serviceExpanded = true
                }
                R.id.maps -> {
                    selectedMenuItem = menuItem
                    actionStream.value = ACTION.OpenChildFragment(
                        TopServicesMapFragment(),
                        TopServicesMapFragment.TAG
                    )
                    servicesStateLiveData.value = STATE.Collapsed
                }
                R.id.more -> {
                    selectedMenuItem = menuItem
                    actionStream.value =
                        ACTION.OpenChildFragment(SettingsFragment(), SettingsFragment.TAG)
                    servicesStateLiveData.value = STATE.Collapsed
                }
            }
        } else {
            servicesStateLiveData.value = STATE.Collapsed
        }
    }

}