package ro.westaco.carhome.utils

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ro.westaco.carhome.R

internal class DialogUtils {

    companion object {

        fun showErrorInfo(
            context: Context?,
            massage: String
        ) {
            if (context != null) {
                MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_App_MaterialAlertDialog)
                    .setTitle(context.getString(R.string.information_items_))
                    .setMessage(massage)
                    .setPositiveButton("Ok", null)
                    .show()
            }
        }
    }

}