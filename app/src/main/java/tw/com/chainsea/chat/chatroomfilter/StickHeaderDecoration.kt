package tw.com.chainsea.chat.chatroomfilter

import android.graphics.Canvas
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tw.com.chainsea.android.common.ui.UiHelper

class StickHeaderDecoration(private val recyclerView: RecyclerView) :
    RecyclerView.ItemDecoration() {
    interface StickHeaderInterFace {
        fun isStick(position: Int): Boolean
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val adapter = recyclerView.adapter ?: return
        val manager = recyclerView.layoutManager ?: return
        val childAt = parent.getChildAt(0) ?: return
        val stickHeaderInterface = adapter as StickHeaderInterFace
        val childViewHolder = parent.getChildViewHolder(childAt)
        var position = childViewHolder.absoluteAdapterPosition
        if ((manager as LinearLayoutManager).findFirstVisibleItemPosition() == 0) {
            position = 0
        }
        for (i in position downTo 0) {
            if (stickHeaderInterface.isStick(i)) {
                var top = 0
                if (position + 1 < adapter.itemCount) {
                    if (stickHeaderInterface.isStick(position + 1)) {
                        val childNext = parent.getChildAt(1)
                        top = if (manager.getDecoratedTop(childNext) < 0) 0 else manager
                            .getDecoratedTop(childNext)
                    }
                }
                val inflate = adapter
                    .createViewHolder(
                        parent,
                        adapter.getItemViewType(i)
                    )
                val paddingStart = UiHelper.dip2px(inflate.itemView.context, 15F)
                val padding = UiHelper.dip2px(inflate.itemView.context, 10F)
                inflate.itemView.setPadding(paddingStart, padding, padding, padding)
                adapter.bindViewHolder(inflate, i)
                val measureHeight = getMeasureHeight(inflate.itemView)
                c.save()
                if (top < inflate.itemView.measuredHeight && top > 0) {
                    c.translate(0F, (top - measureHeight).toFloat())
                }
                inflate.itemView.draw(c)
                c.restore()
                return
            }
        }
    }

    private fun getMeasureHeight(header: View): Int {
        val widthSpec = View.MeasureSpec.makeMeasureSpec(
            recyclerView.width, View
                .MeasureSpec.EXACTLY
        )
        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        header.measure(widthSpec, heightSpec)
        header.layout(0, 0, header.measuredWidth, header.measuredHeight)
        return header.measuredHeight
    }
}