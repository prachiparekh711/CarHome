package ro.westaco.carhome.presentation.screens.splash

import android.annotation.SuppressLint
import android.content.Context
import dagger.hilt.android.AndroidEntryPoint
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import ro.westaco.carhome.presentation.base.BaseActivity
import ro.westaco.carhome.presentation.base.ContextWrapper
import java.util.*


@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : BaseActivity<SplashViewModel>() {

    override fun getContentView() = R.layout.activity_splash

    override fun setupUi() {
    }

    override fun attachBaseContext(newBase: Context) {

        if (AppPreferencesDelegates.get().language.isEmpty()) {
            AppPreferencesDelegates.get().language = "en-US"
        }

        val newLocale: Locale = if (AppPreferencesDelegates.get().language == "en-US") {
            Locale("en")
        } else {
            Locale("ro")
        }
        val context: Context = ContextWrapper.wrap(newBase, newLocale)
        super.attachBaseContext(context)
    }

    override fun setupObservers() {

    }

}