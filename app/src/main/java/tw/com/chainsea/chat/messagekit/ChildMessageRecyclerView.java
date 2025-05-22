package tw.com.chainsea.chat.messagekit;

import android.content.Context;
import android.util.AttributeSet;

import java.lang.ref.WeakReference;
import java.util.List;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.chat.messagekit.child.adapter.ChildMessageAdapter;
import tw.com.chainsea.chat.ui.adapter.WrapContentLinearLayoutManager;
import tw.com.chainsea.custom.view.recyclerview.MaxHeightRecyclerView;

/**
 * Create by evan on 1/22/21
 *
 * @author Evan Wang
 * date 1/22/21
 */
public class ChildMessageRecyclerView extends MaxHeightRecyclerView {

    private WeakReference<Context> weakReference;
    private ChildMessageAdapter<ChatRoomEntity , MessageEntity> adapter;

    public ChildMessageRecyclerView(Context context) {
        super(context);
        init(context);
    }

    public ChildMessageRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ChildMessageRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.weakReference = new WeakReference<Context>(context);
        setLayoutManager(new WrapContentLinearLayoutManager(context));
    }

    public void setContainer(List<MessageEntity> messageEntities, ChatRoomEntity chatRoomEntity) {
        this.adapter = new ChildMessageAdapter(this.weakReference.get(), messageEntities, chatRoomEntity);
        setAdapter(this.adapter);
    }

    public ChildMessageRecyclerView refreshData() {
        if (this.adapter != null) {
            this.adapter.refreshData();
        }
        return this;
    }

    public void refreshToBottom(boolean isScroll) {
        this.adapter.refreshData();
        if (isScroll) {
            scrollToPosition(this.adapter.getItemCount() - 1);
        }
    }
}
