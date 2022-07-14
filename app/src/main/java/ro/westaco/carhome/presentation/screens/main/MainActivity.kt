package ro.westaco.carhome.presentation.screens.main

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Lifecycle
import dagger.hilt.android.AndroidEntryPoint
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import ro.westaco.carhome.data.sources.remote.responses.models.ProfileItem
import ro.westaco.carhome.data.sources.remote.responses.models.Siruta
import ro.westaco.carhome.presentation.base.BaseActivity
import ro.westaco.carhome.presentation.base.ContextWrapper
import ro.westaco.carhome.utils.SirutaUtil.Companion.countyList
import ro.westaco.carhome.utils.SirutaUtil.Companion.defaultCity
import ro.westaco.carhome.utils.SirutaUtil.Companion.defaultCounty
import ro.westaco.carhome.utils.SirutaUtil.Companion.sirutaList
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
        var profileItem: ProfileItem? = null
        var activeService: String = ""
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
            }
        } catch (e: PackageManager.NameNotFoundException) {
        } catch (e: NoSuchAlgorithmException) {
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
        sirutaList.clear()
        countyList.clear()
        defaultCounty = null
        defaultCity = null
    }

    override fun setupObservers() {
        viewModel.sirutaData.observe(this) { sirutaData ->
            if (this.lifecycle.currentState == Lifecycle.State.RESUMED) {

                if (sirutaData != null) {
                    sirutaList = sirutaData
                    sirutaList.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
                    for (i in sirutaList.indices) {
                        if (sirutaList[i].parentCode == null && !countyList.contains(sirutaList[i])) {
                            countyList.add(sirutaList[i])
                        }
                    }

                    countyList.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
                    defaultCounty = countyList[0]

                    if (defaultCounty != null) {
                        val cityList: ArrayList<Siruta> = ArrayList()
                        for (i in sirutaList.indices) {
                            if (defaultCounty?.code == sirutaList[i].parentCode) {
                                cityList.add(sirutaData[i])
                            }
                        }
                        cityList.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
                        defaultCity = cityList[0]
                    }
                }
            }
        }
    }

}