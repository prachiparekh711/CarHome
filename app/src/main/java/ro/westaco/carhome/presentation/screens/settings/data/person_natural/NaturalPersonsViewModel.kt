package ro.westaco.carhome.presentation.screens.settings.data.person_natural

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.responses.models.NaturalPerson
import ro.westaco.carhome.navigation.BundleProvider
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.settings.data.person_natural.add_new.AddNewNaturalPersonFragment
import ro.westaco.carhome.presentation.screens.settings.data.person_natural.details.NaturalPersonDetailsFragment
import ro.westaco.carhome.utils.DeviceUtils
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject


@HiltViewModel
class NaturalPersonsViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {

    val naturalPersonsLiveData = MutableLiveData<ArrayList<NaturalPerson>>()

    override fun onFragmentCreated() {
        fetchRemoteData()
    }

    private fun fetchRemoteData() {
        api.getNaturalPersons()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                naturalPersonsLiveData.value = resp?.data
            }, {
                //   it.printStackTrace()
            })
    }

    /*
    ** User Interaction
    */
    internal fun onItemClick(item: NaturalPerson) {
        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.NaturalPersonDetails, object : BundleProvider() {
                override fun onAddArgs(bundle: Bundle?): Bundle {
                    return Bundle().apply {
                        putSerializable(NaturalPersonDetailsFragment.ARG_NATURAL_PERSON, item)
                    }
                }
            }))
    }

    internal fun onItemEdit(item: NaturalPerson) {
        if (!DeviceUtils.isOnline(app)) {
            uiEventStream.value = UiEvent.ShowToast(R.string.int_not_connect)
            return
        }

        item.id?.let {
            api.getNaturalPerson(it)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({ resp ->
                    uiEventStream.value = UiEvent.Navigation(
                        NavAttribs(
                            Screen.AddNaturalPerson,
                            object : BundleProvider() {
                                override fun onAddArgs(bundle: Bundle?): Bundle {
                                    return Bundle().apply {

                                        putSerializable(
                                            AddNewNaturalPersonFragment.ARG_IS_EDIT,
                                            true
                                        )
                                        putSerializable(
                                            AddNewNaturalPersonFragment.ARG_NATURAL_PERSON,
                                            resp.data
                                        )
                                    }
                                }
                            })
                    )
                }, {
                    //   it.printStackTrace()
                })
        }
    }

    internal fun onAddNew() {
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.AddNaturalPerson))
    }
}