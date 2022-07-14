package ro.westaco.carhome.dialog

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
                MaterialAlertDialogBuilder(
                    context,
                    R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog
                )
                    .setTitle(context.getString(R.string.information_items_))
                    .setMessage(massage)
                    .setPositiveButton(context.resources.getString(R.string.ok), null)
                    .show()
            }
        }
    }

}