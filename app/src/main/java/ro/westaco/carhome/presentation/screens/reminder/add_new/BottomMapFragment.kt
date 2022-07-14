package ro.westaco.carhome.presentation.screens.reminder.add_new

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_bottom_map.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.LocationV2Item
import ro.westaco.carhome.data.sources.remote.responses.models.SectionModel
import ro.westaco.carhome.databinding.DialogLocationBinding
import ro.westaco.carhome.databinding.DirectionPopupBinding
import ro.westaco.carhome.databinding.FragmentBottomMapBinding
import ro.westaco.carhome.databinding.LocationSelectorBinding
import ro.westaco.carhome.dialog.DialogUtils.Companion.showErrorInfo
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.main.MainActivity
import ro.westaco.carhome.presentation.screens.maps.LocationViewModel
import ro.westaco.carhome.presentation.screens.maps.MapFilterAdapter
import ro.westaco.carhome.presentation.screens.maps.MapFiltersBottomSheetDialog
import java.io.IOException
import java.util.*

@AndroidEntryPoint
class BottomMapFragment : BaseFragment<LocationViewModel>() {
    private var mActivity: Activity? = null
    private var dialog: Dialog? = null
    private var dialog2: Dialog? = null
    private var adapter: MapAdapter? = null
    lateinit var binding: FragmentBottomMapBinding
    lateinit var selectBinding: DialogLocationBinding
    private var client: FusedLocationProviderClient? = null
    private var latitude = 0.0
    private var longitude: Double = 0.0
    var nearbyLocationList: ArrayList<LocationV2Item> = ArrayList()
    var mapFilters: ArrayList<SectionModel> = ArrayList()
    private var allFiltersNumber = 0
    private lateinit var mapFilterAdapter: MapFilterAdapter
    private lateinit var fragment: MapFiltersBottomSheetDialog
    private var currentLocation = ClientLocation()
    private var searchViewText: String = ""

