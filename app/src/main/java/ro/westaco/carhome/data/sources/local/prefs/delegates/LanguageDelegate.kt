package ro.westaco.carhome.data.sources.local.prefs.delegates

import com.pixplicity.easyprefs.library.Prefs
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

//  C- LanguageDelegate Getter & Setter
class LanguageDelegate : ReadWriteProperty<AppPreferencesDelegates, String> {

    companion object {
        const val PREF_KEY_LANGUAGE = "language"
    }

    override fun getValue(thisRef: AppPreferencesDelegates, property: KProperty<*>): String =
        Prefs.getString(PREF_KEY_LANGUAGE, "")

    override fun setValue(thisRef: AppPreferencesDelegates, property: KProperty<*>, value: String) {
        Prefs.putString(PREF_KEY_LANGUAGE, value)
    }

}
