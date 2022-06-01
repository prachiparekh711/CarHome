package ro.westaco.carhome.presentation.screens.settings.extras

import android.webkit.WebView
import android.webkit.WebViewClient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_contact_us.back
import kotlinx.android.synthetic.main.fragment_contact_us.home
import kotlinx.android.synthetic.main.fragment_gdpr.*
import ro.westaco.carhome.R
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.utils.Progressbar


@AndroidEntryPoint
class GDPRFragment : BaseFragment<CommenViewModel>() {

    var progressbar: Progressbar? = null
    override fun getContentView() = R.layout.fragment_gdpr

    override fun initUi() {
        progressbar = Progressbar(requireContext())
        progressbar?.showPopup()

        back.setOnClickListener {
            viewModel.onBack()
        }

        home.setOnClickListener {
            viewModel.onMain()
        }

        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webView.loadUrl(requireContext().resources.getString(R.string.gdpr))
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