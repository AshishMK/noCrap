package com.x.nocrap.scenes.homeScene.playerFragment

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.View.OnTouchListener
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DataSource
import com.x.nocrap.R
import com.x.nocrap.databinding.PlayerFragmentBinding
import com.x.nocrap.di.module.ApiModule.Companion.base_url_download
import com.x.nocrap.scenes.homeScene.HomeActivity
import com.x.nocrap.scenes.homeScene.VideoEntity
import com.x.nocrap.scenes.likeScene.MyLikesActivity
import com.x.nocrap.views.exoplayer.PreviewTimeBar
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.exo_playback_control_view.view.*
import timber.log.Timber
import javax.inject.Inject

class PlayerFragment : DaggerFragment() {
    var binding: PlayerFragmentBinding? = null
    var isDummyFrag = ObservableBoolean(false)
    var isImage = ObservableBoolean(false)

    @Inject
    lateinit var dataSourceFactory: DataSource.Factory
    var videoEntity: VideoEntity? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initialiseView(container!!)
        // Inflate the layout for this fragment
        return binding!!.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding!!.isdummyFrag = isDummyFrag
        if (videoEntity == null) {
            isDummyFrag.set(true)
            return
        }
        isDummyFrag.set(false)
        isImage.set(videoEntity!!.category.toInt() == 2)
        binding!!.content = videoEntity
        binding!!.img = base_url_download + videoEntity!!.url
        if (videoEntity!!.category.toInt() == 2) {
            showController(true)
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("ttssa($videoEntity==null)")
        if (arguments != null) {
            videoEntity = requireArguments().getParcelable("videoEntity")
        }
    }

    /*
     * Initialising the View using Data Binding
     * */
    private fun initialiseView(viewGroup: ViewGroup) {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(activity),
            R.layout.fragment_player,
            viewGroup,
            false
        );
        binding!!.thumb = ""
        binding!!.isImage = isImage

