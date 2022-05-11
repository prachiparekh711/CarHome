package ro.westaco.carhome.presentation.screens.dashboard

import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_dashboard.*
import ro.westaco.carhome.R
import ro.westaco.carhome.prefrences.SharedPrefrences
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.driving_mode.DrivingModeFragment

@AndroidEntryPoint
class DashboardFragment : BaseFragment<DashboardViewModel>() {


    companion object {
        const val TAG = "Dashboard"
        var CAR_MODE = ""
        var bnv: BottomNavigationView? = null

        fun changeMenu() {
            bnv?.menu?.findItem(R.id.home)?.isChecked = true
        }
    }


    override fun getContentView() = R.layout.fragment_dashboard

    override fun initUi() {

//        SharedPrefrences.setCarMode(
//            requireContext(),
//            requireContext().resources.getString(R.string.driving)
//        )

        CAR_MODE = SharedPrefrences.getCarMode(requireActivity()).toString()

        bnv = bottomNavigationView
        bottomNavigationView.itemIconTintList = null
        bottomNavigationView.setOnItemSelectedListener {
            viewModel.onItemSelected(it)
            true
        }

        collapseServices.setOnClickListener {
            viewModel.onCollapseServices()
        }

        servicesOverlay.setOnClickListener {
            viewModel.onCollapseServices()
        }

        roadTaxIc.setOnClickListener {
            viewModel.onRoadTax()
        }

        bridgeTaxIc.setOnClickListener {
            viewModel.onBridgeTax()
        }

        insuranceIc.setOnClickListener {
            viewModel.onInsurance()
        }

        roadTaxLabel.setOnClickListener {
            viewModel.onRoadTax()
        }

        user_details.setOnClickListener {
            viewModel.onDataClicked(0)
        }

        documents.setOnClickListener {
            viewModel.onNewDocument()
        }

        purchases.setOnClickListener {
            viewModel.onHistoryClicked()
        }

    }


    override fun setObservers() {

        viewModel.servicesStateLiveData.observe(viewLifecycleOwner) { state ->
            servicesExpandedGroup.visibility =
                if (state == DashboardViewModel.STATE.Expanded)
                    View.VISIBLE
                else
                    View.GONE
        }

        viewModel.actionStream.observe(viewLifecycleOwner) {
            when (it) {
                is DashboardViewModel.ACTION.OpenChildFragment -> {
                    openChildFragment(it.fragment, it.tag)
                }
                is DashboardViewModel.ACTION.CheckMenuItem -> {
                    it.menuItem?.isChecked = true
                }
            }
        }
    }

    private fun openChildFragment(fragment: Fragment, tag: String?) {

        if (tag == DrivingModeFragment.TAG) {
            childFragmentManager.beginTransaction()
                .replace(R.id.childContent, fragment)
                .commit()
        } else {
            childFragmentManager.beginTransaction()
                .replace(R.id.childContent, fragment)
                .addToBackStack(tag)
                .commit()
        }

        if (DashboardViewModel.selectedMenuItem == null) {
            bottomNavigationView.menu.findItem(R.id.home).isChecked = true
        }
    }


}