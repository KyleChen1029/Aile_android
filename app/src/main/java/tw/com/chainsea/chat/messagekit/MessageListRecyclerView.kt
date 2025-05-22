package tw.com.chainsea.chat.messagekit

import android.content.Context
import android.util.AttributeSet
import android.view.ViewTreeObserver
import androidx.recyclerview.widget.RecyclerView
import tw.com.chainsea.chat.ui.adapter.WrapContentLinearLayoutManager

class MessageListRecyclerView : RecyclerView {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var isNeedToSetStackFromEnd = true

    init {
        val wrapContentLinearLayoutManager = WrapContentLinearLayoutManager(context)
        wrapContentLinearLayoutManager.stackFromEnd = true
        wrapContentLinearLayoutManager.recycleChildrenOnDetach = true
        layoutManager = wrapContentLinearLayoutManager
    }

    fun setRecyclerViewShowLastItem() {
        post {
            if (!isNeedToSetStackFromEnd) return@post
            val layoutManager = layoutManager as? WrapContentLinearLayoutManager
            val adapter = adapter
            if (layoutManager == null || adapter == null) return@post

            val recyclerViewHeight = height // RecyclerView 的實際高度
            var totalContentHeight = 0
            var childCount = layoutManager.childCount

            // 遍歷可見的子項來計算總高度
            for (i in 0 until childCount) {
                val child = layoutManager.getChildAt(i)
                child?.apply {
                    viewTreeObserver.addOnGlobalLayoutListener(
                        object : ViewTreeObserver.OnGlobalLayoutListener {
                            override fun onGlobalLayout() {
                                isNeedToSetStackFromEnd = false
                                viewTreeObserver.removeOnGlobalLayoutListener(this)
                                totalContentHeight += child.height
                                childCount--
                                if (childCount == 0) {
                                    layoutManager.stackFromEnd = totalContentHeight > recyclerViewHeight
                                    setLayoutManager(layoutManager)
                                    layoutManager.scrollToPosition(layoutManager.childCount - 1)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

fun RecyclerView.isScrolledToBottom(): Boolean = !canScrollVertically(1)
