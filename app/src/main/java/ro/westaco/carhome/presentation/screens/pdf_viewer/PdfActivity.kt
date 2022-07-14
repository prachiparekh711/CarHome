package ro.westaco.carhome.presentation.screens.pdf_viewer

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.github.barteksc.pdfviewer.listener.OnErrorListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_pdf.*
import okhttp3.ResponseBody
import ro.westaco.carhome.BuildConfig
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.RowsItem
import ro.westaco.carhome.di.ApiModule
import ro.westaco.carhome.dialog.DialogUtils.Companion.showErrorInfo
import ro.westaco.carhome.presentation.base.BaseActivity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


@AndroidEntryPoint
class PdfActivity : BaseActivity<PdfModel>() {

    companion object {
        var ARG_DATA = "arg_data"
        var ARG_FROM = "arg_from"
        var ARG_INSURER = "arg_insurer"
        var ARG_SERVICE_TYPE = "arg_service_type"
        var ARG_ITEM = "arg_item"
    }

    override fun getContentView() = R.layout.activity_pdf
    private var rowItem: RowsItem? = null
    private var documentData: String? = null
    var outputByteArray: ByteArrayOutputStream? = null
    var outputBitmap: Bitmap? = null

    override fun setupUi() {

        back.setOnClickListener { finish() }

        share.setOnClickListener {
            if (rowItem?.mimeType == "image/jpeg" || rowItem?.mimeType == "image/png" || rowItem?.mimeType == "image/*") {
                shareImage()
            } else {
                sharePdf()
            }
        }

        val from = intent.getStringExtra(ARG_FROM)
        documentData = intent.getStringExtra(ARG_DATA)
        val insurer = intent.getStringExtra(ARG_INSURER)
        val serviceType = intent.getStringExtra(ARG_SERVICE_TYPE)

//        rowItem for documentSection
        rowItem = intent.getSerializableExtra(ARG_ITEM) as? RowsItem?

        when (from) {
            "DOCUMENT" -> {

                val url = if (rowItem != null) {
                    "${ApiModule.BASE_URL_RESOURCES}${rowItem?.href}"
                } else {
                    documentData
                }
                url?.let { viewModel.fetchDocumentData(it) }
            }
            "SERVICE" -> insurer?.let {
                if (serviceType != null) {
                    viewModel.onViewPID(it, serviceType)
                }
            }
        }
    }


    override fun setupObservers() {

        viewModel.documentData?.observe(this) { documentResponse ->

            if (documentResponse != null) {
                if (rowItem == null) {
                    showPDF(documentResponse)
                } else {
                    if (rowItem?.mimeType == "image/jpeg" || rowItem?.mimeType == "image/png" || rowItem?.mimeType == "image/*") {
                        pdfView.isVisible = false
                        scrollView.isVisible = true

                        val options = RequestOptions()
                        imgView.clipToOutline = true
                        Glide.with(baseContext)
                            .asBitmap()
                            .load(documentResponse.bytes())
                            .diskCacheStrategy(DiskCacheStrategy.DATA)
                            .apply(
                                options.fitCenter()
                                    .priority(Priority.HIGH)
                                    .format(DecodeFormat.PREFER_ARGB_8888)
                            )
                            .into(object : SimpleTarget<Bitmap?>() {
                                override fun onResourceReady(
                                    resource: Bitmap,
                                    transition: com.bumptech.glide.request.transition.Transition<in Bitmap?>?
                                ) {
                                    imgView.setImageBitmap(resource)
                                    outputBitmap = resource
                                }
                            })


                    } else {
                        showPDF(documentResponse)
                    }
                }
            }
        }

    }

    private fun showPDF(documentResponse: ResponseBody) {
        val buffer = ByteArray(8192)
        var bytesRead: Int
        val output = ByteArrayOutputStream()
        while (documentResponse.byteStream().read(buffer)
                .also { bytesRead = it } != -1
        ) {
            output.write(buffer, 0, bytesRead)
        }
        val onErrorListener = OnErrorListener {
            showErrorInfo(this@PdfActivity, it.message.toString())
        }

        this.outputByteArray = output
        pdfView.fromBytes(output.toByteArray()).onError(onErrorListener).load()
        pdfView.isVisible = true
        scrollView.isVisible = false
    }

    private fun uriFromFile(context: Context, file: File): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", file)
        } else {
            Uri.fromFile(file)
        }
    }

    private fun sharePdf() {
        try {
            val tempDir = System.getProperty("java.io.tmpdir")
            val file = File("${tempDir}/${rowItem?.name}")
            val fos = FileOutputStream(file)
            fos.write(outputByteArray?.toByteArray())

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.putExtra(Intent.EXTRA_STREAM, uriFromFile(baseContext, file))
            val chooser =
                Intent.createChooser(shareIntent, baseContext.resources.getString(R.string.share))
            val resInfoList = this.packageManager.queryIntentActivities(
                chooser,
                PackageManager.MATCH_DEFAULT_ONLY
            )
            shareIntent.type = "application/pdf"
            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                grantUriPermission(
                    packageName,
                    uriFromFile(baseContext, file),
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun shareImage() {
        val tempDir = System.getProperty("java.io.tmpdir")
        val file = File("${tempDir}/${rowItem?.name}.png")
        val stream = FileOutputStream(file)
        outputBitmap?.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.flush()
        stream.close()

        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.putExtra(Intent.EXTRA_STREAM, uriFromFile(baseContext, file))
        val chooser =
            Intent.createChooser(shareIntent, baseContext.resources.getString(R.string.share))
        val resInfoList = this.packageManager.queryIntentActivities(
            chooser,
            PackageManager.MATCH_DEFAULT_ONLY
        )
        shareIntent.type = "image/*"
        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            grantUriPermission(
                packageName,
                uriFromFile(baseContext, file),
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        startActivity(chooser)

    }

}