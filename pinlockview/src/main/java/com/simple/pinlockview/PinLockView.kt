package com.simple.pinlockview

/**
 * PinLockView - fully programmatic PIN entry view.
 *
 * Features:
 * - Numeric keypad (0-9) and delete button
 * - Indicator dots that fill as user types
 * - Listener callbacks for PIN change and completion
 * - Methods to customize length, clear, and reset
 *
 * Usage:
 * val pinLock = PinLockView(context)
 * pinLock.maxLength = 4
 * pinLock.setPinLockListener(object: PinLockView.PinLockListener { ... })
 * parent.addView(pinLock)
 */
import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.use

class PinLockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var maxLength = 4
        set(value) {
            field = value.coerceAtLeast(1)
            buildIndicators()
        }

    var enableRipple: Boolean = true
    var enableHaptic: Boolean = true

    var buttonColor: Int = resolveAttrColor(android.R.attr.colorBackground)
    var buttonStrokeColor: Int = resolveAttrColor(android.R.attr.textColorTertiary)
    var buttonTextColor: Int = resolveAttrColor(android.R.attr.textColorPrimary)
    var rippleColor: Int = resolveAttrColor(android.R.attr.colorControlHighlight)

    private var entered = StringBuilder()
    private val indicatorsContainer: LinearLayout
    private val keypad: GridLayout
    private var listener: PinLockListener? = null

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        val pad = dpToPx(12)
        setPadding(pad, pad, pad, pad)

        context.obtainStyledAttributes(attrs, R.styleable.PinLockView).use {
            enableRipple = it.getBoolean(R.styleable.PinLockView_enableRipple, true)
            enableHaptic = it.getBoolean(R.styleable.PinLockView_enableHaptic, true)
            buttonColor = it.getColor(R.styleable.PinLockView_buttonColor, buttonColor)
            buttonStrokeColor =
                it.getColor(R.styleable.PinLockView_buttonStrokeColor, buttonStrokeColor)
            buttonTextColor = it.getColor(R.styleable.PinLockView_buttonTextColor, buttonTextColor)
            rippleColor = it.getColor(R.styleable.PinLockView_rippleColor, rippleColor)
        }

        indicatorsContainer = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
            val lp = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            lp.setMargins(0, 0, 0, dpToPx(16))
            layoutParams = lp
        }

        keypad = GridLayout(context).apply {
            columnCount = 3
            rowCount = 4
            val lp = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            layoutParams = lp
        }

        addView(indicatorsContainer)
        addView(keypad)

        buildIndicators()
        buildKeypad()
    }

    private fun buildIndicators() {
        indicatorsContainer.removeAllViews()
        repeat(maxLength) {
            val dot = View(context).apply {
                val size = dpToPx(12)
                layoutParams = LayoutParams(size, size).also {
                    (it as? LayoutParams)?.setMargins(dpToPx(6), 0, dpToPx(6), 0)
                }
                background = dotDrawable(false)
            }
            indicatorsContainer.addView(dot)
        }
        refreshIndicators()
    }

    private fun dotDrawable(filled: Boolean) = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        val s = dpToPx(12)
        setSize(s, s)
        setColor(
            if (filled) resolveAttrColor(android.R.attr.colorAccent) else resolveAttrColor(
                android.R.attr.textColorTertiary
            )
        )
        setStroke(dpToPx(1), resolveAttrColor(android.R.attr.textColorTertiary))
    }

    private fun refreshIndicators() {
        for (i in 0 until indicatorsContainer.childCount) {
            val v = indicatorsContainer.getChildAt(i)
            val filled = i < entered.length
            v.background = dotDrawable(filled)
            if (filled) animateDot(v)
        }
    }

    private fun animateDot(v: View) {
        val anim = ScaleAnimation(
            0.8f, 1f, 0.8f, 1f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 120
            fillAfter = true
        }
        v.startAnimation(anim)
    }

    private fun buildKeypad() {
        keypad.removeAllViews()

        val nums = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9")
        for (n in nums) keypad.addView(makeNumButton(n))

        keypad.addView(makeEmpty())
        keypad.addView(makeNumButton("0"))
        keypad.addView(makeDeleteButton())
    }

    private fun makeNumButton(text: String): View {
        val btn = TextView(context).apply {
            this.text = text
            setTextColor(buttonTextColor)
            gravity = Gravity.CENTER
            val size = dpToPx(64)
            layoutParams = GridLayout.LayoutParams().apply {
                width = size
                height = size
                setMargins(dpToPx(12), dpToPx(6), dpToPx(12), dpToPx(6))
            }
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            background = createCircularBackground()
            isClickable = true
            isFocusable = true
            setOnClickListener {
                if (enableHaptic) performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                appendDigit(text)
            }
        }
        return btn
    }

    private fun makeDeleteButton(): View {
        val btn = TextView(context).apply {
            text = "⌫"
            setTextColor(buttonTextColor)
            gravity = Gravity.CENTER
            val size = dpToPx(64)
            layoutParams = GridLayout.LayoutParams().apply {
                width = size
                height = size
                setMargins(dpToPx(12), dpToPx(6), dpToPx(12), dpToPx(6))
            }
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            background = createCircularBackground()
            isClickable = true
            isFocusable = true
            setOnClickListener {
                if (enableHaptic) performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                deleteLast()
            }
        }
        return btn
    }

    private fun makeEmpty(): View {
        return View(context).apply {
            layoutParams = GridLayout.LayoutParams().apply {
                width = dpToPx(64)
                height = dpToPx(64)
                setMargins(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6))
            }
        }
    }

    private fun createCircularBackground(): Drawable {
        val radius = dpToPx(32).toFloat()
        val normalDrawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            cornerRadius = radius
            setColor(buttonColor)
            setStroke(dpToPx(1), buttonStrokeColor)
        }

        return if (enableRipple && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            RippleDrawable(
                android.content.res.ColorStateList.valueOf(rippleColor),
                normalDrawable,
                null
            )
        } else {
            val stateDrawable = StateListDrawable()
            stateDrawable.addState(
                intArrayOf(android.R.attr.state_pressed),
                GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    cornerRadius = radius
                    setColor(rippleColor)
                })
            stateDrawable.addState(intArrayOf(), normalDrawable)
            stateDrawable
        }
    }

    private fun appendDigit(d: String) {
        if (entered.length >= maxLength) return
        entered.append(d)
        refreshIndicators()
        listener?.onPinChanged(entered.toString())
        if (entered.length == maxLength) {
            listener?.onComplete(entered.toString())
        }
    }

    private fun deleteLast() {
        if (entered.isNotEmpty()) {
            entered.deleteCharAt(entered.length - 1)
            refreshIndicators()
            listener?.onPinChanged(entered.toString())
        }
    }

    fun setPinLockListener(l: PinLockListener) {
        listener = l
    }

    fun reset() {
        entered.clear()
        refreshIndicators()
    }

    fun getPin(): String = entered.toString()

    fun setInputEnabled(enabled: Boolean) {
        for (i in 0 until keypad.childCount) {
            keypad.getChildAt(i).isEnabled = enabled
            keypad.getChildAt(i).alpha = if (enabled) 1.0f else 0.5f
        }
    }

    interface PinLockListener {
        fun onPinChanged(pin: String)
        fun onComplete(pin: String)
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    private fun resolveAttrColor(attr: Int): Int {
        val typed = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(attr, typed, true)
        return typed.data
    }
}

