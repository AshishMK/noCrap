package com.x.nocrap.scenes.likeScene

import android.view.View

interface LikeActivityHandler {
    public fun onReportClicked(view: View)
    public fun onRotateClicked(view: View)
    public fun onSlideClicked(view: View)
    public fun onPlayPauseClicked(view: View)
    public fun onVolumeClicked(view: View)
    public fun onCropVideo(view: View)
    public fun onBackClick(view: View)
}