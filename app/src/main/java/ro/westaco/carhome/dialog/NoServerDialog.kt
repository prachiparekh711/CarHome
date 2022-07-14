package ro.westaco.carhome.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ro.westaco.carhome.R

class NoServerDialog {


    companion object {
        private lateinit var customAlertDialogView: View

        fun showServerErrorInfo(
            context: Context?
        ) {
            if (context != null) {

                customAlertDialogView = LayoutInflater.from(context)
                    .inflate(R.layout.server_timeout_layout, null, false)
                val cta = customAlertDialogView.findViewById<TextView>(R.id.cta)

                val dialog = MaterialAlertDialogBuilder(
                    context,
                    R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog
                )
                    .setCancelable(false)
                    .setView(customAlertDialogView)
                    .show()

                dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                cta.setOnClickListener {
                    dialog.dismiss()
                }
            }
        }
    }

}