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
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
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
import ro.westaco.carhome.data.sources.remote.responses.models.LocationFilterItem
import ro.westaco.carhome.data.sources.remote.responses.models.LocationV2Item
import ro.westaco.carhome.databinding.DirectionPopupBinding
import ro.westaco.carhome.databinding.LocationSelectorBinding
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.main.MainActivity
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

    //    lateinit var selectBinding: DialogLocationBinding
    private var client: FusedLocationProviderClient? = null
    private var latitude = 0.0
    private var longitude: Double = 0.0
    var nearbyLocationList: ArrayList<LocationV2Item> = ArrayList()
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

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

        client = LocationServices.getFusedLocationProviderClient(requireActivity())
        displayLocationSettingsRequest(requireActivity())
//        displayLocationSettingsRequest(requireActivity())
        startLocation()

        if (MainActivity.activeUser != null) {
            mText.text = resources.getString(R.string.hello_name, MainActivity.activeUser)
        }
        placeAutoComplete()

    }

    fun placeAutoComplete() {

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
        viewModel.filterData.observe(viewLifecycleOwner) { filterList ->
            if (filterList != null) {
                recycler.layoutManager = LinearLayoutManager(
                    requireActivity(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                val allFilterList = filterList as ArrayList<LocationFilterItem>
                allFilterList.add(0, LocationFilterItem(0, "All"))
                recycler.adapter = LocationFilterAdapter(
                    requireActivity(),
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
                    mNoData.visibility = View.GONE
                }
            } else {
                mNoData.visibility = View.VISIBLE
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
//                Toast.makeText(
//                    requireActivity(),
//                    resources.getString(R.string.data_not_available),
//                    Toast.LENGTH_SHORT
//                ).show()
            }
            frame.visibility = View.VISIBLE
            mNoData.visibility = View.GONE
            mRelative.visibility = View.INVISIBLE
        }

        viewModel.profileLogoData?.observe(viewLifecycleOwner) { profileLogo ->
            if (profileLogo != null) {
                val options = RequestOptions()
                mProfile.clipToOutline = true
                Glide.with(requireActivity())
                    .load(profileLogo)
                    .apply(
                        options.centerCrop()
                            .skipMemoryCache(true)
                            .priority(Priority.HIGH)
                            .format(DecodeFormat.PREFER_ARGB_8888)
                    )
                    .into(mProfile)
            }
        }
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
                                    viewModel.accessLocation()
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
            mRelative_location.visibility = View.GONE
            back.visibility = View.VISIBLE
        }
        mSearch.setOnCloseListener {
            mRelative_location.visibility = View.VISIBLE
            back.visibility = View.INVISIBLE
            false
        }
        map_button.setOnClickListener {
            val i = Intent(requireActivity(), MapActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            requireActivity().startActivity(i)
        }
        mProfile.setOnClickListener {
            viewModel.onProfileClicked()
        }
        mText.setOnClickListener {
            viewModel.onProfileClicked()
        }

    }

    private fun requestPermissions() {


    }


    private fun locationFilter() {
        mRelative.visibility = View.VISIBLE
        mTab.visibility = View.VISIBLE
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


                    if (mLiveLocation != null) {

                        mLiveLocation.text = addresses[0].getAddressLine(0)

                    }

//                    Toast.makeText(requireActivity(), "" + addresses[0].getAddressLine(0), Toast.LENGTH_SHORT).show()
//                    Log.i(TAG, "locationFilter: " + )

                    viewModel.getLocationFilter()
//                    viewModel.getLocationData(location.latitude.toString(), location.longitude.toString())
//                    Static data for location
                    viewModel.getLocationData(
                        "27.57", "44.01"
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


    @SuppressLint("LogNotTimber")
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
//                        Toast.makeText(requireActivity(), "All location settings are satisfied.", Toast.LENGTH_SHORT).show()
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