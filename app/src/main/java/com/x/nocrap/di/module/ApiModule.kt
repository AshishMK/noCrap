package com.x.nocrap.di.module

import android.app.Application
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tonyodev.fetch2.FetchConfiguration
import com.tonyodev.fetch2rx.RxFetch
import com.x.nocrap.data.local.pref.PreferencesStorage
import com.x.nocrap.data.local.pref.SharedPrefStorage
import com.x.nocrap.data.remote.api.ContentApiService
import com.x.nocrap.data.remote.interceptor.RequestInterceptor
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/*
 * Module to provide api call functionality
 */
@Module
class ApiModule {

    /**
     * Provide [android.content.SharedPreferences] operations
     */
    lateinit var preferenceStorage: PreferencesStorage<Any>

    companion object {
        val base_url = "http://134.209.155.176:4080/"
        val base_url_download = base_url + "get_file/"
        val base_url_download_TIK = base_url + "getTikTok/"
    }
    /*  enum class Endpoints {
          register, login
      }*/

    /**
     * The method returns the [RxFetchobject]
     *
     * @return
     */
    @Provides
    @Singleton
    fun provideRxFetch(fetchConfiguration: FetchConfiguration): RxFetch {
        return RxFetch.Impl.getRxInstance(fetchConfiguration)
    }

    /**
     * The method returns the [//FetchConfiguration]
     *
     * @return
     */
    @Provides
    @Singleton
    fun provideFetchConfiguration(application: Application): FetchConfiguration {
        return FetchConfiguration.Builder(application).build()
    }

    /*
     * The method returns the Gson object
     * */
    @Provides
    @Singleton
    fun provideGson(): Gson {
        val gsonBuilder = GsonBuilder()
        return gsonBuilder.create()
    }


    /*
     * The method returns the Cache object
     * */
    @Provides
    @Singleton
    fun provideCache(application: Application): Cache {
        preferenceStorage = SharedPrefStorage(application)
        val cacheSize = 10 * 1024 * 1024.toLong() // 10 MB
        val httpCacheDirectory = File(application.cacheDir, "http-cache")
        return Cache(httpCacheDirectory, cacheSize)
    }


    /*
     * The method returns the Okhttp object
     * */
    @Provides
    @Singleton
    fun provideOkhttpClient(cache: Cache): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        val httpClient = OkHttpClient.Builder()
        httpClient.cache(cache)
        httpClient.addInterceptor(logging)
        /*if (!TextUtils.isEmpty(UserManager.getUserToken(preferenceStorage))) {
            System.out.println("sdssc token " + UserManager.getUserToken(preferenceStorage))
            httpClient.addInterceptor { chain ->
                val newRequest = chain.request().newBuilder()
                    .addHeader(
                        "Authorization",
                        "Bearer " + UserManager.getUserToken(preferenceStorage)
                    )
                    .build()
                chain.proceed(newRequest)
            }
        }*/
        httpClient.addNetworkInterceptor(RequestInterceptor())
        httpClient.connectTimeout(30, TimeUnit.SECONDS)
        httpClient.readTimeout(30, TimeUnit.SECONDS)
        return httpClient.build()
    }


    /*
     * The method returns the Retrofit object
     * */
    @Provides
    @Singleton
    fun provideRetrofit(gson: Gson, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl(base_url)
            .client(okHttpClient)
            .build()
    }

    /*
     * We need the ContentApiService module.
     * For this, We need the Retrofit object, Gson, Cache and OkHttpClient .
     * So we will define the providers for these objects here in this module.
     *
     * */

    /*
     * We need the ContentApiService module.
     * For this, We need the Retrofit object, Gson, Cache and OkHttpClient .
     * So we will define the providers for these objects here in this module.
     *
     * */
    @Provides
    @Singleton
    fun provideMovieApiService(retrofit: Retrofit): ContentApiService {
        return retrofit.create(ContentApiService::class.java)
    }

}