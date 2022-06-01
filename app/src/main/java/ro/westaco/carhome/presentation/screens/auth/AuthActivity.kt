package ro.westaco.carhome.presentation.screens.auth

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_auth.*
import kotlinx.android.synthetic.main.layout_bottom_sheet.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import ro.westaco.carhome.prefrences.SharedPrefrences
import ro.westaco.carhome.presentation.base.BaseActivity
import ro.westaco.carhome.presentation.base.ContextWrapper
import ro.westaco.carhome.utils.BiometricUtil
import java.util.*
import java.util.concurrent.Executor


//C - Separate Auth Activity
@AndroidEntryPoint
class AuthActivity : BaseActivity<AuthModel>() {

    private var firebaseAuth = FirebaseAuth.getInstance()

    // Google
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_GOOGLE_SIGN_IN by lazy { 100 }

    // Facebook
    private lateinit var fbCallbackManager: CallbackManager

    override fun getContentView() = R.layout.activity_auth


    companion object {
        var BIOMETRICS: Boolean = false
        var BIOMETRICS_MODE: String = ""
        var BIOMETRICS_SETUP: Boolean = false
    }

    var dialogError: BottomSheetDialog? = null
    var dialogRetry: BottomSheetDialog? = null
    var mErrorText: TextView? = null
    var mTryagain: TextView? = null
    var mClose: TextView? = null
    var termFlag: Boolean = false
    var gdprFlag: Boolean = false


    override fun setupUi() {
        if (firebaseAuth.currentUser != null && firebaseAuth.currentUser?.isEmailVerified == true) {
            if (!SharedPrefrences.getBiometricsSetup(this)) {
                if (BiometricUtil.isHardwareAvailable(this@AuthActivity) && BiometricUtil.hasBiometricEnrolled(
                        this@AuthActivity
                    )
                )
                    viewModel.navigateToBiometric()
                else
                    intiUi()
            } else {
                intiUi()
            }
        } else {
            intiUi()
        }
    }

