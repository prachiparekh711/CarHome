package ro.westaco.carhome.presentation.screens.service.person

import android.annotation.TargetApi
import android.app.AlertDialog
import android.os.Build
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.Lifecycle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_billing_information.*
import kotlinx.android.synthetic.main.fragment_data.back
import kotlinx.android.synthetic.main.fragment_data.home
import kotlinx.android.synthetic.main.fragment_data.pager
import kotlinx.android.synthetic.main.fragment_data.tabs
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.PaymentResponse
import ro.westaco.carhome.data.sources.remote.responses.models.Vehicle
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.settings.data.DataFragment
import ro.westaco.carhome.utils.ViewUtils

@AndroidEntryPoint
class BillingInformationFragment : BaseFragment<BillingInfoViewModel>() {

    companion object {
        const val ARG_GUID = "arg_guid"
        const val ARG_CAR = "arg_car"
        const val ARG_OF = "arg_of"
        const val INDEX = "arg_index"
        var index = 0
    }

    var adapter: BillingPagerAdapter? = null
    var guidModel: PaymentResponse? = null
    var carModel: Vehicle? = null
    var arg_of: String? = null

    override fun getContentView() = R.layout.fragment_billing_information

    override fun initUi() {

        back.setOnClickListener {
            viewModel.onBack()
        }

        home.setOnClickListener {
            viewModel.onMain()
        }

        tabs.addTab(tabs.newTab().setText(getString(R.string.natural_person)))
        tabs.addTab(tabs.newTab().setText(getString(R.string.legal_person)))

        adapter = BillingPagerAdapter(childFragmentManager)
        pager.adapter = adapter
        setTitle(DataFragment.index)
        pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab) {
                pager.currentItem = tab.position
                setTitle(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        arguments?.let {
            guidModel = it.getSerializable(ARG_GUID) as PaymentResponse
            carModel = it.getSerializable(ARG_CAR) as Vehicle?
            index = it.getInt(INDEX)
            arg_of = it.getString(ARG_OF)
            pager.setCurrentItem(index, true)
            tabs.getTabAt(index)?.select()
        }

        cta_next.setOnClickListener {
            guidModel?.guid?.let { it1 -> viewModel.onNextClick(it1) }
        }

        tv_previous.setOnClickListener {
            viewModel.onBack()
        }
    }

    override fun setObservers() {
        viewModel.initTransectionData.observe(viewLifecycleOwner) { model ->
            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {

                if (model != null) {
                    if (model.warnings.isNullOrEmpty()) {
                        model.html?.let {

                            showPurchaseBottomSheetDialog(
                                model,
                                vehicle = carModel
                            )
                        }
                    }
                } else {
                    var warning = ""
                    if (model?.warnings != null) {
                        for (i in model.warnings) {
                            warning += i + "\n"
                        }

                        showPurchaseWarningsDialog(
                            model, warning
                        )

                    }
                }
            }

        }
    }

    private var purchaseDialog: AlertDialog? = null

    private fun showPurchaseWarningsDialog(model: PaymentResponse, warning: String) {
        purchaseDialog?.dismiss()
        purchaseDialog = AlertDialog.Builder(requireContext(), R.style.DialogTheme).create()
        purchaseDialog?.setMessage(warning)
        purchaseDialog?.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        purchaseDialog?.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { dialog, _ ->
            dialog.dismiss()
            carModel?.let { showPurchaseBottomSheetDialog(model, it) }
        }
        purchaseDialog?.show()
    }

    private fun showPurchaseBottomSheetDialog(model: PaymentResponse, vehicle: Vehicle?) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_purchase)
        var navigated = false

        val webView = bottomSheetDialog.findViewById<WebView>(R.id.webView)
        webView?.clearHistory()

        webView?.settings?.javaScriptEnabled = true
        webView?.settings?.javaScriptCanOpenWindowsAutomatically = true
        webView?.settings?.loadsImagesAutomatically = true

        webView?.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(webView: WebView, url: String): Boolean {
                return shouldOverrideUrlLoading(url)
            }

            @TargetApi(Build.VERSION_CODES.N)
            override fun shouldOverrideUrlLoading(
                webView: WebView,
                request: WebResourceRequest,
            ): Boolean {
                val uri = request.url
                return shouldOverrideUrlLoading(uri.toString())
            }

            private fun shouldOverrideUrlLoading(url: String): Boolean {
                return if (url.contains("payment-done")) {
                    if (!navigated) {
                        navigated = true
                        arg_of?.let { viewModel.onPaymentSuccess(model, arg_of = it) }
                    }
                    bottomSheetDialog.dismiss()
                    true
                } else {
                    false
                }
            }

        }

        model.html?.replace("&#39;", "")
            ?.let { webView?.loadData(it, "text/html; charset=UTF-8", null) }

        bottomSheetDialog.findViewById<View>(R.id.dismiss)?.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        val parentLayout =
            bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        parentLayout?.let { it ->
            val behaviour = BottomSheetBehavior.from(it)
            ViewUtils.setViewHeightAsWindowPercent(requireContext(), it, 85)
            behaviour.state = BottomSheetBehavior.STATE_EXPANDED
        }

        bottomSheetDialog.show()

    }

    override fun onResume() {
        super.onResume()
        adapter?.notifyDataSetChanged()
    }

    fun setTitle(position: Int) {
        when (position) {
            0 -> title.text = getString(R.string.billing_info)
            else -> title.text = getString(R.string.billing_info)
        }
    }

}