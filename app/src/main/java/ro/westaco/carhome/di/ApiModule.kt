package ro.westaco.carhome.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import ro.westaco.carhome.BuildConfig
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.interceptors.HeaderAuthInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApiModule {

    companion object {
        var BASE_URL_PRODUCTION = "https://build3.westaco.com:8443/"
        var BASE_URL_RESOURCES = "https://carhome-build.westaco.com"
    }

    /*
    ** Api
    */

    @Provides
    @Singleton
    internal fun providePostcardApi(retrofit: Retrofit) = retrofit.create(CarHomeApi::class.java)

    @Provides
    @Singleton
    internal fun provideRxRetrofit(
        rxCallAdapterFactory: RxJavaCallAdapterFactory,
        gsonConverterFactory: GsonConverterFactory,
        okHttpClient: OkHttpClient
    ) =
        Retrofit.Builder()
            .baseUrl(BASE_URL_RESOURCES)
            .addConverterFactory(gsonConverterFactory)
            .addCallAdapterFactory(rxCallAdapterFactory)
            .client(okHttpClient)
            .build()


    @Provides
    @Singleton
    internal fun provideRxCallAdapterFactory() = RxJavaCallAdapterFactory.create()

    @Provides
    @Singleton
    internal fun provideGson() = GsonConverterFactory.create()

    @Provides
    @Singleton
    internal fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        headerAuthInterceptor: HeaderAuthInterceptor
    ) =
        OkHttpClient.Builder()
            .addInterceptor(headerAuthInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

    @Provides
    @Singleton
    internal fun provideHeaderAuthInterceptor() = HeaderAuthInterceptor()

    @Provides
    @Singleton
    internal fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level =
            if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.NONE

        return httpLoggingInterceptor
    }


}