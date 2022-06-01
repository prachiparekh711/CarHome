package ro.westaco.carhome.presentation.screens.home

import com.github.barteksc.pdfviewer.listener.OnErrorListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_pdf.*
import ro.westaco.carhome.R
import ro.westaco.carhome.presentation.base.BaseActivity
import ro.westaco.carhome.utils.DialogUtils.Companion.showErrorInfo
import ro.westaco.carhome.utils.Progressbar
import java.io.ByteArrayOutputStream


@AndroidEntryPoint
class PdfActivity : BaseActivity<PdfModel>() {

    companion object {
        var ARG_DATA = "arg_data"
        var ARG_FROM = "arg_from"
        var ARG_INSURER = "arg_insurer"
        var ARG_SERVICE_TYPE = "arg_service_type"
    }

    var progressbar: Progressbar? = null

    override fun getContentView() = R.layout.activity_pdf

    override fun setupUi() {

        progressbar = Progressbar(this@PdfActivity)
        progressbar?.showPopup()
        back.setOnClickListener { finish() }

        val from = intent.getStringExtra(ARG_FROM)
        val documentData = intent.getStringExtra(ARG_DATA)
        val insurer = intent.getStringExtra(ARG_INSURER)
        val serviceType = intent.getStringExtra(ARG_SERVICE_TYPE)

        when (from) {
            "DOCUMENT" -> documentData?.let { viewModel.fetchDocumentData(it) }
            "SERVICE" -> insurer?.let {
                if (serviceType != null) {
                    viewModel.onViewPID(it, serviceType)
                }
            }
        }

    }

    override fun setupObservers() {

        viewModel.documentData?.observe(this) { documentData ->

            if (documentData != null) {
                val buffer = ByteArray(8192)
                var bytesRead: Int
                val output = ByteArrayOutputStream()
                while (documentData.byteStream().read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                }
                val onErrorListener = OnErrorListener {
                    showErrorInfo(this@PdfActivity, it.message.toString())
                }
                pdfView.fromBytes(output.toByteArray()).onError(onErrorListener).load()
                progressbar?.dismissPopup()
            }

        }

    }

}