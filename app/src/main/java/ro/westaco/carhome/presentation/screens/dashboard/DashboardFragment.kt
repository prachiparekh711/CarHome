package ro.westaco.carhome.presentation.screens.dashboard

import android.view.View
import android.webkit.WebView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.layout_bottom_sheet.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import ro.westaco.carhome.data.sources.remote.responses.models.TermsResponseItem
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.dashboard.DashboardViewModel.Companion.serviceExpanded
import ro.westaco.carhome.presentation.screens.driving_mode.DrivingModeFragment

@AndroidEntryPoint
class DashboardFragment : BaseFragment<DashboardViewModel>() {

    companion object {
        const val TAG = "Dashboard"
        var CAR_MODE = ""
        var bnv: BottomNavigationView? = null
    }

    override fun getContentView() = R.layout.fragment_dashboard

    override fun initUi() {

//        SharedPrefrences.setCarMode(
//            requireContext(),
//            requireContext().resources.getString(R.string.driving)
//        )

        CAR_MODE = AppPreferencesDelegates.get().carMode

        bnv = bottomNavigationView
        bottomNavigationView.itemIconTintList = null
        bottomNavigationView.setOnItemSelectedListener {
            viewModel.onItemSelected(it)
            true
        }

        if (!serviceExpanded)
            viewModel.onCollapseServices()

        collapseServices.setOnClickListener {
            viewModel.onCollapseServices()
        }

        servicesOverlay.setOnClickListener {
            viewModel.onCollapseServices()
        }

        roadTaxIc.setOnClickListener {
            viewModel.onServiceClicked("RO_VIGNETTE")
        }

        bridgeTaxIc.setOnClickListener {
            viewModel.onServiceClicked("RO_PASS_TAX")
        }

        insuranceIc.setOnClickListener {
            viewModel.onInsurance()
        }

        roadTaxLabel.setOnClickListener {
            viewModel.onServiceClicked("RO_VIGNETTE")
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

    var index = 0
    override fun setObservers() {
        viewModel.termsLiveData.observe(viewLifecycleOwner) { termsList ->
            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                if (!termsList.isNullOrEmpty()) {
                    val termsList1: ArrayList<TermsResponseItem> = ArrayList()
                    for (i in termsList.indices) {
                        if (termsList[i].mandatory == true)
                            termsList1.add(termsList[i])
                    }
                    showBottomSheetDialog(termsList1)
                }
            }
        }

        viewModel.servicesStateLiveData.observe(viewLifecycleOwner) { state ->
            servicesExpandedGroup.visibility =
                if (state == DashboardViewModel.STATE.Expanded)
                    View.VISIBLE
                else
                    View.GONE
            serviceExpanded = servicesExpandedGroup.isVisible
        }

        viewModel.actionStream.observe(viewLifecycleOwner) {
            when (it) {
                is DashboardViewModel.ACTION.OpenChildFragment -> {
                    openChildFragment(it.fragment, it.tag)
                }
                is DashboardViewModel.ACTION.CheckMenuItem -> {
                    it.menuItem?.itemId?.let { it1 ->
                        bottomNavigationView.menu.findItem(it1).isChecked = true
                    }
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
            DashboardViewModel.selectedMenuItem = bnv?.menu?.findItem(R.id.home)
            bottomNavigationView.menu.findItem(R.id.home).isChecked = true
        }
    }

    private fun showBottomSheetDialog(
        termsList: ArrayList<TermsResponseItem>
    ) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.home_term_layout)
        bottomSheetDialog.setCancelable(false)
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetDialog.findViewById<TextView>(R.id.title)?.text = termsList[index].title
        val webSettings = bottomSheetDialog.webView.settings
        webSettings.javaScriptEnabled = true

        bottomSheetDialog.webView.loadUrl("https://carhome-build.westaco.com/carhome/rest/public/terms/" + termsList[index].versionId)
        WebView.setWebContentsDebuggingEnabled(false)

        bottomSheetDialog.findViewById<TextView>(R.id.btnAgree)?.setOnClickListener {
            termsList[index].allowed = true
            bottomSheetDialog.dismiss()

            if (termsList.size - 1 > index) {
                index++
                showBottomSheetDialog(termsList)
            } else {
                index = 0
                viewModel.saveTerms(termsList)
            }
        }

        bottomSheetDialog.show()
    }

}