package ro.westaco.carhome.presentation.screens.maps

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
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
import android.widget.LinearLayout
import android.widget.Toast
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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
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
import kotlinx.android.synthetic.main.direction_bottom.view.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.LocationFilterItem
import ro.westaco.carhome.data.sources.remote.responses.models.LocationV2Item
import ro.westaco.carhome.data.sources.remote.responses.models.OffsetItem
import ro.westaco.carhome.databinding.DirectionPopupBinding
import ro.westaco.carhome.databinding.LocationSelectorBinding
import ro.westaco.carhome.presentation.base.BaseActivity
import ro.westaco.carhome.presentation.screens.main.MainActivity.Companion.activeUser
import ro.westaco.carhome.utils.FirebaseAnalyticsList
import java.io.IOException
import java.util.*


//C- Map section
//C- Clustering
@AndroidEntryPoint
class MapActivity : BaseActivity<LocationViewModel>(),

    ClusterManager.OnClusterClickListener<OffsetItem>,
    ClusterManager.OnClusterItemClickListener<OffsetItem> {
    lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private var adapter: LocationAdapter? = null
    private var supportMapFragment: SupportMapFragment? = null
    private var client: FusedLocationProviderClient? = null
    private var mClusterManager: ClusterManager<OffsetItem>? = null
    private var latitudeItem = 0.0
    private var longitudeItem = 0.0
    var googleMap: GoogleMap? = null
    protected val REQUEST_CHECK_SETTINGS = 0x1
    protected val REQUEST_LOCATION_PERMISSION = 1
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

    companion object {
        var nearbyLocationList: ArrayList<LocationV2Item> = ArrayList()
    }

    override fun getContentView() = R.layout.activity_map

    override fun setupUi() {
        if (activeUser != null) {
            mText.text = resources.getString(R.string.hello_name, activeUser)
        }

        bottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet)

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        bottomSheetBehavior.isHideable = false
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                sRelative?.visibility = View.INVISIBLE
                if (newState == STATE_COLLAPSED) {
                    bottomSheetBehavior.peekHeight = 125
                } else if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                } else {
                    bottomSheetBehavior.setPeekHeight(125)
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}

        })

        placeAutoComplete()
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
                    Place.Field.ADDRESS_COMPONENTS
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
//                mLive.text = place.
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                val status = Autocomplete.getStatusFromIntent(data)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        displayLocationSettingsRequest(this@MapActivity)
        viewModel = ViewModelProvider(this).get(LocationViewModel::class.java)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
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
                            currentLocation
                            val params = Bundle()
                            mFirebaseAnalytics.logEvent(
                                FirebaseAnalyticsList.ACCESS_LOCATION_ANDROID,
                                params
                            )
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
        mSearch.setOnSearchClickListener { v ->
            mRelative_location.visibility = View.GONE
            mSearchRelative.visibility = View.VISIBLE
        }
        mSearch.setOnCloseListener {
            mRelative_location.visibility = View.VISIBLE
            mSearchRelative.visibility = View.GONE
            false
        }
    }

    val currentLocation: Unit
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


                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }

            task?.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    supportMapFragment?.getMapAsync { googleMap: GoogleMap ->
                        this.googleMap = googleMap
                        mClusterManager = ClusterManager<OffsetItem>(this@MapActivity, googleMap)
                        googleMap.setOnCameraIdleListener(mClusterManager)
                        googleMap.setOnMarkerClickListener(mClusterManager)

                        viewModel.getLocationFilter()
                        viewModel.getLocationData(latitudeItem.toString(), longitudeItem.toString())

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
                        } else {
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                REQUEST_LOCATION_PERMISSION
                            )
                        }

                    }
                } else {

                }
            }

        }

    override fun setupObservers() {

        viewModel.filterData.observe(this) { filterList ->
            if (filterList != null) {
                recycler.layoutManager = LinearLayoutManager(
                    this@MapActivity,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                val allFilterList = filterList as ArrayList<LocationFilterItem>
                allFilterList.add(0, LocationFilterItem(0, "All"))
                recycler.adapter = LocationFilterAdapter(
                    this@MapActivity,
                    allFilterList
                ) { pos ->
                    val serviceID = allFilterList[pos].nomLSId
                    val filterLocationList: ArrayList<LocationV2Item> = ArrayList()
                    for (i in nearbyLocationList.indices) {
                        val serviceIDList = nearbyLocationList[i].serviceIds
                        if (serviceIDList != null) {
                            if (serviceIDList.contains(serviceID.toString(), false)) {
                                filterLocationList.add(nearbyLocationList[i])
                            }
                        }
                    }

                    setAdapter(filterLocationList)
                }
            } else {
                Toast.makeText(
                    applicationContext,
                    resources.getString(R.string.data_not_available),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        viewModel.nearbyLocationData.observe(this) { nearbyLocationData ->

            if (nearbyLocationData != null) {
                nearbyLocationList = nearbyLocationData
                map_view.recycler_item.layoutManager = LinearLayoutManager(
                    this@MapActivity,
                    LinearLayoutManager.VERTICAL,
                    false
                )

                setAdapter(nearbyLocationList)
            } else {
                Toast.makeText(
                    applicationContext,
                    resources.getString(R.string.data_not_available),
                    Toast.LENGTH_SHORT
                ).show()
            }
            map_view.mRelative.visibility = View.INVISIBLE
        }
    }

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
                            latitudeItem = currentLocation.latitude!!
                            longitudeItem =
                                currentLocation.longitude!!
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

//            Toast.makeText(this, "" + nearbyLocationData[i].brandId , Toast.LENGTH_SHORT).show()

            mClusterManager?.addItem(offsetItem)
            nearbyLocationData[0].latitude?.let {
                nearbyLocationData[0].longitude?.let { it1 ->
                    LatLng(
                        it,
                        it1
                    )
                }
            }?.let {
                CameraUpdateFactory.newLatLngZoom(
                    it, 5f
                )
            }?.let {
                googleMap?.moveCamera(
                    it
                )
            }
            googleMap?.setOnMapClickListener { latLng: LatLng? ->
                sRelative.visibility = View.INVISIBLE
            }
        }
        mClusterManager?.cluster()

        val anInterface = object : LocationAdapter.ClickLocationItem {

            override fun click(pos: Int, openMap: Boolean) {

                val sheetDialog =
                    BottomSheetDialog(
                        this@MapActivity,
                        R.style.BottomSheetStyle
                    )
                val dialogBinding: DirectionPopupBinding =
                    DirectionPopupBinding.inflate(
                        LayoutInflater.from(
                            this@MapActivity
                        )
                    )
                sheetDialog.setContentView(dialogBinding.root)

                nearbyLocationData[pos].id?.let {
                    viewModel.getCurrentLocationData(
                        it
                    ).observe(
                        this@MapActivity
                    ) { currentLocation ->

//                        Toast.makeText(this@MapActivity, "" + currentLocation.email, Toast.LENGTH_SHORT).show()

                        if (currentLocation.email?.endsWith("petrom.com") == true) {
                            dialogBinding.mImg.setImageResource(R.drawable.petrom)
                        } else {
                            dialogBinding.mImg.setImageResource(R.drawable.omv)
                        }
                        dialogBinding.name.text = currentLocation.name

                        dialogBinding.services.text = currentLocation.services
                        dialogBinding.mAddress.text =
                            currentLocation.fullAddress
                        dialogBinding.mkm.text =
                            "• " + (nearbyLocationData[pos].distance?.toInt()) + " km away"
                        if (currentLocation.openNow == false) {
                            dialogBinding.status.text =
                                getString(R.string.closed)
                            dialogBinding.status.setTextColor(
                                resources.getColor(R.color.closed)
                            )
                        } else {
                            dialogBinding.status.text =
                                getString(R.string.open_24_hours)
                            dialogBinding.status.setTextColor(
                                resources.getColor(R.color.list_time)
                            )
                        }

                        if (currentLocation.latitude != null && currentLocation.longitude != null) {
                            latitudeItem = currentLocation.latitude
                            longitudeItem = currentLocation.longitude
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
            this@MapActivity,
            nearbyLocationData,
            anInterface
        )

        map_view.text.text =
            nearbyLocationData.size.toString() + " " + getString(R.string.fuel_stations)
        map_view.recycler_item.adapter = adapter
        mSearch.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                adapter?.filter?.filter(newText)
                return true
            }
        })
    }

    fun openMapDialog() {
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

    private fun openPlayStoreApplication(appPackageName: String) {
        var appPackageName = appPackageName
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

        private var zoom = 15f
        private var oldZoom: Float? = null
        var map: GoogleMap? = null
        var context: Context? = null
        var mActivity: Activity? = null
        var mImageView: ImageView? = null
        private val mDimension: Int
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

        private fun getItemIcon(offset: OffsetItem): BitmapDescriptor {

            if (offset.title!!.endsWith("90002")) {
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
            mDimension = context?.resources?.getDimension(R.dimen.size_48)?.toInt()!!
            mImageView?.layoutParams = ViewGroup.LayoutParams(mDimension, mDimension)
            val padding = (context.resources?.getDimension(R.dimen.size_2))?.toInt()!!
            mImageView?.setPadding(padding, padding, padding, padding)
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

        bottomSheetBehavior.state = STATE_COLLAPSED
        if (item != null) {
            sRelative?.visibility = View.VISIBLE
            onMarkerClick(nearbyLocationList[item.snippet!!.toInt()])
        }
        return false
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
                LocationSettingsStatusCodes.SUCCESS -> Log.i(
                    ContentValues.TAG,
                    "All location settings are satisfied."
                )
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Check if location permissions are granted and if so enable the
        // location data layer.
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> if (grantResults.size > 0
                && grantResults[0]
                == PackageManager.PERMISSION_GRANTED
            ) {
                currentLocation
            }
        }
    }

}