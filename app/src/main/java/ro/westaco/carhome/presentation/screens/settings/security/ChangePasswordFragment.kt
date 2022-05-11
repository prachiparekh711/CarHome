package ro.westaco.carhome.presentation.screens.settings.security

import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_change_password.*
import ro.westaco.carhome.R
import ro.westaco.carhome.presentation.base.BaseFragment

//C- Redesign
@AndroidEntryPoint
class ChangePasswordFragment : BaseFragment<ChangePasswordModel>() {

    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)

    override fun getContentView() = R.layout.fragment_change_password

    override fun initUi() {
        val view = layoutInflater.inflate(R.layout.reset_via_email_layout, null)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setCancelable(false)
        dialog.setContentView(view)
        val mClose = view.findViewById<ImageView>(R.id.mClose)
        val gotoMailCta = view.findViewById<TextView>(R.id.gotoMailCta)
        mClose.setOnClickListener { dialog.dismiss() }
        gotoMailCta.setOnClickListener { dialog.dismiss() }
        resetEmail?.setOnClickListener {
            dialog.show()
        }

        changePasswordCta?.setOnClickListener {
            viewModel.OnPasswordChangeSuccess()
        }

        back.setOnClickListener {
            viewModel.onBack()
        }

        home.setOnClickListener {
            viewModel.onMain()
        }
    }

    override fun setObservers() {
    }

}