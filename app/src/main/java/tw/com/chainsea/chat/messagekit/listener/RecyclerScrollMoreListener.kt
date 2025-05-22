package tw.com.chainsea.chat.messagekit.listener

import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

private const val VISIBLE_THRESHOLD = 1
internal class RecyclerScrollMoreListener(
    private val layoutManager: LinearLayoutManager,
    private val onLoadMore: (Int, Int) -> Unit
) : RecyclerView.OnScrollListener() {
    override fun onScrolled(view: RecyclerView, dx: Int, dy: Int) {
        val totalItemCount = layoutManager.itemCount
        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
        Log.d("Kyle116", "totalItemCount = $totalItemCount, lastVisibleItemPosition = $lastVisibleItemPosition")
        //if (totalItemCount == (lastVisibleItemPosition + VISIBLE_THRESHOLD)) {
            onLoadMore(totalItemCount, lastVisibleItemPosition)
        //}
    }
}