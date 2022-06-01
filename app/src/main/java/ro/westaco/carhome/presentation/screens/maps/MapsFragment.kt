package ro.westaco.carhome.presentation.screens.maps

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.analytics.FirebaseAnalytics
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_maps.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.LocationV2Item
import ro.westaco.carhome.data.sources.remote.responses.models.SectionModel
import ro.westaco.carhome.databinding.DirectionPopupBinding
import ro.westaco.carhome.databinding.LocationSelectorBinding
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.main.MainActivity
import ro.westaco.carhome.utils.Progressbar
import java.io.IOException
import java.util.*


//C- Map tab screen
@AndroidEntryPoint
class MapsFragment : BaseFragment<LocationViewModel>(), PlaceSelectionListener {

    private lateinit var mActivity: Activity
    private var dialog: Dialog? = null
    private var dialog2: Dialog? = null
    private var adapter: LocationAdapter? = null
    protected val REQUEST_CHECK_SETTINGS = 0x1
    var progressbar: Progressbar? = null
    private var client: FusedLocationProviderClient? = null
    private var latitude = 0.0
    private var longitude: Double = 0.0
    var nearbyLocationList: ArrayList<LocationV2Item> = ArrayList()
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
    private lateinit var fragment: MapFiltersBottomSheetDialog
    private var mapFilters: ArrayList<SectionModel> = ArrayList()
    private lateinit var mapFilterAdapter: MapFilterAdapter
    private var allFiltersNumber: Int = 0
    private var currentLocation = ClientLocation()
    private var searchViewText: String = ""

    inner class ClientLocation {
        var lat: Double = 0.0
        var lon: Double = 0.0
    }

    companion object {
        const val TAG = "MapsFragment"
    }

