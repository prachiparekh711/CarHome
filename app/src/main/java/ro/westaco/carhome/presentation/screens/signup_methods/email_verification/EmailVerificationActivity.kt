package ro.westaco.carhome.presentation.screens.signup_methods.email_verification

import android.content.ActivityNotFoundException
import android.content.Intent
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_email_verification.*
import ro.westaco.carhome.R
import ro.westaco.carhome.presentation.base.BaseActivity
import ro.westaco.carhome.utils.BiometricUtil
import ro.westaco.carhome.utils.DialogUtils.Companion.showErrorInfo
import ro.westaco.carhome.utils.Progressbar

//C- Email Verification Section
@AndroidEntryPoint
class EmailVerificationActivity : BaseActivity<EmailVerificationModel>() {

    private var firebaseAuth = FirebaseAuth.getInstance()
    override fun getContentView() = R.layout.activity_email_verification
    private var progress: Progressbar? = null

    override fun onResume() {
        super.onResume()
        progress = Progressbar(this@EmailVerificationActivity)
        val appLinkAction: String? = intent?.action
        if (appLinkAction != null) {
            progress?.showPopup()
        }
    }

    override fun setupUi() {
        val user = firebaseAuth.currentUser

        val url = resources.getString(R.string.deep_url)
        val actionCodeSettings = ActionCodeSettings.newBuilder()
            .setUrl(url)
            .setHandleCodeInApp(true)
            .build()
        firebaseAuth.currentUser?.reload()
            ?.addOnSuccessListener(this@EmailVerificationActivity) {
                if (firebaseAuth.currentUser != null) {
                    val isEmailVerified: Boolean =
                        firebaseAuth.currentUser?.isEmailVerified == true
                    if (isEmailVerified) {
                        viewModel.login()
                    } else {
                        user?.sendEmailVerification(actionCodeSettings)
                            ?.addOnCompleteListener {

                            }
                    }
                }
            }

        back.setOnClickListener {
            onBackPressed()
        }

        gotoMailCta.setOnClickListener {
            try {

                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_APP_EMAIL)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                this.startActivity(intent)

            } catch (e: ActivityNotFoundException) {
                showErrorInfo(this@EmailVerificationActivity, getString(R.string.no_email_app))
            }
        }

        resendLL.setOnClickListener {
            if (firebaseAuth.currentUser?.isEmailVerified == true) {
                showErrorInfo(this@EmailVerificationActivity, getString(R.string.email_verified))

                viewModel.login()
            } else {
                user?.sendEmailVerification(actionCodeSettings)
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {

                            showErrorInfo(
                                this@EmailVerificationActivity,
                                getString(R.string.email_sent)
                            )

                        }
                    }
            }
        }

        mConfirm.setOnClickListener {
            firebaseAuth.currentUser?.reload()
                ?.addOnSuccessListener(this@EmailVerificationActivity) {
                    if (firebaseAuth.currentUser != null) {
                        val isEmailVerified: Boolean =
                            firebaseAuth.currentUser?.isEmailVerified == true
                        if (isEmailVerified) {
                            viewModel.login()
                        } else {

                            showErrorInfo(
                                this@EmailVerificationActivity,
                                getString(R.string.not_verified)
                            )

                        }
                    }
                }
        }
    }

    override fun setupObservers() {
        viewModel.actionStream.observe(this) {
            when (it) {
                is EmailVerificationModel.ACTION.LoginSuccess -> {
                    redirectOnSuccess()
                }

            }
        }
    }

    private fun redirectOnSuccess() {
        progress?.dismissPopup()

        if (BiometricUtil.isHardwareAvailable(this@EmailVerificationActivity)) {
            if (BiometricUtil.hasBiometricEnrolled(this@EmailVerificationActivity)) {
                viewModel.navigateToBiometric()
            }
        } else {
            viewModel.navigateToProgress()
        }
    }
}