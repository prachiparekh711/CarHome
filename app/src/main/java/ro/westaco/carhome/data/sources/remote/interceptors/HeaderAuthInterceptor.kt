package ro.westaco.carhome.data.sources.remote.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import ro.westaco.carhome.BuildConfig
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
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
        return chain.proceed(request)
    }
}