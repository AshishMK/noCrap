package com.x.nocrap.views

import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.x.nocrap.utils.Screen
import timber.log.Timber

public object DataBindingAdapters {

    @BindingAdapter("layoutMarginTop")
    @JvmStatic
    fun setLayoutMarginBottom(view: View, dimen: Int) {
        val layoutParams = view.layoutParams as MarginLayoutParams
        layoutParams.topMargin = Screen.dp(dimen)
        view.layoutParams = layoutParams
    }


    @BindingAdapter("fromGif")
    @JvmStatic
    fun setLoader(view: ImageView, drawable: Drawable?) {
        //Glide.with(view.context).asGif().load(R.drawable.equi).into(view)
    }


    @BindingAdapter("profileImage")
    @JvmStatic
    fun loadImage(view: ImageView, imageUrl: String?) {
        Timber.v(imageUrl)
        Glide.with(view.context)
            .load(imageUrl)
            //   .apply(RequestOptions.placeholderOf(R.drawable.profile_default))
            .into(view)
    }

    @BindingAdapter("profileImageNoQ")
    @JvmStatic
    fun loadImageQ(view: ImageView, imageUrl: String?) {
        Timber.v(imageUrl)
        Glide.with(view.context)
            .load(imageUrl)
            .apply(
                RequestOptions().format(DecodeFormat.PREFER_ARGB_8888)
                    .override(com.bumptech.glide.request.target.Target.SIZE_ORIGINAL)
            )
            .into(view)
    }

    @BindingAdapter("profileImageNoPlace")
    @JvmStatic
    fun loadImageNoPlace(
        view: ImageView,
        imageUrl: String?
    ) {
        Timber.v(imageUrl)
        Glide.with(view.context)
            .load(imageUrl)
            .apply(RequestOptions.fitCenterTransform())
            .into(view)
    }

    @BindingAdapter("profileImageCircle")
    @JvmStatic
    fun loadImageCircle(
        view: ImageView,
        imageUrl: String?
    ) {
        Timber.v(DataBindingAdapters::class.java.name, imageUrl)
        Glide.with(view.context)
            .load(imageUrl)
            .apply(RequestOptions.circleCropTransform())
            //  .apply(RequestOptions.placeholderOf(R.drawable.ic_social))
            .into(view)
    }

    @BindingAdapter("android:src")
    @JvmStatic
    fun setImageUri(view: ImageView, imageUri: String?) {
        if (imageUri == null) {
            view.setImageURI(null)
        } else {
            view.setImageURI(Uri.parse(imageUri))
        }
    }

    @BindingAdapter("android:src")
    @JvmStatic
    fun setImageUri(view: ImageView, imageUri: Uri?) {
        view.setImageURI(imageUri)
    }

    @BindingAdapter("android:src")
    @JvmStatic
    fun setImageDrawable(view: ImageView, drawable: Drawable?) {
        view.setImageDrawable(drawable)
    }

    @BindingAdapter("android:src")
    @JvmStatic
    fun setImageResource(imageView: ImageView, resource: Int) {
        if (resource == 0) {
            return
        }
        imageView.setImageDrawable(ActivityCompat.getDrawable(imageView.context, resource))
    }
}