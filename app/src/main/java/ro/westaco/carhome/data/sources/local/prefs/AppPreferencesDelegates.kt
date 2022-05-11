package ro.westaco.carhome.data.sources.local.prefs

import ro.westaco.carhome.data.sources.local.prefs.delegates.LanguageDelegate
import ro.westaco.carhome.data.sources.local.prefs.delegates.LastLoginMillisDelegate
import ro.westaco.carhome.data.sources.local.prefs.delegates.TokenDelegate
import ro.westaco.carhome.data.sources.local.prefs.delegates.WasOnboardingSeenDelegate

class AppPreferencesDelegates private constructor() {
    var wasOnboardingSeen by WasOnboardingSeenDelegate()
    var token by TokenDelegate()
    var language by LanguageDelegate()
    var lastLoginMillis by LastLoginMillisDelegate()

    companion object {
        private var INSTANCE: AppPreferencesDelegates? = null
        fun get(): AppPreferencesDelegates = INSTANCE ?: AppPreferencesDelegates()
    }

}