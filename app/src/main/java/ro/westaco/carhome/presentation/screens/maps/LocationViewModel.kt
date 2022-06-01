package ro.westaco.carhome.presentation.screens.maps

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.responses.ApiResponse
import ro.westaco.carhome.data.sources.remote.responses.models.LocationV2Data
import ro.westaco.carhome.data.sources.remote.responses.models.LocationV2Item
import ro.westaco.carhome.data.sources.remote.responses.models.SectionModel
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.SingleLiveEvent
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import javax.inject.Inject


@HiltViewModel
class LocationViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {

    val filterDataMaps = MutableLiveData<List<SectionModel>>()
    val resultData = MutableLiveData<LocationV2Data>()
    val nearbyLocationData = MutableLiveData<ArrayList<LocationV2Item>>()
    val nearbyLocationsFiltered = MutableLiveData<ArrayList<LocationV2Item>>()
    val dialogResultData: MutableLiveData<LocationV2Item> = MutableLiveData<LocationV2Item>()
    val selectedItemsLiveData: SingleLiveEvent<ArrayList<SectionModel>> = SingleLiveEvent()

    fun getSelectedItems(): SingleLiveEvent<ArrayList<SectionModel>> {
        return selectedItemsLiveData
    }

    fun getLocationFilter() {
        api.locationFilterMaps()
            .enqueue(object : Callback<ApiResponse<List<SectionModel>>> {
                override fun onFailure(
                    call: Call<ApiResponse<List<SectionModel>>>,
                    t: Throwable
                ) {
                }

                override fun onResponse(
                    call: Call<ApiResponse<List<SectionModel>>>,
                    response: Response<ApiResponse<List<SectionModel>>>
                ) {
                    if (response.code() in 200..299)
                        filterDataMaps.value = response.body()?.data!!
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

    fun getLocationDataFiltered(
        currentLat: String,
        currentLong: String,
        searchText: String?,
        filters: ArrayList<Int>
    ) {
        api.getLocationsFiltered(currentLat, currentLong, searchText, filters)
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
                    nearbyLocationsFiltered.value =
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
                    dialogResultData.value = response.body()?.data
                }
            })
        return dialogResultData
    }

    internal fun onProfileClicked() {
        uiEventStream.value = UiEvent.Navigation(NavAttribs(Screen.Profile))
    }


}