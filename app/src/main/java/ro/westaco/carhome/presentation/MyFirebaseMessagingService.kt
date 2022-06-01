package ro.westaco.carhome.presentation

import android.content.Context
import com.google.firebase.messaging.FirebaseMessagingService


class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        getSharedPreferences("_", MODE_PRIVATE).edit().putString("fb", s).apply()
    }

    companion object {
        fun getToken(context: Context): String {
            return context.getSharedPreferences("_", MODE_PRIVATE).getString("fb", "empty")
                .toString()
        }
    }
}