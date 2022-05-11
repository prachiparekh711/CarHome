package ro.westaco.carhome.presentation.screens.main

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import ro.westaco.carhome.presentation.base.BaseActivity
import ro.westaco.carhome.presentation.base.ContextWrapper
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


//C- Set Language
@AndroidEntryPoint
class MainActivity : BaseActivity<MainViewModel>() {

    override fun getContentView() = R.layout.activity_main

    companion object {
        var activeUser: String? = null
        var activeId: Int? = null

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            val info = packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_SIGNATURES
            )
            for (signature in info.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.e("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("e1:", e.message.toString())
        } catch (e: NoSuchAlgorithmException) {
            Log.e("e2:", e.message.toString())
        }

        Handler(Looper.getMainLooper()).postDelayed({
            when (intent.getStringExtra("navigate")) {
                "profile" -> viewModel.onEditAccount()
                "car_add" -> viewModel.onAddNewCar()
                "car_id" -> {
                    val cid = intent.getIntExtra("cid", 0)
                    viewModel.onEditCar(cid)
                }
            }
        }, 2000)

    }

    override fun setupUi() {

    }

    override fun setupObservers() {

    }

}