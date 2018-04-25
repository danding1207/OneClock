package com.msc.onclockview

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.support.v4.view.animation.LinearOutSlowInInterpolator
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.nineoldandroids.view.ViewHelper
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_one_clock.view.*
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by danding1207 on 18/4/25.
 */
class OneClockView : LinearLayout {

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
        initCustomAttrs(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(context)
        initCustomAttrs(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        initView(context)
        initCustomAttrs(context, attrs)
    }

    private var isRotation: Boolean = false
    private var hour_text_color: Int = 0
    private var minute_text_color: Int = 0
    private var second_text_color: Int = 0
    private var background_color: Int = 0

    private var fontSize: Int = 0
    private var mDisposable: Disposable? = null

    /**
     * 获取自定义属性
     */
    private fun initCustomAttrs(context: Context, attrs: AttributeSet?) {
        //获取自定义属性。
        val ta = context.obtainStyledAttributes(attrs, R.styleable.OneClock)
        //获取字体大小,默认大小是74dp
        fontSize = ta.getDimension(R.styleable.OneClock_text_font_size, 74f).toInt()

        //获取颜色
        hour_text_color = ta.getColor(R.styleable.OneClock_hour_text_color, Color.WHITE)
        minute_text_color = ta.getColor(R.styleable.OneClock_minute_text_color, Color.WHITE)
        second_text_color = ta.getColor(R.styleable.OneClock_second_text_color, Color.WHITE)
        background_color = ta.getColor(R.styleable.OneClock_background_color,
                ContextCompat.getColor(context, R.color.cardview_gray_background))

        isRotation = ta.getBoolean(R.styleable.OneClock_isRotation, true)

        ta.recycle()
    }

    private fun initView(context: Context) {
        LayoutInflater.from(context)
                .inflate(R.layout.layout_one_clock, this, true)
        setCameraDistance()

        mDisposable = Observable.interval(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { onTimeChanged() }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (mDisposable != null) {
            mDisposable!!.dispose()
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        ViewHelper.setPivotY(min_bottom_c!!, 0.0f)
        ViewHelper.setPivotY(min_top_b!!, min_top_b!!.height.toFloat())
        ViewHelper.setPivotY(hour_bottom_c!!, 0.0f)
        ViewHelper.setPivotY(hour_top_b!!, min_top_b!!.height.toFloat())
    }

    private var hourTemp = 0
    private var minuteTemp = 0
    private var secondTemp = 0

    private var hourString: String? = null
    private var hourStringNext: String? = null

    private var minuteString: String? = null

    private var secondString: String? = null

    private fun onTimeChanged() {

        val calendar = Calendar.getInstance()
        //小时
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        //分钟
        val minute = calendar.get(Calendar.MINUTE)
        //秒
        val second = calendar.get(Calendar.SECOND)

        setClock(hour, minute, second)

        isRotation(hour, minute, second)
    }

    /**
     * 更新时间
     * @param hour
     * *
     * @param minute
     * *
     * @param second
     */
    private fun setClock(hour: Int, minute: Int, second: Int) {
        if (hour != hourTemp) {
            hourTemp = hour
            if (hour < 10) {
                hourString = "0" + hour
            } else {
                hourString = "" + hour
            }
            tv_hour_top!!.text = hourString
            tv_hour_bottom!!.text = hourString
        }

        if (minute != minuteTemp) {
            minuteTemp = minute
            if (minute < 10) {
                minuteString = "0" + minute
            } else {
                minuteString = "" + minute
            }
            tv_min_top!!.text = minuteString
            tv_min_bottom!!.text = minuteString
        }


        if (second != secondTemp) {
            secondTemp = second
            if (second < 10) {
                secondString = "0" + second
            } else {
                secondString = "" + second
            }
            tv_second!!.text = secondString
        }
    }

    /**
     * 判断是否需要翻页
     * @param hour
     * *
     * @param minute
     * *
     * @param second
     */
    private fun isRotation(hour: Int, minute: Int, second: Int) {
        if (!isRotation)
            return
        if (second == 59) {
            val minuteStringNext: String
            if (minute < 9 && minute >= 0) {
                minuteString = "0" + minute
                minuteStringNext = "0" + (minute + 1)
            } else if (minute == 9) {
                minuteString = "0" + minute
                minuteStringNext = "" + (minute + 1)
            } else if (minute == 59) {
                minuteString = "0" + minute
                minuteStringNext = "00"

                if (hour < 23) {
                    hourStringNext = "" + (hour + 1)
                } else {
                    hourStringNext = "00"
                }

                tv_hour_top_b!!.text = hourString
                tv_hour_bottom_c!!.text = hourStringNext

                startRotation(hour_top_b, hour_bottom_c, tv_hour_top, hourStringNext!!)

            } else {
                minuteString = "" + minute
                minuteStringNext = "" + (minute + 1)
            }
            tv_min_top_b!!.text = minuteString
            tv_min_bottom_c!!.text = minuteStringNext

            startRotation(min_top_b, min_bottom_c, tv_min_top, minuteStringNext)
        }
    }

    /**
     * 改变视角距离, 贴近屏幕
     */
    private fun setCameraDistance() {
        val distance = 10000
        val scale = resources.displayMetrics.density * distance
        min_top_b!!.cameraDistance = scale
        min_bottom_c!!.cameraDistance = scale
        hour_top_b!!.cameraDistance = scale
        hour_bottom_c!!.cameraDistance = scale
    }

    /**
     * 翻页方法
     * @param topView 上半页
     * *
     * @param bottomView 下半页
     * *
     * @param tvTop 上半页显示新字符
     * *
     * @param tvTopString 上半页新的字符
     */
    private fun startRotation(topView: View, bottomView: View, tvTop: TextView, tvTopString: String) {
        ViewHelper.setRotationX(bottomView, 180f)
        val animatorSet = AnimatorSet()

        val animator1 = ObjectAnimator.ofFloat(bottomView, View.ROTATION_X, 0f)
        val animator2 = ObjectAnimator.ofFloat(topView, View.ROTATION_X, -180f)

        animator2.interpolator = LinearOutSlowInInterpolator()
        animator1.interpolator = LinearOutSlowInInterpolator()
        animator2.duration = 1000
        animator1.duration = 1000

        animator1.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            if (85.0 <= value && value <= 95.0) {
                topView.visibility = View.INVISIBLE
                bottomView.visibility = View.VISIBLE
                tvTop.text = tvTopString
            }
        }

        animator1.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {
                topView.visibility = View.VISIBLE
                bottomView.visibility = View.INVISIBLE
            }

            override fun onAnimationEnd(animator: Animator) {
                topView.visibility = View.INVISIBLE
                bottomView.visibility = View.INVISIBLE
                ViewHelper.setRotationX(topView, 0f)//恢复原位置
            }

            override fun onAnimationCancel(animator: Animator) {}
            override fun onAnimationRepeat(animator: Animator) {}
        })
        animator2.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {
                topView.visibility = View.VISIBLE
                bottomView.visibility = View.INVISIBLE
            }

            override fun onAnimationEnd(animator: Animator) {
                topView.visibility = View.INVISIBLE
                bottomView.visibility = View.INVISIBLE
                ViewHelper.setRotationX(topView, 0f)//恢复原位置
            }

            override fun onAnimationCancel(animator: Animator) {}
            override fun onAnimationRepeat(animator: Animator) {}
        })
        animatorSet.play(animator1).with(animator2)
        animatorSet.start()
    }

}
