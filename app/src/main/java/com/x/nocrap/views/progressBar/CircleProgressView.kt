package com.x.nocrap.views.progressBar

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ProgressBar
import androidx.annotation.IntDef
import com.x.nocrap.R
import com.x.nocrap.utils.Utils
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.max

class CircleProgressView : ProgressBar {
    @IntDef(
        PROGRESS_STYLE_NORMAL,
        PROGRESS_STYLE_FILL_IN,
        PROGRESS_STYLE_FILL_IN_ARC
    )
    @Retention(RetentionPolicy.SOURCE)
    annotation class ProgressStyle
    companion object {
        private const val PROGRESS_STYLE_NORMAL = 0
        private const val PROGRESS_STYLE_FILL_IN = 1
        private const val PROGRESS_STYLE_FILL_IN_ARC = 2


        private val STATE = "state"
        private val PROGRESS_STYLE = "progressStyle"
        private val TEXT_COLOR = "textColor"
        private val TEXT_SIZE = "textSize"
        private val TEXT_SKEW_X = "textSkewX"
        private val TEXT_VISIBLE = "textVisible"
        private val TEXT_SUFFIX = "textSuffix"
        private val TEXT_PREFIX = "textPrefix"
        private val REACH_BAR_COLOR = "reachBarColor"
        private val REACH_BAR_SIZE = "reachBarSize"
        private val NORMAL_BAR_COLOR = "normalBarColor"
        private val NORMAL_BAR_SIZE = "normalBarSize"
        private val IS_REACH_CAP_ROUND = "isReachCapRound"
        private val RADIUS = "radius"
        private val START_ARC = "startArc"
        private val INNER_BG_COLOR = "innerBgColor"
        private val INNER_PADDING = "innerPadding"
        private val OUTER_COLOR = "outerColor"
        private val OUTER_SIZE = "outerSize"
    }

    private var mReachBarSize: Int = Utils.dp2px(context, 2)
    private var mNormalBarSize: Int = Utils.dp2px(context, 2)
    private var mReachBarColor = Color.parseColor("#108ee9")
    private var mNormalBarColor = Color.parseColor("#FFD3D6DA")
    private var mTextSize = Utils.sp2px(context, 14)
    private var mTextColor = Color.parseColor("#108ee9")
    private var mTextSkewX = 0f
    private var mTextSuffix = "%"
    private var mTextPrefix = ""
    private var mTextVisible = true
    private var mReachCapRound = false
    private var mRadius = Utils.dp2px(context, 20)
    private var mStartArc = 0
    private var mInnerBackgroundColor = 0
    private var mProgressStyle = PROGRESS_STYLE_NORMAL
    private var mInnerPadding = Utils.dp2px(context, 1)
    private var mOuterColor = 0
    private var needDrawInnerBackground = false
    private lateinit var rectInner: RectF
    private lateinit var rectF: RectF
    private var mOuterSize = Utils.dp2px(context, 1)
    private lateinit var mTextPaint: Paint
    private lateinit var mNormalPaint: Paint
    private lateinit var mReachPaint: Paint
    private lateinit var mInnerBackgroundPaint: Paint
    private lateinit var mOutPaint: Paint
    private var mRealWidth = 0
    private var mRealHeight = 0

