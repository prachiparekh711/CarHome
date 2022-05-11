package ro.westaco.carhome.presentation.screens.maps

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.responses.ApiResponse
import ro.westaco.carhome.data.sources.remote.responses.models.LocationFilterItem
import ro.westaco.carhome.data.sources.remote.responses.models.LocationV2Data
import ro.westaco.carhome.data.sources.remote.responses.models.LocationV2Item
import ro.westaco.carhome.di.ApiModule.Companion.BASE_URL_RESOURCES
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.utils.FirebaseAnalyticsList
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File
import javax.inject.Inject


@HiltViewModel
class LocationViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {

    val filterData = MutableLiveData<List<LocationFilterItem>>()
    val resultData = MutableLiveData<LocationV2Data>()
    val nearbyLocationData = MutableLiveData<ArrayList<LocationV2Item>>()
    var profileLogoData: MutableLiveData<Bitmap>? = MutableLiveData()
    val dialogresultData: MutableLiveData<LocationV2Item> = MutableLiveData<LocationV2Item>()
    var mFirebaseAnalytics = FirebaseAnalytics.getInstance(app)

    override fun onFragmentCreated() {
        super.onFragmentCreated()
        fetchProfileData()
    }

    fun accessLocation() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.ACCESS_LOCATION_ANDROID, params)
    }

    fun getLocationFilter() {

        val url = "$BASE_URL_RESOURCES/locations/rest/getLocationFilter"

        api.locationFilter()
            .enqueue(object : Callback<ApiResponse<List<LocationFilterItem>>> {
                override fun onFailure(
                    call: Call<ApiResponse<List<LocationFilterItem>>>,
                    t: Throwable
                ) {
                }

                override fun onResponse(
                    call: Call<ApiResponse<List<LocationFilterItem>>>,
                    response: Response<ApiResponse<List<LocationFilterItem>>>
                ) {
                    filterData.value = response.body()?.data
                }
            })

    }

    fun getLocationData(currentLat: String, currentLong: String) {

        api.getLocation(currentLat, currentLong)
            .enqueue(object : Callback<LocationV2Data> {
                override fun onFailure(
                    call: Call<LocationV2Data>,
                    t: Throwable
                ) {
                }

                override fun onResponse(
                    call: Call<LocationV2Data>,
                    response: Response<LocationV2Data>
                ) {
                    resultData.value = response.body()
                    nearbyLocationData.value =
                        resultData.value?.data?.locations as ArrayList<LocationV2Item>?
                }
            })
    }

    fun getCurrentLocationData(id: Int): LiveData<LocationV2Item> {

        api.getCurrentLocation(id)
            .enqueue(object : Callback<ApiResponse<LocationV2Item>> {
                override fun onFailure(call: Call<ApiResponse<LocationV2Item>>, t: Throwable) {}
                override fun onResponse(
                    call: Call<ApiResponse<LocationV2Item>>,
                    response: Response<ApiResponse<LocationV2Item>>
                ) {
                    dialogresultData.value = response.body()?.data
                }
            })
        return dialogresultData
    }

    internal fun onProfileClicked() {
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.Profile))
    }

    internal fun fetchProfileData() {
        api.getProfileLogo()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                try {
                    val byteArray = it.source().readByteArray()
                    val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                    profileLogoData?.value = bitmap
                } catch (e: Exception) {
                }
            }, {
                profileLogoData?.value = null
//                it.printStackTrace()
//                uiEventStream.value = UiEvent.ShowToast(R.string.failed_server)
            })
    }

    internal fun onAddLogo(
        logoFile: File
    ) {
        val requestFile: RequestBody =
            logoFile.asRequestBody("multipart/form-data".toMediaTypeOrNull())

        val body: MultipartBody.Part =
            MultipartBody.Part.createFormData("attachment", logoFile.name, requestFile)

        api.addProfileLogo(body)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                uiEventStream.value = UiEvent.ShowToast(R.string.logo_success_msg)
                fetchProfileData()
            }, {
                it.printStackTrace()
                uiEventStream.value =
                    UiEvent.ShowToast(R.string.server_saving_error)
            })
    }

}