package ro.westaco.carhome.presentation.base

import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.layout_bottom_sheet.*
import ro.westaco.carhome.R
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.screens.dashboard.DashboardFragment
import ro.westaco.carhome.presentation.screens.dashboard.DashboardFragment.Companion.bnv
import ro.westaco.carhome.presentation.screens.dashboard.DashboardViewModel.Companion.selectedMenuItem
import ro.westaco.carhome.presentation.screens.home.HomeFragment
import ro.westaco.carhome.utils.DeviceUtils
import ro.westaco.carhome.utils.FirebaseAnalyticsList
import ro.westaco.carhome.utils.ViewUtils
import java.lang.reflect.ParameterizedType


abstract class BaseActivity<VM : BaseViewModel> : AppCompatActivity() {


    lateinit var viewModel: VM
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getContentView())

        viewModel = ViewModelProvider(this).get(getViewModelClass())

        viewModel.uiEventStream.observe(this) { uiEvent -> processUiEvent(uiEvent) }

        setupUi()
        setupObservers()
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this@BaseActivity)
        viewModel.onActivityCreated()
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
        }
    }

//    fun updateConfig(wrapper: ContextThemeWrapper) {
//        if(dLocale==Locale("") )
//            return
//        Locale.setDefault(dLocale)
//        val configuration = Configuration()
//        configuration.setLocale(dLocale)
//        wrapper.applyOverrideConfiguration(configuration)
//    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
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
                    replace(R.id.content, newFragment, screen.name)
                    if (navAttribs.addToBackStack) {
                        addToBackStack(screen.name)
                    }
                    commitAllowingStateLoss()
                }
            }
        }
    }


    override fun onBackPressed() {
        DeviceUtils.hideKeyboard(this)

        val fragmentsNumber = supportFragmentManager.backStackEntryCount
        val f = getVisibleFragment()
        when {
            fragmentsNumber == 0 -> {
                val params = Bundle()
                mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.APP_LEAVES_ANDROID, params)
                selectedMenuItem = null
                finishAffinity()
            }
            fragmentsNumber > 0 -> {
                if (f is DashboardFragment) {
                    val c = getChildVisibleFragment(f)
                    Log.e("c:", c.toString())
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


                /*if (!f?.tag.equals(DashboardFragment.TAG)) {
                    supportFragmentManager.popBackStack()
                } else {
                    if (f?.isVisible == true) {
                        val childFm = f.childFragmentManager
                        if (childFm.backStackEntryCount > 0) {
                            if (bottomNavigationView.selectedItemId != R.id.home) {
                                changeMenu()
                                childFm.popBackStack(HomeFragment.TAG, 0)
                                return
                            } else {
                                finish()
                                val params = Bundle()
                                mFirebaseAnalytics.logEvent("App_Leaves_AND", params)
                            }
                        } else {
                            finish()
                            val params = Bundle()
                            mFirebaseAnalytics.logEvent("App_Leaves_AND", params)
                        }
                    }
                }*/
            }
        }
    }

    /* override fun onBackPressed() {
         DeviceUtils.hideKeyboard(this)

         val fragmentsNumber = supportFragmentManager.backStackEntryCount

         when {
             fragmentsNumber == 1 -> finish()
             supportFragmentManager.backStackEntryCount > 1 -> supportFragmentManager.popBackStack()
             else -> super.onBackPressed()
         }
     }*/

    open fun getVisibleFragment(): Fragment? {
        val fragmentManager: FragmentManager = supportFragmentManager
        val fragments: List<Fragment> = fragmentManager.fragments
        for (fragment in fragments) {
            if (fragment.isVisible) return fragment
        }
        return null
    }

    open fun getChildVisibleFragment(fragment: Fragment): Fragment? {
        val fragmentManager: FragmentManager = fragment.childFragmentManager
        val fragments: List<Fragment> = fragmentManager.fragments
        for (fragment in fragments) {
            if (fragment.isVisible) return fragment
        }
        return null
    }

}