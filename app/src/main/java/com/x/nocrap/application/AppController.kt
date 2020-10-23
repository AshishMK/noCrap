package com.x.nocrap.application

import android.app.Activity
import android.app.Application
import android.app.Service
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
import com.x.nocrap.BuildConfig
import com.x.nocrap.data.local.pref.SharedPrefStorage
import com.x.nocrap.data.remote.api.ContentApiService
import com.x.nocrap.di.appComponent.DaggerAppComponent
import com.x.nocrap.utils.RetrofitOkhttpUtil
import com.x.nocrap.utils.Utils
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasServiceInjector
import dagger.android.support.HasSupportFragmentInjector
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import timber.log.Timber.DebugTree
import javax.inject.Inject


class AppController : Application(), HasActivityInjector, HasSupportFragmentInjector,
    HasServiceInjector {
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    var appVersion = 1

    /**
     * Provide [android.content.SharedPreferences] operations
     */
    @Inject
    lateinit var preferenceStorage: SharedPrefStorage

    @Inject
    lateinit var contentApiService: ContentApiService
    var enableAd = true

    companion object {
        const val API_VERSION = "api_version"
        private lateinit var mInstance: AppController

        /*returns Application object or application context
     * */
        @Synchronized
        @JvmStatic
        fun getInstance(): AppController {
            return AppController.mInstance
        }
    }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    override fun activityInjector(): DispatchingAndroidInjector<Activity>? {
        return dispatchingAndroidInjector
    }


    /*Inject android fragments @see @link{#FragmentModule} for list of injected fragments*/
    @Inject
    lateinit var dispatchingFragmentAndroidInjector: DispatchingAndroidInjector<Fragment>

    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment>? {
        return dispatchingFragmentAndroidInjector
    }


    /*Inject android services @see @link{#ServiceModule} for list of injected services*/
    @Inject
    lateinit var dispatchingServiceAndroidInjector: DispatchingAndroidInjector<Service?>

    override fun serviceInjector(): DispatchingAndroidInjector<Service?>? {
        return dispatchingServiceAndroidInjector
    }


    override fun onCreate() {
        super.onCreate()
        AppController.mInstance = this@AppController
        inject()
        Utils.createNotificationChannel()
        setAnalytics()
        if (BuildConfig.DEBUG) {
            MobileAds.setRequestConfiguration(Utils.adConfigure())
            Timber.plant(DebugTree())
        }
        MobileAds.initialize(this)
    }

    public fun inject() {
        DaggerAppComponent.builder()
            .application(this)
            .build()
            .inject(this)
    }

    private fun setAnalytics() {
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "1")
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "appopen")
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image")
        mFirebaseAnalytics!!.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    /***
     * get the latest version of app so to force user to update app
     */
    fun getAppVersion() {
        Timber.v("get App version")
        contentApiService!!.getAppVersion(RetrofitOkhttpUtil.getRequestBodyText(""))
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                preferenceStorage.writeValue(
                    API_VERSION,
                    response.message.toInt()
                )
            }) { throwable -> Timber.v(throwable.toString()) }
    }
}
