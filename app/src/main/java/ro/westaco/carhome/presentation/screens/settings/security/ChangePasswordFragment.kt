package ro.westaco.carhome.presentation.screens.settings.security

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_change_password.*
import ro.westaco.carhome.R
import ro.westaco.carhome.dialog.DialogUtils
import ro.westaco.carhome.presentation.base.BaseFragment

//C- Redesign
@AndroidEntryPoint
class ChangePasswordFragment : BaseFragment<ChangePasswordModel>() {

    private var firebaseAuth = FirebaseAuth.getInstance()
    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)

    override fun getContentView() = R.layout.fragment_change_password

    override fun initUi() {
        val user = firebaseAuth.currentUser

        val view = layoutInflater.inflate(R.layout.reset_via_email_layout, null)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setCancelable(false)
        dialog.setContentView(view)
        val mClose = view.findViewById<ImageView>(R.id.mClose)
        val gotoMailCta = view.findViewById<TextView>(R.id.gotoMailCta)
        val mResend = view.findViewById<RelativeLayout>(R.id.mResend)
        mClose.setOnClickListener {
            dialog.dismiss()
            viewModel.onBack()
        }
        gotoMailCta.setOnClickListener {
            dialog.dismiss()
            try {

                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_APP_EMAIL)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                this.startActivity(intent)

            } catch (e: ActivityNotFoundException) {
                DialogUtils.showErrorInfo(
                    requireContext(),
                    getString(R.string.no_email_app)
                )
            }
        }

        user?.email?.let {
            FirebaseAuth.getInstance().sendPasswordResetEmail(it)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        dialog.show()
                    }
                }
        }

        mResend.setOnClickListener {
            user?.email?.let {
                FirebaseAuth.getInstance().sendPasswordResetEmail(it)
            }
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