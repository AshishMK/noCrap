package com.x.nocrap.utils

import android.R
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.StateListDrawable
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.RoundRectShape
import android.util.DisplayMetrics
import android.util.StateSet
import android.widget.FrameLayout
import com.x.nocrap.application.AppController
import java.lang.reflect.Method

object Screen {
    const val MATCH_PARENT = -1
    const val WRAP_CONTENT = -2
    private var density = 0f
    private var scaledDensity = 0f

    var displayMetrics = DisplayMetrics()

    @JvmStatic
    public fun dp(dp: Int): Int {
        if (density == 0f) density =
            AppController.getInstance().resources.displayMetrics.density
        displayMetrics = AppController.getInstance().resources.displayMetrics
        return (dp * density + .5f).toInt()
    }

    @JvmStatic
    fun createRoundRectDrawable(rad: Int, defaultColor: Int): Drawable? {
        val defaultDrawable = ShapeDrawable(
            RoundRectShape(
                floatArrayOf(
                    rad.toFloat(),
                    rad.toFloat(),
                    rad.toFloat(),
                    rad.toFloat(),
                    rad.toFloat(),
                    rad.toFloat(),
                    rad.toFloat(),
                    rad.toFloat()
                ), null, null
            )
        )
        defaultDrawable.paint.color = defaultColor
        return defaultDrawable
    }

    @JvmStatic
    private fun getSize(size: Float): Int {
        return (if (size < 0) size else dp(size.toInt())) as Int
    }

    @JvmStatic
    fun createFrame(width: Int, height: Float): FrameLayout.LayoutParams? {
        return FrameLayout.LayoutParams(getSize(width.toFloat()), getSize(height))
    }

    @JvmStatic
    fun createFrame(width: Int, height: Int, gravity: Int): FrameLayout.LayoutParams? {
        return FrameLayout.LayoutParams(
            getSize(width.toFloat()),
            getSize(height.toFloat()),
            gravity
        )
    }

    @JvmStatic
    fun createFrame(
        width: Int,
        height: Float,
        gravity: Int,
        leftMargin: Float,
        topMargin: Float,
        rightMargin: Float,
        bottomMargin: Float
    ): FrameLayout.LayoutParams? {
        val layoutParams =
            FrameLayout.LayoutParams(getSize(width.toFloat()), getSize(height), gravity)
        layoutParams.setMargins(
            dp(leftMargin.toInt()),
            dp(topMargin.toInt()),
            dp(rightMargin.toInt()),
            dp(bottomMargin.toInt())
        )
        return layoutParams
    }

    @JvmStatic
    fun getPixelsInCM(cm: Float, isX: Boolean): Float {
        return cm / 2.54f * if (isX) displayMetrics.xdpi else displayMetrics.ydpi
    }

    @JvmStatic
    fun sp(sp: Float): Int {
        if (scaledDensity == 0f) scaledDensity =
            AppController.getInstance().getResources().getDisplayMetrics().scaledDensity
        return (sp * scaledDensity + .5f).toInt()
    }

    @JvmStatic
    fun getWidth(): Int {
        return AppController.getInstance().getResources().getDisplayMetrics().widthPixels
    }

    @JvmStatic
    fun getHeight(): Int {
        return AppController.getInstance().getResources().getDisplayMetrics().heightPixels
    }

    @JvmStatic
    fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId: Int = AppController.getInstance().getResources()
            .getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result =
                AppController.getInstance().getResources().getDimensionPixelSize(resourceId)
        }
        return result
    }

    @JvmStatic
    fun getNavbarHeight(): Int {
        if (hasNavigationBar()) {
            val resourceId: Int = AppController.getInstance().getResources()
                .getIdentifier("navigation_bar_height", "dimen", "android")
            if (resourceId > 0) {
                return AppController.getInstance().getResources()
                    .getDimensionPixelSize(resourceId)
            }
        }
        return 0
    }

    @JvmStatic
    fun hasNavigationBar(): Boolean {
        val resources: Resources =
            AppController.getInstance().getResources()
        val id = resources.getIdentifier("config_showNavigationBar", "bool", "android")
        return id > 0 && resources.getBoolean(id)
    }

    fun getDensity(): Float {
        return density
    }

    @JvmStatic
    fun createSimpleSelectorDrawable(
        context: Context,
        resource: Int,
        defaultColor: Int,
        pressedColor: Int
    ): Drawable? {
        val resources = context.resources
        val defaultDrawable = resources.getDrawable(resource).mutate()
        if (defaultColor != 0) {
            defaultDrawable.colorFilter = PorterDuffColorFilter(
                defaultColor,
                PorterDuff.Mode.MULTIPLY
            )
        }
        val pressedDrawable = resources.getDrawable(resource).mutate()
        if (pressedColor != 0) {
            pressedDrawable.colorFilter = PorterDuffColorFilter(
                pressedColor,
                PorterDuff.Mode.MULTIPLY
            )
        }
        val stateListDrawable: StateListDrawable =
            object : StateListDrawable() {
                override fun selectDrawable(index: Int): Boolean {
//                if (Build.VERSION.SDK_INT < 21) {
//                    Drawable drawable = getStateDrawable(this, index);
//                    ColorFilter colorFilter = null;
//                    if (drawable instanceof BitmapDrawable) {
//                        colorFilter = ((BitmapDrawable) drawable).getPaint().getColorFilter();
//                    } else if (drawable instanceof NinePatchDrawable) {
//                        colorFilter = ((NinePatchDrawable) drawable).getPaint().getColorFilter();
//                    }
//                    boolean result = super.selectDrawable(index);
//                    if (colorFilter != null) {
//                        drawable.setColorFilter(colorFilter);
//                    }
//                    return result;
//                }
                    return super.selectDrawable(index)
                }
            }
        stateListDrawable.addState(intArrayOf(R.attr.state_pressed), pressedDrawable)
        stateListDrawable.addState(intArrayOf(R.attr.state_selected), pressedDrawable)
        stateListDrawable.addState(StateSet.WILD_CARD, defaultDrawable)
        return stateListDrawable
    }

    var StateListDrawable_getStateDrawableMethod: Method? = null

    @SuppressLint("PrivateApi")
    @JvmStatic
    private fun getStateDrawable(drawable: Drawable, index: Int): Drawable? {
        if (StateListDrawable_getStateDrawableMethod == null) {
            try {
                StateListDrawable_getStateDrawableMethod =
                    StateListDrawable::class.java.getDeclaredMethod(
                        "getStateDrawable",
                        Int::class.javaPrimitiveType
                    )
            } catch (ignore: Throwable) {
            }
        }
        if (StateListDrawable_getStateDrawableMethod == null) {
            return null
        }
        try {
            return StateListDrawable_getStateDrawableMethod!!.invoke(
                drawable,
                index
            ) as Drawable
        } catch (ignore: Exception) {
        }
        return null
    }

    @JvmStatic
    fun createCircleDrawable(size: Int, color: Int): Drawable? {
        val ovalShape = OvalShape()
        ovalShape.resize(size.toFloat(), size.toFloat())
        val defaultDrawable = ShapeDrawable(ovalShape)
        defaultDrawable.paint.color = color
        return defaultDrawable
    }


}