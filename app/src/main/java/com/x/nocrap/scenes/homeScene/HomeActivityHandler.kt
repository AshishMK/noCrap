package com.x.nocrap.scenes.homeScene

import android.view.View

interface HomeActivityHandler {
    public fun onPawClicked(view: View)
    public fun onHumanClicked(view: View)
    public fun onImageClicked(view: View)
    public fun onFavClicked(view: View)
    public fun onSlideClicked(view: View)
    public fun onReportClicked(view: View)
    public fun onRotateClicked(view: View)
    public fun onPlayPauseClicked(view: View)
    public fun onVolumeClicked(view: View)
    public fun onSettingsClicked(view: View)
    public fun showLikesClicked(view: View)
    public fun onThemeChanged(view: View)
    public fun onVideoCrop(view: View)
    public fun onRetry(view: View)
}