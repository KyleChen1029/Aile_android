package tw.com.chainsea.chat.messagekit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.ui.UiHelper;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.chat.messagekit.lib.MessageDomino;
import tw.com.chainsea.chat.messagekit.listener.CheckFacebookCommentStatus;
import tw.com.chainsea.chat.messagekit.listener.OnFacebookReplyClick;
import tw.com.chainsea.chat.messagekit.listener.OnMainMessageControlEventListener;
import tw.com.chainsea.chat.messagekit.listener.OnMainMessageScrollStatusListener;
import tw.com.chainsea.chat.messagekit.listener.OnMessageSlideReply;
import tw.com.chainsea.chat.messagekit.listener.OnRobotChatMessageClickListener;
import tw.com.chainsea.chat.messagekit.listener.OnTemplateClickListener;
import tw.com.chainsea.chat.messagekit.main.adapter.MessageAdapter;
import tw.com.chainsea.chat.messagekit.main.adapter.MessageAdapterMode;
import tw.com.chainsea.chat.ui.adapter.WrapContentLinearLayoutManager;
import tw.com.chainsea.custom.view.recyclerview.NestedRecyclerView;

/**
 * MsgPanel
 * Created by 90Chris on 2016/6/3.
 */
public class MessageRecyclerView extends NestedRecyclerView {
    private MessageAdapter<MessageEntity> adapter;
    private InputMethodManager inputMethodManager;

    private boolean isUserScroll = false;
    private OnMainMessageScrollStatusListener onMainMessageScrollStatusListener;

    public MessageRecyclerView(Context ctx, AttributeSet attrs, int defStyleAttr) {
        super(ctx, attrs, defStyleAttr);
        init(ctx);
    }

