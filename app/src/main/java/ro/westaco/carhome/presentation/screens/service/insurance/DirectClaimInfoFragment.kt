package ro.westaco.carhome.presentation.screens.service.insurance

import android.webkit.WebView
import android.webkit.WebViewClient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_direct_claim_info.*
import ro.westaco.carhome.R
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.settings.extras.CommenViewModel
import ro.westaco.carhome.utils.Progressbar

@AndroidEntryPoint
class DirectClaimInfoFragment : BaseFragment<CommenViewModel>() {

    var progressbar: Progressbar? = null
    override fun getContentView() = R.layout.fragment_direct_claim_info

    override fun initUi() {
        progressbar = Progressbar(requireContext())
        progressbar?.showPopup()

        back.setOnClickListener {
            viewModel.onBack()
        }

        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webView.loadUrl(requireContext().resources.getString(R.string.direct_claim_url))
        WebView.setWebContentsDebuggingEnabled(false)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                progressbar?.dismissPopup()
            }
        }
    }

    override fun setObservers() {

    }
}