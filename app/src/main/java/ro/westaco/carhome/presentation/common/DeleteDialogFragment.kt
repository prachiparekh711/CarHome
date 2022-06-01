package ro.westaco.carhome.presentation.common

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import ro.westaco.carhome.R


class DeleteDialogFragment : DialogFragment() {

    companion object {
        const val TAG = "DeleteDialogFragment"
    }

    interface OnDialogInteractionListener {
        fun onPosClicked()
    }


    var layoutResId: Int = 0
    var listener: OnDialogInteractionListener? = null

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(layoutResId, container, false)

        if (showsDialog) {
            dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        view.findViewById<View>(R.id.pos).setOnClickListener {
            listener?.onPosClicked()
        }
        view.findViewById<View>(R.id.neg).setOnClickListener {
            dismissAllowingStateLoss()
        }


        return view
    }
}