    constructor(context: Context) : this(context, null) {
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {

        obtainAttributes(attrs)
        initPaint()
    }

    private fun initPaint() {
        mTextPaint = Paint()
        mTextPaint.color = mTextColor
        mTextPaint.style = Paint.Style.FILL
        mTextPaint.textSize = mTextSize.toFloat()
        mTextPaint.textSkewX = mTextSkewX
        mTextPaint.isAntiAlias = true
        mNormalPaint = Paint()
        mNormalPaint.color = mNormalBarColor
        mNormalPaint.style =
            if (mProgressStyle == PROGRESS_STYLE_FILL_IN_ARC) Paint.Style.FILL else Paint.Style.STROKE
        mNormalPaint.isAntiAlias = true
        mNormalPaint.strokeWidth = mNormalBarSize.toFloat()
        mReachPaint = Paint()
        mReachPaint.color = mReachBarColor
        mReachPaint.style =
            if (mProgressStyle == PROGRESS_STYLE_FILL_IN_ARC) Paint.Style.FILL else Paint.Style.STROKE
        mReachPaint.isAntiAlias = true
        mReachPaint.strokeCap = if (mReachCapRound) Paint.Cap.ROUND else Paint.Cap.BUTT
        mReachPaint.strokeWidth = mReachBarSize.toFloat()
        if (needDrawInnerBackground) {
            mInnerBackgroundPaint = Paint()
            mInnerBackgroundPaint.style = Paint.Style.FILL
            mInnerBackgroundPaint.isAntiAlias = true
            mInnerBackgroundPaint.color = mInnerBackgroundColor
        }
        if (mProgressStyle == PROGRESS_STYLE_FILL_IN_ARC) {
            mOutPaint = Paint()
            mOutPaint.style = Paint.Style.STROKE
            mOutPaint.color = mOuterColor
            mOutPaint.strokeWidth = mOuterSize.toFloat()
            mOutPaint.isAntiAlias = true
        }
    }

    private fun obtainAttributes(attrs: AttributeSet?) {
        val ta =
            context.obtainStyledAttributes(attrs, R.styleable.CircleProgressView)
        mProgressStyle = ta.getInt(
            R.styleable.CircleProgressView_progressStyle,
            PROGRESS_STYLE_NORMAL
        )
        mNormalBarSize = ta.getDimension(
            R.styleable.CircleProgressView_progressNormalSize,
            mNormalBarSize.toFloat()
        ).toInt()
        mNormalBarColor =
            ta.getColor(R.styleable.CircleProgressView_progressNormalColor, mNormalBarColor)
        mReachBarSize = ta.getDimension(
            R.styleable.CircleProgressView_progressReachSize,
            mReachBarSize.toFloat()
        ).toInt()
        mReachBarColor =
            ta.getColor(R.styleable.CircleProgressView_progressReachColor, mReachBarColor)
        mTextSize = ta.getDimension(
            R.styleable.CircleProgressView_progressTextSize,
            mTextSize.toFloat()
        ).toInt()
        mTextColor = ta.getColor(R.styleable.CircleProgressView_progressTextColor, mTextColor)
        mTextSkewX = ta.getDimension(R.styleable.CircleProgressView_progressTextSkewX, 0f)
        if (ta.hasValue(R.styleable.CircleProgressView_progressTextSuffix)) {
            mTextSuffix = ta.getString(R.styleable.CircleProgressView_progressTextSuffix)!!
        }
        if (ta.hasValue(R.styleable.CircleProgressView_progressTextPrefix)) {
            mTextPrefix = ta.getString(R.styleable.CircleProgressView_progressTextPrefix)!!
        }
        mTextVisible =
            ta.getBoolean(R.styleable.CircleProgressView_progressTextVisible, mTextVisible)
        mRadius = ta.getDimension(R.styleable.CircleProgressView_radius, mRadius.toFloat()).toInt()
        rectF = RectF(
            (-mRadius).toFloat(),
            (-mRadius).toFloat(),
            mRadius.toFloat(),
            mRadius.toFloat()
        )
        when (mProgressStyle) {
            PROGRESS_STYLE_FILL_IN -> {
                mReachBarSize = 0
                mNormalBarSize = 0
                mOuterSize = 0
            }
            PROGRESS_STYLE_FILL_IN_ARC -> {
                mStartArc = ta.getInt(R.styleable.CircleProgressView_progressStartArc, 0) + 270
                mInnerPadding = ta.getDimension(
                    R.styleable.CircleProgressView_innerPadding,
                    mInnerPadding.toFloat()
                ).toInt()
                mOuterColor = ta.getColor(R.styleable.CircleProgressView_outerColor, mReachBarColor)
                mOuterSize = ta.getDimension(
                    R.styleable.CircleProgressView_outerSize,
                    mOuterSize.toFloat()
                ).toInt()
                mReachBarSize = 0
                mNormalBarSize = 0
                if (!ta.hasValue(R.styleable.CircleProgressView_progressNormalColor)) {
                    mNormalBarColor = Color.TRANSPARENT
                }
                val mInnerRadius = mRadius - mOuterSize / 2 - mInnerPadding
                rectInner = RectF(
                    (-mInnerRadius).toFloat(),
                    (-mInnerRadius).toFloat(),
                    mInnerRadius.toFloat(),
                    mInnerRadius.toFloat()
                )
            }
            PROGRESS_STYLE_NORMAL -> {
                mReachCapRound = ta.getBoolean(R.styleable.CircleProgressView_reachCapRound, true)
                mStartArc = ta.getInt(R.styleable.CircleProgressView_progressStartArc, 0) + 270
                if (ta.hasValue(R.styleable.CircleProgressView_innerBackgroundColor)) {
                    mInnerBackgroundColor = ta.getColor(
                        R.styleable.CircleProgressView_innerBackgroundColor,
                        Color.argb(0, 0, 0, 0)
                    )
                    needDrawInnerBackground = true
                }
            }
        }
        ta.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val maxBarPaintWidth = max(mReachBarSize, mNormalBarSize)
        val maxPaintWidth = max(maxBarPaintWidth, mOuterSize)
        var height = 0
        var width = 0
        when (mProgressStyle) {
            PROGRESS_STYLE_FILL_IN -> {
                height = (paddingTop + paddingBottom
                        + abs(mRadius * 2))
                width = (paddingLeft + paddingRight
                        + abs(mRadius * 2))
            }
            PROGRESS_STYLE_FILL_IN_ARC -> {
                height = (paddingTop + paddingBottom
                        + abs(mRadius * 2)
                        + maxPaintWidth)
                width = (paddingLeft + paddingRight
                        + abs(mRadius * 2)
                        + maxPaintWidth)
            }
            PROGRESS_STYLE_NORMAL -> {
                height = (paddingTop + paddingBottom
                        + abs(mRadius * 2)
                        + maxBarPaintWidth)
                width = (paddingLeft + paddingRight
                        + abs(mRadius * 2)
                        + maxBarPaintWidth)
            }
        }
        mRealWidth = View.resolveSize(width, widthMeasureSpec)
        mRealHeight = View.resolveSize(height, heightMeasureSpec)
        setMeasuredDimension(mRealWidth, mRealHeight)
    }

    override fun onDraw(canvas: Canvas?) {
        when (mProgressStyle) {
            PROGRESS_STYLE_NORMAL -> drawNormalCircle(canvas!!)
            PROGRESS_STYLE_FILL_IN -> drawFillInCircle(canvas!!)
            PROGRESS_STYLE_FILL_IN_ARC -> drawFillInArcCircle(canvas!!)
        }
    }

    /**
     * PROGRESS_STYLE_FILL_IN_ARC
     */
    private fun drawFillInArcCircle(canvas: Canvas) {
        canvas.save()
        canvas.translate(mRealWidth / 2.toFloat(), mRealHeight / 2.toFloat())
        canvas.drawArc(rectF, 0f, 360f, false, mOutPaint)
        val reachArc = progress * 1.0f / max * 360
        canvas.drawArc(rectInner, mStartArc.toFloat(), reachArc, true, mReachPaint)
        if (reachArc != 360f) {
            canvas.drawArc(rectInner, reachArc + mStartArc, 360 - reachArc, true, mNormalPaint)
        }
        canvas.restore()
    }

    /**
     * PROGRESS_STYLE_FILL_IN
     */
    private fun drawFillInCircle(canvas: Canvas) {
        canvas.save()
        canvas.translate(mRealWidth / 2.toFloat(), mRealHeight / 2.toFloat())
        val progressY = progress * 1.0f / max * (mRadius * 2)
        val angle =
            (acos((mRadius - progressY) / mRadius.toDouble()) * 180 / Math.PI).toFloat()
        val startAngle = 90 + angle
        val sweepAngle = 360 - angle * 2
        rectF = RectF(
            (-mRadius).toFloat(),
            (-mRadius).toFloat(),
            mRadius.toFloat(),
            mRadius.toFloat()
        )
        mNormalPaint.style = Paint.Style.FILL
        canvas.drawArc(rectF, startAngle, sweepAngle, false, mNormalPaint)
        canvas.rotate(180f)
        mReachPaint.style = Paint.Style.FILL
        canvas.drawArc(rectF, 270 - angle, angle * 2, false, mReachPaint)
        canvas.rotate(180f)
        if (mTextVisible) {
            val text = mTextPrefix + progress + mTextSuffix
            val textWidth = mTextPaint.measureText(text)
            val textHeight = mTextPaint.descent() + mTextPaint.ascent()
            canvas.drawText(text, -textWidth / 2, -textHeight / 2, mTextPaint)
        }
    }

    /**
     * PROGRESS_STYLE_NORMAL
     */
    private fun drawNormalCircle(canvas: Canvas) {
        canvas.save()
        canvas.translate(mRealWidth / 2.toFloat(), mRealHeight / 2.toFloat())
        if (needDrawInnerBackground) {
            canvas.drawCircle(
                0f, 0f, mRadius - Math.min(mReachBarSize, mNormalBarSize) / 2.toFloat(),
                mInnerBackgroundPaint
            )
        }
        if (mTextVisible) {
            val text = mTextPrefix + progress + mTextSuffix
            val textWidth = mTextPaint.measureText(text)
            val textHeight = mTextPaint.descent() + mTextPaint.ascent()
            canvas.drawText(text, -textWidth / 2, -textHeight / 2, mTextPaint)
        }
        val reachArc = progress * 1.0f / max * 360
        if (reachArc != 360f) {
            canvas.drawArc(rectF, reachArc + mStartArc, 360 - reachArc, false, mNormalPaint)
        }
        canvas.drawArc(rectF, mStartArc.toFloat(), reachArc, false, mReachPaint)
        canvas.restore()
    }

    /**
     * 动画进度(0-当前进度)
     *
     * @param duration 动画时长
     */
    fun runProgressAnim(duration: Long) {
        setProgressInTime(0, duration)
    }

    /**
     * @param progress 进度值
     * @param duration 动画播放时间
     */
    fun setProgressInTime(progress: Int, duration: Long) {
        setProgressInTime(progress, getProgress(), duration)
    }

    /**
     * @param startProgress 起始进度
     * @param progress 进度值
     * @param duration 动画播放时间
     */
    fun setProgressInTime(
        startProgress: Int,
        progress: Int,
        duration: Long
    ) {
        val valueAnimator = ValueAnimator.ofInt(startProgress, progress)
        valueAnimator.addUpdateListener { animator -> //获得当前动画的进度值，整型，1-100之间
            val currentValue = animator.animatedValue as Int
            setProgress(currentValue)
        }
        val interpolator = AccelerateDecelerateInterpolator()
        valueAnimator.interpolator = interpolator
        valueAnimator.duration = duration
        valueAnimator.start()
    }

    fun getReachBarSize(): Int {
        return mReachBarSize
    }

    fun setReachBarSize(reachBarSize: Int) {
        mReachBarSize = Utils.dp2px(context, reachBarSize)
        invalidate()
    }

    fun getNormalBarSize(): Int {
        return mNormalBarSize
    }

    fun setNormalBarSize(normalBarSize: Int) {
        mNormalBarSize = Utils.dp2px(context, normalBarSize)
        invalidate()
    }

    fun getReachBarColor(): Int {
        return mReachBarColor
    }

    fun setReachBarColor(reachBarColor: Int) {
        mReachBarColor = reachBarColor
        invalidate()
    }

    fun getNormalBarColor(): Int {
        return mNormalBarColor
    }

    fun setNormalBarColor(normalBarColor: Int) {
        mNormalBarColor = normalBarColor
        invalidate()
    }

    fun getTextSize(): Int {
        return mTextSize
    }

    fun setTextSize(textSize: Int) {
        mTextSize = Utils.sp2px(context, textSize)
        invalidate()
    }

    fun getTextColor(): Int {
        return mTextColor
    }

    fun setTextColor(textColor: Int) {
        mTextColor = textColor
        invalidate()
    }

    fun getTextSkewX(): Float {
        return mTextSkewX
    }

    fun setTextSkewX(textSkewX: Float) {
        mTextSkewX = textSkewX
        invalidate()
    }

    fun getTextSuffix(): String? {
        return mTextSuffix
    }

    fun setTextSuffix(textSuffix: String) {
        mTextSuffix = textSuffix
        invalidate()
    }

    fun getTextPrefix(): String? {
        return mTextPrefix
    }

    fun setTextPrefix(textPrefix: String) {
        mTextPrefix = textPrefix
        invalidate()
    }

    fun isTextVisible(): Boolean {
        return mTextVisible
    }

    fun setTextVisible(textVisible: Boolean) {
        mTextVisible = textVisible
        invalidate()
    }

    fun isReachCapRound(): Boolean {
        return mReachCapRound
    }

    fun setReachCapRound(reachCapRound: Boolean) {
        mReachCapRound = reachCapRound
        invalidate()
    }

    fun getRadius(): Int {
        return mRadius
    }

    fun setRadius(radius: Int) {
        mRadius = Utils.dp2px(context, radius)
        invalidate()
    }

    fun getStartArc(): Int {
        return mStartArc
    }

    fun setStartArc(startArc: Int) {
        mStartArc = startArc
        invalidate()
    }

    fun getInnerBackgroundColor(): Int {
        return mInnerBackgroundColor
    }

    fun setInnerBackgroundColor(innerBackgroundColor: Int) {
        mInnerBackgroundColor = innerBackgroundColor
        invalidate()
    }

    fun getProgressStyle(): Int {
        return mProgressStyle
    }

    fun setProgressStyle(progressStyle: Int) {
        mProgressStyle = progressStyle
        invalidate()
    }

    fun getInnerPadding(): Int {
        return mInnerPadding
    }

    fun setInnerPadding(innerPadding: Int) {
        mInnerPadding = Utils.dp2px(context, innerPadding)
        val mInnerRadius = mRadius - mOuterSize / 2 - mInnerPadding
        rectInner = RectF(
            (-mInnerRadius).toFloat(),
            (-mInnerRadius).toFloat(),
            mInnerRadius.toFloat(),
            mInnerRadius.toFloat()
        )
        invalidate()
    }

    fun getOuterColor(): Int {
        return mOuterColor
    }

    fun setOuterColor(outerColor: Int) {
        mOuterColor = outerColor
        invalidate()
    }

    fun getOuterSize(): Int {
        return mOuterSize
    }

    fun setOuterSize(outerSize: Int) {
        mOuterSize = Utils.dp2px(context, outerSize)
        invalidate()
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable(CircleProgressView.STATE, super.onSaveInstanceState())
        bundle.putInt(CircleProgressView.PROGRESS_STYLE, getProgressStyle())
        bundle.putInt(CircleProgressView.RADIUS, getRadius())
        bundle.putBoolean(CircleProgressView.IS_REACH_CAP_ROUND, isReachCapRound())
        bundle.putInt(CircleProgressView.START_ARC, getStartArc())
        bundle.putInt(CircleProgressView.INNER_BG_COLOR, getInnerBackgroundColor())
        bundle.putInt(CircleProgressView.INNER_PADDING, getInnerPadding())
        bundle.putInt(CircleProgressView.OUTER_COLOR, getOuterColor())
        bundle.putInt(CircleProgressView.OUTER_SIZE, getOuterSize())
        bundle.putInt(CircleProgressView.TEXT_COLOR, getTextColor())
        bundle.putInt(CircleProgressView.TEXT_SIZE, getTextSize())
        bundle.putFloat(CircleProgressView.TEXT_SKEW_X, getTextSkewX())
        bundle.putBoolean(CircleProgressView.TEXT_VISIBLE, isTextVisible())
        bundle.putString(CircleProgressView.TEXT_SUFFIX, getTextSuffix())
        bundle.putString(CircleProgressView.TEXT_PREFIX, getTextPrefix())
        bundle.putInt(CircleProgressView.REACH_BAR_COLOR, getReachBarColor())
        bundle.putInt(CircleProgressView.REACH_BAR_SIZE, getReachBarSize())
        bundle.putInt(CircleProgressView.NORMAL_BAR_COLOR, getNormalBarColor())
        bundle.putInt(CircleProgressView.NORMAL_BAR_SIZE, getNormalBarSize())
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            val bundle = state
            mProgressStyle = bundle.getInt(CircleProgressView.PROGRESS_STYLE)
            mRadius = bundle.getInt(CircleProgressView.RADIUS)
            mReachCapRound = bundle.getBoolean(CircleProgressView.IS_REACH_CAP_ROUND)
            mStartArc = bundle.getInt(CircleProgressView.START_ARC)
            mInnerBackgroundColor = bundle.getInt(CircleProgressView.INNER_BG_COLOR)
            mInnerPadding = bundle.getInt(CircleProgressView.INNER_PADDING)
            mOuterColor = bundle.getInt(CircleProgressView.OUTER_COLOR)
            mOuterSize = bundle.getInt(CircleProgressView.OUTER_SIZE)
            mTextColor = bundle.getInt(CircleProgressView.TEXT_COLOR)
            mTextSize = bundle.getInt(CircleProgressView.TEXT_SIZE)
            mTextSkewX = bundle.getFloat(CircleProgressView.TEXT_SKEW_X)
            mTextVisible = bundle.getBoolean(CircleProgressView.TEXT_VISIBLE)
            mTextSuffix = bundle.getString(CircleProgressView.TEXT_SUFFIX)!!
            mTextPrefix = bundle.getString(CircleProgressView.TEXT_PREFIX)!!
            mReachBarColor = bundle.getInt(CircleProgressView.REACH_BAR_COLOR)
            mReachBarSize = bundle.getInt(CircleProgressView.REACH_BAR_SIZE)
            mNormalBarColor = bundle.getInt(CircleProgressView.NORMAL_BAR_COLOR)
            mNormalBarSize = bundle.getInt(CircleProgressView.NORMAL_BAR_SIZE)
            initPaint()
            super.onRestoreInstanceState(bundle.getParcelable(CircleProgressView.STATE))
            return
        }
        super.onRestoreInstanceState(state)
    }

    override fun invalidate() {
        initPaint()
        super.invalidate()
    }


}