package com.x.nocrap.scenes.homeScene

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.an.trailers.factory.ViewModelFactory
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.FileDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSink
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.x.nocrap.R
import com.x.nocrap.application.AppController
import com.x.nocrap.application.AppController.Companion.API_VERSION
import com.x.nocrap.data.Resource
import com.x.nocrap.data.Resource.Companion.error
import com.x.nocrap.data.Resource.Companion.success
import com.x.nocrap.data.Status
import com.x.nocrap.data.local.pref.SharedPrefStorage
import com.x.nocrap.data.local.pref.UserManager
import com.x.nocrap.databinding.HomeActivityBinding
import com.x.nocrap.di.module.ApiModule.Companion.base_url_download
import com.x.nocrap.scenes.homeScene.playerFragment.PlayerFragment
import com.x.nocrap.scenes.likeScene.MyLikesActivity
import com.x.nocrap.utils.AlertDialogProvider
import com.x.nocrap.utils.Utils
import com.x.nocrap.utils.animation.AnimUtil
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import javax.inject.Inject

class HomeActivity : AppCompatActivity(), HomeActivityHandler {
    @Inject
    lateinit var simpleExoPlayer: SimpleExoPlayer

    @Inject
    lateinit var dataSourceFactory: DataSource.Factory

    /**
     * I am using Data binding
     * */
    private lateinit var binding: HomeActivityBinding

    /**
     * Provide [android.content.SharedPreferences] operations
     */
    @Inject
    lateinit var preferenceStorage: SharedPrefStorage

    @Inject
    lateinit var simpleCache: SimpleCache

    @Inject
    lateinit var cacheDataSourceFactory: CacheDataSourceFactory

    /*
     * we need to
     * inject the ViewModelFactory. The ViewModelFactory class
     * has a list of ViewModels and will provide
     * the corresponding ViewModel in this activity
     * */
    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private var concatenatedSource: ConcatenatingMediaSource? = null
    var playerBinded: Boolean = false
    var lastIndex = 0
    var pagerAdapter: PagerAdapter? = null
    var isLoadingMore = false
    var mediaCategory: MediaCategory = MediaCategory.ANIMAL
    var initialized = false
    var showControls = ObservableBoolean(false)
    var mediaCatXml = ObservableInt(0)
    var themeMode = ObservableInt(0)
    private var entity = VideoEntity()
    var rewardedAd: RewardedAd? = null
    var interstitialAd: InterstitialAd? = null
    var timeDiff = 60 * 1000 * 4 //5 min
    val AD_TYPE_INTER: Int = 0
    val AD_TYPE_REW: Int = 1
    var lastShownAd = AD_TYPE_REW

    /*
     * This is our ViewModel class
     *
     * */
    private lateinit var loginViewModel: HomeViewModel

