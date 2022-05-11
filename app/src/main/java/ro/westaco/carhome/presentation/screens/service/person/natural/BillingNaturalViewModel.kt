package ro.westaco.carhome.presentation.screens.service.person.natural

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.responses.models.NaturalPerson
import ro.westaco.carhome.data.sources.remote.responses.models.NaturalPersonDetails
import ro.westaco.carhome.navigation.BundleProvider
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.settings.data.person_natural.add_new.AddNewNaturalPersonFragment
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class BillingNaturalViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {

    val naturalPersonsLiveData = MutableLiveData<ArrayList<NaturalPerson>>()
    val naturalPersDetailsLiveData: MutableLiveData<NaturalPersonDetails?> = MutableLiveData()
    var naturalLogoData: MutableLiveData<Bitmap>? = MutableLiveData()


    override fun onFragmentCreated() {
        fetchRemoteData()
    }

    private fun fetchRemoteData() {
        api.getNaturalPersons()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp -> naturalPersonsLiveData.value = resp?.data }, {})
    }


    internal fun onAddNew() {
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.AddBillNaturalPerson))
    }

    internal fun onNaturalPerson(id: Int?) {
        if (id != null) {
            api.getNaturalPerson(id.toLong())
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({ resp ->
                    naturalPersDetailsLiveData.value = resp?.data
                }, {
                    naturalPersDetailsLiveData.value = null
//                    //   it.printStackTrace()
                })

            api.getPersonLogo(id.toLong())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                    try {
                        val byteArray = it.source().readByteArray()
                        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                        naturalLogoData?.value = bitmap
                    } catch (e: Exception) {
                    }
                }, {
                    naturalLogoData?.value = null
//                //   it.printStackTrace()
//                uiEventStream.value = UiEvent.ShowToast(R.string.failed_server)
                })
        }
    }

    internal fun onEdit() {
        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.AddNaturalPerson, object : BundleProvider() {
                override fun onAddArgs(bundle: Bundle?): Bundle {
                    return Bundle().apply {
                        putSerializable(AddNewNaturalPersonFragment.ARG_IS_EDIT, true)
                        putSerializable(
                            AddNewNaturalPersonFragment.ARG_NATURAL_PERSON,
                            naturalPersDetailsLiveData.value
                        )
                    }
                }
            }))
    }


}