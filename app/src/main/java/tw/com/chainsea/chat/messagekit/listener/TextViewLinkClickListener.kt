package tw.com.chainsea.chat.messagekit.listener

import android.text.Layout
import android.text.Selection
import android.text.Spannable
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.TextView

open class TextViewLinkClickListener(
    val spannable: Spannable
) : OnTouchListener {
    override fun onTouch(
        v: View?,
        event: MotionEvent
    ): Boolean {
        val action = event.action
        if (v !is TextView) {
            return false
        }
        if (action == MotionEvent.ACTION_UP ||
            action == MotionEvent.ACTION_DOWN
        ) {
            var x = event.x.toInt()
            var y = event.y.toInt()

            x -= v.totalPaddingLeft
            y -= v.totalPaddingTop

            x += v.scrollX
            y += v.scrollY

            val layout: Layout = v.layout
            val line: Int = layout.getLineForVertical(y)
            val off: Int = layout.getOffsetForHorizontal(line, x.toFloat())

            val link =
                spannable.getSpans(
                    off,
                    off,
                    ClickableSpan::class.java
                )

            if (link.isNotEmpty()) {
                if (action == MotionEvent.ACTION_UP) {
                    link[0].onClick(v)
                } else if (action == MotionEvent.ACTION_DOWN) {
                    Selection.setSelection(
                        spannable,
                        spannable.getSpanStart(link[0]),
                        spannable.getSpanEnd(link[0])
                    )
                }
                return true
            } else {
                Selection.removeSelection(spannable)
            }
        }
        v.performClick()
        return false
    }
}
