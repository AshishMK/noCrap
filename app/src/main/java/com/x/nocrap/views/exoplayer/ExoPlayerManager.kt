package com.x.nocrap.views.exoplayer

import android.widget.ImageView
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.x.nocrap.views.exoplayer.previewseekbar.PreviewBar
import com.x.nocrap.views.exoplayer.previewseekbar.PreviewLoader

public class ExoPlayerManager(
    val player: SimpleExoPlayer,
    val playerView: PlayerView,
    val previewTimeBar: PreviewTimeBar,
    val imageView: ImageView,
    val thumbnailsUrl: String
) : PreviewLoader, PreviewBar.OnScrubListener {
    init {
        previewTimeBar.addOnScrubListener(this)
        previewTimeBar.setPreviewLoader(this)
    }

    override fun loadPreview(currentPosition: Long, max: Long) {
        if (player.isPlaying) {
            player.playWhenReady = false
        }
        println("let sayloading previeew")
        /*Glide.with(imageView)
            .load(thumbnailsUrl)
            .override(
                Target.SIZE_ORIGINAL,
                Target.SIZE_ORIGINAL
            )
            .transform(GlideThumbnailTransformation(currentPosition))
            .into(imageView)*/
    }

    override fun onScrubMove(previewBar: PreviewBar?, progress: Int, fromUser: Boolean) {
        println("let sayloading previeew start")
    }

    override fun onScrubStart(previewBar: PreviewBar?) {
        //player.playWhenReady = false
    }

    override fun onScrubStop(previewBar: PreviewBar?) {
        //player.playWhenReady = true
    }
}