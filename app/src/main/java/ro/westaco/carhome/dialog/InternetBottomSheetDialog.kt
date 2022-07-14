package ro.westaco.carhome.dialog

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.NonNull
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ro.westaco.carhome.CarHomeApplication
import ro.westaco.carhome.R
import ro.westaco.carhome.databinding.NoInternetLayoutBinding
import ro.westaco.carhome.utils.DeviceUtils
import ro.westaco.carhome.views.Progressbar


internal class InternetBottomSheetDialog : BottomSheetDialogFragment() {
    var binding: NoInternetLayoutBinding? = null
    var progressbar: Progressbar? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.no_internet_layout, container, false)
        isCancelable = false
        progressbar = Progressbar(requireContext())

        binding?.cta?.setOnClickListener {
            progressbar?.showPopup()
            if (DeviceUtils.isOnline(CarHomeApplication.appInstance)) {
                dismiss()
            }
            Handler(Looper.getMainLooper()).postDelayed({
                progressbar?.dismissPopup()
            }, 800)
        }

        return binding?.root
    }

    override fun onResume() {
        super.onResume()
        if (DeviceUtils.isOnline(CarHomeApplication.appInstance)) {
            dismiss()
        }
    }

    private fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        bottomSheet.layoutParams = layoutParams
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setOnShowListener {

            val bottomSheetDialog = it as BottomSheetDialog
            val parentLayout =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let { it1 ->
                val behaviour = BottomSheetBehavior.from(it1)
                setupFullHeight(it1)
                behaviour.state = BottomSheetBehavior.STATE_EXPANDED

                behaviour.addBottomSheetCallback(object : BottomSheetCallback() {
                    override fun onStateChanged(@NonNull bottomSheet: View, newState: Int) {
                        if (newState == BottomSheetBehavior.STATE_COLLAPSED || newState == BottomSheetBehavior.STATE_DRAGGING) {
                            behaviour.state = BottomSheetBehavior.STATE_EXPANDED
                        }
                    }

                    override fun onSlide(@NonNull bottomSheet: View, slideOffset: Float) {
                        // React to dragging events
                    }
                })
            }
        }

        return dialog
    }

}
