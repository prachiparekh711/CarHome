package ro.westaco.carhome.presentation.screens.settings.extras

import android.webkit.WebView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_terms_cond.*
import ro.westaco.carhome.R
import ro.westaco.carhome.presentation.base.BaseFragment

@AndroidEntryPoint
class TermsAndConFragment : BaseFragment<CommenViewModel>() {

    override fun getContentView() = R.layout.fragment_terms_cond

    override fun initUi() {
        back.setOnClickListener {
            viewModel.onBack()
        }

        home.setOnClickListener {
            viewModel.onMain()
        }

        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webView.loadUrl(requireContext().resources.getString(R.string.tc))
        WebView.setWebContentsDebuggingEnabled(false)
    }

    override fun setObservers() {
    }

}