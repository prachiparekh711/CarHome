package ro.westaco.carhome.presentation.screens.settings.extras

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_terms_cond.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.TermsResponseItem
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.views.Progressbar

@AndroidEntryPoint
class TermsAndConFragment : BaseFragment<CommenViewModel>() {

    var progressbar: Progressbar? = null
    override fun getContentView() = R.layout.fragment_terms_cond

    companion object {
        const val ARG_TERM_ITEM = "arg_term_item"
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun initUi() {
        progressbar = Progressbar(requireContext())
        progressbar?.showPopup()


        back.setOnClickListener {
            viewModel.onBack()
        }

        home.setOnClickListener {
            viewModel.onMain()
        }

        arguments?.let {
            val termItem = it.getSerializable(ARG_TERM_ITEM) as TermsResponseItem

            title.text = termItem.title
            val webSettings = webView.settings
            webSettings.javaScriptEnabled = true
            webView.loadUrl("https://carhome-build.westaco.com/carhome/rest/public/terms/" + termItem.versionId)
            WebView.setWebContentsDebuggingEnabled(false)
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    progressbar?.dismissPopup()
                }
            }
        }
    }

    override fun setObservers() {
    }

}