    override fun getContentView() = R.layout.fragment_maps

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Activity) {
            mActivity = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(requireActivity())

    }

    override fun getStatusBarColor() = ContextCompat.getColor(requireActivity(), R.color.white)

    override fun initUi() {
        dialog = Dialog(requireActivity())
        dialog2 = Dialog(requireActivity())
        progressbar = Progressbar(requireContext())
        mapFilterAdapter = MapFilterAdapter(requireContext(), mapFilters)
        recycler.adapter = mapFilterAdapter
        recycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        client = LocationServices.getFusedLocationProviderClient(requireActivity())
        displayLocationSettingsRequest(requireActivity())
        startLocation()

        if (MainActivity.activeUser != null) {
            mText.text = resources.getString(R.string.your_location)
        }
        placeAutoComplete()
        setFilterButton()
    }

    private fun setFilterButton() {
        openFiltersImageView.setOnClickListener {
            fragment.show(childFragmentManager, "map_filters")
        }
    }

    private fun placeAutoComplete() {

        if (!Places.isInitialized()) {
            Places.initialize(
                requireActivity().applicationContext,
                requireActivity().resources.getString(R.string.google_app_key),
                Locale.US
            )
        }

        placeLL.setOnClickListener {
            try {
                val fields = listOf(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.ADDRESS,
                    Place.Field.ADDRESS_COMPONENTS
                )
                val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(requireActivity())
                startActivityForResult(intent, 100)
            } catch (e: GooglePlayServicesRepairableException) {
                e.printStackTrace()
            } catch (e: GooglePlayServicesNotAvailableException) {
                e.printStackTrace()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            if (resultCode == Activity.RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data)
                mLiveLocation.text = place.name
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                var status = Autocomplete.getStatusFromIntent(data)
            }
        }
    }

    override fun onError(status: Status) {
    }

    override fun onPlaceSelected(place: Place) {
    }

    override fun setObservers() {
        fragment = MapFiltersBottomSheetDialog(viewModel)
        viewModel.nearbyLocationsFiltered.observe(viewLifecycleOwner) { filteredLocations ->
            if (filteredLocations != null) {
                setAdapter(filteredLocations)
                mNoData.visibility = View.GONE
            }
        }
        viewModel.getSelectedItems().observe(viewLifecycleOwner) { filters ->
            mapFilters = filters
            var isFiltered = false
            filters.forEach {
                if (it.filters.size != 0)
                    isFiltered = true
            }
            if (isFiltered) {
                mTab.visibility = View.VISIBLE
                appliedFiltersTextView.text =
                    getMapFiltersSize().toString() + "/" + allFiltersNumber
            } else {
                mTab.visibility = View.GONE
            }
            mapFilterAdapter.data = mapFilters
            getFilteredLocations()
            mapFilterAdapter.notifyDataSetChanged()
        }

        mapFilterAdapter.getRemoveFromListEvent().observe(viewLifecycleOwner) { removedPosition ->
            removeItemAtPositionInMapFilters(removedPosition)
            appliedFiltersTextView.text = getMapFiltersSize().toString() + "/" + allFiltersNumber
            if (getMapFiltersSize() == 0) {
                mTab.visibility = View.GONE
            }
            getFilteredLocations()
            mapFilterAdapter.notifyDataSetChanged()
        }

        viewModel.filterDataMaps.observe(viewLifecycleOwner) {
            allFiltersNumber = 0
            it?.forEach { sectionModel ->
                allFiltersNumber += sectionModel.filters.size
            }
        }

        viewModel.nearbyLocationData.observe(viewLifecycleOwner) { allLocationList ->
            if (allLocationList != null) {

                nearbyLocationList = allLocationList
                recycler_item.layoutManager = LinearLayoutManager(
                    requireActivity(),
                    LinearLayoutManager.VERTICAL,
                    false
                )

                setAdapter(nearbyLocationList)

            } else {
                frame.visibility = View.GONE
                mNoData.visibility = View.VISIBLE

            }
            frame.visibility = View.VISIBLE
            mNoData.visibility = View.GONE
            progressbar?.dismissPopup()
//            mRelative.visibility = View.INVISIBLE
        }
    }

    private fun removeItemAtPositionInMapFilters(position: Int) {
        var itemCount = 0
        var newPosition = 0
        mapFilters.forEach {
            if (position < it.filters.size) {
                it.filters.removeAt(position)
                return
            }
            if (itemCount + it.filters.size > position) {
                newPosition = position % itemCount
                it.filters.removeAt(newPosition)
                return
            } else {
                itemCount += it.filters.size
            }

        }
    }

    private fun getMapFiltersSize(): Int {
        var itemsCount = 0
        mapFilters.forEach {
            itemsCount += it.filters.size
        }
        return itemsCount
    }

    private fun getFilteredLocations() {
        val arrayOfIds = getFiltersArrayList()
        viewModel.getLocationDataFiltered(
            currentLocation.lat.toString(),
            currentLocation.lon.toString(),
            searchViewText,
            arrayOfIds
        )
    }

    private fun setAdapter(nearbyLocationList: ArrayList<LocationV2Item>) {

        val anInterface = object : LocationAdapter.ClickLocationItem {
            @SuppressLint("SetTextI18n")
            override fun click(pos: Int, openMap: Boolean) {
                val sheetDialog = BottomSheetDialog(
                    requireActivity(),
                    R.style.BottomSheetStyle
                )
                val dialogBinding = DirectionPopupBinding.inflate(
                    LayoutInflater.from(requireActivity())
                )
                sheetDialog.setContentView(dialogBinding.root)
                nearbyLocationList[pos].id?.let {
                    viewModel.getCurrentLocationData(
                        it
                    ).observe(viewLifecycleOwner) { currentLocation ->

                        if (currentLocation.email != null && currentLocation.email.endsWith("petrom.com")) {
                            dialogBinding.mImg.setImageResource(R.drawable.petrom)
                        } else {
                            dialogBinding.mImg.setImageResource(R.drawable.omv)
                        }
                        dialogBinding.name.text = currentLocation.name
                        dialogBinding.services.text =
                            currentLocation.services
                        dialogBinding.mAddress.text =
                            currentLocation.fullAddress
                        dialogBinding.mkm.text =
                            "• " + (nearbyLocationList.get(pos).distance?.toInt()) + " km away"
                        if (currentLocation.openNow == false) {
                            dialogBinding.status.text =
                                requireActivity().getString(R.string.closed)
                            dialogBinding.status.setTextColor(
                                resources.getColor(R.color.closed)
                            )
                        } else {
                            dialogBinding.status.text =
                                requireActivity().getString(R.string.open_24_hours)
                            dialogBinding.status.setTextColor(
                                resources.getColor(R.color.list_time)
                            )
                        }
                        if (currentLocation?.latitude != null && currentLocation.longitude != null) {
                            latitude = currentLocation.latitude
                            longitude = currentLocation.longitude
                        }
                    }
                }

                dialogBinding.mapButton.setOnClickListener {
                    sheetDialog.dismiss()
                    openMapDialog()
                }
                if (openMap) {
                    openMapDialog()
                } else {
                    sheetDialog.show()
                }
            }

        }

        adapter = LocationAdapter(
            requireActivity(),
            nearbyLocationList,
            anInterface
        )

        recycler_item.adapter = adapter
        mSearch.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searchViewText = newText
                if (newText.isEmpty())
                    searchImageView.visibility = View.VISIBLE
                else
                    searchImageView.visibility = View.INVISIBLE
                val arrayOfIds = getFiltersArrayList()
                viewModel.getLocationDataFiltered(
                    currentLocation.lat.toString(),
                    currentLocation.lon.toString(),
                    searchViewText,
                    arrayOfIds
                )
                return true
            }
        })
    }

    private fun getFiltersArrayList(): java.util.ArrayList<Int> {
        val arrayOfIds = java.util.ArrayList<Int>()
        mapFilters.forEach { sectionModel ->
            sectionModel.filters.forEach {
                arrayOfIds.add(it.nomLSId)
            }
        }
        return arrayOfIds
    }

    fun openMapDialog() {
        val isAppInstalled =
            appInstalledOrNot("com.waze")
        val bottomSheetDialog =
            BottomSheetDialog(
                requireActivity(),
                R.style.BottomSheetStyle
            )
        val selectorBinding =
            LocationSelectorBinding.inflate(
                LayoutInflater.from(
                    requireActivity()
                )
            )
        bottomSheetDialog.setContentView(
            selectorBinding.root
        )
        bottomSheetDialog.show()
        selectorBinding.google.setOnClickListener {
            bottomSheetDialog.dismiss()
            val gmmIntentUri =
                Uri.parse("google.navigation:q=$latitude,$longitude")
            val mapIntent =
                Intent(
                    Intent.ACTION_VIEW,
                    gmmIntentUri
                )
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }

        selectorBinding.Waze.setOnClickListener {
            bottomSheetDialog.dismiss()
            if (isAppInstalled) {
                val p = String.format(
                    Locale.ENGLISH,
                    "geo:%f,%f",
                    latitude,
                    longitude
                )
                val i = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(p)
                )
                i.setPackage("com.waze")
                startActivity(i)
            } else {
                openPlayStoreApplication("https://play.google.com/store/apps/details?id=com.waze")
            }
        }
        selectorBinding.Cancel.setOnClickListener { bottomSheetDialog.dismiss() }
    }

    private fun startLocation() {

        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            mLinear.visibility = View.VISIBLE

            location.setOnClickListener {
                Dexter.withContext(requireActivity())
                    .withPermissions(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    .withListener(object : MultiplePermissionsListener {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            report?.let {
                                if (report.areAllPermissionsGranted()) {
                                    mLinear.visibility = View.GONE
                                    map_button.visibility = View.VISIBLE
                                    list_location.visibility = View.VISIBLE
                                    locationFilter()
                                }
                            }
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            permissions: MutableList<PermissionRequest>?,
                            token: PermissionToken?
                        ) {
                            token?.continuePermissionRequest()
                        }
                    }).withErrorListener {}
                    .check()
            }

        } else {

            if (permission()) {
                map_button.visibility = View.VISIBLE
                list_location.visibility = View.VISIBLE
                locationFilter()
            }

        }
    }

    private fun permission(): Boolean {

        return ActivityCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

    }

    override fun onResume() {
        super.onResume()
        mSearch.setOnSearchClickListener {
            searchImageView.visibility = View.VISIBLE
        }
        mSearch.setOnCloseListener {
            searchImageView.visibility = View.INVISIBLE
            false
        }
        map_button.setOnClickListener {
            val i = Intent(requireActivity(), MapActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            requireActivity().startActivity(i)
        }
        mText.setOnClickListener {
            viewModel.onProfileClicked()
        }

    }

    private fun locationFilter() {

        progressbar?.showPopup()
//        mRelative.visibility = View.VISIBLE
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        client?.lastLocation?.addOnCompleteListener { task ->
            val location = task.result
            if (location != null) {
                try {
                    val geocoder = Geocoder(requireActivity(), Locale.getDefault())
                    val addresses =
                        geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    currentLocation.lat = location.latitude
                    currentLocation.lon = location.longitude

                    if (mLiveLocation != null) {
                        mLiveLocation.text = addresses[0].getAddressLine(0)
                    }
                    viewModel.getLocationFilter()
//                    viewModel.getLocationData(location.latitude.toString(), location.longitude.toString())
//                    Static data for location
                    viewModel.getLocationData(
                        currentLocation.lat.toString(), currentLocation.lon.toString()
                    )
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun appInstalledOrNot(uri: String): Boolean {
        val pm = requireActivity().packageManager
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
        }
        return false
    }

    private fun openPlayStoreApplication(appPackageName: String) {
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$appPackageName")
                )
            )
        } catch (anfe: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                )
            )
        }
    }

    private fun displayLocationSettingsRequest(context: Context) {
        val googleApiClient = GoogleApiClient.Builder(context)
            .addApi(LocationServices.API).build()
        googleApiClient.connect()
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10000
        locationRequest.fastestInterval = (10000 / 2).toLong()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val result =
            LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
        result.setResultCallback { result ->
            val status = result.status

            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS ->
                    Log.i(ContentValues.TAG, "All location settings are satisfied.")

                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {

                    Log.i(
                        ContentValues.TAG,
                        "Location settings are not satisfied. Show the user a dialog to upgrade location settings "
                    )
                    try {
                        // Show the dialog by calling startResolutionForResult(), and check the result
                        // in onActivityResult().
                        status.startResolutionForResult(
                            requireActivity(),
                            REQUEST_CHECK_SETTINGS
                        )
                    } catch (e: SendIntentException) {
                        Log.i(ContentValues.TAG, "PendingIntent unable to execute request.")
                    }
                }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> Log.i(
                    ContentValues.TAG,
                    "Location settings are inadequate, and cannot be fixed here. Dialog not created."
                )
            }
        }

    }

}