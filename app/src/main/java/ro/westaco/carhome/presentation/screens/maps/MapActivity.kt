package ro.westaco.carhome.presentation.screens.maps

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.coroutines.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import ro.westaco.carhome.data.sources.remote.responses.models.LocationV2Item
import ro.westaco.carhome.data.sources.remote.responses.models.OffsetItem
import ro.westaco.carhome.data.sources.remote.responses.models.SectionModel
import ro.westaco.carhome.databinding.DirectionPopupBinding
import ro.westaco.carhome.databinding.LocationSelectorBinding
import ro.westaco.carhome.dialog.DialogUtils.Companion.showErrorInfo
import ro.westaco.carhome.presentation.base.BaseActivity
import ro.westaco.carhome.presentation.base.ContextWrapper
import ro.westaco.carhome.presentation.screens.main.MainActivity.Companion.activeUser
import java.io.IOException
import java.util.*


//C- Map section
//C- Clustering
@AndroidEntryPoint
class MapActivity : BaseActivity<LocationViewModel>(),

    ClusterManager.OnClusterClickListener<OffsetItem>,
    ClusterManager.OnClusterItemClickListener<OffsetItem>,
    LocationListener {
    private var supportMapFragment: SupportMapFragment? = null
    private var client: FusedLocationProviderClient? = null
    private var mClusterManager: ClusterManager<OffsetItem>? = null
    private var latitudeItem: Double = 0.0
    private var longitudeItem: Double = 0.0
    var googleMap: GoogleMap? = null
    protected val REQUEST_CHECK_SETTINGS = 0x1
    protected val REQUEST_LOCATION_PERMISSION = 1
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
    private lateinit var fragment: MapFiltersBottomSheetDialog
    private var mapFilters: ArrayList<SectionModel> = ArrayList()
    private lateinit var mapFilterAdapter: MapFilterAdapter
    private lateinit var googleApiClient: GoogleApiClient
    private var allFiltersNumber: Int = 0
    private var searchViewText: String = ""
    private var searchJob: Job? = null

    companion object {
        var nearbyLocationList: ArrayList<LocationV2Item> = ArrayList()
        var PARENT = "parent"
        var HOME_FRAGMENT = "HOME_FRAGMENT"
        var LOCATIONS_LIST = "LOCATIONS_LIST"
    }

    override fun attachBaseContext(newBase: Context) {
        val newLocale: Locale = if (AppPreferencesDelegates.get().language == "en-US") {
            Locale("en")
        } else {
            Locale("ro")
        }
        val context: Context = ContextWrapper.wrap(newBase, newLocale)
        super.attachBaseContext(context)
    }

    override fun getContentView() = R.layout.activity_map

    override fun setupUi() {

        if (activeUser != null) {
            mText.text = resources.getString(R.string.your_location)
        }

        mapFilterAdapter = MapFilterAdapter(this, mapFilters)
        recycler.adapter = mapFilterAdapter
        recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        googleApiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API).build()
        if (!googleApiClient.isConnected)
            googleApiClient.connect()
        placeAutoComplete()
        setFilterButton()
    }

    private fun setFilterButton() {
        openFiltersImageView2.setOnClickListener {
            fragment.show(supportFragmentManager, "map_filters")
        }
    }

    private fun placeAutoComplete() {

        if (!Places.isInitialized()) {
            Places.initialize(
                applicationContext,
                resources.getString(R.string.google_app_key),
                Locale.US
            )
        }

        placeLL.setOnClickListener {
            try {
                val fields = listOf(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.ADDRESS,
                    Place.Field.ADDRESS_COMPONENTS,
                    Place.Field.LAT_LNG
                )
                val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(this@MapActivity)
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
                mLive.text = place.name
                place.latLng?.let {
                    latitudeItem = it.latitude
                    longitudeItem = it.longitude
                }
                getFilteredLocations()
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                val status = Autocomplete.getStatusFromIntent(data)
            }
        }
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                displayLocationSettingsRequest(this)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(LocationViewModel::class.java)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        supportMapFragment = this.supportFragmentManager
            .findFragmentById(R.id.google_map) as SupportMapFragment?
        client = LocationServices.getFusedLocationProviderClient(this)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this@MapActivity)

        Dexter.withContext(applicationContext)
            .withPermissions(
                Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let {
                        if (report.areAllPermissionsGranted()) {
                            displayLocationSettingsRequest(this@MapActivity)
                            val params = Bundle()
                            mFirebaseAnalytics.logEvent("Access_Location_AND", params)
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
        back.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        mSearch.clearFocus()
        mSearch.setOnSearchClickListener {
            searchImageView2.visibility = View.VISIBLE
        }
        mSearch.setOnCloseListener {
            searchImageView2.visibility = View.INVISIBLE
            false
        }
        list_button.setOnClickListener {
            val parent = intent.extras?.let {
                it[PARENT]
            }
            if (parent == HOME_FRAGMENT) {
                var intent = Intent(this, LocationsListActivity::class.java)
                startActivity(intent)
            } else if (parent == LOCATIONS_LIST) {
                finish()
            }

        }
    }

    override fun onBackPressed() {
        finish()
    }

    val currentLocation: Unit
        @SuppressLint("PotentialBehaviorOverride")
        get() {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            val task = client?.lastLocation
            client?.lastLocation?.addOnCompleteListener { task1: Task<Location?> ->
                val location = task1.result
                if (location != null) {
                    try {
                        val geocoder =
                            Geocoder(this@MapActivity, Locale.getDefault())
                        val addresses =
                            geocoder.getFromLocation(
                                location.latitude, location.longitude, 1
                            )
//                            geocoder.getFromLocation(
//                                45.251161, 25.464916, 1
//                            )
                        mLive.text = Html.fromHtml(
                            "<font color='#303065'><b></b><br></font>" + addresses[0].getAddressLine(
                                0
                            )
                        )
                        latitudeItem = location.latitude
                        longitudeItem = location.longitude

                        viewModel.fetchLocationFilter()

                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } else {
                    val (locationRequest, builder) = createGoogleApiClientAndLocationRequest()
                    createLocationRequest(locationRequest)
                }
            }

            task?.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    supportMapFragment?.getMapAsync { googleMap: GoogleMap ->
                        this.googleMap = googleMap
                        mClusterManager = ClusterManager<OffsetItem>(this@MapActivity, googleMap)
                        googleMap.setOnCameraIdleListener(mClusterManager)
                        googleMap.setOnMarkerClickListener(mClusterManager)
                        googleMap.uiSettings.isMapToolbarEnabled = false


                        val renderer = ZoomBasedRenderer(
                            baseContext,
                            this@MapActivity,
                            googleMap,
                            mClusterManager!!
                        )

                        mClusterManager?.renderer = renderer
                        mClusterManager?.setOnClusterClickListener(this)
                        mClusterManager?.setOnClusterItemClickListener(this)



                        if (ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                            == PackageManager.PERMISSION_GRANTED
                        ) {
                            googleMap.isMyLocationEnabled = true
                            moveToCurrentLocation(
                                LatLng(
                                    location.latitude,
                                    location.longitude
                                )
                            )
                            createCustomMyLocationButton()
                            getLocationButton()?.let {
                                it.callOnClick()
                            }
                        } else {
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                REQUEST_LOCATION_PERMISSION
                            )
                        }

                    }
                }
            }

        }

    private fun moveToCurrentLocation(currentLocation: LatLng) {
        googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
        googleMap!!.animateCamera(CameraUpdateFactory.zoomIn())
        googleMap!!.animateCamera(CameraUpdateFactory.zoomTo(15f), 2000, null)
    }

    private fun getLocationButton(): ImageView? {
        return supportMapFragment?.requireView()?.findViewById<ImageView>(0x2)
    }

    @SuppressLint("ResourceType")
    private fun createCustomMyLocationButton() {
        try {
            val locationButton = getLocationButton()
            if (locationButton != null) {
                locationButton.visibility = View.GONE
                myLocation.setOnClickListener {
                    if (googleMap != null) {
                        locationButton.callOnClick()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun setupObservers() {
        viewModel.getSelectedItems().observe(this) { filters ->
            mapFilters = filters
            var isFiltered = false
            filters.forEach {
                if (it.filters.size != 0)
                    isFiltered = true
            }
            if (isFiltered) {
                mTab2.visibility = View.VISIBLE
                appliedFiltersTextView2.text =
                    getMapFiltersSize().toString() + "/" + allFiltersNumber
            } else {
                mTab2.visibility = View.GONE
            }
            mapFilterAdapter.data = mapFilters
            mapFilterAdapter.notifyDataSetChanged()
            getFilteredLocations()
        }

        mapFilterAdapter.getRemoveFromListEvent().observe(this) { removedPosition ->
            removeItemAtPositionInMapFilters(removedPosition)
            appliedFiltersTextView2.text = getMapFiltersSize().toString() + "/" + allFiltersNumber
            if (getMapFiltersSize() == 0) {
                mTab2.visibility = View.GONE
            }
            mapFilterAdapter.notifyDataSetChanged()
            getFilteredLocations()
        }

        viewModel.filterDataMaps.observe(this) {
            allFiltersNumber = 0
            it?.forEach { sectionModel ->
                allFiltersNumber += sectionModel.filters.size
            }
            fragment = if (intent.getSerializableExtra("SECTION_EXTRA") != null) {
                val sectionModel = intent.getSerializableExtra("SECTION_EXTRA") as SectionModel
                MapFiltersBottomSheetDialog(viewModel, sectionModel)
            } else {
                MapFiltersBottomSheetDialog(viewModel, null)
            }
        }

        viewModel.nearbyLocationsFiltered.observe(this) {
            if (it != null) {
                nearbyLocationList = it
                setAdapter(it)
            } else {
                showErrorInfo(applicationContext, getString(R.string.data_not_available))
            }
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
            latitudeItem.toString(),
            longitudeItem.toString(),
            searchViewText,
            arrayOfIds
        )
        viewModel.nearbyLocationsFiltered.observe(this) { filteredLocations ->
            if (filteredLocations != null) {
                setAdapter(filteredLocations)
            }
        }
    }

    private fun getFiltersArrayList(): ArrayList<Int> {
        val arrayOfIds = ArrayList<Int>()
        mapFilters.forEach { sectionModel ->
            sectionModel.filters.forEach {
                arrayOfIds.add(it.nomLSId)
            }
        }
        return arrayOfIds
    }

    @SuppressLint("SetTextI18n")
    private fun onMarkerClick(dataItem: LocationV2Item) {

        try {

            if (dataItem.openNow == false) {
                mTime.text = resources.getString(R.string.closed)
                mTime?.setTextColor(
                    resources.getColor(
                        R.color.closed
                    )
                )
            } else {
                mTime.text = resources.getString(R.string.open_24_hours)
                mTime.setTextColor(
                    resources.getColor(
                        R.color.list_time
                    )
                )
            }
            mName.text = dataItem.name
            mGarage.text = dataItem.services
            mLocation.text = dataItem.fullAddress

            mKm.text = "• " + (dataItem.distance?.toInt()) + " km"
            sRelative.setOnClickListener { v ->
                val sheetDialog =
                    BottomSheetDialog(
                        this@MapActivity,
                        R.style.BottomSheetStyle
                    )
                val dialogBinding: DirectionPopupBinding =
                    DirectionPopupBinding.inflate(
                        LayoutInflater.from(this@MapActivity)
                    )
                sheetDialog.setContentView(dialogBinding.root)
                sheetDialog.show()
                dataItem.id?.let {
                    viewModel.getCurrentLocationData(it)
                        .observe(this@MapActivity) { currentLocation: LocationV2Item ->
                            if (currentLocation.email?.endsWith("petrom.com") == true) {
                                dialogBinding.mImg.setImageResource(
                                    R.drawable.petrom
                                )
                            } else {
                                dialogBinding.mImg.setImageResource(
                                    R.drawable.omv
                                )
                            }

                            dialogBinding.name.text = currentLocation.name
                            dialogBinding.status.text =
                                currentLocation.timetable?.locationStatus
                            dialogBinding.services.text =
                                currentLocation.services
                            dialogBinding.mAddress.text =
                                currentLocation.fullAddress
                            dialogBinding.mkm.text =
                                "• " + (dataItem.distance?.toInt()) + " km away"
                            if (currentLocation.timetable?.locationStatus == "CLOSE") {
                                dialogBinding.status.setTextColor(
                                    resources.getColor(R.color.closed)
                                )
                            } else {
                                dialogBinding.status.setTextColor(
                                    resources.getColor(R.color.list_time)
                                )
                            }
                            currentLocation.latitude?.let { lat ->
                                latitudeItem = lat
                            }
                            currentLocation.longitude?.let { lon ->
                                longitudeItem = lon
                            }
                            dialogBinding.mapButton.setOnClickListener { v1 ->
                                sheetDialog.dismiss()
                                openMapDialog()
                            }
                        }
                }
            }
        } catch (ex: NumberFormatException) { // handle your exception
        }
    }

    private fun setAdapter(nearbyLocationData: ArrayList<LocationV2Item>) {
        mClusterManager?.clearItems()

        for (i in nearbyLocationData.indices) {
            val offsetItem = nearbyLocationData[i].latitude?.let {
                nearbyLocationData[i].longitude?.let { it1 ->
                    OffsetItem(
                        it,
                        it1,
                        "" + nearbyLocationData[i].brandId.toString(),
                        "" + i
                    )
                }
            }

            mClusterManager?.addItem(offsetItem)
//            nearbyLocationData[0].latitude?.let {
//                nearbyLocationData[0].longitude?.let { it1 ->
//                    LatLng(
//                        it,
//                        it1
//                    )
//                }
//            }?.let {
//                CameraUpdateFactory.newLatLngZoom(
//                    it, 5f
//                )
//            }?.let {
//                googleMap?.moveCamera(
//                    it
//                )
//            }
            googleMap?.setOnMapClickListener {
                details_card.visibility = View.GONE
                mSearch.clearFocus()
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
            }
        }
        mClusterManager?.cluster()

        mSearch.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                mSearch.clearFocus()
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searchViewText = newText
                if (newText.isEmpty()) {
                    searchImageView2.visibility = View.VISIBLE

                } else
                    searchImageView2.visibility = View.INVISIBLE
                searchJob?.cancel()
                searchJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(500L)
                    val arrayOfIds = getFiltersArrayList()
                    viewModel.getLocationDataFiltered(
                        latitudeItem.toString(),
                        longitudeItem.toString(),
                        newText,
                        arrayOfIds
                    )
                }
                return true
            }
        })
        getLocationButton()?.let {
            it.callOnClick()
        }
    }

    private fun openMapDialog() {
        val isAppInstalled =
            appInstalledOrNot("com.waze")
        val bottomSheetDialog =
            BottomSheetDialog(
                this@MapActivity,
                R.style.BottomSheetStyle
            )
        val selectorBinding =
            LocationSelectorBinding.inflate(
                LayoutInflater.from(
                    this@MapActivity
                )
            )
        bottomSheetDialog.setContentView(
            selectorBinding.root
        )
        bottomSheetDialog.show()
        selectorBinding.google.setOnClickListener {
            bottomSheetDialog.dismiss()
            val gmmIntentUri =
                Uri.parse("http://maps.google.com/maps?saddr=45.251161,25.464916&daddr=$latitudeItem,$longitudeItem")
//                Uri.parse("google.navigation:q=$latitudeItem,$longitudeItem")
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
                    latitudeItem,
                    longitudeItem
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

    private fun appInstalledOrNot(uri: String): Boolean {
        val pm = packageManager
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
        }
        return false
    }

    private fun openPlayStoreApplication(appPackageNameId: String) {
        var appPackageName = appPackageNameId
        appPackageName = appPackageName.substring(appPackageName.indexOf("=") + 1)
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

    override fun onClusterClick(cluster: Cluster<OffsetItem>?): Boolean {
        val builder = LatLngBounds.builder()
        if (cluster != null) {
            for (item in cluster.items) {
                builder.include(item.position)
            }
        }

        try {
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true

    }

    private class ZoomBasedRenderer(
        context: Context?,
        activity: Activity?,
        map1: GoogleMap?,
        clusterManager: ClusterManager<OffsetItem>
    ) :
        DefaultClusterRenderer<OffsetItem>(context, map1, clusterManager),
        GoogleMap.OnCameraIdleListener {

        private var zoom = 5f
        private var oldZoom: Float? = null
        var map: GoogleMap? = null
        var context: Context? = null
        var mActivity: Activity? = null
        var mImageView: ImageView? = null
        private var mDimension: Int? = null
        private val mIconGenerator = IconGenerator(context)

        override fun onBeforeClusterItemRendered(
            offset: OffsetItem,
            markerOptions: MarkerOptions,
        ) {
            markerOptions
                .icon(getItemIcon(offset))
//                .title(offset.title)
        }

        override fun onClusterItemUpdated(offset: OffsetItem, marker: Marker) {
            marker.setIcon(getItemIcon(offset))
//            marker.title = offset.title
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        private fun getItemIcon(offset: OffsetItem): BitmapDescriptor {

            if (offset.title?.endsWith("90002") == true) {
                mImageView?.setImageDrawable(context?.resources?.getDrawable(R.drawable.petrom_icon))

            } else {
                mImageView?.setImageDrawable(context?.resources?.getDrawable(R.drawable.omv_icon))
            }

            mIconGenerator.setBackground(null)
            val icon = mIconGenerator.makeIcon()
            return BitmapDescriptorFactory.fromBitmap(icon)

        }

        init {
            this.map = map1
            this.context = context
            this.mActivity = activity
            mImageView = ImageView(context)
            mDimension = context?.resources?.getDimension(R.dimen._35sdp)?.toInt()
            mImageView?.layoutParams = mDimension?.let { ViewGroup.LayoutParams(it, it) }
            val padding = (context?.resources?.getDimension(R.dimen._1sdp))?.toInt()
            if (padding != null) {
                mImageView?.setPadding(padding, padding, padding, padding)
            }
            mIconGenerator.setContentView(mImageView)

        }

        override fun onCameraIdle() {
            oldZoom = zoom
            zoom = map?.cameraPosition?.zoom!!
        }


        override fun shouldRenderAsCluster(cluster: Cluster<OffsetItem?>): Boolean {
            return zoom < ZOOM_THRESHOLD
        }

        private fun crossedZoomThreshold(oldZoom: Float?, newZoom: Float?): Boolean {
            return if (oldZoom == null || newZoom == null) {
                true
            } else oldZoom < ZOOM_THRESHOLD && newZoom > ZOOM_THRESHOLD ||
                    oldZoom > ZOOM_THRESHOLD && newZoom < ZOOM_THRESHOLD
        }

        override fun shouldRender(
            oldClusters: Set<Cluster<OffsetItem?>>,
            newClusters: Set<Cluster<OffsetItem?>>,
        ): Boolean {
            return if (crossedZoomThreshold(oldZoom, zoom)) {
                true
            } else {
                super.shouldRender(oldClusters, newClusters)
            }
        }

        companion object {
            private const val ZOOM_THRESHOLD = 9f
        }
    }

    override fun onClusterItemClick(item: OffsetItem?): Boolean {
        if (item != null) {
            details_card?.visibility = View.VISIBLE
            onMarkerClick(nearbyLocationList[item.snippet!!.toInt()])
        }
        return false
    }

    private fun displayLocationSettingsRequest(context: Context) {
        val (locationRequest, builder) = createGoogleApiClientAndLocationRequest()
        val result =
            LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
        result.setResultCallback { result ->
            val status = result.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS -> currentLocation
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    Log.i(
                        ContentValues.TAG,
                        "Location settings are not satisfied. Show the user a dialog to upgrade location settings "
                    )
                    try {
                        // Show the dialog by calling startResolutionForResult(), and check the result
                        // in onActivityResult().
                        status.startResolutionForResult(
                            this@MapActivity,
                            REQUEST_CHECK_SETTINGS
                        )
                    } catch (e: IntentSender.SendIntentException) {
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

    private fun createGoogleApiClientAndLocationRequest(): Pair<LocationRequest, LocationSettingsRequest.Builder> {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10000
        locationRequest.fastestInterval = (10000 / 2).toLong()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        return Pair(locationRequest, builder)
    }

    protected fun createLocationRequest(locationRequest: LocationRequest) {
        LocationServices.FusedLocationApi.removeLocationUpdates(
            googleApiClient,
            this
        )
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
            googleApiClient,
            locationRequest,
            this
        )
    }

    override fun onLocationChanged(p0: Location) {
        currentLocation
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Check if location permissions are granted and if so enable the
        // location data layer.
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> if (grantResults.size > 0
                && grantResults[0]
                == PackageManager.PERMISSION_GRANTED
            ) {
                displayLocationSettingsRequest(this)
            }
        }
    }

}