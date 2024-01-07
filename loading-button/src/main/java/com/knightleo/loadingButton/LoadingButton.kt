package com.knightleo.loadingButton

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.DeadObjectException
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.res.ResourcesCompat

/**
 * A button that can show a loading icon, extended from [AppCompatButton]
 *
 * Optional styles to customize:
 * - [R.styleable.LoadingButton_circularDrawableType]: What should be shown by default, a image with a rotating
 * animation, or a circular drawable with multiple animating points
 * - [R.styleable.LoadingButton_circularDrawableImage]: The image to show when the type is set to customAnimation
 * WARNING: Do not pass a vector asset to this component, else it will cause a [DeadObjectException]
 * - [R.styleable.LoadingButton_circularDrawablePadding]: The padding of the button for the loading icon
 * - [R.styleable.LoadingButton_circularDrawableAutoRotate]: Whether of not you want a rotation animation on
 * the drawable in case the type is set to customDrawable (default: true)
 * - [R.styleable.LoadingButton_circularDrawableColor]: The color of the circularDrawable when the type has been set
 * to circularDrawable (default: [Color.GRAY])
 * - [R.styleable.LoadingButton_circularDrawableWidth]: The width of the circularDrawable line that is shown when loading,
 * in case the type has been set to circularDrawable (default: 5)
 *
 * @property loading The loading state of the button
 * @property loaderDrawable The lazy instance of the drawable that will be showed
 *
 * @see LoadingButton.loading
 * @see LoadingButton.setNewLoaderDrawable
 *
 * @sample [LoadingButton.setLoadingAndDisableOnClick]
 * @sample [LoadingButton.setCustomDrawable]
 */
class LoadingButton : AppCompatButton {
    constructor(context: Context) : super(context) {
        setupAttributes(null, 0)
    }
    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        setupAttributes(attributeSet, 0)
    }
    constructor(context: Context, attributeSet: AttributeSet?, defStyle: Int) : super(context, attributeSet, defStyle) {
        setupAttributes(attributeSet, defStyle)
    }

    companion object {
        private fun setLoadingAndDisableOnClick(loadingButton: LoadingButton) {
            loadingButton.setOnClickListener {
                loadingButton.loading = true
                loadingButton.isEnabled = false
            }
        }
        private fun setCustomDrawable(loadingButton: LoadingButton) {
            loadingButton.setNewLoaderDrawable(CircularDrawableAnimated(Color.WHITE, 5f))
        }

        private val defaultDrawableRes
            get() = R.drawable.ic_loader
    }

    private val buttonText: String = text.toString()
    private var radius: Int = width / 2
    private lateinit var canvas: Canvas
    private lateinit var loaderDrawable: Lazy<Drawable>
    private var padding: Int = 0
    private var isImage = false

    var loading: Boolean = false
        set(value) {
            field = value
            showLoading(value)
        }

    private fun setupAttributes(attributeSet: AttributeSet?, defStyle: Int){
        with(
            context.obtainStyledAttributes(
                attributeSet,
                R.styleable.LoadingButton,
                defStyle,
                0)
        ) {
            padding =
                getDimension(R.styleable.LoadingButton_circularDrawablePadding, 40f).toInt()
            if(getInt(R.styleable.LoadingButton_circularDrawableType, 0) == 0) {
                val loaderColor =
                    getColor(R.styleable.LoadingButton_circularDrawableColor, Color.GRAY)
                val borderWidth =
                    getFloat(R.styleable.LoadingButton_circularDrawableWidth, 5f)
                loaderDrawable = lazy {
                    CircularDrawableAnimated(loaderColor, borderWidth).apply {
                        setupPosition(padding)
                        start()
                    }
                }
            } else {
                isImage = true
                val rotateImage =
                    getBoolean(
                        R.styleable.LoadingButton_circularDrawableAutoRotate,
                        true
                    )
                val img = getDrawable(R.styleable.LoadingButton_circularDrawableImage) ?:
                    ResourcesCompat.getDrawable(resources, defaultDrawableRes, null)!!
                loaderDrawable =
                    if(rotateImage) lazy {
                        img.setupPosition(padding)
                        img.toRotationAnimatedDrawable()
                    } else lazy {
                        img.setupPosition(padding)
                        img
                    }
            }
        }
    }
    private fun Drawable.setupPosition(padding: Int) {
        val offset = (width - height) / 2
        setBounds(
            offset + padding,
            padding,
            width - offset - padding,
            height - padding
        )
        setPadding(padding, padding, padding, padding)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        radius = width / 2
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if(!this::canvas.isInitialized) this.canvas = canvas
        showLoading(loading, canvas)
    }

    private fun showLoading(loading: Boolean = this.loading, canvas: Canvas = this.canvas) {
        text = if(loading) {
            loaderDrawable.value.draw(canvas)
            ""
        } else {
            buttonText
        }
    }

    private fun Drawable.toRotationAnimatedDrawable(duration: Long = 1000L): Drawable {
        var angle = 0f
        ValueAnimator.ofFloat(0f, -360f).apply {
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            this.duration = duration
            addUpdateListener { angle = it.animatedValue as Float }
            start()
        }
        return object : Drawable() {
            override fun draw(canvas: Canvas) {
                canvas.rotate(angle, pivotX, pivotY)
                this@toRotationAnimatedDrawable.draw(canvas)
            }
            override fun setAlpha(p0: Int) {}
            override fun setColorFilter(p0: ColorFilter?) {}
            override fun getOpacity(): Int = PixelFormat.TRANSPARENT
        }
    }

    /**
     * Sets a new custom drawable to the loading icon, this will be showed instead of the default
     * or whatever has been set in the xml view.
     *
     * If the new drawable implements the interface [Animatable], this function starts the animation,
     * (this only happens if the autoRotate parameter is set to false)
     *
     * WARNING: Do not set this to a vector drawable, else it will cause a [DeadObjectException]
     *
     * @param drawable The drawable to be shown when loading
     * @param setAutoRotate Whether or not it should be rotated, should be false if you're already
     * using an animation that rotates the drawable (default: false)
     *
     */
    fun setNewLoaderDrawable(
        drawable: Drawable,
        setAutoRotate: Boolean = false
    ) {
        loaderDrawable = lazy {
            drawable.setupPosition(padding)
            if(setAutoRotate) drawable.toRotationAnimatedDrawable()
            else {
                if(drawable is Animatable) drawable.start()
                drawable
            }
        }
    }
}