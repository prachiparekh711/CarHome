package ro.westaco.carhome.data.sources.local.prefs.delegates

import com.pixplicity.easyprefs.library.Prefs
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class BiometricModeDelegate : ReadWriteProperty<AppPreferencesDelegates, String> {

    companion object {
        const val BIOMETRICS_MODE = "BIOMETRICS_MODE"
    }

    override fun getValue(thisRef: AppPreferencesDelegates, property: KProperty<*>): String =
        Prefs.getString(BIOMETRICS_MODE, "")

    override fun setValue(
        thisRef: AppPreferencesDelegates,
        property: KProperty<*>,
        value: String
    ) {
        Prefs.putString(BIOMETRICS_MODE, value)
    }
}