    /**
     * Handler for visibility toggles
     * */
    val handler = Handler()
    val handlerLoader = Handler()
    private lateinit var sheetBehavior: BottomSheetBehavior<*>
    var playerPosition = C.TIME_UNSET
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        themeMode.set(UserManager.getThemeMode(preferenceStorage))
        if (themeMode.get() == 1)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        /*window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )*/
        pagerAdapter = PagerAdapter(supportFragmentManager)
        initialiseView()
        initialiseViewModel()
        loadAd()


    }

    private fun loadAd() {
        UserManager.setRewardedTime(preferenceStorage, System.currentTimeMillis() - (3 * 60 * 1000))
        if (lastShownAd == AD_TYPE_INTER)
            rewardedAd = Utils.buildRewardedAd(this)
        else
            interstitialAd = Utils.buildInterstitialAd(this)
    }


    private fun showAd() {
        if (System.currentTimeMillis() - UserManager.getRewardedTime(
                preferenceStorage
            ) >= timeDiff
        ) {
            if (lastShownAd == AD_TYPE_INTER) {
                if (rewardedAd != null && rewardedAd!!.isLoaded) {
                    if (mediaCategory != MediaCategory.IMAGE)
                        simpleExoPlayer.playWhenReady = false
                    toggleVisibility(true, 4000)
                    handler.removeCallbacksAndMessages(null)
                    lastShownAd = AD_TYPE_REW
                    rewardedAd!!.show(this@HomeActivity, object : RewardedAdCallback() {
                        override fun onUserEarnedReward(rewardItem: RewardItem) {
                        }

                        override fun onRewardedAdClosed() {
                            super.onRewardedAdClosed()
                            loadAd()
                        }

                        override fun onRewardedAdFailedToShow(p0: AdError?) {
                            super.onRewardedAdFailedToShow(p0)
                            loadAd()
                        }

                    })

                } else
                    rewardedAd = Utils.buildRewardedAd(this)
            } else {

                if (interstitialAd != null && interstitialAd!!.isLoaded) {
                    if (mediaCategory != MediaCategory.IMAGE)
                        simpleExoPlayer.playWhenReady = false
                    toggleVisibility(true, 4000)

                    handler.removeCallbacksAndMessages(null)
                    interstitialAd!!.show()
                    lastShownAd = AD_TYPE_INTER
                    loadAd()
                } else {
                    if (interstitialAd != null && interstitialAd!!.isLoading)
                        return
                    interstitialAd = Utils.buildInterstitialAd(this)
                }
            }


        }

    }

    /*
     * Initialising the View using Data Binding
     * */
    private fun initialiseView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        binding.utils = Utils.Companion
        binding.handler = this
        binding.mediaCat = mediaCatXml
        binding.themeMode = themeMode
        binding.showControls = showControls
        binding.entity = entity
        binding.bottomSheet.descriptionDetail.movementMethod = LinkMovementMethod.getInstance()
        sheetBehavior = BottomSheetBehavior.from<View>(binding.bottomSheet.bottomSheet)
        sheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }


    private fun showSplashStatus(status: Status) {
        if (binding.splash.splash.visibility == View.GONE) {
            if (status == Status.LOADING) {
                binding.pbSec.visibility = View.VISIBLE
            } else if (status == Status.ERROR) {
                binding.pbSec.visibility = View.GONE
                Snackbar.make(binding.root, R.string.no_network, Snackbar.LENGTH_LONG).show()
            } else {
                //binding.pbSec.visibility = View.GONE
            }
            return
        }
        if (status == Status.LOADING) {
            binding.splash.msg.setText(R.string.splash_msg)
            binding.splash.sendPb.visibility = View.VISIBLE
            binding.splash.retry.visibility = View.GONE
            binding.splash.splash.visibility = View.VISIBLE
        } else if (status == Status.ERROR) {
            binding.splash.msg.setText(R.string.no_network)
            binding.splash.sendPb.visibility = View.GONE
            binding.splash.retry.visibility = View.VISIBLE
            binding.splash.splash.visibility = View.VISIBLE
        }
    }

    /*
     * Initialising the ViewModel class here.
     * We are adding the ViewModelFactory class here.
     * We are observing the LiveData
     * */
    private fun initialiseViewModel() {

        loginViewModel =
            ViewModelProvider(this, viewModelFactory).get(HomeViewModel::class.java)
        loginViewModel.getUserLiveData().observe(this, Observer { resource ->
            if (resource!!.isLoading) {
                showSplashStatus(Status.LOADING)
                println("loginin loading")
            } else if (resource.data != null && resource.data!!) {

                startApp()
                //updateMoviesList(resource.data)
                println("success")
            } else {
                showSplashStatus(Status.ERROR)
            }
            //handleErrorResponse()
        })

        loginViewModel.getLikeLiveData().observe(this, Observer { resource ->
            if (resource!!.isLoading) {
                println("loginin loading")
            } else if (resource.data != null) {
                //updateMoviesList(resource.data)
                pagerAdapter!!.list1[binding.pager.currentItem].mid =
                    if (resource.data!!) Integer(1) else null
                var t: Int = pagerAdapter!!.list1[binding.pager.currentItem].likes.toInt()
                t += if (resource.data!!) 1 else -1
                pagerAdapter!!.list1[binding.pager.currentItem].likes = Integer(t)
                setPageValues(binding.pager.currentItem, false, false)

                println("success")
            } else {
            }
            //handleErrorResponse()
        })

        loginViewModel.getContentLiveData().observe(this, Observer { resource ->
            if (resource!!.isLoading) {
                showSplashStatus(Status.LOADING)
            } else if (resource.data?.data != null) {
                showSplashStatus(Status.SUCCESS)
                //updateMoviesList(resource.data)
                binding.splash.splash.visibility = View.GONE
                println("loginin loading " + loginViewModel.getContentLiveData().value!!.data!!.data!!.size)
                //pagerAdapter.addFragDummy(new PlayerFragment(), "tst");
                if (mediaCategory == MediaCategory.IMAGE) {
                    getImages()
                    return@Observer
                }
                playerBinded = false
                isLoadingMore = resource.data.data!!.size < 10
                if (!initialized) {
                    binding.pager.adapter = pagerAdapter
                    setUpPlayer()
                } else {

                    getPlayList()
                }
                println("loginin success" + resource.data.data!!.size)
            } else {
                showSplashStatus(Status.ERROR)
            }
            //handleErrorResponse()
        })

        loginViewModel.getReportLiveData().observe(this, Observer { resource ->
            if (resource == Status.LOADING) {
                Snackbar.make(binding.root, R.string.sending_report, Snackbar.LENGTH_LONG).show()
            } else if (resource.status === Status.ERROR) {
                Snackbar.make(binding.root, R.string.try_again, Snackbar.LENGTH_LONG).show()
            } else if (resource.status === Status.SUCCESS) {
                if (resource.data == null) {
                    Snackbar.make(binding.root, R.string.try_again, Snackbar.LENGTH_LONG).show()
                }
                Snackbar.make(
                    binding.root,
                    if (resource.data!!) R.string.sent_report else R.string.try_again,
                    Snackbar.LENGTH_LONG
                ).show()
            }
            //handleErrorResponse()
        })
        checkPerMission()


    }

    private fun startApp() {
        if (isAppVersionObsolete()) {
            return
        }
        if (!UserManager.isLoggedIn(preferenceStorage)) {
            val uniqueID: String = UUID.randomUUID().toString()
            loginViewModel.loginUser(uniqueID, "$uniqueID@g.com", "")
        } else {
            loginViewModel.getVideos(
                mediaCategory.ordinal,
                if (pagerAdapter == null) 0 else pagerAdapter!!.count
            )
            println("loginin id")
        }
    }

    override fun onResume() {
        super.onResume()
        Utils.requestFullScreenIfLandscape(this)
        if (mediaCategory != MediaCategory.IMAGE) {
            playerBinded = false
            simpleExoPlayer.addListener(listener)
            if (concatenatedSource != null)
                simpleExoPlayer.prepare(concatenatedSource!!)
            if (binding.pager.currentItem > 0 || playerPosition != C.TIME_UNSET)
                simpleExoPlayer.seekTo(binding.pager.currentItem, playerPosition)
            simpleExoPlayer.playWhenReady = true

        }
    }

    fun checkPerMission() {
        val PERMISSIONS = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val status: Utils.PermissionStatus = Utils.checkPermissions(
            this,
            Utils.PERMISSION_REQUEST,
            PERMISSIONS,
            null,
            R.string.storage_permission_msg
        )
        if (status === Utils.PermissionStatus.SUCCESS) {
            startApp()
        } else if (status === Utils.PermissionStatus.ERROR) {
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty()) {
            simpleCache.release();
            AppController.getInstance().inject()
            AndroidInjection.inject(this)
            //startActivity(Intent(this@HomeActivity,HomeActivity::class.java))
            //finish()
            startApp()
        }
    }

    override fun onPause() {
        super.onPause()

        handler.removeCallbacksAndMessages(null)
        if (isFinishing) {
            if (pagerAdapter != null) {
                pagerAdapter = null
            }
            loginViewModel.clearData()
            simpleExoPlayer.stop(true)
        }
        if (mediaCategory != MediaCategory.IMAGE) {
            playerPosition = simpleExoPlayer.currentPosition
            simpleExoPlayer.removeListener(listener)
            simpleExoPlayer.playWhenReady = false
        }
    }


    /***
     * view click operations
     * @see HomeActivityHandler
     * ***/
    override fun onPawClicked(view: View) {
        if (mediaCategory == MediaCategory.ANIMAL) {
            return
        }
        setEmojiParams(true)
        mediaCatXml.set(0)
        mediaCategory = MediaCategory.ANIMAL
        getVideo()
    }

    override fun onHumanClicked(view: View) {
        if (mediaCategory == MediaCategory.HUMAN) {
            return
        }
        setEmojiParams(true)
        mediaCatXml.set(1)
        mediaCategory = MediaCategory.HUMAN
        getVideo()
    }

    fun getVideo() {
        lastIndex = 0;
        simpleExoPlayer.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
        simpleExoPlayer.playWhenReady = false
        if (pagerAdapter != null) {
            pagerAdapter!!.removePages()
        }
        concatenatedSource?.clear()
        loginViewModel.getVideos(mediaCategory.ordinal, 0)
    }

    override fun onImageClicked(view: View) {
        if (mediaCategory == MediaCategory.IMAGE) {
            return
        }
        setEmojiParams(false)
        mediaCatXml.set(2)
        mediaCategory = MediaCategory.IMAGE
        getVideo()
    }

    override fun onFavClicked(view: View) {
        if (pagerAdapter != null && pagerAdapter!!.count > 0) {
            loginViewModel.addLike(pagerAdapter!!.list1[binding.pager.currentItem].id.toInt())
            AnimUtil.likeAnimation(
                binding.likeButtonLayout.vDotsView,
                binding.likeButtonLayout.vCircle,
                binding.likeButtonLayout.ivStar
            )
        }
    }

    override fun onSlideClicked(view: View) {
        binding.bottomSheet.swipe.rotation =
            if (sheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) 0F else 180F
        if (sheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            handler.postDelayed(runnable, 2000)
        } else {
            if (simpleExoPlayer.isPlaying)
                playPause()
            //simpleExoPlayer.playWhenReady = false
            handler.removeCallbacksAndMessages(null)
        }
        sheetBehavior.state =
            if (sheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) BottomSheetBehavior.STATE_EXPANDED else BottomSheetBehavior.STATE_COLLAPSED

    }

    override fun onReportClicked(view: View) {
        showReportVideoDialog()
    }

    override fun onRotateClicked(view: View) {
        requestedOrientation =
            if (resources.configuration.orientation === Configuration.ORIENTATION_LANDSCAPE) ActivityInfo.SCREEN_ORIENTATION_PORTRAIT else ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    override fun onPlayPauseClicked(view: View) {
        playPause()

    }

    fun playPause() {

        if (mediaCategory != MediaCategory.IMAGE) {

            if (simpleExoPlayer.isPlaying)
                handler.removeCallbacksAndMessages(null)
            else
                handler.postDelayed(runnable, 2000)
            binding.playPauseMid.setImageResource(if (!simpleExoPlayer.playWhenReady) R.drawable.ic_baseline_pause_24 else R.drawable.ic_baseline_play_arrow_24)
            simpleExoPlayer.playWhenReady = !simpleExoPlayer.playWhenReady
        } else {
            toggleVisibility(!showControls.get(), 4000)
        }
    }

    var curentVol = 0F
    override fun onVolumeClicked(view: View) {
        if (pagerAdapter == null || mediaCategory == MediaCategory.IMAGE || pagerAdapter!!.count <= binding.pager.currentItem) {
            return
        }
        if (simpleExoPlayer.volume == 0f) {
            simpleExoPlayer.volume = curentVol
            curentVol = 0F
        } else {
            curentVol = simpleExoPlayer.volume
            simpleExoPlayer.volume = 0F
        }
        binding.mute.setImageResource(if (simpleExoPlayer.volume == 0F) R.drawable.ic_baseline_volume_up_24 else R.drawable.ic_baseline_volume_off_24)

    }

    override fun onSettingsClicked(view: View) {
        showSettingAlert()
    }

    override fun showLikesClicked(view: View) {
        startActivity(Intent(this@HomeActivity, MyLikesActivity::class.java))
    }

    override fun onThemeChanged(view: View) {
        //inclined of
        /*val isNightTheme = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (isNightTheme) {
            Configuration.UI_MODE_NIGHT_YES ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            Configuration.UI_MODE_NIGHT_NO ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }*/
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES ->
                UserManager.setThemeMode(preferenceStorage, 0)
            //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            Configuration.UI_MODE_NIGHT_NO ->
                UserManager.setThemeMode(preferenceStorage, 1)
            //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        finish()
        startActivity(Intent(this@HomeActivity, HomeActivity::class.java))

    }

    override fun onVideoCrop(view: View) {
        if (pagerAdapter != null && mediaCategory != MediaCategory.IMAGE) {
            var playerFragment: PlayerFragment =
                pagerAdapter!!.getItem(binding.pager.currentItem) as PlayerFragment
            playerFragment.cropVideo()
        }
    }

    override fun onRetry(view: View) {
        checkPerMission()
    }

    private fun setUpPlayer() {
        getPlayList()
        if (!UserManager.hasShownGuide(preferenceStorage)) {
            startActivity(Intent(this, WalkThroughActivity::class.java))
            initialized = true
            UserManager.setHasShownGuide(preferenceStorage)
        }
        simpleExoPlayer.prepare(concatenatedSource!!)
        simpleExoPlayer.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
        simpleExoPlayer.repeatMode = Player.REPEAT_MODE_ONE
        //  simpleExoPlayer.setPlayWhenReady(true);
        binding.pager.setOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                setPageValues(position, toggleControl = true, afterLoadFirstTime = false)

                if (mediaCategory == MediaCategory.IMAGE) {
                    showAd()
                    return
                }
                if (position < lastIndex && simpleExoPlayer.hasPrevious()) {
                    simpleExoPlayer.previous()
                } else if (position > lastIndex && simpleExoPlayer.hasNext()) {
                    simpleExoPlayer.next()

                    //  simpleExoPlayer.getCurrentTimeline().nex
                }
                playerBinded = false
                var playerFragment: PlayerFragment =
                    pagerAdapter!!.getItem(lastIndex) as PlayerFragment
                if (playerFragment != null) playerFragment.unBindPlayer()
                playerFragment = pagerAdapter!!.getItem(position) as PlayerFragment
                if (playerFragment != null) playerFragment.showthumb(
                    pagerAdapter!!.list1[position].thumb
                )
                //simpleExoPlayer.setPlayWhenReady(pagerAdapter.isDummyFrag(position) ? false : true);
                lastIndex = position
                if (!isLoadingMore && position > pagerAdapter!!.count - 5) {
                    isLoadingMore = true

                    loginViewModel.getVideos(mediaCategory.ordinal, pagerAdapter!!.count)
                }
                if (position % 4 == 0) {
                    // Utils.buildRewardedAd(getActivity())
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        if (pagerAdapter != null && pagerAdapter!!.count > 0) {
            val playerFragment: PlayerFragment = pagerAdapter!!.getItem(0) as PlayerFragment
            playerFragment.showthumb(pagerAdapter!!.list1[0].thumb)
        }
        setPageValues(0, toggleControl = true, afterLoadFirstTime = true)
    }

    fun setPageValues(
        position: Int,
        toggleControl: Boolean,
        afterLoadFirstTime: Boolean
    ) {
        if (pagerAdapter == null || pagerAdapter!!.count <= position) {

            return
        }
        if (mediaCategory == MediaCategory.IMAGE) {
            updateViews()
        }
        val videoEntity = pagerAdapter!!.list1[position]
        entity.setValues(videoEntity)
        binding.bottomSheet.like.setImageResource(if (pagerAdapter!!.list1[position].mid == null) R.drawable.ic_love else R.drawable.ic_love_fill)
        if (toggleControl) {
            if (afterLoadFirstTime)
                sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            toggleVisibility(true, if (afterLoadFirstTime) 2500 else 1500)

        }

    }

    fun getImages() {
        val list = ArrayList<VideoEntity>()
        list.addAll(loginViewModel.getContentLiveData().value!!.data!!.data!!)
        for (entity in list) {
            val frg = PlayerFragment()
            val b = Bundle()
            b.putParcelable("videoEntity", entity)
            frg.arguments = b
            //  if(!isLoadingMore){
            pagerAdapter!!.addFrag(frg, "" + (entity.id), entity)
            pagerAdapter!!.notifyDataSetChanged()
            //}
        }
        setPageValues(0, true, true)
    }

    fun getPlayList() {
        simpleExoPlayer.playWhenReady = true
        val list = ArrayList<VideoEntity>()
        list.addAll(loginViewModel.getContentLiveData().value!!.data!!.data!!)
        //saveCache(list).subscribe()
        val tmp = arrayOfNulls<MediaSource>(list.size)
        var i = 0
        for (entity in list) {
            tmp[i] = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                .createMediaSource(Uri.parse(base_url_download + entity.url)) //proxyServer.getProxyUrl(base_url_download + entity.getVideo())));
            i++
            val frg = PlayerFragment()
            val b = Bundle()
            b.putParcelable("videoEntity", entity)
            frg.arguments = b
            //  if(!isLoadingMore){
            pagerAdapter!!.addFrag(frg, "" + (pagerAdapter!!.getCount() + i), entity)
            pagerAdapter!!.notifyDataSetChanged()
            //}
        }
        //LoopingMediaSource loopingSources = new LoopingMediaSource(secondSource);
        if (concatenatedSource == null) {
            concatenatedSource = ConcatenatingMediaSource(*tmp)
        } else {
            concatenatedSource!!.addMediaSources(listOf(*tmp))
        }

        //pagerAdapter.notifyDataSetChanged();
    }

    fun saveCache(list: ArrayList<VideoEntity>): Observable<Resource<Boolean>> {
        val result: Observable<Resource<Boolean>>
        val source: Observable<Resource<Boolean>> =
            Observable.fromCallable<Boolean> { saveCacheFiles(list) }.subscribeOn(Schedulers.io())
                .flatMap { s: Boolean ->
                    Observable.just(s).map { data: Boolean -> success(data) }
                }
                .onErrorResumeNext { t: Throwable ->
                    Observable.just(false)
                        .map { data: Boolean -> error(t.message ?: "", data) }
                }
                .observeOn(AndroidSchedulers.mainThread())
        result = Observable.concat(
            Observable.just(false)
                .map { data: Boolean -> Resource.loading(data) }.take(1)
            ,
            source
        )
        return result
    }

    fun saveCacheFiles(list: ArrayList<VideoEntity>): Boolean {
        for (t in list) getCachedData(
            this@HomeActivity,
            base_url_download + t.url,
            File(Utils.getCache2Directory(), t.url.replace(".mp4", ""))
        )
        return true
    }

    private var mTotalBytesToRead = 0L
    private var mBytesReadFromCache: Long = 0
    private var mBytesReadFromNetwork: Long = 0
    fun getCachedData(
        context: Context, url: String, target: File
    ): Boolean {
        val tempFile =
            File(Utils.getCache2Directory(), Utils.getFileName(".mp4"))
        var isSuccessful = false
        val dataSource = CacheDataSource(
            simpleCache,  // If the cache doesn't have the whole content, the missing data will be read from upstream
            dataSourceFactory.createDataSource(),
            FileDataSource(),  // Set this to null if you don't want the downloaded data from upstream to be written to cache
            CacheDataSink(
                simpleCache,
                CacheDataSink.DEFAULT_BUFFER_SIZE.toLong()
            ),  /* flags= */
            0,  /* eventListener= */
            null
        )

        // Listen to the progress of the reads from cache and the network.
        //dataSource.addTransferListener(this);
        var outFile: FileOutputStream? = null
        var bytesRead = 0

        // Total bytes read is the sum of these two variables.
        mTotalBytesToRead = C.LENGTH_UNSET.toLong()
        mBytesReadFromCache = 0
        mBytesReadFromNetwork = 0
        try {
            outFile = FileOutputStream(tempFile)
            mTotalBytesToRead = dataSource.open(DataSpec(Uri.parse(url)))
            // Just read from the data source and write to the file.
            val data = ByteArray(1024)
            Log.d(
                "getCachedData",
                "<<<<Starting fetch..." + tempFile.name + " " + target.name
            )
            while (bytesRead != C.RESULT_END_OF_INPUT) {
                bytesRead = dataSource.read(data, 0, data.size)
                if (bytesRead != C.RESULT_END_OF_INPUT) {
                    outFile.write(data, 0, bytesRead)
                }
            }
            Log.d("getCachedData", "<<<<Starting fetch...$url")
            //copyFile(tempfile.getAbsolutePath(),target.getAbsolutePath());
            tempFile.delete()
            isSuccessful = true
        } catch (e: IOException) {
            Log.d("getCachedData", "<<<<Starting fetch..." + e.localizedMessage)
            // error processing
        } finally {
            try {
                dataSource.close()
                outFile!!.flush()
                outFile.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return isSuccessful
    }

    fun onTouch(singleTouch: Boolean) {
        if (singleTouch) {
            toggleVisibility(!showControls.get(), 4000)

        } else {
            binding.bottomSheet.like.performClick()
        }

    }

    private val runnable = Runnable {
        toggleVisibility(false, 4000)
    }

    private fun toggleVisibility(showControls: Boolean, duration: Int) {
        if (showControls) {
            handler.postDelayed(runnable, duration.toLong())

        } else {
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            handler.removeCallbacksAndMessages(null)

        }
        if (mediaCategory != MediaCategory.IMAGE)
            getPlayerFragment()?.showController(!showControls)
        this.showControls.set(showControls)
    }

    private fun getPlayerFragment(): PlayerFragment? {
        if (pagerAdapter != null && pagerAdapter!!.count != 0) {
            return pagerAdapter!!.getItem(binding.pager.currentItem) as PlayerFragment
        } else
            return null

    }


    private fun showReportVideoDialog() {
        val playerFragment =
            pagerAdapter!!.getItem(binding.pager.currentItem) as PlayerFragment
        if (playerFragment == null) {
            Toast.makeText(this@HomeActivity, R.string.wait_reporet, Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialogProvider.getInstance(
            getString(R.string.report_video),
            getString(R.string.report_video_msg),
            AlertDialogProvider.TYPE_EDIT_BIG,
            true
        )
            .setAlertDialogListener(object : AlertDialogProvider.AlertDialogListener {
                override fun onDialogCancel() {
                    //simpleExoPlayer.playWhenReady = true
                }

                override fun onDialogOk(text: String, dialog: AlertDialogProvider) {
                    if (TextUtils.isEmpty(text) || text.length < 15) {
                        Toast.makeText(
                            this@HomeActivity,
                            R.string.valid_report_text,
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                    dialog.dismiss()
                    //simpleExoPlayer.playWhenReady = true
                    loginViewModel.reportVideo(
                        "" + pagerAdapter!!.list1[binding.pager.currentItem].id.toInt(),
                        text
                    )
                }
            }).show(supportFragmentManager, "report_video")
    }

    enum class MediaCategory {
        ANIMAL,
        HUMAN,
        IMAGE
    }

    val listener = object : Player.EventListener {
        override fun onTimelineChanged(timeline: Timeline, reason: Int) {}
        override fun onPlayerError(error: ExoPlaybackException) {}
        override fun onTracksChanged(
            trackGroups: TrackGroupArray,
            trackSelections: TrackSelectionArray
        ) {
            showAd()
            /*val playerFragment: PlayerFragment =
                pagerAdapter!!.getItem(binding.pager.currentItem) as PlayerFragment
            if (playerFragment != null) {
                // playerFragment.updateViews()
            }*/
        }

        override fun onPlayerStateChanged(
            playWhenReady: Boolean,
            playbackState: Int
        ) {
            if (playbackState == Player.STATE_BUFFERING)
                displayLoader()
            if (playbackState == Player.STATE_READY) {
                hideLoader()

                if (pagerAdapter != null) {
                    val playerFragment: PlayerFragment =
                        pagerAdapter!!.getItem(binding.pager.currentItem) as PlayerFragment
                    playerFragment.videoPlaying()

                    updateViews()
                    if (!playerBinded) {

                        pagerAdapter!!.notifyDataSetChanged()
                        playerBinded = playerFragment.bindPlayer()

                    }
                }
                showAd()
                // binding.pager.setCurrentItem(0,false);
            }
            if (playbackState == Player.STATE_ENDED) {
                //simpleExoPlayer.seekTo(0, C.TIME_UNSET);
            }
        }
    }


    private fun setEmojiParams(showVideoControls: Boolean) {
        if (!showVideoControls) {
            (binding.emotion.parent as View).setBackgroundColor(Color.parseColor("#90000000"))
            binding.grup.visibility = View.GONE
            /*  (binding.emotion.layoutParams as ConstraintLayout.LayoutParams).topToTop =
                  ConstraintLayout.LayoutParams.PARENT_ID
              (binding.emotion.layoutParams as ConstraintLayout.LayoutParams).bottomToBottom =
                  ConstraintLayout.LayoutParams.PARENT_ID
              (binding.trackTitle.layoutParams as ConstraintLayout.LayoutParams).bottomToTop =
                  ConstraintLayout.LayoutParams.UNSET
              (binding.trackTitle.layoutParams as ConstraintLayout.LayoutParams).bottomMargin = 0*/
            binding.guideLine.setGuidelinePercent(.58f)
        } else {
            (binding.emotion.parent as View).setBackgroundColor(0)
            binding.grup.visibility = View.VISIBLE
            /*(binding.emotion.layoutParams as ConstraintLayout.LayoutParams).topToTop =
                ConstraintLayout.LayoutParams.UNSET
            (binding.emotion.layoutParams as ConstraintLayout.LayoutParams).bottomToBottom =
                ConstraintLayout.LayoutParams.UNSET
            (binding.trackTitle.layoutParams as ConstraintLayout.LayoutParams).bottomToTop =
                R.id.guideLine
            (binding.trackTitle.layoutParams as ConstraintLayout.LayoutParams).bottomMargin =
                Screen.dp(12)*/
            //resources.getDimension(R.dimen.keyline_1_minus_8dp).toInt()
            binding.guideLine.setGuidelinePercent(.49f)

        }
    }

    private fun updateViews() {
        if (pagerAdapter != null && pagerAdapter!!.count > 0) {
            loginViewModel.updateViews(pagerAdapter!!.list1[binding.pager.currentItem].id.toInt())

        }
    }

    fun isAppVersionObsolete(): Boolean {
        val ver =
            preferenceStorage.readValue(API_VERSION, AppController.getInstance().appVersion) as Int
        if (ver - AppController.getInstance().appVersion >= 2) { //forcefullyUpdate
            AlertDialogProvider.getInstance(
                getString(R.string.update_app_title),
                getString(R.string.update_app_msg),
                AlertDialogProvider.TYPE_UPDATE_FORCE,
                true
            ).setAlertDialogListener(object : AlertDialogProvider.AlertDialogListener {
                override fun onDialogCancel() {}
                override fun onDialogOk(
                    text: String,
                    dialog: AlertDialogProvider
                ) {
                    Utils.launchMarket()
                }
            }).show(supportFragmentManager, "version")
            return true
        } else {
            if (ver - AppController.getInstance().appVersion === 1) { //Update app
                AlertDialogProvider.getInstance(
                    getString(R.string.update_app_title),
                    getString(R.string.update_app_msg2),
                    AlertDialogProvider.TYPE_UPDATE,
                    true
                ).setAlertDialogListener(object : AlertDialogProvider.AlertDialogListener {
                    override fun onDialogCancel() {}
                    override fun onDialogOk(
                        text: String,
                        dialog: AlertDialogProvider
                    ) {
                        Utils.launchMarket()
                    }
                }).show(supportFragmentManager, "version")
            }
            AppController.getInstance().getAppVersion()
        }
        return false
    }

    private fun displayLoader() {
        handlerLoader.postDelayed(Runnable {
            toggleVisibility(false, 0)
            binding.loader.visibility = View.VISIBLE
            binding.loader.start()
        }, 500)

    }

    private fun hideLoader() {
        handlerLoader.removeCallbacksAndMessages(null)
        handlerLoader.post(Runnable {
            binding.loader.visibility = View.GONE
            binding.loader.stop()
        })

    }

    private fun showSettingAlert() {
        AlertDialogProvider.getInstance(
            getString(R.string.info_dialog_title),
            getString(R.string.info_dialog_msg),
            AlertDialogProvider.TYPE_NORMAL,
            true
        ).setAlertDialogListener(object : AlertDialogProvider.AlertDialogListener {
            override fun onDialogCancel() {
                Utils.shareApplication(this@HomeActivity)
            }

            override fun onDialogOk(text: String, dialog: AlertDialogProvider) {
                Utils.shareApplication(this@HomeActivity)
                dialog.dismiss()
            }
        }).show(supportFragmentManager, "version")
    }
}
