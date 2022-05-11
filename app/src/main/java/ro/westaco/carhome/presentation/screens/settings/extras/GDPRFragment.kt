package ro.westaco.carhome.presentation.screens.settings.extras

import android.webkit.WebView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_contact_us.back
import kotlinx.android.synthetic.main.fragment_contact_us.home
import kotlinx.android.synthetic.main.fragment_gdpr.*
import ro.westaco.carhome.R
import ro.westaco.carhome.presentation.base.BaseFragment

@AndroidEntryPoint
class GDPRFragment : BaseFragment<CommenViewModel>() {


    override fun getContentView() = R.layout.fragment_gdpr

    override fun initUi() {
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
    }

    override fun setObservers() {
    }


}