package com.x.nocrap.scenes.likeScene

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.an.trailers.factory.ViewModelFactory
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.x.nocrap.R
import com.x.nocrap.data.Status
import com.x.nocrap.databinding.MyLikesActivityBinding
import com.x.nocrap.di.module.ApiModule.Companion.base_url_download
import com.x.nocrap.scenes.homeScene.VideoEntity
import com.x.nocrap.scenes.homeScene.playerFragment.PlayerFragment
import com.x.nocrap.utils.AlertDialogProvider
import com.x.nocrap.utils.Screen
import com.x.nocrap.utils.Utils
import dagger.android.AndroidInjection
import javax.inject.Inject

class MyLikesActivity : AppCompatActivity(), VideoListAdapter.ItemListener, LikeActivityHandler {
    /**
     * I am using Data binding
     * */
    private lateinit var binding: MyLikesActivityBinding

    /*
     * This is our ViewModel class
     *
     * */
    private lateinit var viewModel: MyLikesViewModel

    /*
     * we need to
     * inject the ViewModelFactory. The ViewModelFactory class
     * has a list of ViewModels and will provide
     * the corresponding ViewModel in this activity
     * */
    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var simpleExoPlayer: SimpleExoPlayer

    @Inject
    lateinit var simpleCache: SimpleCache

    @Inject
    lateinit var cacheDataSourceFactory: CacheDataSourceFactory

    @Inject
    lateinit var dataSourceFactory: DataSource.Factory
    var playerPosition = C.TIME_UNSET
    var isLoadingMore = false
    var visibleItemCount = 0
    var totalItemCount: Int = 0
    var pastVisiblesItems: Int = 0
    var playerBinded: Boolean = false
    var showToolbar = ObservableBoolean(true)
    var showControls = ObservableBoolean(false)
    lateinit var videoListAdapter: VideoListAdapter
    private lateinit var sheetBehavior: BottomSheetBehavior<*>
    lateinit var mGridLayoutManager: GridLayoutManager
    private var entity = VideoEntity()

    /**
     * Handler for visibility toggles
     * */
    val handler = Handler()
    private val runnable = Runnable {
        toggleVisibility(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        Utils.requestFullScreenIfLandscape(this)
        initialiseView()
        fragmentListener()
        setupRecyclerView()
        initialiseViewModel()

    }

    /*
     * Initialising the View using Data Binding
     * */
    private fun initialiseView() {

        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_likes)
        sheetBehavior = BottomSheetBehavior.from<View>(binding.bottomSheet.bottomSheet)
        sheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        binding.handler = this
        binding.status = Status.SUCCESS
        binding.utils = Utils.Companion
        binding.entity = entity
        binding.showControls = showControls
        binding.showToolbar = showToolbar
    }

