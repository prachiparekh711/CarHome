package ro.westaco.carhome.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.view.inputmethod.InputMethodManager
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import ro.westaco.carhome.R
import ro.westaco.carhome.presentation.screens.auth.AuthActivity
import ro.westaco.carhome.presentation.screens.splash.SplashActivity

object DeviceUtils {

    fun hideKeyboard(context: Context) {
        try {
            val inputManager =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            (context as? Activity)?.currentFocus?.windowToken.let {
                inputManager.hideSoftInputFromWindow(it, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getProfileImage(context: Context, user: FirebaseUser?): String? {
        return if (user != null) {
            var facebookUserId = ""
            var googlePhotoUrl = ""
            for (profile in user.providerData) {
                // check if the provider id matches "facebook.com"
                if (FacebookAuthProvider.PROVIDER_ID == profile.providerId) {
                    facebookUserId = profile.uid
                } else if (GoogleAuthProvider.PROVIDER_ID == profile.providerId) {
                    googlePhotoUrl =
                        if (profile.photoUrl != null) profile.photoUrl.toString() else ""
                }
            }
            if (facebookUserId != "") {
                // facebook photo url
                context.getString(R.string.fb_photo_url_template, facebookUserId)
            } else if (googlePhotoUrl != "") {
                // google photo url
                googlePhotoUrl.replace("=s96-c", "=s500-c")
            } else {
                if (user.photoUrl != null) user.photoUrl.toString() else null
            }
        } else {
            null
        }
    }

    fun restartApp(context: Context) {
        val intent = Intent(context, SplashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun logoutApp(context: Context) {
        val intent = Intent(context, AuthActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun isOnline(app: Application): Boolean {
        val connMgr = app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = connMgr.activeNetworkInfo
        return networkInfo?.isConnected == true
    }
}