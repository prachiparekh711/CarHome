package ro.westaco.carhome.presentation.screens.data.person_natural

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.responses.models.NaturalPerson
import ro.westaco.carhome.navigation.BundleProvider
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.data.person_natural.add_new.AddNewNaturalPersonFragment
import ro.westaco.carhome.presentation.screens.data.person_natural.details.NaturalPersonDetailsFragment
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

    @SuppressLint("NullSafeMutableLiveData")
    private fun fetchRemoteData() {
        api.getNaturalPersons()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                naturalPersonsLiveData.value = resp?.data
            }, {
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
                        putBoolean(NaturalPersonDetailsFragment.ARG_MENU, true)
                    }
                }
            }))
    }

    internal fun onItemEdit(item: NaturalPerson) {

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

    internal fun openDialPad(item: NaturalPerson) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:${item.phone}")
        intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        uiEventStream.postValue(
            UiEvent.OpenIntent(
                intent,
                false
            )
        )
    }

    internal fun openComposedMail(item: NaturalPerson) {
        val intent = Intent(
            Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", item.email, null
            )
        )
        intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        uiEventStream.postValue(
            UiEvent.OpenIntent(
                intent,
                false
            )
        )
    }
}