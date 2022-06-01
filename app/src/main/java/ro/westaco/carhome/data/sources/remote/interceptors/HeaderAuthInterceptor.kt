package ro.westaco.carhome.data.sources.remote.interceptors

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.Response
import ro.westaco.carhome.BuildConfig
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import ro.westaco.carhome.data.sources.remote.responses.ApiResponse
import ro.westaco.carhome.presentation.base.BaseActivity.Companion.instance
import java.io.IOException


class HeaderAuthInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {

        val token = "Bearer ${AppPreferencesDelegates.get().token}"
        val language = AppPreferencesDelegates.get().language
        val version = BuildConfig.VERSION_NAME
        val os = "android"
        val device = "${android.os.Build.BRAND} ${android.os.Build.MODEL}"

        val original = chain.request()
        val builder = original.newBuilder()
            .header("Authorization", token)
            .header("Version", version)
            .header("OS", os)
            .header("Device", device)
            .header("Accept-Language", language)
        val request = builder.build()
        val response = chain.proceed(request)

        if (!response.isSuccessful) {

            val gson = GsonBuilder().create()
            try {
                val pojo = gson.fromJson(
                    response.peekBody(Long.MAX_VALUE).string(),
                    ApiResponse::class.java
                )

                if (!pojo.success) {

                    if (pojo.errorCode != "FILE_NOT_FOUND") {
//                    Log.e("request", request.toString())
//                    Log.e("pojo", pojo.toString())

                        Handler(Looper.getMainLooper()).post {

                            instance?.let {
                                MaterialAlertDialogBuilder(
                                    it,
                                    R.style.ThemeOverlay_App_MaterialAlertDialog
                                )
                                    .setTitle(instance?.resources?.getString(R.string.error))
                                    .setMessage(pojo.errorDetails)
                                    .setPositiveButton("Ok", null)
                                    .show()
                            }

                        }
                    }

                }
            } catch (e: IOException) {
                e.message.toString().let { Log.e("IOException:", it) }
            }

        }

        return response
    }

}