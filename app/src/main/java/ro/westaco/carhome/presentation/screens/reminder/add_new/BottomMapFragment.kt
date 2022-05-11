package ro.westaco.carhome.presentation.screens.reminder.add_new

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import dagger.hilt.android.AndroidEntryPoint
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.LocationFilterItem
import ro.westaco.carhome.data.sources.remote.responses.models.LocationV2Item
import ro.westaco.carhome.databinding.DialogLocationBinding
import ro.westaco.carhome.databinding.DirectionPopupBinding
import ro.westaco.carhome.databinding.FragmentBottomMapBinding
import ro.westaco.carhome.databinding.LocationSelectorBinding
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.maps.LocationFilterAdapter
import ro.westaco.carhome.presentation.screens.maps.LocationViewModel
import java.io.IOException
import java.util.*

@AndroidEntryPoint
class BottomMapFragment : BaseFragment<LocationViewModel>() {
    private lateinit var mActivity: Activity
    private var dialog: Dialog? = null
    private var dialog2: Dialog? = null
    private var adapter: MapAdapter? = null
    lateinit var binding: FragmentBottomMapBinding
    lateinit var selectBinding: DialogLocationBinding
    private var client: FusedLocationProviderClient? = null
    private var latitude = 0.0
    private var longitude: Double = 0.0
    var nearbyLocationList: ArrayList<LocationV2Item> = ArrayList()

    companion object {
        const val TAG = "BottomMapFragment"
    }

    override fun getContentView() = R.layout.fragment_bottom_map

    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivity = requireActivity()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBottomMapBinding.inflate(inflater, container, false)

        dialog = Dialog(requireActivity())
        dialog2 = Dialog(requireActivity())

        client = LocationServices.getFusedLocationProviderClient(mActivity)

        startLocation()

        return binding.root
    }

    override fun initUi() {
    }

    override fun setObservers() {
        viewModel.filterData.observe(viewLifecycleOwner) { filterList ->
            if (filterList != null) {
                binding.recycler.layoutManager = LinearLayoutManager(
                    mActivity,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                val allFilterList = filterList as ArrayList<LocationFilterItem>
                allFilterList.add(0, LocationFilterItem(0, "All"))
                binding.recycler.adapter = LocationFilterAdapter(
                    mActivity,
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
                Toast.makeText(
                    mActivity,
                    resources.getString(R.string.data_not_available),
                    Toast.LENGTH_SHORT
                ).show()
            }
            binding.mRelative.visibility = View.INVISIBLE
        }
    }

    private fun setAdapter(nearbyLocationList: ArrayList<LocationV2Item>) {

        val anInterface = object : MapAdapter.ClickLocationItem {
            override fun click(pos: Int, openMap: Boolean) {
                val sheetDialog = BottomSheetDialog(
                    mActivity,
                    R.style.BottomSheetStyle
                )
                val dialogBinding = DirectionPopupBinding.inflate(
                    LayoutInflater.from(mActivity)
                )
                sheetDialog.setContentView(dialogBinding.root)
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

        adapter = MapAdapter(
            mActivity,
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

        binding.recyclerItem.adapter = adapter
        binding.mSearch.setOnQueryTextListener(object :
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
                mActivity,
                R.style.BottomSheetStyle
            )
        val selectorBinding =
            LocationSelectorBinding.inflate(
                LayoutInflater.from(
                    mActivity
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
                mActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                mActivity, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            selectBinding = DialogLocationBinding.inflate(LayoutInflater.from(mActivity))

            dialog?.setContentView(selectBinding.root)
            dialog?.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog?.show()
            selectBinding.location.setOnClickListener {
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
                                            permissionToken: PermissionToken
                                        ) {
                                            permissionToken.continuePermissionRequest()
                                        }
                                    }).check()
                            }
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            permissionRequest: PermissionRequest?,
                            permissionToken: PermissionToken
                        ) {
                            permissionToken.continuePermissionRequest()
                        }
                    }).check()
                dialog?.dismiss()
            }
        } else {
            dialog?.dismiss()
            binding.listLocation.visibility = View.VISIBLE
            locationFilter()
        }
    }

    private fun locationFilter() {
        binding.mRelative.visibility = View.VISIBLE
        binding.mTab.visibility = View.VISIBLE
        if (ActivityCompat.checkSelfPermission(
                mActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                mActivity, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        client?.lastLocation?.addOnCompleteListener { task ->
            val location = task.result
            if (location != null) {
                try {

                    viewModel.getLocationFilter()
//                    viewModel.getLocationData(location.latitude.toString(), location.longitude.toString())
//                    Static data for location
                    viewModel.getLocationData("27.57", "44.01")
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

    }


    private fun appInstalledOrNot(uri: String): Boolean {
        val pm = mActivity.packageManager
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

}