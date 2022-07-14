package ro.westaco.carhome.presentation.base

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.WebView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.analytics.FirebaseAnalytics
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.layout_bottom_sheet.*
import ro.westaco.carhome.CarHomeApplication
import ro.westaco.carhome.NetworkChangeReceiver
import ro.westaco.carhome.R
import ro.westaco.carhome.dialog.DialogUtils.Companion.showErrorInfo
import ro.westaco.carhome.dialog.InternetBottomSheetDialog
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.screens.dashboard.DashboardFragment
import ro.westaco.carhome.presentation.screens.dashboard.DashboardFragment.Companion.bnv
import ro.westaco.carhome.presentation.screens.dashboard.DashboardViewModel.Companion.selectedMenuItem
import ro.westaco.carhome.presentation.screens.home.HomeFragment
import ro.westaco.carhome.presentation.screens.onboarding.OnboardingViewModel
import ro.westaco.carhome.presentation.screens.splash.SplashViewModel
import ro.westaco.carhome.utils.DeviceUtils
import ro.westaco.carhome.utils.FirebaseAnalyticsList
import ro.westaco.carhome.utils.ViewUtils
import ro.westaco.carhome.views.Progressbar
import java.lang.reflect.ParameterizedType


abstract class BaseActivity<VM : BaseViewModel> : AppCompatActivity() {

    companion object {
        var instance: Context? = null
        var profileLogoListner: OnProfileLogoChangeListener? = null
        var sManager: FragmentManager? = null
        var isFragmentVisible = false
    }

    lateinit var viewModel: VM
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
    var progressbar1: Progressbar? = null
    private var mNetworkReceiver: BroadcastReceiver? = null
    private var networkReceiver: NetworkReceiver? = null

    interface OnProfileLogoChangeListener {
        fun onChangeLogo(uri: Uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getContentView())

        viewModel = ViewModelProvider(this)[getViewModelClass()]

        viewModel.uiEventStream.observe(this) { uiEvent -> processUiEvent(uiEvent) }

        setupUi()
        setupObservers()
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this@BaseActivity)
        viewModel.onActivityCreated()

        sManager = supportFragmentManager