    public MessageRecyclerView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        init(ctx);
    }

    public MessageRecyclerView(Context ctx) {
        super(ctx);
        init(ctx);
    }

    private void init(Context ctx) {
        WrapContentLinearLayoutManager wrapContentLinearLayoutManager = new WrapContentLinearLayoutManager(ctx);
        wrapContentLinearLayoutManager.setStackFromEnd(true);
        setLayoutManager(wrapContentLinearLayoutManager);
    }

    public InputMethodManager getInputMethodManager() {
        if (null == inputMethodManager) {
            inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        return inputMethodManager;
    }

    public void setContainer(List<MessageEntity> entities, ChatRoomEntity chatRoomEntity, UserProfileEntity chatOwner) {
        this.adapter = new MessageAdapter<>(entities, chatRoomEntity, chatOwner);
        setAdapter(this.adapter);
    }

    public void refreshToBottom(boolean isScroll) {
        this.adapter.refreshData();
        if (isScroll) {
            scrollToPosition(this.adapter.getItemCount() - 1);
        }
        setRecyclerViewShowLastItem();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refreshToBottom(int position) {
        this.adapter.notifyDataSetChanged();
        post(() -> {
            setRecyclerViewShowLastItem();
            if (position == -1) {
                if (((LinearLayoutManager) getLayoutManager()).findLastVisibleItemPosition() != adapter.getItemCount() - 1 && !isUserScroll) {
                    scrollToPosition(this.adapter.getItemCount() - 1);
                }
            } else {
                scrollToPosition(position);
            }
        });
    }

    public void refreshToBottom(boolean isScroll, int originSize, int size) {
//        this.adapter.refreshData(originSize, size);
        if (isScroll) {
            scrollToPosition(this.adapter.getItemCount() - 1);
        }
        setRecyclerViewShowLastItem();
    }

    public void setRecyclerViewShowLastItem() {
        post(() -> {
            WrapContentLinearLayoutManager layoutManager = (WrapContentLinearLayoutManager) getLayoutManager();
            RecyclerView.Adapter adapter = getAdapter();
            if (layoutManager == null || adapter == null) return;
            View lastVisibleView = layoutManager.getChildAt(adapter.getItemCount() - 1);
            Rect rect = new Rect();
            getHitRect(rect);
            if (lastVisibleView != null) {
                layoutManager.setStackFromEnd(!lastVisibleView.getLocalVisibleRect(rect));
            } else {
                layoutManager.setStackFromEnd(true);
            }
            setLayoutManager(layoutManager);
        });
    }

    /**
     * 移動item到指定可視位置
     */
    public void scrollToPosition(boolean isScroll, int index) {
        if (isScroll) {
            this.adapter.refreshData();
            if (index > -1 && index <= this.adapter.getItemCount() - 1) {
                smoothScrollToPosition(index);
            } else {
                scrollToPosition(this.adapter.getItemCount() - 1);
            }
        }
        setRecyclerViewShowLastItem();
    }

    public void mScrollToPosition(int position) {
        if (adapter != null) {
            this.adapter.refreshData();
            scrollToPosition(position);
        }
    }

    public void refreshToPosition(int position) {
        this.adapter.notifyItemRangeInserted(0, position);
        ((LinearLayoutManager) getLayoutManager()).scrollToPositionWithOffset(position, 0);
        setRecyclerViewShowLastItem();
    }

    /**
     * 获取最后一个可见view的位置
     */
    public int getLastItemPosition() {
        LinearLayoutManager linearManager = (LinearLayoutManager) getLayoutManager();
        return linearManager.findLastVisibleItemPosition();
    }

    public boolean canScrollBottom() {
        return UiHelper.canScrollBottom(this);
//        return UiHelper.canScrollBottom(this, lastVisibleItemPosition);
    }

    //点击一条回复消息，定位原来消息的位置
    public void moveToPosition(int position) {
        ((LinearLayoutManager) getLayoutManager()).scrollToPositionWithOffset(position, 0);
        //TODO 定位时变颜色有问题，下版本处理
//        View childAt  = mLayoutManager.findViewByPosition(position);
//        final LinearLayout llMsgBubbleRoot = childAt.findViewById(R.id.ll_msg_bubble_root);
//        llMsgBubbleRoot.setBackgroundColor(Color.parseColor("#D8D8D8"));
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                llMsgBubbleRoot.setBackgroundColor(Color.parseColor("#FFFFFF"));
//            }
//        }, 500);
    }

    /**
     * 滾動狀態監聽器
     */
    public void setOnMainMessageScrollStatusListener(OnMainMessageScrollStatusListener onMainMessageScrollStatusListener) {
        this.onMainMessageScrollStatusListener = onMainMessageScrollStatusListener;


        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE: // 停止滚动
                        if (onMainMessageScrollStatusListener != null) onMainMessageScrollStatusListener.onStopScrolling(recyclerView);
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING: // 正在被外部拖拽,一般为用户正在用手指滚动
                        isUserScroll = true;
                        if (onMainMessageScrollStatusListener != null) onMainMessageScrollStatusListener.onDragScrolling(recyclerView);
                        break;
                    case RecyclerView.SCROLL_STATE_SETTLING: // 自动滚动开始
                        isUserScroll = true;
                        if (onMainMessageScrollStatusListener != null) onMainMessageScrollStatusListener.onAutoScrolling(recyclerView);
                        break;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    public void setOnMessageControlEventListener(OnMainMessageControlEventListener<MessageEntity> onMainMessageControlEventListener) {
        if (adapter != null) this.adapter.setOnMessageControlEventListener(onMainMessageControlEventListener);
    }

    public void setOnRobotClickListener(OnRobotChatMessageClickListener onRobotChatMessageClickListener) {
        if (adapter != null) this.adapter.setOnRobotMessageClickListener(onRobotChatMessageClickListener);
    }

    public void setOnTemplateClickListener(OnTemplateClickListener onTemplateClickListener) {
        if (adapter != null) this.adapter.setOnTemplateClickListener(onTemplateClickListener);
    }

    public void setOnMessageClickListener(OnMessageSlideReply onMessageSlideReply) {
        if (adapter != null) this.adapter.setOnMessageSlideReply(onMessageSlideReply);
    }

    public void setOnFacebookPublicReplyClick(OnFacebookReplyClick onFacebookReplyClick) {
        if (adapter != null) this.adapter.setOnFacebookReplyClick(onFacebookReplyClick);
    }

    public void setCheckCommentStatus(CheckFacebookCommentStatus checkCommentStatus) {
        if (adapter != null) this.adapter.checkCommentStatus(checkCommentStatus);
    }


//    public void setIsShowCheckBox(boolean isShowCheckBox) {
//        int count = this.adapter.getItemCount();
//        RecyclerView.LayoutManager layoutManager = this.getLayoutManager();
//        if (layoutManager instanceof LinearLayoutManager) {
//            LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
//            //获取最后一个可见view的位置
//            int lastItemPosition = linearManager.findLastVisibleItemPosition();
//            //获取第一个可见view的位置
//            int firstItemPosition = linearManager.findFirstVisibleItemPosition();
////            Log.i("first:: ", "");
////            this.adapter.setIsShowCheckBox(isShowCheckBox).notifyItemRangeChanged(firstItemPosition, lastItemPosition);
//            this.adapter.setIsShowCheckBox(isShowCheckBox).refreshData();
//        }
//    }

    public boolean getIsShowCheckBox() {
        if (adapter == null) return false;
        else return adapter.getIsShowCheckBox();
    }


    public MessageRecyclerView setAdapterMode(MessageAdapterMode mode) {
        this.adapter.setMode(mode).refreshData();
        return this;
    }


    boolean isAnonymous = false;

    public void switchAnonymous() {
        isAnonymous = !isAnonymous;
        if (this.isAnonymous) {
            MessageDomino.init2(0);
        } else {
            MessageDomino.clear();
        }
        this.adapter.setAnonymous(isAnonymous).refreshData();
    }

    public void clearAnonymous() {
        isAnonymous = false;
        this.adapter.setAnonymous(false).refreshData();
    }

    public MessageRecyclerView setKeyword(String keyword) {
        //    private MessageSectionedAdapter adapter;
        if (this.adapter != null) {
            this.adapter.setMode(MessageAdapterMode.DEFAULT)
                .setKeyword(keyword);
        }
        return this;
    }

    public MessageRecyclerView refreshData() {
        if (this.adapter != null) {
            this.adapter.refreshData();
        }
        return this;
    }

    public MessageRecyclerView refreshData(int position, MessageEntity entity) {
        if (this.adapter != null) {
            this.adapter.refreshData(position, entity);
        }
        return this;
    }

    public void notifyChange(MessageEntity message) {
        if (adapter != null) {
            adapter.notifyMessage(message);
        }
    }

    public void refreshCurrentMessage(int position) {
        if (adapter != null) {
            adapter.notifyItemChanged(position);
        }
    }

    public void setIsUserScroll(boolean isUserScroll) {
        this.isUserScroll = isUserScroll;
    }


    @Override
    public MessageAdapter getAdapter() {
        return this.adapter;
    }
}

