package ro.westaco.carhome

import com.google.firebase.auth.FirebaseAuth
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates

class AccountManager {

    companion object {
        fun refreshToken() {
            val firebaseAuth = FirebaseAuth.getInstance()
            firebaseAuth.currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    AppPreferencesDelegates.get().token = task.result.token.toString()
//                    Log.e("refreshToken", AppPreferencesDelegates.get().token)
                }
            }
        }
    }

}