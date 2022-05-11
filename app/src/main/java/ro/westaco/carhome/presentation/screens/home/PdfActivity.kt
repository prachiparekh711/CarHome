package ro.westaco.carhome.presentation.screens.home

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.listener.OnErrorListener
import dagger.hilt.android.AndroidEntryPoint
import ro.westaco.carhome.R

@AndroidEntryPoint
class PdfActivity : AppCompatActivity() {

    companion object {
        const val ARG_DATA = "arg_data"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf)

        val documentData = intent.getByteArrayExtra(ARG_DATA)
        val pdfview = findViewById<PDFView>(R.id.pdfview)
        val back = findViewById<AppCompatImageView>(R.id.back)

        val onErrorListener = OnErrorListener {
            Toast.makeText(
                baseContext,
                resources.getString(R.string.pdf_exception),
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
        if (documentData != null) {
            pdfview.fromBytes(documentData).onError(onErrorListener).load()
        } else {
            Toast.makeText(
                baseContext,
                resources.getString(R.string.pdf_exception),
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
        back.setOnClickListener { finish() }
    }


}