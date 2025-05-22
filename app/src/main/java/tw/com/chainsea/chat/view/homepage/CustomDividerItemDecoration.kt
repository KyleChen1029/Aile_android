package tw.com.chainsea.chat.view.homepage

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.recyclerview.widget.RecyclerView
import tw.com.chainsea.android.common.ui.UiHelper

class CustomItemDecoration(context: Context) : RecyclerView.ItemDecoration() {
    private val paint = Paint()

    init {
        paint.color = Color.parseColor("#d8d8d8")
        paint.strokeWidth = UiHelper.dip2px(context, 1f).toFloat()
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(canvas, parent, state)

        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight
        val childCount = parent.childCount

        for (i in 0 until childCount - 1) { // 忽略最後一項
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin
            val bottom = top + paint.strokeWidth.toInt()
            canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
        }
    }
}