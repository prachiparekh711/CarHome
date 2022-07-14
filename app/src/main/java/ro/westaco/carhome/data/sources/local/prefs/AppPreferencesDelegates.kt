package ro.westaco.carhome.data.sources.local.prefs

import ro.westaco.carhome.data.sources.local.prefs.delegates.*

class AppPreferencesDelegates private constructor() {
    var wasOnboardingSeen by WasOnboardingSeenDelegate()
    var token by TokenDelegate()
    var language by LanguageDelegate()
    var lastLoginMillis by LastLoginMillisDelegate()
    var biometric by BiometricDelegate()
    var biometricMode by BiometricModeDelegate()
    var biometricSetUp by BiometricSetUpDelegate()
    var carMode by CarModeDelegate()

    companion object {
        private var INSTANCE: AppPreferencesDelegates? = null
        fun get(): AppPreferencesDelegates = INSTANCE ?: AppPreferencesDelegates()
    }

}