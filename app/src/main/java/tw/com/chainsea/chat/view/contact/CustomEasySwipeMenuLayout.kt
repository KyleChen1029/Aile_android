package tw.com.chainsea.chat.view.contact

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import com.guanaj.easyswipemenulibrary.EasySwipeMenuLayout
import kotlin.math.abs

class CustomEasySwipeMenuLayout : EasySwipeMenuLayout {

    private var startX = 0f
    private var startY = 0f
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop // 系統預設的最小滑動距離

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs,
        defStyleAttr
    )

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                // 記錄初始觸控位置
                startX = ev.x
                startY = ev.y
                parent.requestDisallowInterceptTouchEvent(true) // 預設不讓父容器攔截
            }
            MotionEvent.ACTION_MOVE -> {
                // 計算水平與垂直滑動距離
                val deltaX = ev.x - startX
                val deltaY = ev.y - startY

                // 判斷滑動方向
                if (abs(deltaX) > abs(deltaY) && abs(deltaX) > touchSlop) {
                    // 水平滑動：不讓父容器攔截
                    parent.requestDisallowInterceptTouchEvent(true)
                } else if (abs(deltaY) > abs(deltaX) && abs(deltaY) > touchSlop) {
                    // 垂直滑動：讓父容器攔截（如果是需要的行為）
                    parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // 結束時重置，恢復預設行為
                parent.requestDisallowInterceptTouchEvent(false)
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}