        //  binding!!.playerView.controllerAutoShow = false
        binding!!.playerView.controllerHideOnTouch = false
        binding!!.playerView.controllerShowTimeoutMs = 0
        binding!!.playerView.useController = true
        binding!!.playerView.player = null
        showController(!showbar)
        binding!!.playerView.setKeepContentOnPlayerReset(true)
        //binding!!.playerView.setShutterBackgroundColor(Color.TRANSPARENT)
        binding!!.playerView.keepScreenOn = true
        setupPreviewBar()
        setupTouchListener()
    }

    private fun setupTouchListener() {
        if (videoEntity != null && videoEntity!!.category.toInt() == 2) {


            binding!!.photoView.setOnOutsidePhotoTapListener {
                if (activity is MyLikesActivity) {

                    (activity as MyLikesActivity).onTouch(true)
                } else
                    (activity as HomeActivity).onTouch(true)
            }

            binding!!.photoView.setOnPhotoTapListener { view, x, y ->
                if (activity is MyLikesActivity) {

                    (activity as MyLikesActivity).onTouch(true)
                } else
                    (activity as HomeActivity).onTouch(true)
            }
            return
        }

        binding!!.playerView.setOnTouchListener(object : OnTouchListener {
            private val gestureDetector =
                GestureDetector(activity, object : SimpleOnGestureListener() {
                    override fun onDoubleTap(e: MotionEvent): Boolean {
                        //    updateLike()
                        if (activity is MyLikesActivity) {
                            (activity as MyLikesActivity).onTouch(false)
                        } else
                            (activity as HomeActivity).onTouch(false)
                        return super.onDoubleTap(e)
                    }

                    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                        if (activity is MyLikesActivity) {
                            //   binding!!.photoView.setOnPhotoTapListener { view, x, y -> (activity as MyLikesActivity).onTouch(true)}
                            (activity as MyLikesActivity).onTouch(true)
                        } else
                            (activity as HomeActivity).onTouch(true)
                        return super.onSingleTapConfirmed(e)
                    }
                })

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                Log.d(
                    "TEST",
                    "Raw event: " + event.action + ", (" + event.rawX + ", " + event.rawY + ")"
                )
                gestureDetector.onTouchEvent(event)

                return true
            }
        })
    }

    fun cropVideo() {
        binding!!.playerView.resizeMode =
            if (binding!!.playerView.resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FIT) AspectRatioFrameLayout.RESIZE_MODE_FILL else AspectRatioFrameLayout.RESIZE_MODE_FIT
    }

    fun bindPlayer(): Boolean {
//        Toast.makeText(activity,"($videoEntity==null)",1).show()
        print("hjk ($videoEntity==null)")
        if (videoEntity == null || videoEntity!!.category.toInt() == 2) {
            return false
        }
        binding!!.playerView.player = null
        if (binding!!.playerView.player == null) {

            if (activity is MyLikesActivity) {
                binding!!.playerView.player = (activity as MyLikesActivity).simpleExoPlayer

                return true
            } else if (activity is HomeActivity)
                binding!!.playerView.player = (activity as HomeActivity).simpleExoPlayer
            return true
            //ExoPlayerManager(binding!!.playerView.player as SimpleExoPlayer,binding!!.playerView,
            //  binding!!.playerView.exo_progress as PreviewTimeBar,binding!!.playerView.imageView,getString(R.string.url_thumbnails))
            //((HomeFragment) getParentFragment()).simpleExoPlayer.setPlayWhenReady(true);
        }
        return true
    }

    fun videoPlaying() {

        Handler().postDelayed({

            if (binding != null) {
                binding!!.thumb = null
                binding!!.playerView.videoSurfaceView?.setBackgroundColor(Color.TRANSPARENT)
            }
        }, 300)
    }

    fun showthumb(thumb: String) {
        //   binding.setThumb(null);

        Handler().postDelayed({
            if (activity is MyLikesActivity && binding != null && !(activity as MyLikesActivity).simpleExoPlayer.isPlaying) {
                binding!!.playerView.videoSurfaceView?.setBackgroundColor(Color.WHITE)
                binding!!.thumb = "$base_url_download$thumb.png"

            } else if (activity is HomeActivity && binding != null && !(activity as HomeActivity).simpleExoPlayer.isPlaying) {
                binding!!.playerView.videoSurfaceView?.setBackgroundColor(Color.WHITE)
                binding!!.thumb = "$base_url_download$thumb.png"
            }
        }, 500)
    }

    fun unBindPlayer() {
        binding!!.thumb = ""
        binding!!.playerView.videoSurfaceView?.setBackgroundColor(Color.WHITE)
        if (videoEntity!!.category.toInt() == 2) {
            return
        }
        binding!!.playerView.player = null
    }

    override fun onPause() {
        super.onPause()
        if (activity is MyLikesActivity && videoEntity!!.category.toInt() != 2)
            (activity as MyLikesActivity).simpleExoPlayer.playWhenReady = false
    }

    override fun onResume() {
        super.onResume()
        if (activity is MyLikesActivity && videoEntity!!.category.toInt() != 2)
            (activity as MyLikesActivity).simpleExoPlayer.playWhenReady = true
    }

    private fun setupPreviewBar() {
        val timebar = (binding!!.playerView.exo_progress as PreviewTimeBar)
        timebar.setPreviewThumbTintResource(R.color.dark_icon)
        timebar.isPreviewEnabled = false
        timebar.setAutoHidePreview(true)
        timebar.setPlayedColor(ContextCompat.getColor(requireContext(), R.color.dark_icon))
        timebar.setBufferedColor(ContextCompat.getColor(requireContext(), R.color.white_dull))
    }

    var showbar: Boolean = false
    public fun showController(hide: Boolean) {
        showbar = !hide
        if (binding == null)
            return
        if (!hide)
            binding!!.playerView.showController()
        else
            binding!!.playerView.hideController()
    }
}