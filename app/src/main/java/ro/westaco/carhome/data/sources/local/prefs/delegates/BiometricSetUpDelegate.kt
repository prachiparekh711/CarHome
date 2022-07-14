package ro.westaco.carhome.data.sources.local.prefs.delegates

import com.pixplicity.easyprefs.library.Prefs
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class BiometricSetUpDelegate : ReadWriteProperty<AppPreferencesDelegates, Boolean> {

    companion object {
        const val BIOMETRICS_SETUP = "BIOMETRICS_SETUP"
    }

    override fun getValue(thisRef: AppPreferencesDelegates, property: KProperty<*>): Boolean =
        Prefs.getBoolean(BIOMETRICS_SETUP, false)

    override fun setValue(
        thisRef: AppPreferencesDelegates,
        property: KProperty<*>,
        value: Boolean
    ) {
        Prefs.putBoolean(BIOMETRICS_SETUP, value)
    }
}