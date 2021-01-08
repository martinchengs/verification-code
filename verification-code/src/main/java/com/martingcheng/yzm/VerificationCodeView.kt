package com.martingcheng.yzm

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.text.InputFilter
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.content.res.use

class VerificationCodeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatEditText(context, attrs, defStyleAttr) {
    private val cursorPaint = Paint()
    private val stylePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
    private var mLength: Int = 0
    private var mCursorWidth: Int = 0
    private var mCursorColor: Int = 0
    private var mOutlook: Int = 0
    private var mOutlookStrokeWidth: Int = 0
    private var mOutlookStrokeNormalColor: Int = 0
    private var mOutlookStrokeActiveColor: Int = 0
    private var mOutlookBackgroundColor: Int? = null
    private var mPasswordStyle: Boolean = false
    private var mOutlookRadius: Int = 0
    private var mPerSpacing: Int = 0
    private var mPerWidth = 0
    private var mPasswordTxt = "•"
    private val rect by lazy { RectF() }
    private var mListeners: ArrayList<OnVerificationCodeWatcher>? = null
    private val cursorAnim: ValueAnimator = ValueAnimator.ofInt(0, 2).apply {
        duration = 1000
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.RESTART
    }
    private var isAttachedToWindows: Boolean = false

    init {
        context.obtainStyledAttributes(attrs, R.styleable.VerificationCodeView).use {
            mLength = it.getInt(R.styleable.VerificationCodeView_vcv_length, 4)
            mCursorWidth =
                it.getDimensionPixelSize(R.styleable.VerificationCodeView_vcv_cursorWidth, 2.toDp())
            mCursorColor = it.getColor(R.styleable.VerificationCodeView_vcv_cursorColor, Color.GRAY)
            mOutlook = it.getInt(R.styleable.VerificationCodeView_vcv_outlook, 0)
            mOutlookStrokeWidth = it.getDimensionPixelSize(
                R.styleable.VerificationCodeView_vcv_outlookStrokeWidth,
                1.toDp()
            )
            mOutlookRadius =
                it.getDimensionPixelSize(R.styleable.VerificationCodeView_cvc_outlookRadius, 0)
            mOutlookStrokeNormalColor = it.getColor(
                R.styleable.VerificationCodeView_vcv_outlookStrokeNormalColor,
                Color.GRAY
            )
            mOutlookStrokeActiveColor = it.getColor(
                R.styleable.VerificationCodeView_vcv_outlookStrokeActiveColor,
                Color.BLACK
            )
            if (it.hasValue(R.styleable.VerificationCodeView_vcv_outlookBackgroundColor)) {
                mOutlookBackgroundColor =
                    it.getColor(
                        R.styleable.VerificationCodeView_vcv_outlookBackgroundColor,
                        Color.TRANSPARENT
                    )
            }
            mPasswordStyle =
                it.getBoolean(R.styleable.VerificationCodeView_cvc_passwordStyle, false)
            mPerSpacing =
                it.getDimensionPixelSize(R.styleable.VerificationCodeView_vcv_perSpacing, 10.toDp())
            cursorPaint.color = mCursorColor
        }
        //设置最大长度
        filters = arrayOf(*filters, InputFilter.LengthFilter(mLength))
        //背景
        setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        //最大行数
        maxLines = 1
    }


    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        if (!isAttachedToWindows) return
        if ((text?.length ?: 0) >= mLength) {
            cursorAnim.takeIf { it.isStarted || it.isRunning }?.end()
            if (text?.length ?: 0 == mLength) {
                mListeners?.forEach { it.onTextCompleted(text) }
            }
        } else if (!cursorAnim.isRunning) {
            cursorAnim.start()
        }
        mListeners?.forEach { it.onTextChanged(text, start, lengthBefore, lengthAfter) }
    }

    fun addOnVerificationCodeChangeListener(listener: OnVerificationCodeWatcher) {
        val listeners =
            mListeners ?: arrayListOf<OnVerificationCodeWatcher>().also { mListeners = it }
        listeners.add(listener)
    }

    fun removeVerificationCodeChangeListener(listener: OnVerificationCodeWatcher) {
        val listeners = mListeners ?: return
        val index = listeners.indexOf(listener)
        if (index >= 0) listeners.removeAt(index)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val viewWidth = if (widthMode == MeasureSpec.EXACTLY) {
            MeasureSpec.getSize(widthMeasureSpec)
        } else {
            screenWidth()
        }
        //计算每一个的宽度
        mPerWidth = (viewWidth - paddingLeft - paddingRight - (mLength - 1) * mPerSpacing) / mLength
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val viewHeight = if (heightMode == MeasureSpec.EXACTLY) {
            MeasureSpec.getSize(heightMeasureSpec)
        } else {
            mPerWidth + paddingTop + paddingBottom
        }
        val wordSize = paint.measureText("H")
        if (mPerWidth < wordSize) throw IllegalArgumentException("width is illegal")
        setMeasuredDimension(viewWidth, viewHeight)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            showKeyboard()
            text?.let { setSelection(it.length) }
            return false
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        //获取当前已经输入的文字长度
        val textLength = text?.length ?: 0
        //绘制外观
        drawOutlookStyle(textLength, canvas)
        //绘制指示器
        drawCursor(textLength, canvas)
        //绘制文字
        drawText(textLength, canvas)
    }

    private fun drawText(textLength: Int, canvas: Canvas) {
        if (textLength == 0) return
        for (i in 0 until textLength) {
            var txt = text?.substring(i, i + 1) ?: ""
            txt = if (mPasswordStyle) mPasswordTxt else txt
            val textWidth = paint.measureText(txt)
            var left = (((mPerWidth + mPerSpacing) * i) + paddingLeft).toFloat()
            val right = left + mPerWidth.toFloat()
            left += (right - left - textWidth) / 2
            val top =
                ((height - paddingTop - paddingBottom) - (paint.descent() + paint.ascent())) / 2 + paddingTop
            canvas.drawText(txt, left, top, paint)
        }
    }

    private fun drawOutlookStyle(textLength: Int, canvas: Canvas) {
        if (mOutlook == 0) { //line
            drawLineOutlookStyle(textLength, canvas)
        } else if (mOutlook == 1) { //rect
            val fill = mOutlookBackgroundColor != null
            drawRectOutlookStyle(
                textLength,
                canvas,
                mOutlookBackgroundColor ?: Color.TRANSPARENT,
                fill
            )
        }
    }

    private fun drawLineOutlookStyle(textLength: Int, canvas: Canvas) {
        stylePaint.style = Paint.Style.FILL
        for (i in 0 until mLength) {
            if (textLength == i) {
                stylePaint.color = mOutlookStrokeActiveColor
            } else {
                stylePaint.color = mOutlookStrokeNormalColor
            }
            val left = (((mPerWidth + mPerSpacing) * i) + paddingLeft).toFloat()
            val top = (height - mOutlookStrokeWidth).toFloat()
            val right = left + mPerWidth.toFloat()
            val bottom = height.toFloat()
            canvas.drawRect(left, top, right, bottom, stylePaint)
        }
    }

    private fun drawRectOutlookStyle(
        textLength: Int, canvas: Canvas, color: Int = 0, fill: Boolean = false
    ) {
        for (i in 0 until mLength) {
            val left = (((mPerWidth + mPerSpacing) * i) + paddingLeft).toFloat()
            val top = paddingTop.toFloat()
            val right = left + mPerWidth.toFloat()
            val bottom = (height - paddingBottom).toFloat()
            rect.setEmpty()
            rect.set(left, top, right, bottom)
            rect.inset(mOutlookStrokeWidth * 2f, mOutlookStrokeWidth * 2f)
            val radius = mOutlookRadius.toFloat()
            if (mOutlookRadius == 0) {
                if (fill) {
                    stylePaint.style = Paint.Style.FILL
                    stylePaint.color = color
                    canvas.drawRect(rect, stylePaint)
                }
                if (mOutlookStrokeWidth > 0) {
                    if (textLength == i) {
                        stylePaint.color = mOutlookStrokeActiveColor
                    } else {
                        stylePaint.color = mOutlookStrokeNormalColor
                    }
                    stylePaint.style = Paint.Style.STROKE
                    stylePaint.strokeWidth = mOutlookStrokeWidth.toFloat()
                    canvas.drawRect(rect, stylePaint)
                }
            } else {
                if (fill) {
                    stylePaint.style = Paint.Style.FILL
                    stylePaint.color = color
                    canvas.drawRoundRect(rect, radius, radius, stylePaint)
                }
                if (mOutlookStrokeWidth > 0) {
                    if (textLength == i) {
                        stylePaint.color = mOutlookStrokeActiveColor
                    } else {
                        stylePaint.color = mOutlookStrokeNormalColor
                    }
                    stylePaint.style = Paint.Style.STROKE
                    stylePaint.strokeWidth = mOutlookStrokeWidth.toFloat()
                    canvas.drawRoundRect(rect, radius, radius, stylePaint)
                }
            }
        }
    }

    private fun drawCursor(textLength: Int, canvas: Canvas) {
        var left = (((mPerWidth + mPerSpacing) * textLength) + paddingLeft).toFloat()
        val top = paddingTop.toFloat() + compoundDrawablePadding + mOutlookStrokeWidth
        var right = left + mPerWidth.toFloat()
        val bottom =
            (height - paddingBottom).toFloat() - compoundDrawablePadding - mOutlookStrokeWidth
        left += (right - left - mCursorWidth) / 2
        right = left + mCursorWidth
        canvas.drawRect(left, top, right, bottom, cursorPaint)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        this.isAttachedToWindows = true
        if (!cursorAnim.isRunning) {
            cursorAnim.start()
        }
        cursorAnim.addUpdateListener {
            val v = it.animatedValue as Int
            if (v == 0) {
                cursorPaint.color = Color.TRANSPARENT
            } else {
                cursorPaint.color = mCursorColor
            }
            postInvalidate()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        this.isAttachedToWindows = false
        if (cursorAnim.isRunning || cursorAnim.isStarted) {
            cursorAnim.end()
        }
        cursorAnim.removeAllUpdateListeners()
    }

    fun setPasswordTxt(txt: String) {
        this.mPasswordTxt = txt;
        invalidate()
    }

    fun setPasswordStyle(password: Boolean) {
        this.mPasswordStyle = password
        invalidate()
    }

    private fun showKeyboard() {
        isFocusable = true
        isFocusableInTouchMode = true
        requestFocus()
        val im =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager ?: return
        im.showSoftInput(this, InputMethodManager.SHOW_FORCED)
    }

    private fun Int.toDp() = (this * Resources.getSystem().displayMetrics.density + .5f).toInt()

    /*获取屏幕宽度*/
    private fun screenWidth() = Resources.getSystem().displayMetrics.widthPixels

    interface OnVerificationCodeWatcher {
        fun onTextChanged(
            text: CharSequence?,
            start: Int,
            lengthBefore: Int,
            lengthAfter: Int
        )

        fun onTextCompleted(text: CharSequence?)
    }
}