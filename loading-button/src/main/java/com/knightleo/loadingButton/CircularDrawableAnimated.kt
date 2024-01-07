package com.knightleo.loadingButton

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.util.Property
import android.view.animation.LinearInterpolator
import androidx.core.animation.addListener

class CircularDrawableAnimated(
    borderColor: Int,
    private val borderWidth: Float
) : Drawable(), Animatable {

    companion object {
        private const val ANGLE_ANIMATION_DURATION: Long = 1000
        private const val SWEEP_ANIMATION_DURATION: Long = 1000
        private const val MIN_SWEEP = 60
    }

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = borderWidth
        color = borderColor
    }
    private val fBounds = RectF()
    private var isRunning = false
    private var currentAngle: Float = 0f
    private var currentSweep: Float = 0f
    private var angleOffset: Float = 0f
    private var decreasing: Boolean = false

    private val angleProperty = object : Property<CircularDrawableAnimated, Float>(
        Float::class.java,
        "angle"
    ) {
        override fun get(anim: CircularDrawableAnimated): Float = anim.currentAngle
        override fun set(anim: CircularDrawableAnimated, value: Float) {
            anim.currentAngle = value
            invalidateSelf()
        }
    }
    private val sweepProperty = object : Property<CircularDrawableAnimated, Float>(
        Float::class.java,
        "sweep"
    ) {
        override fun get(anim: CircularDrawableAnimated): Float = anim.currentSweep
        override fun set(anim: CircularDrawableAnimated, value: Float) {
            anim.currentSweep = value
            invalidateSelf()
        }
    }

    private val angleAnimator =
        ObjectAnimator.ofFloat(this, angleProperty, 360f).apply {
            interpolator = LinearInterpolator()
            duration = ANGLE_ANIMATION_DURATION
            repeatCount = ValueAnimator.INFINITE
        }
    private val sweepAnimator =
        ObjectAnimator.ofFloat(this, sweepProperty, 360f - MIN_SWEEP * 2).apply {
            interpolator = LinearInterpolator()
            duration = SWEEP_ANIMATION_DURATION
            repeatCount = ValueAnimator.INFINITE
            addListener {
                decreasing = !decreasing
                if(decreasing) angleOffset = (angleOffset + MIN_SWEEP * 2) % 360
            }
        }

    override fun draw(canvas: Canvas) {
        if(decreasing) {
            canvas.drawArc(fBounds,
                currentAngle - angleOffset,
                currentSweep + MIN_SWEEP,
                false,
                paint
            )
        } else {
            canvas.drawArc(fBounds,
                currentAngle + currentSweep - angleOffset,
                360 - currentSweep - MIN_SWEEP,
                false,
                paint
            )
        }

    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(filter: ColorFilter?) {
        paint.colorFilter = filter
    }

    @Deprecated("", ReplaceWith("PixelFormat.TRANSPARENT", "android.graphics.PixelFormat"))
    override fun getOpacity(): Int = PixelFormat.TRANSPARENT

    override fun start() {
        if(isRunning) return
        isRunning = true
        angleAnimator.start()
        sweepAnimator.start()
        invalidateSelf()
    }

    override fun stop() {
        isRunning = false
        angleAnimator.cancel()
        sweepAnimator.cancel()
        invalidateSelf()
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        fBounds.set(
            bounds.left + borderWidth / 2f + .5f,
            bounds.top + borderWidth / 2f + .5f,
            bounds.right - borderWidth / 2f - .5f,
            bounds.bottom - borderWidth / 2f - .5f
        )
    }

    override fun isRunning(): Boolean = isRunning
}