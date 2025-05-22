package tw.com.chainsea.chat.messagekit.main.viewholder

import android.os.Handler
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.widget.TextView
import tw.com.chainsea.chat.util.VibratorKit
import kotlin.math.abs

class TextClickMovementMethod(
    private val onMessageClick: () -> Unit,
    private val onMessageLongClick: () -> Unit
) : LinkMovementMethod() {

    private var clickCount = 0
    private var downX = 0f
    private var downY = 0f
    private var moveX = 0f
    private var moveY = 0f
    private var upX = 0f
    private var upY = 0f

    private var lastDownTime: Long = 0
    private var lastUpTime: Long = 0
    private var firstClick: Long = 0
    private var secondClick: Long = 0

    private var isDoubleClick = false
    private val MAX_LONG_PRESS_TIME = 500
    private val MAX_SINGLE_CLICK_TIME = 200
    private val MAX_MOVE_FOR_CLICK = 20

    private val eventHandler = Handler()
    var widget: TextView? = null
    var link: ClickableSpan? = null

    private val longPressTask = Runnable {
        clickCount = 0
        VibratorKit.longClick()
        onMessageLongClick()
    }

    private val singleClickTask: Runnable = Runnable {
        clickCount = 0
        link?.let {
            //                if (checkBox.getVisibility() == View.VISIBLE) {
//                    val isCheck: Boolean = checkBox.isChecked()
//                    checkBox.setChecked(!isCheck)
//                } else {
                    link!!.onClick(widget!!)
//                }
            widget = null
            link = null
        } ?: run {
            onMessageClick
        }
    }

    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
        this.widget = widget
        val action = event.action
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            var x = event.x.toInt()
            var y = event.y.toInt()

            x -= widget.totalPaddingLeft
            y -= widget.totalPaddingTop

            x += widget.scrollX
            y += widget.scrollY

            val layout = widget.layout
            val line = layout.getLineForVertical(y)
            val off = layout.getOffsetForHorizontal(line, x.toFloat())

            val links = buffer.getSpans(
                off, off,
                ClickableSpan::class.java
            )
            if (links.isNotEmpty()) {
                link = links[0]
            }
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                eventHandler.removeCallbacks(longPressTask)
                lastDownTime = System.currentTimeMillis()
                //                closeOpenMenu();
                downX = event.x
                downY = event.y
                clickCount++
                if (singleClickTask != null) {
                    eventHandler.removeCallbacks(singleClickTask)
                }
                if (!isDoubleClick) eventHandler.postDelayed(
                    longPressTask,
                    MAX_LONG_PRESS_TIME.toLong()
                )
                if (1 == clickCount) {
                    firstClick = System.currentTimeMillis()
                } else if (clickCount == 2) {
                    secondClick = System.currentTimeMillis()
                    if (secondClick - firstClick <= MAX_LONG_PRESS_TIME) {
                        //處理雙擊
                        VibratorKit.doubleClick()
//                            doubleClick(this.message)
                        isDoubleClick = true
                        clickCount = 0
                        firstClick = 0
                        secondClick = 0
                        eventHandler.removeCallbacks(singleClickTask)
                        eventHandler.removeCallbacks(longPressTask)
                    }
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                eventHandler.removeCallbacks(singleClickTask)
                eventHandler.removeCallbacks(longPressTask)
            }

            MotionEvent.ACTION_MOVE -> {
                moveX = event.x
                moveY = event.y
                val absMx = abs((moveX - downX).toDouble()).toFloat()
                val absMy = abs((moveY - downY).toDouble()).toFloat()
                if (absMy > MAX_MOVE_FOR_CLICK) {
                    eventHandler.removeCallbacks(longPressTask)
                    eventHandler.removeCallbacks(singleClickTask)
                    isDoubleClick = false
                    clickCount = 0
                }
            }

            MotionEvent.ACTION_UP -> {
                lastUpTime = System.currentTimeMillis()
                upX = event.x
                upY = event.y


                val mx = abs((upX - downX).toDouble()).toFloat()
                val my = abs((upY - downY).toDouble()).toFloat()
                if (my <= MAX_MOVE_FOR_CLICK) {
                    if ((lastUpTime - lastDownTime) <= MAX_LONG_PRESS_TIME) {
                        eventHandler.removeCallbacks(longPressTask)
                        if (!isDoubleClick) eventHandler.postDelayed(
                            singleClickTask!!,
                            MAX_SINGLE_CLICK_TIME.toLong()
                        )
                    } else {
                        clickCount = 0
                    }
                } else {
                    eventHandler.removeCallbacks(longPressTask)
                    clickCount = 0
                    downX = 0.0f
                    return true
                }
                if (isDoubleClick) isDoubleClick = false
            }
        }
        return true
    }
}