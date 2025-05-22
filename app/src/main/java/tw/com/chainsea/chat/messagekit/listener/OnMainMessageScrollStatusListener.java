package tw.com.chainsea.chat.messagekit.listener;

import androidx.recyclerview.widget.RecyclerView;

/**
 * current by evan on 2020-04-23
 *
 * @author Evan Wang
 * @date 2020-04-23
 */
public interface OnMainMessageScrollStatusListener {
    void onStopScrolling(RecyclerView recyclerView); // 停止滾動，空閒中

    void onDragScrolling(RecyclerView recyclerView); // 被外部拖曳中

    void onAutoScrolling(RecyclerView recyclerView); // 自動滾動中

}
