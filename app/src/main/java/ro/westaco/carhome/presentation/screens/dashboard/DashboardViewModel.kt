package ro.westaco.carhome.presentation.screens.dashboard

import android.app.Application
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.R
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
import ro.westaco.carhome.presentation.screens.maps.MapsFragment
import ro.westaco.carhome.presentation.screens.reminder.ReminderFragment
import ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.select_car.SelectCarFragment
import ro.westaco.carhome.presentation.screens.settings.SettingsFragment
import ro.westaco.carhome.utils.default
import javax.inject.Inject


@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val app: Application
) : BaseViewModel() {

    var servicesStateLiveData = MutableLiveData<STATE>().default(STATE.Collapsed)

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
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.Insurance))
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
                    actionStream.value = ACTION.OpenChildFragment(MapsFragment(), MapsFragment.TAG)
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