    private fun intiUi() {


        val view = layoutInflater.inflate(R.layout.biometric_failure_layout, null)
        dialogError = BottomSheetDialog(this)
        dialogError?.setCancelable(false)
        dialogError?.setContentView(view)
        mErrorText = view.findViewById(R.id.mErrorText)

        val view1 = layoutInflater.inflate(R.layout.boimetric_retry_layout, null)
        dialogRetry = BottomSheetDialog(this)
        dialogRetry?.setCancelable(false)
        dialogRetry?.setContentView(view1)
        mTryagain = view1.findViewById(R.id.mTryagain)
        mClose = view1.findViewById(R.id.mClose)

        mTryagain?.setOnClickListener {
            dialogRetry?.dismiss()
            startAuthentication()
        }

        mClose?.setOnClickListener {
            dialogRetry?.dismiss()
            finishAffinity()
        }

        google.setOnClickListener {
            viewModel.onGoogleAuth()
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.gcp_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        facebook.setOnClickListener {
            viewModel.onFacebookAuth()
        }

        fbCallbackManager = CallbackManager.Factory.create()

        forgotPassword.setOnClickListener {
            viewModel.onForgotPassword()
        }

        termFlag = SharedPrefrences.getTCStatus(this)
        gdprFlag = SharedPrefrences.getGDPRStatus(this)
        if (termFlag && gdprFlag) {
            checkBox.setImageResource(R.drawable.checkbox_background)
        } else {
            checkBox.setImageResource(R.drawable.uncheckbox_background)
        }
        checkBox.setOnClickListener {
            termFlag = SharedPrefrences.getTCStatus(this)
            gdprFlag = SharedPrefrences.getGDPRStatus(this)
            if (termFlag && gdprFlag) {
                checkBox.setImageResource(R.drawable.uncheckbox_background)
                SharedPrefrences.setGDPRStatus(this, false)
                SharedPrefrences.setTCStatus(this, false)
            } else {
                termsCheckBottomSheet()
            }
        }

        switchAuthCta.setOnClickListener {
            viewModel.onSwitchAuth()
        }

        revealPassword.setOnClickListener {
            viewModel.onRevealPasswordClicked()
        }

        revealConfirmPassword.setOnClickListener {
            viewModel.onRevealConfirmPasswordClicked()
//            requireActivity().onBackPressed()
        }

    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun termsCheckBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_bottom_sheet, null)
        dialog.setCancelable(false)
        dialog.setContentView(view)

        val webSettings = dialog.webView.settings
        webSettings.javaScriptEnabled = true
        if (!termFlag) {
            dialog.title.text = getString(R.string.terms_services)
            dialog.webView.loadUrl(this.resources.getString(R.string.tc))
        } else {
            dialog.title.text = getString(R.string.terms_gdpr)
            dialog.webView.loadUrl(this.resources.getString(R.string.gdpr))
        }
        WebView.setWebContentsDebuggingEnabled(false)

        dialog.btnDisagree.setOnClickListener {
            if (dialog.title.text == getString(R.string.terms_services)) {
                dialog.title.text = getString(R.string.terms_gdpr)
                dialog.webView.loadUrl(this.resources.getString(R.string.gdpr))
            } else {
                dialog.dismiss()
            }
            checkBox.setImageResource(R.drawable.uncheckbox_background)
        }

        dialog.btnAgree.setOnClickListener {
            if (dialog.title.text == getString(R.string.terms_services)) {
                SharedPrefrences.setTCStatus(this, true)
                dialog.title.text = getString(R.string.terms_gdpr)
                dialog.webView.loadUrl(this.resources.getString(R.string.gdpr))
            } else if (dialog.title.text == getString(R.string.terms_gdpr)) {
                SharedPrefrences.setGDPRStatus(this, true)
                checkBox.setImageResource(R.drawable.checkbox_background)
                dialog.dismiss()
            }
        }

        dialog.dismiss.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun attachBaseContext(newBase: Context) {

        val newLocale: Locale = if (AppPreferencesDelegates.get().language == "en-US") {
            Locale("en")
        } else {
            Locale("ro")
        }

        val context: Context = ContextWrapper.wrap(newBase, newLocale)
        super.attachBaseContext(context)
    }

    override fun setupObservers() {

        BIOMETRICS = SharedPrefrences.getBiometricsStatus(this)
        BIOMETRICS_MODE = SharedPrefrences.getBiometricsMode(this).toString()
        BIOMETRICS_SETUP = SharedPrefrences.getBiometricsSetup(this)
        viewModel.authStateLiveData.observe(this) { state ->
            var switchDescriptionResId = R.string.switch_to_signup
            var switchCtaResId = R.string.sign_up

            when (state) {
                AuthModel.STATE.Login -> {
                    confirmPasswordGroup2.visibility = View.GONE
                    forgotPassword.visibility = View.VISIBLE
                    authCta2.text = getString(R.string.login)
                    or2.text = getString(R.string.login_with)
                    termsDescription2.visibility = View.GONE
                    checkBox.visibility = View.GONE
                    //termsDescription.text = getString(R.string.auth_login_terms_description)
                    switchAuthCta.text = getString(R.string.switch_to_signup)
                    //termsDescriptionResId = R.string.auth_login_terms_description
                    switchDescriptionResId = R.string.switch_to_signup
                    switchCtaResId = R.string.sign_up
                    authCta2.setOnClickListener {
                        viewModel.onLogin(email2.text.toString(), password.text.toString())
                    }
                }
                AuthModel.STATE.SignUp -> {
                    confirmPasswordGroup2.visibility = View.VISIBLE
                    forgotPassword.visibility = View.GONE
                    authCta2.text = getString(R.string.sign_up)
                    or2.text = getString(R.string.sign_up_with)
                    termsDescription2.visibility = View.VISIBLE
                    checkBox.visibility = View.VISIBLE
                    termsDescription2.text = getString(R.string.sign_up_terms)
                    switchAuthCta.text = getString(R.string.switch_to_login)
                    switchDescriptionResId = R.string.switch_to_login
                    switchCtaResId = R.string.login
                    authCta2.setOnClickListener {
                        viewModel.onSignup(
                            email2.text.toString(),
                            password.text.toString(),
                            confirmPassword.text.toString()
                        )
                    }
                }
            }

            setupSpannables(switchDescriptionResId, switchCtaResId)
        }

        viewModel.actionStream.observe(this) {
            when (it) {
                is AuthModel.ACTION.LaunchSignInWithEmailAndPassword -> {
                    launchSignInWithEmailAndPassword(it.email, it.pass)
                }

                is AuthModel.ACTION.CreateUserWithEmailAndPassword -> {
                    createUserWithEmailAndPassword(it.email, it.pass)
                }

                AuthModel.ACTION.LaunchSignInWithGoogle -> {
                    launchSignInWithGoogle()
                }

                AuthModel.ACTION.LaunchSignInWithFacebook -> {
                    launchSignInWithFacebook()
                }

                is AuthModel.ACTION.ChangePasswordState -> {
                    password.inputType =
                        if (it.isHidden)
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        else
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                }

                is AuthModel.ACTION.ChangeConfirmPasswordState -> {
                    confirmPassword.inputType =
                        if (it.isHidden)
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        else
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                }

//*             Authenticate via biometrics (S4)
                is AuthModel.ACTION.AuthenticateViaBiometrics -> {
                    startAuthentication()
                }

                is AuthModel.ACTION.UserFailure -> {
                    authBackground2.isVisible = false
                }


                is AuthModel.ACTION.UserSuccess -> {
                    authBackground2.isVisible = true
                }

                is AuthModel.ACTION.SetSharedPrefrences -> {
                    SharedPrefrences.setBiometricsStatus(this, false)
                }

                is AuthModel.ACTION.UserRetrievalSuccess -> {
                    userRetrievalSuccess()
                }
            }
        }

    }

    private fun userRetrievalSuccess() {
        if (BiometricUtil.isHardwareAvailable(this) && BiometricUtil.hasBiometricEnrolled(this)) {

            viewModel.navigateToBiometric()

        } else {

            viewModel.navigateToProgress()
        }
    }

    fun startAuthentication() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                showBiomertricDialog()
            }
        }
    }

    private fun showBiomertricDialog() {
        authBackground2.isVisible = true
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                @SuppressLint("SwitchIntDef")
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    when (errorCode) {
                        android.hardware.biometrics.BiometricPrompt.BIOMETRIC_ERROR_LOCKOUT -> {
                            dialogError?.show()
                            var i = 30
                            val mainHandler = Handler(Looper.getMainLooper())
                            mainHandler.post(object : Runnable {
                                override fun run() {
                                    i--
                                    if (i < 0) {
                                        dialogError?.dismiss()
                                        startAuthentication()
                                    } else {
                                        mErrorText?.text = getString(R.string.incorrect_attempt, i)
                                        mainHandler.postDelayed(this, 1000)
                                    }
                                }
                            })
                        }
                        13 -> {
                            dialogRetry?.show()
                        }
                        android.hardware.biometrics.BiometricPrompt.BIOMETRIC_ERROR_USER_CANCELED -> {
                            dialogRetry?.show()
                        }
                    }
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    viewModel.onUserSuccess()
                }
            })

        promptInfo = if (BIOMETRICS_MODE == "BOTH") {
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Login")
                .setNegativeButtonText("Cancel")
                .setConfirmationRequired(false)
                .build()
        } else {
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Login")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .setNegativeButtonText("Cancel")
                .build()
        }
        biometricPrompt.authenticate(promptInfo)
    }

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private fun setupSpannables(
        @StringRes switchDescriptionResId: Int,
        @StringRes switchCtaResId: Int
    ) {


        val termsDescriptionSpannable =
            SpannableString(baseContext.resources.getString(R.string.sign_up_terms))
//        SpannableString(getString(termsDescriptionResId))
        // apply terms span
        val termsStr = resources.getString(R.string.terms_services)
//        getString(R.string.terms_services)
        val termsStart = termsDescriptionSpannable.indexOf(termsStr)

//        Log.e("spannable:", termsDescriptionSpannable.toString())
//        Log.e("language:", AppPreferencesDelegates.get().language)
        if (AppPreferencesDelegates.get().language == resources.getString(R.string.english_lan)) {


            termsDescriptionSpannable.setSpan(
                StyleSpan(Typeface.BOLD),
                termsStart,
                termsStart + termsStr.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            termsDescriptionSpannable.setSpan(
                object : ClickableSpan() {
                    override fun onClick(textView: View) {
                        viewModel.onTermsClicked(true)
                    }
                },
                termsStart,
                termsStart + termsStr.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

        } else {

            termsDescriptionSpannable.setSpan(
                StyleSpan(Typeface.BOLD),
                77,
                99,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            termsDescriptionSpannable.setSpan(
                object : ClickableSpan() {
                    override fun onClick(textView: View) {
                        viewModel.onTermsClicked(true)
                    }
                },
                77,
                99,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }


        // apply gdpr span
        val gdprStr = getString(R.string.terms_gdpr)
        val gdprStart = termsDescriptionSpannable.indexOf(gdprStr)
        termsDescriptionSpannable.setSpan(
            StyleSpan(Typeface.BOLD),
            gdprStart,
            gdprStart + gdprStr.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        termsDescriptionSpannable.setSpan(
            object : ClickableSpan() {
                override fun onClick(textView: View) {
                    viewModel.onGdprClicked(true)
                }
            },
            gdprStart,
            gdprStart + gdprStr.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        // set final terms description text

        termsDescription2.movementMethod = LinkMovementMethod.getInstance()
        termsDescription2.text = termsDescriptionSpannable

        val switchDescriptionSpannable = SpannableString(getString(switchDescriptionResId))
        val switchCtaStr = getString(switchCtaResId)
        val switchCtaStart = switchDescriptionSpannable.indexOf(switchCtaStr)
        switchDescriptionSpannable.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    this,
                    R.color.clickable_subtext
                )
            ),
            switchCtaStart,
            switchCtaStart + switchCtaStr.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        switchAuthCta.text = switchDescriptionSpannable
    }

    private fun launchSignInWithEmailAndPassword(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                viewModel.onLoginTaskCompleted(task)
            }
    }

    private fun createUserWithEmailAndPassword(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                viewModel.onSignupTaskCompleted(task)
            }
    }

    private fun launchSignInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    viewModel.onLoginSucceded()
                } else {
                    viewModel.onGoogleLoginFailed()
                }
            }
    }

    private fun launchSignInWithFacebook() {
        LoginManager.getInstance()
            .logInWithReadPermissions(this, listOf("email", "public_profile"))

        LoginManager.getInstance()
            .registerCallback(fbCallbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    handleFacebookAccessToken(loginResult.accessToken)
                }

                override fun onCancel() {

                }

                override fun onError(error: FacebookException) {
                    Log.e("fb_error", error.message.toString())
                    viewModel.onFacebookLoginFailed()
                }
            })
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    viewModel.onLoginSucceded()
                } else {
                    viewModel.onFacebookLoginFailed()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Google
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account as GoogleSignInAccount)
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
                viewModel.onGoogleLoginFailed()
            }
        }
        // Facebook
        if (data != null) {
            fbCallbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }


}