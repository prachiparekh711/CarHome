package ro.westaco.carhome.presentation.screens.main

import android.app.Application
import android.os.Bundle
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.responses.models.ProfileItem
import ro.westaco.carhome.navigation.BundleProvider
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.dashboard.profile.edit.EditProfileFragment
import ro.westaco.carhome.presentation.screens.main.MainActivity.Companion.activeId
import ro.westaco.carhome.presentation.screens.main.MainActivity.Companion.activeUser
import ro.westaco.carhome.presentation.screens.settings.data.cars.details.CarDetailsFragment
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val api: CarHomeApi,
    private val app: Application
) : BaseViewModel() {


    var profileItem: ProfileItem? = null

    override fun onActivityCreated() {


        api.getProfile()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.success && it.data != null) {
                    profileItem = it.data
                    activeUser = profileItem?.firstName ?: " "
                    activeId = profileItem?.id
                }
            }, {

            })

        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.Dashboard, null, true))
    }

    internal fun onEditAccount() {
        if (profileItem != null) {
            uiEventStream.value =
                UiEvent.Navigation(
                    NavAttribs(
                        Screen.EditAccount,
                        object : BundleProvider() {
                            override fun onAddArgs(bundle: Bundle?): Bundle {
                                return Bundle().apply {
                                    putSerializable(
                                        EditProfileFragment.ARG_PROFILE,
                                        profileItem
                                    )
                                }
                            }
                        })
                )
        }

    }

    internal fun onAddNewCar() {
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.QueryCar))
    }

    internal fun onEditCar(itemID: Int) {
        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.CarDetails, object : BundleProvider() {
                override fun onAddArgs(bundle: Bundle?): Bundle {
                    return Bundle().apply {
                        putInt(CarDetailsFragment.ARG_CAR_ID, itemID)
                    }
                }
            }))
    }
}