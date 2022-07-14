package ro.westaco.carhome.data.sources.remote.interceptors

import android.os.Handler
import android.os.Looper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.internal.http2.ConnectionShutdownException
import ro.westaco.carhome.AccountManager
import ro.westaco.carhome.BuildConfig
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import ro.westaco.carhome.data.sources.remote.responses.ApiResponse
import ro.westaco.carhome.dialog.NoServerDialog
import ro.westaco.carhome.presentation.base.BaseActivity.Companion.instance
import ro.westaco.carhome.presentation.screens.data.cars.query_details.QueryCarDetailsFragment
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException


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
        try {
            val response = chain.proceed(request)

            if (!response.isSuccessful) {

                val gson = GsonBuilder().create()
                try {
                    val pojo = gson.fromJson(
                        response.peekBody(Long.MAX_VALUE).string(),
                        ApiResponse::class.java
                    )

                    var showError = true
                    if (!pojo.success) {
//                        Log.e("request", request.toString())
//                        Log.e("pojo", pojo.toString())
                        if (pojo.errorDetails == "FILE_NOT_FOUND, args=[]") {
                            showError = false
                        }

                        when (pojo.errorCode) {
                            null, "FILE_NOT_FOUND", "TRANSACTION_VIGNETTE_INTERVAL_OVERLAP" -> {
                                showError = false
                            }
                            "AUTHENTICATION_MISSING", "AUTHENTICATION_FAILED" -> {
                                showError = false
                                AccountManager.refreshToken()
                            }
                            "VEHICLE_NOT_FOUND" -> {
                                showError = !QueryCarDetailsFragment.queryError
                            }
                        }

                        if (showError) {

                            var dialogBody = pojo.errorMessage
                            if (pojo.validationResult?.isNotEmpty() == true && pojo.validationResult != null) {
                                var warningStr = ""
                                for (i in pojo.validationResult!!.indices) {
                                    val field = instance?.resources?.getIdentifier(
                                        "${pojo.validationResult?.get(i)?.field}",
                                        "string",
                                        instance?.packageName
                                    )
                                        ?.let { instance?.resources?.getString(it) }
                                    warningStr =
                                        "$warningStr${field} : ${pojo.validationResult?.get(i)?.warning}\n"
                                }
                                dialogBody = "$dialogBody\n$warningStr"
                            }

                            Handler(Looper.getMainLooper()).post {
                                instance?.let {
                                    MaterialAlertDialogBuilder(
                                        it,
                                        R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog
                                    )
                                        .setTitle(instance?.resources?.getString(R.string.error))
                                        .setMessage(dialogBody)
                                        .setPositiveButton(
                                            it.resources.getString(R.string.ok),
                                            null
                                        )
                                        .show()
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                }

            }
            return response
        } catch (e: Exception) {
            var msg = ""
            when (e) {
                is SocketTimeoutException -> {
                    msg = "Timeout - Please check your internet connection"
                }
                is UnknownHostException -> {
                    msg = "Unable to make a connection. Please check your internet"
                }
                is ConnectionShutdownException -> {
                    msg = "Connection shutdown. Please check your internet"
                }
                is IOException -> {
                    msg = "Server is unreachable, please try again later. ${e.message}"
                    Handler(Looper.getMainLooper()).post {
                        instance?.let { NoServerDialog.showServerErrorInfo(it) }
                    }
                }
                is IllegalStateException -> {
                    msg = "${e.message}"
                }
                else -> {
                    msg = "${e.message}"
                }
            }

            return Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(999)
                .message(msg)
                .body("{${e}}".toResponseBody(null)).build()
        }


    }

}