    /*
    * Initialising the ViewModel class here.
    * We are adding the ViewModelFactory class here.
    * We are observing the LiveData
    * */
    private fun initialiseViewModel() {

        viewModel =
            ViewModelProvider(this, viewModelFactory).get(MyLikesViewModel::class.java)

        viewModel.getContentLiveData().observe(this, Observer { resource ->
            if (resource!!.isLoading) {
                if (videoListAdapter.contents.size < 1)
                    binding.status = Status.LOADING
                else
                    Snackbar.make(binding.root, R.string.loading_more, Snackbar.LENGTH_LONG).show()
                println("loginin loading")
            } else if (resource.data?.data != null) {

                videoListAdapter.setItems(resource.data!!.data!!)
                isLoadingMore = resource.data!!.data!!.size < 10
                binding.status =
                    if (videoListAdapter.contents.size == 0) Status.NOTFOUND else Status.SUCCESS
            } else {
                if (videoListAdapter.contents.size < 1)
                    binding.status = Status.ERROR
                else
                    Snackbar.make(binding.root, R.string.no_connection, Snackbar.LENGTH_LONG).show()
            }
            //handleErrorResponse()
        })
        viewModel.getReportLiveData().observe(this, Observer { resource ->
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

        viewModel.getLikeLiveData().observe(this, Observer { resource ->
            if (resource!!.isLoading) {
                Snackbar.make(binding.root, R.string.removing, Snackbar.LENGTH_LONG).show()
            } else if (resource.data != null) {
                //updateMoviesList(resource.data)
                println("success")

                binding.status =
                    if (videoListAdapter.contents.size == 0) Status.NOTFOUND else Status.SUCCESS

            } else {
                Snackbar.make(binding.root, R.string.try_again, Snackbar.LENGTH_LONG).show()
            }
            //handleErrorResponse()
        })



        getUserLikesVideos()

    }

    private fun getUserLikesVideos() {

        isLoadingMore = false
        viewModel.getUserLikesVideos(videoListAdapter.itemCount)
    }

    fun setupRecyclerView() {
        videoListAdapter = VideoListAdapter(this, this)
        mGridLayoutManager = GridLayoutManager(
            this,
            3
        )
        binding.likesList.layoutManager = mGridLayoutManager
        binding.likesList.adapter = videoListAdapter
        //binding.list.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        binding.likesList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) //check for scroll down
                {
                    loadMore()
                }
            }
        })
    }

    override fun onItemClickListener(entity: VideoEntity, position: Int) {

        showContent(entity)
    }

    override fun onItemDeleteClickListener(entity: VideoEntity, position: Int) {

        AlertDialogProvider.getInstance(
            getString(R.string.remove_like),
            getString(R.string.remove_like_msg),
            AlertDialogProvider.TYPE_NORMAL,
            true
        )
            .setAlertDialogListener(object : AlertDialogProvider.AlertDialogListener {
                override fun onDialogCancel() {
                    //simpleExoPlayer.playWhenReady = true
                }

                override fun onDialogOk(text: String, dialog: AlertDialogProvider) {

                    dialog.dismiss()
                    //simpleExoPlayer.playWhenReady = true
                    videoListAdapter.removeItem(position)
                    viewModel.addLike(entity.id.toInt())
                }
            }).show(supportFragmentManager, "unlike_video")
    }

    fun loadMore(): Boolean {
        visibleItemCount = mGridLayoutManager.childCount
        totalItemCount = mGridLayoutManager.itemCount
        pastVisiblesItems = mGridLayoutManager.findFirstVisibleItemPosition()
        if (!isLoadingMore) {
            if (visibleItemCount + pastVisiblesItems >= totalItemCount - 3) {
                isLoadingMore = true

                getUserLikesVideos()

                return true
            }
        }
        return false
    }

    lateinit var frg: PlayerFragment
    private fun showContent(entity: VideoEntity) {
        this.entity.setValues(entity)
        frg = PlayerFragment()
        val b = Bundle()
        b.putParcelable("videoEntity", entity)
        frg.arguments = b
        supportFragmentManager.beginTransaction().replace(R.id.container, frg).addToBackStack(null)
            .commit()
        showControls.set(true)
        Handler().postDelayed(Runnable { toggleVisibility(true) }, 300)

        sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        if (entity.category.toInt() == 2) {
            updateViews()
            setEmojiParams(false)
            return
        }
        setEmojiParams(true)
        val concatenatedSource = ConcatenatingMediaSource(
            ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                .createMediaSource(Uri.parse(base_url_download + entity.url))
        )
        playAfterDelay(concatenatedSource)

    }

    private fun playAfterDelay(concatenatedSource: ConcatenatingMediaSource) {
        Handler().postDelayed(Runnable {
            simpleExoPlayer.prepare(concatenatedSource)
            simpleExoPlayer.playWhenReady = true
            frg!!.showthumb(entity.thumb)
            simpleExoPlayer.addListener(listener)
        }, 200)

    }


    val listener = object : Player.EventListener {
        override fun onTimelineChanged(timeline: Timeline, reason: Int) {}
        override fun onPlayerError(error: ExoPlaybackException) {}
        override fun onTracksChanged(
            trackGroups: TrackGroupArray,
            trackSelections: TrackSelectionArray
        ) {
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
            if (playbackState == Player.STATE_READY) {
                Toast.makeText(this@MyLikesActivity, "onreadyss", 0).show()
                updateViews()
                frg!!.videoPlaying()
                if (!playerBinded) {
                    playerBinded = frg!!.bindPlayer()
                }


            }
            // binding.pager.setCurrentItem(0,false);

            if (playbackState == Player.STATE_ENDED) {
                //simpleExoPlayer.seekTo(0, C.TIME_UNSET);
            }
        }
    }

    override fun onPause() {
        super.onPause()
        simpleExoPlayer.removeListener(listener)
        if (this::frg.isInitialized && frg!!.videoEntity!!.category.toInt() != 2) {
            playerPosition = simpleExoPlayer.currentPosition

        }
        if (isFinishing) {

            if (this::frg.isInitialized)
                frg!!.unBindPlayer()
        }

    }

    override fun onResume() {
        super.onResume()

        simpleExoPlayer.addListener(listener)
        if (this::frg.isInitialized && frg!!.videoEntity!!.category.toInt() != 2) {


            if (playerPosition != C.TIME_UNSET)
                simpleExoPlayer.seekTo(0, playerPosition)
        }
    }

    private fun fragmentListener() {
        supportFragmentManager.addOnBackStackChangedListener {
            binding.likesList.visibility =
                if (supportFragmentManager.backStackEntryCount > 0) View.GONE else View.VISIBLE
            if (supportFragmentManager.backStackEntryCount == 0) {
                showControls.set(false)
                showToolbar.set(true)
                playerBinded = false
            } else {
                showToolbar.set(false)
            }
        }
    }

    override fun onReportClicked(view: View) {
        if (this::frg.isInitialized && frg.videoEntity != null) {
            showReportVideoDialog(frg.videoEntity!!.id.toInt())
        }
    }

    override fun onRotateClicked(view: View) {
        requestedOrientation =
            if (resources.configuration.orientation === Configuration.ORIENTATION_LANDSCAPE) ActivityInfo.SCREEN_ORIENTATION_PORTRAIT else ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    override fun onSlideClicked(view: View) {
        binding.bottomSheet.swipe.rotation =
            if (sheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) 0F else 180F
        if (sheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            //handler.postDelayed(runnable,2000)
        } else {
            if (simpleExoPlayer.isPlaying)
                onPlayPauseClicked(binding.playPauseMid)
            //simpleExoPlayer.playWhenReady = false

            //handler.removeCallbacksAndMessages(null)
        }
        sheetBehavior.state =
            if (sheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) BottomSheetBehavior.STATE_EXPANDED else BottomSheetBehavior.STATE_COLLAPSED

    }

    override fun onPlayPauseClicked(view: View) {
        if (!this::frg.isInitialized && frg.videoEntity == null) {
            return
        }
        if (frg.videoEntity!!.category.toInt() != 2) {

            if (simpleExoPlayer.isPlaying)
                handler.removeCallbacksAndMessages(null)
            else
                handler.postDelayed(runnable, 2000)
            binding.playPauseMid.setImageResource(if (!simpleExoPlayer.playWhenReady) R.drawable.ic_baseline_pause_24 else R.drawable.ic_baseline_play_arrow_24)
            simpleExoPlayer.playWhenReady = !simpleExoPlayer.playWhenReady
        } else {
            toggleVisibility(!showControls.get())
        }
    }

    var curentVol = 0F
    override fun onVolumeClicked(view: View) {
        if (simpleExoPlayer.volume == 0f) {
            simpleExoPlayer.volume = curentVol
            curentVol = 0F
        } else {
            curentVol = simpleExoPlayer.volume
            simpleExoPlayer.volume = 0F
        }
        binding.mute.setImageResource(if (simpleExoPlayer.volume == 0F) R.drawable.ic_baseline_volume_up_24 else R.drawable.ic_baseline_volume_off_24)

    }

    override fun onCropVideo(view: View) {
        if (this::frg.isInitialized) {
            frg.cropVideo()
        }
    }

    override fun onBackClick(view: View) {
        onBackPressed()
    }

    private fun updateViews() {
        if (this::frg.isInitialized && frg.videoEntity != null) {
            viewModel.updateViews(frg.videoEntity!!.id.toInt())

        }
    }

    private fun toggleVisibility(showControls: Boolean) {

        if (showControls) {
            handler.postDelayed(runnable, 4000)
        } else {
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            handler.removeCallbacksAndMessages(null)
        }
        if (this::frg.isInitialized && frg.videoEntity != null && frg.videoEntity!!.category.toInt() != 2)
            frg.showController(!showControls)
        this.showControls.set(showControls)
    }

    fun onTouch(singleTouch: Boolean) {
        if (singleTouch) {

            toggleVisibility(!showControls.get())

        }

    }

    private fun showReportVideoDialog(mid: Int) {

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
                            this@MyLikesActivity,
                            R.string.valid_report_text,
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                    dialog.dismiss()
                    //simpleExoPlayer.playWhenReady = true
                    viewModel.reportVideo(
                        "" + mid,
                        text
                    )
                }
            }).show(supportFragmentManager, "report_video")
    }

    private fun setEmojiParams(showVideoControls: Boolean) {
        if (!showVideoControls) {
            binding.grup.visibility = View.GONE
            (binding.emotion.parent as View).setBackgroundColor(Color.parseColor("#90000000"))
            (binding.emotion.layoutParams as ConstraintLayout.LayoutParams).topToTop =
                ConstraintLayout.LayoutParams.PARENT_ID
            (binding.emotion.layoutParams as ConstraintLayout.LayoutParams).bottomToBottom =
                ConstraintLayout.LayoutParams.PARENT_ID
            (binding.emotion.layoutParams as ConstraintLayout.LayoutParams).bottomToTop =
                ConstraintLayout.LayoutParams.UNSET
            (binding.emotion.layoutParams as ConstraintLayout.LayoutParams).bottomMargin = 0
        } else {
            (binding.emotion.parent as View).setBackgroundColor(0)
            binding.grup.visibility = View.VISIBLE
            (binding.emotion.layoutParams as ConstraintLayout.LayoutParams).topToTop =
                ConstraintLayout.LayoutParams.UNSET
            (binding.emotion.layoutParams as ConstraintLayout.LayoutParams).bottomToBottom =
                ConstraintLayout.LayoutParams.UNSET
            (binding.emotion.layoutParams as ConstraintLayout.LayoutParams).bottomToTop =
                R.id.guideLine
            (binding.emotion.layoutParams as ConstraintLayout.LayoutParams).bottomMargin =
                Screen.dp(12)
        }
    }

}