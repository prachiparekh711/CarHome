package ro.westaco.carhome.presentation.screens.auth

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.webkit.WebView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import ro.westaco.carhome.data.sources.remote.responses.models.TermsResponseItem
import ro.westaco.carhome.dialog.DialogUtils
import ro.westaco.carhome.presentation.base.BaseActivity
import ro.westaco.carhome.presentation.base.ContextWrapper
import ro.westaco.carhome.utils.BiometricUtil
import ro.westaco.carhome.utils.RegexData
import java.util.*
import java.util.concurrent.Executor


//C - Separate Auth Activity
@AndroidEntryPoint
class AuthActivity : BaseActivity<AuthModel>(),
    TermsAdapter.OnTermsClickListner {

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
    var termsAdapter: TermsAdapter? = null

    override fun setupUi() {


        if (firebaseAuth.currentUser != null && firebaseAuth.currentUser?.isEmailVerified == true) {
            if (!AppPreferencesDelegates.get().biometricSetUp) {
                if (BiometricUtil.isHardwareAvailable(this@AuthActivity) && BiometricUtil.hasBiometricEnrolled(
                        this@AuthActivity
                    )
                ) {
                    viewModel.navigateToBiometric()
                } else {
                    intiUi()
                }
            } else {
                intiUi()
            }
        } else {
            intiUi()
        }
    }

    private fun intiUi() {
        BIOMETRICS = AppPreferencesDelegates.get().biometric
        BIOMETRICS_MODE = AppPreferencesDelegates.get().biometricMode
        BIOMETRICS_SETUP = AppPreferencesDelegates.get().biometricSetUp

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

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.gcp_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        google.setOnClickListener {
            if (viewModel.authStateLiveData.value == AuthModel.STATE.Login) {
                googleSignInClient.signOut()
                LoginManager.getInstance().logOut()
                viewModel.onGoogleAuth()
            } else {
                if (checkTerms()) {
                    email2.text = null
                    password.text = null
                    confirmPassword.text = null
                    googleSignInClient.signOut()
                    LoginManager.getInstance().logOut()
                    viewModel.onGoogleAuth()
                } else {
                    DialogUtils.showErrorInfo(
                        this@AuthActivity,
                        resources.getString(R.string.terms_info)
                    )
                }
            }
        }

        facebook.setOnClickListener {
            if (viewModel.authStateLiveData.value == AuthModel.STATE.Login) {
                viewModel.onFacebookAuth()
            } else {
                if (checkTerms()) {
                    email2.text = null
                    password.text = null
                    confirmPassword.text = null
                    viewModel.onFacebookAuth()
                } else {
                    DialogUtils.showErrorInfo(
                        this@AuthActivity,
                        resources.getString(R.string.terms_info)
                    )
                }
            }
        }

        fbCallbackManager = CallbackManager.Factory.create()

        forgotPassword.setOnClickListener {
            viewModel.onForgotPassword()
        }

        switchAuthCta.setOnClickListener {
            viewModel.onSwitchAuth()
        }

        revealPassword.setOnClickListener {
            viewModel.onRevealPasswordClicked()
        }

        revealConfirmPassword.setOnClickListener {
            viewModel.onRevealConfirmPasswordClicked()
        }


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

        viewModel.termsLiveData.observe(this) { termsList ->
            if (!termsList.isNullOrEmpty()) {
                termsAdapter = TermsAdapter(this@AuthActivity, termsList, this)
                termsRV.layoutManager =
                    LinearLayoutManager(this@AuthActivity, RecyclerView.VERTICAL, false)
                termsRV.adapter = termsAdapter
            }
        }

        viewModel.authStateLiveData.observe(this) { state ->
            var switchDescriptionResId = R.string.switch_to_signup
            var switchCtaResId = R.string.sign_up

            when (state) {
                AuthModel.STATE.Login -> {
                    confirmPasswordGroup2.visibility = View.GONE
                    forgotPassword.visibility = View.VISIBLE
                    authCta2.alpha = 1F
                    authCta2.text = getString(R.string.login)
                    authCta2.isEnabled = true
                    or2.text = getString(R.string.login_with)
                    termsRV.isVisible = false
                    switchAuthCta.text = getString(R.string.switch_to_signup)
                    switchDescriptionResId = R.string.switch_to_signup
                    switchCtaResId = R.string.sign_up
                    authCta2.setOnClickListener {
                        if (!RegexData.checkEmailRegex(email2.text.toString())) {
                            DialogUtils.showErrorInfo(
                                this@AuthActivity,
                                getString(R.string.invalid_email)
                            )
                            return@setOnClickListener
                        }
                        viewModel.onLogin(email2.text.toString(), password.text.toString())
                    }
                }
                AuthModel.STATE.SignUp -> {
                    confirmPasswordGroup2.visibility = View.VISIBLE
                    forgotPassword.visibility = View.GONE
                    authCta2.alpha = 0.4F
                    authCta2.isEnabled = false
                    authCta2.text = getString(R.string.sign_up)
                    or2.text = getString(R.string.sign_up_with)
                    termsRV.isVisible = true
                    switchAuthCta.text = getString(R.string.switch_to_login)
                    switchDescriptionResId = R.string.switch_to_login
                    switchCtaResId = R.string.login
                    authCta2.setOnClickListener {
                        if (!RegexData.checkEmailRegex(email2.text.toString())) {
                            DialogUtils.showErrorInfo(
                                this@AuthActivity,
                                getString(R.string.invalid_email)
                            )
                            return@setOnClickListener
                        }
                        val termsList = termsAdapter?.getItems()

                        if (termsList != null) {
                            viewModel.onSignup(
                                email2.text.toString(),
                                password.text.toString(),
                                confirmPassword.text.toString(),
                                termsList
                            )
                        }
                    }
                }
            }

            setupSpannable(switchDescriptionResId, switchCtaResId)
        }

        viewModel.actionStream.observe(this) {
            when (it) {
                is AuthModel.ACTION.LaunchSignInWithEmailAndPassword -> {
                    launchSignInWithEmailAndPassword(it.email, it.pass)
                }

                is AuthModel.ACTION.CreateUserWithEmailAndPassword -> {
                    createUserWithEmailAndPassword(it.email, it.pass)
                }

                is AuthModel.ACTION.LaunchSignInWithGoogle -> {
                    launchSignInWithGoogle()
                }

                is AuthModel.ACTION.LaunchSignInWithFacebook -> {
                    launchSignInWithFacebook()
                }

                is AuthModel.ACTION.ChangePasswordState -> {
                    password.inputType =
                        if (it.isHidden) {
                            revealPassword.setImageDrawable(resources.getDrawable(R.drawable.ic_eye_slash))
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        } else {
                            revealPassword.setImageDrawable(resources.getDrawable(R.drawable.ic_eye))
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        }
                }

                is AuthModel.ACTION.ChangeConfirmPasswordState -> {
                    confirmPassword.inputType =
                        if (it.isHidden) {
                            revealConfirmPassword.setImageDrawable(resources.getDrawable(R.drawable.ic_eye_slash))
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        } else {
                            revealConfirmPassword.setImageDrawable(resources.getDrawable(R.drawable.ic_eye))
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        }
                }

//*             Authenticate via biometrics (S4)
                is AuthModel.ACTION.AuthenticateViaBiometrics -> {
                    startAuthentication()
                }

                is AuthModel.ACTION.UserFailure -> {
                    authBackground2.isVisible = false
                }

                is AuthModel.ACTION.NetworkFailure -> {
//                    noInternetDialog()
                }

                is AuthModel.ACTION.UserSuccess -> {
                    authBackground2.isVisible = true
                }

                is AuthModel.ACTION.SetSharedPrefrences -> {
                    AppPreferencesDelegates.get().biometric = false
                }

                is AuthModel.ACTION.UserRetrievalSuccess -> {
                    userRetrievalSuccess()
                }

                is AuthModel.ACTION.LoginSuceess -> {
                    viewModel.saveTerms(termsAdapter?.getItems())
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
                showBiometricDialog()
            }
        }
    }

    private fun showBiometricDialog() {
        authBackground2.isVisible = true
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(
            this, executor,
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
                .setTitle(baseContext.resources.getString(R.string.bio_login_title))
                .setNegativeButtonText(baseContext.resources.getString(R.string.cancel))
                .setConfirmationRequired(false)
                .build()
        } else {
            BiometricPrompt.PromptInfo.Builder()
                .setTitle(baseContext.resources.getString(R.string.bio_login_title))
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .setNegativeButtonText(baseContext.resources.getString(R.string.cancel))
                .build()
        }
        biometricPrompt.authenticate(promptInfo)
    }

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private fun setupSpannable(
        @StringRes switchDescriptionResId: Int,
        @StringRes switchCtaResId: Int
    ) {
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
                override fun onSuccess(result: LoginResult) {
                    handleFacebookAccessToken(result.accessToken)
                }

                override fun onCancel() {

                }

                override fun onError(error: FacebookException) {
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

    override fun onTermsClick(item: TermsResponseItem) {
        showBottomSheetDialog(item)
    }

    override fun onChecked() {
        checkTerms()
    }

    private fun checkTerms(): Boolean {
        val termsList = termsAdapter?.getItems()
        if (termsList != null) {
            for (i in termsList.indices) {
                if (termsList[i].mandatory == true) {
                    if (!termsList[i].allowed) {
                        authCta2.alpha = 0.4F
                        authCta2.isEnabled = false
                    } else {
                        authCta2.alpha = 1F
                        authCta2.isEnabled = true
                    }
                }
            }
        }
        return authCta2.isEnabled
    }

    private fun showBottomSheetDialog(
        item: TermsResponseItem
    ) {
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(R.layout.layout_bottom_sheet)

        bottomSheetDialog.findViewById<TextView>(R.id.title)?.text = item.title
        val webSettings = bottomSheetDialog.webView.settings
        webSettings.javaScriptEnabled = true

        bottomSheetDialog.webView.loadUrl("https://carhome-build.westaco.com/carhome/rest/public/terms/" + item.versionId)
        WebView.setWebContentsDebuggingEnabled(false)
        bottomSheetDialog.findViewById<ImageView>(R.id.dismiss)?.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.findViewById<LinearLayout>(R.id.btnDisagree)?.setOnClickListener {
            item.allowed = false
            checkTerms()
            termsAdapter?.notifyDataSetChanged()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.findViewById<LinearLayout>(R.id.btnAgree)?.setOnClickListener {
            item.allowed = true
            checkTerms()
            termsAdapter?.notifyDataSetChanged()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }
}