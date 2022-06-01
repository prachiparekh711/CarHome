package ro.westaco.carhome.presentation.screens.auth

import android.content.ActivityNotFoundException
import android.content.Intent
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.content.ContextCompat
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_forgot_password.*
import ro.westaco.carhome.R
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.utils.DialogUtils.Companion.showErrorInfo

@AndroidEntryPoint
class ForgotPasswordFragment : BaseFragment<ForgotPasswordViewModel>() {

    private var firebaseAuth = FirebaseAuth.getInstance()

    override fun getContentView() = R.layout.fragment_forgot_password

    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)

    override fun initUi() {

        val user = firebaseAuth.currentUser

        val url = resources.getString(R.string.deep_url)
        val actionCodeSettings = ActionCodeSettings.newBuilder()
            .setUrl(url)
            .setHandleCodeInApp(true)
            .build()
        firebaseAuth.currentUser?.reload()
            ?.addOnSuccessListener(requireActivity()) {
                if (firebaseAuth.currentUser != null) {
                    val isEmailVerified: Boolean =
                        firebaseAuth.currentUser?.isEmailVerified == true
                    if (isEmailVerified) {
                        viewModel.login()
                    } else {
                        user?.sendEmailVerification(actionCodeSettings)
                            ?.addOnCompleteListener { task ->

                            }
                    }
                }
            }

        back.setOnClickListener {
            viewModel.onBack()
        }

        mBack.setOnClickListener {
            viewModel.onBack()
        }

        cta.setOnClickListener {
            viewModel.onSubmit(email.text.toString())
        }

        resendPassword.setOnClickListener {

            viewModel.onResend(email.text.toString())

        }

        rememberPassword.setOnClickListener {
            viewModel.onBack()
        }

        backToLogin.setOnClickListener {

            try {

                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_APP_EMAIL)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                this.startActivity(intent)

            } catch (e: ActivityNotFoundException) {
                showErrorInfo(requireContext(), getString(R.string.no_email_app))
            }

//            viewModel.onBack()
        }

        resetPassword.setOnClickListener {
            viewModel.onBack()
        }

        val spannable = SpannableString(getString(R.string.rem_pass))
        val highlightedStr = getString(R.string.forgotpass_login)
        val start = spannable.indexOf(highlightedStr)
        spannable.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.clickable_subtext
                )
            ),
            start,
            start + highlightedStr.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        rememberPassword.text = spannable

        val spannable2 = SpannableString(getString(R.string.err_email_restart))
        val highlightedStr2 = getString(R.string.forgotpass_login_reset)
        val start2 = spannable2.indexOf(highlightedStr2)
        spannable2.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.clickable_subtext
                )
            ),
            start2,
            start2 + highlightedStr2.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        resetPassword.text = spannable2

        val spannable3 = SpannableString(getString(R.string.receive_no_link_resend))
        val highlightedStr3 = getString(R.string.resend)
        val start3 = spannable3.indexOf(highlightedStr3)
        spannable3.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.clickable_subtext
                )
            ),
            start3,
            start3 + highlightedStr3.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        resendPassword.text = spannable3


    }


    override fun setObservers() {
        viewModel.actionStream.observe(viewLifecycleOwner) {
            if (it == ForgotPasswordViewModel.ACTION.ShowSuccessState) succcessState.visibility =
                View.VISIBLE
        }

    }
}