package ro.westaco.carhome.prefrences

import android.content.Context


open class SharedPrefrences {

    companion object {
        private val MyPREFERENCES = "CarHome"
        var BIOMETRICS: String = "BIOMETRICS"
        var BIOMETRICS_SETUP: String = "BIOMETRICS_SETUP"
        var BIOMETRICS_MODE: String = "BIOMETRICS_MODE"
        var CAR_MODE: String = "CAR_MODE"
        var TC: String = "TC"
        var GDPR: String = "GDPR"

        fun setBiometricsSetup(c1: Context, biometricStatus: Boolean) {
            val prefs = c1.getSharedPreferences(
                MyPREFERENCES,
                Context.MODE_PRIVATE
            )
            val edit = prefs.edit()
            edit.putBoolean(
                BIOMETRICS_SETUP,
                biometricStatus
            )
            edit.commit()
        }

        fun getBiometricsSetup(c1: Context): Boolean {
            val sharedpreferences = c1.getSharedPreferences(
                MyPREFERENCES,
                Context.MODE_PRIVATE
            )
            return sharedpreferences.getBoolean(
                BIOMETRICS_SETUP,
                false
            )
        }

        fun setBiometricsStatus(c1: Context, biometricStatus: Boolean) {
            val prefs = c1.getSharedPreferences(
                MyPREFERENCES,
                Context.MODE_PRIVATE
            )
            val edit = prefs.edit()
            edit.putBoolean(
                BIOMETRICS,
                biometricStatus
            )
            edit.commit()
        }

        fun getBiometricsStatus(c1: Context): Boolean {
            val sharedpreferences = c1.getSharedPreferences(
                MyPREFERENCES,
                Context.MODE_PRIVATE
            )
            return sharedpreferences.getBoolean(
                BIOMETRICS,
                false
            )
        }

        fun setBiometricsMode(c1: Context, biometricMode: String) {
            val prefs = c1.getSharedPreferences(
                MyPREFERENCES,
                Context.MODE_PRIVATE
            )
            val edit = prefs.edit()
            edit.putString(
                BIOMETRICS_MODE,
                biometricMode
            )
            edit.commit()
        }

        fun getBiometricsMode(c1: Context): String? {
            val sharedpreferences = c1.getSharedPreferences(
                MyPREFERENCES,
                Context.MODE_PRIVATE
            )
            return sharedpreferences.getString(
                BIOMETRICS_MODE,
                " "
            )
        }

        fun setCarMode(c1: Context, carMode: String) {
            val prefs = c1.getSharedPreferences(
                MyPREFERENCES,
                Context.MODE_PRIVATE
            )
            val edit = prefs.edit()
            edit.putString(
                CAR_MODE,
                carMode
            )
            edit.commit()
        }

        fun getCarMode(c1: Context): String? {
            val sharedpreferences = c1.getSharedPreferences(
                MyPREFERENCES,
                Context.MODE_PRIVATE
            )
            return sharedpreferences.getString(
                CAR_MODE,
                " "
            )
        }

        fun setTCStatus(c1: Context, TCStatus: Boolean) {
            val prefs = c1.getSharedPreferences(
                MyPREFERENCES,
                Context.MODE_PRIVATE
            )
            val edit = prefs.edit()
            edit.putBoolean(
                TC,
                TCStatus
            )
            edit.commit()
        }

        fun getTCStatus(c1: Context): Boolean {
            val sharedpreferences = c1.getSharedPreferences(
                MyPREFERENCES,
                Context.MODE_PRIVATE
            )
            return sharedpreferences.getBoolean(
                TC,
                false
            )
        }

        fun setGDPRStatus(c1: Context, GDPRStatus: Boolean) {
            val prefs = c1.getSharedPreferences(
                MyPREFERENCES,
                Context.MODE_PRIVATE
            )
            val edit = prefs.edit()
            edit.putBoolean(
                GDPR,
                GDPRStatus
            )
            edit.commit()
        }

        fun getGDPRStatus(c1: Context): Boolean {
            val sharedpreferences = c1.getSharedPreferences(
                MyPREFERENCES,
                Context.MODE_PRIVATE
            )
            return sharedpreferences.getBoolean(
                GDPR,
                false
            )
        }

    }
}