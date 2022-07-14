package ro.westaco.carhome.presentation.screens.maps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_top_services_map.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.SectionModel
import ro.westaco.carhome.presentation.base.BaseFragment

@AndroidEntryPoint
class TopServicesMapFragment : BaseFragment<LocationViewModel>() {

    private var categoriesList: ArrayList<SectionModel> = ArrayList()
    private lateinit var topServicesAdapter: TopServicesAdapter

    companion object {
        const val TAG = "TopServicesMapFragment"
    }

    override fun getContentView() = R.layout.fragment_top_services_map

    override fun initUi() {
        startLocation()
    }

    override fun setObservers() {
        viewModel.filterDataMaps.observe(this) {
            categoriesList.addAll(it)
            topServicesAdapter = TopServicesAdapter(
                requireContext(),
                categoriesList
            )
            topServicesRecyclerView.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            topServicesRecyclerView.adapter = topServicesAdapter
            topServicesAdapter.getSelectedItemLiveData()
                .observe(viewLifecycleOwner) { sectionModel ->
                    val i = Intent(requireActivity(), LocationsListActivity::class.java)
                    if (sectionModel.category != "General") {
                        i.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                        i.putExtra("SECTION_EXTRA", sectionModel)
                    }
                    requireActivity().startActivity(i)
                }
        }
    }

    private fun startLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            mLinear.visibility = View.VISIBLE
            location.setOnClickListener {
                Dexter.withContext(requireContext())
                    .withPermissions(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    .withListener(object : MultiplePermissionsListener {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            report?.let {
                                if (report.areAllPermissionsGranted()) {
                                    mLinear.visibility = View.GONE
                                    viewModel.fetchLocationFilter()
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
                viewModel.fetchLocationFilter()
            }

        }
    }

    private fun permission(): Boolean {

        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

}