    inner class ClientLocation {
        var lat: Double = 0.0
        var lon: Double = 0.0
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Activity) {
            mActivity = context
        }
    }

    companion object {
        const val TAG = "BottomMapFragment"
    }

    override fun getContentView() = R.layout.fragment_bottom_map

    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentBottomMapBinding.inflate(inflater, container, false)

        dialog = Dialog(requireActivity())
        dialog2 = Dialog(requireActivity())

        client = mActivity?.let { LocationServices.getFusedLocationProviderClient(it) }

        startLocation()

        return binding.root
    }

    override fun initUi() {
        openFiltersImageView3.setOnClickListener {
            fragment.show(childFragmentManager, "map")
        }
        placeAutoComplete()
        mapFilterAdapter = MapFilterAdapter(requireContext(), mapFilters)
        recycler_filters.adapter = mapFilterAdapter
        recycler_filters.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        if (MainActivity.activeUser != null) {
            mText3.text = resources.getString(R.string.your_location)
        }
    }

    override fun setObservers() {
        fragment = MapFiltersBottomSheetDialog(viewModel, null)
        viewModel.nearbyLocationsFiltered.observe(viewLifecycleOwner) { filteredLocations ->
            if (filteredLocations != null) {
                setAdapter(filteredLocations)
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
                binding.recyclerItem.layoutManager = LinearLayoutManager(
                    mActivity,
                    LinearLayoutManager.VERTICAL,
                    false
                )
                binding.frame.visibility = View.VISIBLE

                setAdapter(nearbyLocationList)

            } else {
                binding.frame.visibility = View.GONE
                showErrorInfo(mActivity, getString(R.string.data_not_available))
            }
            binding.mRelative.visibility = View.INVISIBLE
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

        placeLL2.setOnClickListener {
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

    private fun getFiltersArrayList(): java.util.ArrayList<Int> {
        val arrayOfIds = java.util.ArrayList<Int>()
        mapFilters.forEach { sectionModel ->
            sectionModel.filters.forEach {
                arrayOfIds.add(it.nomLSId)
            }
        }
        return arrayOfIds
    }

    private fun setAdapter(nearbyLocationList: ArrayList<LocationV2Item>) {

        val anInterface = object : MapAdapter.ClickLocationItem {
            override fun click(pos: Int, openMap: Boolean) {
                val sheetDialog = mActivity?.let {
                    BottomSheetDialog(
                        it,
                        R.style.BottomSheetStyle
                    )
                }
                val dialogBinding = DirectionPopupBinding.inflate(
                    LayoutInflater.from(mActivity)
                )
                sheetDialog?.setContentView(dialogBinding.root)
                nearbyLocationList[pos].id?.let {
                    viewModel.getCurrentLocationData(
                        it
                    ).observe(requireActivity()) { currentLocation ->

                        if (currentLocation.email != null && currentLocation.email.endsWith(
                                "petrom.com"
                            )
                        ) {
                            dialogBinding.mImg.setImageResource(
                                R.drawable.petrom
                            )
                        } else {
                            dialogBinding.mImg.setImageResource(
                                R.drawable.omv
                            )
                        }
                        dialogBinding.name.text = currentLocation.name
                        dialogBinding.services.text =
                            currentLocation.services
                        dialogBinding.mAddress.text =
                            currentLocation.fullAddress
                        dialogBinding.mkm.text =
                            "• " + (nearbyLocationList.get(
                                pos
                            ).distance?.toInt()) + " km away"
                        if (currentLocation.openNow == false) {
                            dialogBinding.status.text =
                                requireContext().getString(R.string.closed)
                            dialogBinding.status.setTextColor(
                                resources.getColor(R.color.closed)
                            )
                        } else {
                            dialogBinding.status.text =
                                requireContext().getString(R.string.open_24_hours)
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
                    sheetDialog?.dismiss()
                    openMapDialog()
                }
                if (openMap) {
                    openMapDialog()
                } else {
                    sheetDialog?.show()
                }
            }

        }

        adapter = mActivity?.let {
            MapAdapter(
                it,
                nearbyLocationList,
                anInterface
            ) { pos ->

                nearbyLocationList[pos].id?.let {
                    viewModel.getCurrentLocationData(
                        it
                    ).observe(requireActivity()) { currentLocation ->
                        val lbm1 = context?.let { LocalBroadcastManager.getInstance(it) }
                        val localIn1 = Intent("LOCATION")
                        localIn1.putExtra("locationData", nearbyLocationList[pos])
                        localIn1.putExtra("currentLocation", currentLocation)
                        lbm1?.sendBroadcast(localIn1)
                    }
                }
            }
        }

        binding.recyclerItem.adapter = adapter
        binding.mSearch.setOnQueryTextListener(object :
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
                adapter?.filter?.filter(newText)
                return true
            }
        })
    }

    fun openMapDialog() {
        val isAppInstalled =
            appInstalledOrNot("com.waze")
        val bottomSheetDialog =
            mActivity?.let {
                BottomSheetDialog(
                    it,
                    R.style.BottomSheetStyle
                )
            }
        val selectorBinding =
            LocationSelectorBinding.inflate(
                LayoutInflater.from(
                    mActivity
                )
            )
        bottomSheetDialog?.setContentView(
            selectorBinding.root
        )
        bottomSheetDialog?.show()
        selectorBinding.google.setOnClickListener {
            bottomSheetDialog?.dismiss()
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
            bottomSheetDialog?.dismiss()
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
        selectorBinding.Cancel.setOnClickListener { bottomSheetDialog?.dismiss() }
    }

    private fun startLocation() {
        if (mActivity?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED && mActivity?.let {
                ActivityCompat.checkSelfPermission(
                    it, Manifest.permission.ACCESS_COARSE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED
        ) {
//            selectBinding = DialogLocationBinding.inflate(LayoutInflater.from(mActivity))
//
//            dialog?.setContentView(selectBinding.root)
//            dialog?.window?.setLayout(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT
//            )
//            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//            dialog?.show()
//            selectBinding.location.setOnClickListener {
            Dexter.withContext(requireContext())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(object : PermissionListener {

                    override fun onPermissionGranted(permissionGrantedResponse: PermissionGrantedResponse) {
                        binding.listLocation.visibility = View.VISIBLE
                        locationFilter()
                    }

                    override fun onPermissionDenied(permissionDeniedResponse: PermissionDeniedResponse) {
                            binding.mLinear.visibility = View.VISIBLE
                            binding.location.setOnClickListener {
                                Dexter.withContext(requireContext())
                                    .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                                    .withListener(object : PermissionListener {
                                        override fun onPermissionGranted(permissionGrantedResponse: PermissionGrantedResponse) {
                                            binding.mLinear.visibility = View.GONE
                                            binding.listLocation.visibility = View.VISIBLE
                                            locationFilter()
                                        }

                                        override fun onPermissionDenied(permissionDeniedResponse: PermissionDeniedResponse) {}
                                        override fun onPermissionRationaleShouldBeShown(
                                            permissionRequest: PermissionRequest?,
                                            permissionToken: PermissionToken,
                                        ) {
                                            permissionToken.continuePermissionRequest()
                                        }
                                    }).check()
                            }
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            permissionRequest: PermissionRequest?,
                            permissionToken: PermissionToken,
                        ) {
                            permissionToken.continuePermissionRequest()
                        }
                    }).check()
                dialog?.dismiss()
//            }
        } else {
            dialog?.dismiss()
            binding.listLocation.visibility = View.VISIBLE
            locationFilter()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            if (resultCode == Activity.RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data)
                mLiveLocationBottomMap.text = place.name
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                var status = Autocomplete.getStatusFromIntent(data)
            }
        }
    }

    private fun locationFilter() {
        binding.mRelative.visibility = View.VISIBLE
        if (mActivity?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED && mActivity?.let {
                ActivityCompat.checkSelfPermission(
                    it, Manifest.permission.ACCESS_COARSE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        client?.lastLocation?.addOnCompleteListener { task ->
            val location = task.result
            if (location != null) {
                try {
                    val geocoder = Geocoder(mActivity, Locale.getDefault())
                    val addresses =
                        geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    currentLocation.lat = location.latitude
                    currentLocation.lon = location.longitude

                    if (mLiveLocationBottomMap != null) {

                        mLiveLocationBottomMap.text = addresses[0].getAddressLine(0)

                    }

                    viewModel.fetchLocationFilter()
//                    viewModel.getLocationData(location.latitude.toString(), location.longitude.toString())
//                    Static data for location
                    viewModel.getLocationData(
                        location.latitude.toString(),
                        location.longitude.toString()
                    )
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

    }

    private fun appInstalledOrNot(uri: String): Boolean {
        val pm = mActivity?.packageManager
        try {
            pm?.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
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

}