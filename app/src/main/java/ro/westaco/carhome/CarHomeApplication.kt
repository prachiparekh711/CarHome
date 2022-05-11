package ro.westaco.carhome

import android.content.Context
import android.content.ContextWrapper
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.facebook.FacebookSdk
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.pixplicity.easyprefs.library.Prefs
import dagger.hilt.android.HiltAndroidApp
import ro.westaco.carhome.di.ApiModule.Companion.BASE_URL_PRODUCTION
import ro.westaco.carhome.di.ApiModule.Companion.BASE_URL_RESOURCES

@HiltAndroidApp
class CarHomeApplication : MultiDexApplication() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.FLAVOR == "CarHome_BuildBuildVersion") {
            BASE_URL_PRODUCTION = resources.getString(R.string.BASE_URL_PRODUCTION)
            BASE_URL_RESOURCES = resources.getString(R.string.BASE_URL_RESOURCES)
        } else {
            BASE_URL_PRODUCTION = resources.getString(R.string.TEST_URL_PRODUCTION)
            BASE_URL_RESOURCES = resources.getString(R.string.TEST_URL_RESOURCES)
        }

//        Bugsee.launch(this, getString(R.string.bugsee_token))

        MultiDex.install(this)
        FacebookSdk.setClientToken(resources.getString(R.string.facebook_client_token))
        FacebookSdk.sdkInitialize(applicationContext)
        Prefs.Builder()
            .setContext(this)
            .setMode(ContextWrapper.MODE_PRIVATE)
            .setPrefsName(packageName)
            .setUseDefaultSharedPreference(true)
            .build()

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)

    }

}