        Handler(Looper.getMainLooper()).postDelayed({
            if (viewModel is SplashViewModel || viewModel is OnboardingViewModel) {
                return@postDelayed
            } else {
                mNetworkReceiver = NetworkChangeReceiver()
                registerReceiver(
                    mNetworkReceiver,
                    IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
                )
                networkReceiver = NetworkReceiver()
                networkReceiver.let {
                    if (it != null) {
                        LocalBroadcastManager.getInstance(baseContext).registerReceiver(
                            it,
                            IntentFilter("NETWORK")
                        )
                    }
                }
            }
        }, 800)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mNetworkReceiver != null)
            unregisterReceiver(mNetworkReceiver)
    }

    private class NetworkReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            val fragment = InternetBottomSheetDialog()
            if (!DeviceUtils.isOnline(CarHomeApplication.appInstance)) {
                if (!isFragmentVisible) {
                    sManager?.let { fragment.show(it, "no_internet") }
                    isFragmentVisible = true
                }
            } else {
                isFragmentVisible = false
//                if (isFragmentVisible) {
//                    fragment.dismiss()
//                    isFragmentVisible=false
//                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        instance = this
    }

    abstract fun getContentView(): Int
    abstract fun setupUi()
    abstract fun setupObservers()


    private fun getViewModelClass(): Class<VM> {
        val type = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
        return type as Class<VM>
    }

    fun processUiEvent(uiEvent: UiEvent) {

        when (uiEvent) {

            is UiEvent.Navigation -> {
                onNavigationEvent(uiEvent.navAttribs)
            }
            is UiEvent.NavBack -> {
                onBackPressed()
            }

            is UiEvent.GoToMain -> {
                selectedMenuItem = null
                supportFragmentManager.popBackStack(DashboardFragment.TAG, 0)
            }

            is UiEvent.OpenIntent -> {
                startActivity(uiEvent.intent)
                if (uiEvent.finishSourceActivity) {
                    finish()
                }
            }
            is UiEvent.HideKeyboard -> {
                DeviceUtils.hideKeyboard(this)
            }
            is UiEvent.ShowToast -> {
                showToast(getString(uiEvent.messageResId))
            }
            is UiEvent.ShowToastMsg -> {
                showToast(uiEvent.message)
            }
            is UiEvent.ShowBottomSheet -> {
                showBottomSheetDialog(
                    uiEvent.titleResId,
                    uiEvent.messageResId,
                    uiEvent.layoutResId,
                    uiEvent.linkClicked
                )
            }
            else -> {}
        }
    }

    private fun showToast(message: String) {
        showErrorInfo(this, message)
    }

    private fun showBottomSheetDialog(
        @StringRes titleResId: Int,
        @StringRes contentResId: Int,
        @LayoutRes layoutResId: Int,
        linkClicked: Boolean
    ) {
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(layoutResId)

        val title = getString(titleResId)
        val content = getString(contentResId)

        bottomSheetDialog.findViewById<TextView>(R.id.title)?.text = title
        val webSettings = bottomSheetDialog.webView.settings
        webSettings.javaScriptEnabled = true
        bottomSheetDialog.webView.loadUrl(content)
        WebView.setWebContentsDebuggingEnabled(false)
        bottomSheetDialog.findViewById<View>(R.id.dismiss)?.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.bottomLL.isVisible = !linkClicked

        val parentLayout =
            bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        parentLayout?.let { it ->
            val behaviour = BottomSheetBehavior.from(it)
            ViewUtils.setViewHeightAsWindowPercent(this, it, 85)
            behaviour.state = BottomSheetBehavior.STATE_EXPANDED
        }

        bottomSheetDialog.show()
    }

    private fun onNavigationEvent(navAttribs: NavAttribs) {
        navAttribs.screen?.let {
            DeviceUtils.hideKeyboard(this)
            handleNavigationEvent(navAttribs)
        }
    }

    private fun handleNavigationEvent(navAttribs: NavAttribs) {
        val screen: Screen? = navAttribs.screen

        progressbar1 = Progressbar(this@BaseActivity)
        progressbar1?.showPopup()
        Handler(Looper.getMainLooper()).postDelayed({
            if (screen != null) {
                try {
                    screen.fragmentClass.newInstance() as Fragment
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }?.let { newFragment ->
                    navAttribs.args?.let { args ->
                        newFragment.arguments = args
                    }

                    supportFragmentManager.beginTransaction().apply {
                        /*setCustomAnimations(
                            R.anim.slide_in,
                            R.anim.slide_out,
                            R.anim.slide_in,
                            R.anim.slide_out
                        )*/
                        setCustomAnimations(
                            R.anim.slide_in,
                            0,
                            R.anim.fade_in,
                            0
                        )
                        replace(R.id.content, newFragment, screen.name)
                        val fragments = supportFragmentManager.fragments
                        fragments.forEach {
                            hide(it)
                        }
                        if (navAttribs.addToBackStack) {
                            addToBackStack(screen.name)
                        }
                        commitAllowingStateLoss()
                    }
                }
            }
        }, 500)

        Handler(Looper.getMainLooper()).postDelayed({
            progressbar1?.dismissPopup()
        }, 800)
    }


    override fun onBackPressed() {
        DeviceUtils.hideKeyboard(this)

        val fragmentsNumber = supportFragmentManager.backStackEntryCount
        val f = getVisibleFragment()
        when {
            fragmentsNumber == 0 -> {
                selectedMenuItem = null
                if (f is DashboardFragment) {
                    val params = Bundle()
                    mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.APP_LEAVES_ANDROID, params)
                    finishAffinity()
                } else {
                    finish()
                }
            }
            fragmentsNumber > 0 -> {

                if (f is DashboardFragment) {
                    val c = getChildVisibleFragment(f)

                    if (c is HomeFragment) {
                        val params = Bundle()
                        mFirebaseAnalytics.logEvent(
                            FirebaseAnalyticsList.APP_LEAVES_ANDROID,
                            params
                        )
                        selectedMenuItem = null
                        finishAffinity()
                    } else {
                        bnv?.selectedItemId = R.id.home
                        f.childFragmentManager.popBackStack(HomeFragment.TAG, 0)
                    }
                } else {
                    supportFragmentManager.popBackStack()
                }
            }
        }
    }

    open fun getVisibleFragment(): Fragment? {
        val fragmentManager: FragmentManager = supportFragmentManager
        val fragments: List<Fragment> = fragmentManager.fragments
        fragments.reversed().forEach { fragment ->
            if (fragment.isVisible) return fragment
        }
        return null
    }

    open fun getChildVisibleFragment(fragment: Fragment): Fragment? {
        val fragmentManager: FragmentManager = fragment.childFragmentManager
        val fragments: List<Fragment> = fragmentManager.fragments
        for (fragment1 in fragments) {
            if (fragment1.isVisible) return fragment1
        }
        return null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val resultUri = result.uri

                profileLogoListner?.onChangeLogo(resultUri)
            }
        }
    }

}