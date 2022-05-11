package ro.westaco.carhome.data.sources.local.prefs.delegates

import com.pixplicity.easyprefs.library.Prefs
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class LastLoginMillisDelegate : ReadWriteProperty<AppPreferencesDelegates, Long> {
    companion object {
        const val PREF_KEY_LAST_LOGIN = "last_login"
    }

    override fun getValue(thisRef: AppPreferencesDelegates, property: KProperty<*>): Long =
        Prefs.getLong(PREF_KEY_LAST_LOGIN, 0)

    override fun setValue(thisRef: AppPreferencesDelegates, property: KProperty<*>, value: Long) {
        Prefs.putLong(PREF_KEY_LAST_LOGIN, value)
    }
}
