package tw.com.chainsea.chat.messagekit.child.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.ce.sdk.bean.msg.MessageFlag;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;
import tw.com.chainsea.ce.sdk.bean.msg.SourceType;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ItemMsgBubbleBinding;
import tw.com.chainsea.chat.databinding.ItemMsgBubbleNeedUserBinding;
import tw.com.chainsea.chat.databinding.ItemMsgTipBinding;
import tw.com.chainsea.chat.messagekit.child.viewholder.ChildAtMessageView;
import tw.com.chainsea.chat.messagekit.child.viewholder.ChildBroadcastMessageView;
import tw.com.chainsea.chat.messagekit.child.viewholder.ChildBusinessMessageView;
import tw.com.chainsea.chat.messagekit.child.viewholder.ChildCallMessageView;
import tw.com.chainsea.chat.messagekit.child.viewholder.ChildFileMessageView;
import tw.com.chainsea.chat.messagekit.child.viewholder.ChildImageMessageView;
import tw.com.chainsea.chat.messagekit.child.viewholder.ChildMessageViewBase;
import tw.com.chainsea.chat.messagekit.child.viewholder.ChildNoneMessageView;
import tw.com.chainsea.chat.messagekit.child.viewholder.ChildReplyMessageView;
import tw.com.chainsea.chat.messagekit.child.viewholder.ChildStickerMessageView;
import tw.com.chainsea.chat.messagekit.child.viewholder.ChildTextMessageView;
import tw.com.chainsea.chat.messagekit.child.viewholder.ChildTipMessageView;
import tw.com.chainsea.chat.messagekit.child.viewholder.ChildVideoMessageView;
import tw.com.chainsea.chat.messagekit.child.viewholder.ChildVoiceMessageView;
import tw.com.chainsea.chat.messagekit.listener.OnMainMessageControlEventListener;
import tw.com.chainsea.chat.messagekit.main.adapter.MessageAdapterMode;

/**
 * Create by evan on 1/22/21
 *
 * @author Evan Wang
 * date 1/22/21
 */
public class ChildMessageAdapter<B extends ChatRoomEntity, T extends MessageEntity> extends RecyclerView.Adapter<ChildMessageViewBase> {
    private static final String TAG = ChildMessageAdapter.class.getSimpleName();

    // data
    private final B b;
    private final List<T> entities;

    // control
    private String keyword = "";
//    private boolean isClickEventEnable = true;
//    private boolean isShowCheckBox = false;
//    private boolean isShowRangeSelection = false; // 截圖功能範圍多選

    private MessageAdapterMode mode = MessageAdapterMode.DEFAULT; // 顯示模式
    private boolean isAnonymous = false; //是否啟用匿名

    private Map<Integer, ChildMessageViewBase> holders = Maps.newHashMap();

    // callback
    private OnMainMessageControlEventListener<T> onMessageControlEventListener;

    // verify & makeUp
    private int currentPosition = -1;

    public ChildMessageAdapter(Context context, List<T> entities, B b) {
        this.entities = entities;
        this.b = b;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public int getItemCount() {
        return entities.size();
    }

    @NonNull
    @Override
    public ChildMessageViewBase onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ChildMessageViewBase holder;

        View v;
        ViewBinding viewBinding;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        ChatRoomType type = this.b.getType();
        if (type.equals(ChatRoomType.subscribe) || type.equals(ChatRoomType.services)) {
            viewBinding = ItemMsgBubbleNeedUserBinding.inflate(inflater, parent, false);
//            ItemMsgBubbleNeedUserBinding msgBubbleNeedUserBinding = DataBindingUtil.inflate(inflater, R.layout.item_msg_bubble_need_user, parent, false);
//            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_msg_bubble_need_user, null);
        } else {
            viewBinding = ItemMsgBubbleBinding.inflate(inflater, parent, false);
//            ItemMsgBubbleBinding msgBubbleBinding = DataBindingUtil.inflate(inflater, R.layout.item_msg_bubble, parent, false);
//            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_msg_bubble, null);
        }

        ItemMsgTipBinding msgTipBinding = DataBindingUtil.inflate(inflater, R.layout.item_msg_tip, parent, false);
        switch (viewType) {
            case 1:
                holder = new ChildTipMessageView(msgTipBinding);
                break;
            case 2:
                holder = new ChildReplyMessageView(viewBinding);
                break;
            case 3:
                holder = new ChildTextMessageView(viewBinding);
                break;
            case 4:
                holder = new ChildAtMessageView(viewBinding);
                break;
            case 5:
                holder = new ChildImageMessageView(viewBinding);
                break;
            case 6:
                holder = new ChildStickerMessageView(viewBinding);
                break;
            case 7:
                holder = new ChildFileMessageView(viewBinding);
                break;
            case 8:
                holder = new ChildVoiceMessageView(viewBinding);
                break;
            case 9:
                holder = new ChildVideoMessageView(viewBinding);
                break;
            case 10:
                holder = new ChildCallMessageView(viewBinding);
                break;
            case 11:
                holder = new ChildBusinessMessageView(viewBinding);
                break;
            case 12:
                holder = new ChildBroadcastMessageView(viewBinding);
                break;
            case 0:
            default:
                holder = new ChildNoneMessageView(msgTipBinding);
                Log.i(TAG, viewType + "");
                break;
        }


        holder.setMode(this.mode)
            .setAnonymous(this.isAnonymous)
            .serChatRoomEntity(this.b)
            .setKeyword(this.keyword)
            .setOnMessageControlEventListener(this.onMessageControlEventListener);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ChildMessageViewBase holder, int position) {
        holders.put(position, holder);
        T entity = entities.get(position);

        entity.setShowChecked(MessageAdapterMode.SELECTION.equals(this.mode));
        if (this.onMessageControlEventListener != null) {
            this.onMessageControlEventListener.onItemChange(entity);
        }

        holder.setKeyword(this.keyword)
            .setMode(this.mode)
            .setAnonymous(this.isAnonymous)
            .refresh(entity, false);
    }


    public void onBindViewHolderCache(@NonNull ChildMessageViewBase holder, int position) {
        holders.put(position, holder);
        T entity = entities.get(position);
        holder.refresh(entity, true);
    }


    @Override
    public int getItemViewType(int position) {
        T message = entities.get(position);
        // EVAN_FLAG 2020-05-04 (1.10.0) 當往上滑動，才檢查上一筆訊息
        if (position < this.currentPosition) {
            verifyPreviousMessage(message, position);
        }
        this.currentPosition = position;
        MessageType type = message.getType();
        // EVAN_FLAG 2020-02-10 (1.9.1) 如果是 (收回、系統訊息、以下未讀提示、時間線)
        if (MessageFlag.RETRACT.equals(message.getFlag()) || SourceType.SYSTEM.equals(message.getSourceType())) {
            return 1;
        }
        // EVAN_FLAG 2020-02-10 (1.9.1) 如果是主題訊息
        if (!Strings.isNullOrEmpty(message.getThemeId())) {
            return 2;
        }

        if (type == null) {
            return 0;
        }

        switch (type) {
            case TEXT:
                return 3;
            case AT:
                return 4;
            case IMAGE:
                return 5;
            case STICKER:
                return 6;
            case FILE:
                return 7;
            case VOICE:
                return 8;
            case VIDEO:
                return 9;
            case CALL:
                return 10;
            case BUSINESS:
                return 11;
            case BROADCAST:
                return 12;
            case TRANSFER:
                return 13;
            default:
                return 0;
        }
    }


//    /**
//     * 動畫結束
//     * @param position
//     */
//    @Override
//    public void executeAnimatorEnd(int position) {
//        entities.get(position).setAnimator(false);
////        notifyItemChanged(position);
//    }

    /**
     * 檢查上一筆訊息
     */
    private void verifyPreviousMessage(T message, int position) {
        try {
            if (MessageType.UNDEF.equals(message.getType()) && "TIME_LINE".equals(message.getContent())) {
                message = this.entities.get(position + 1);
            }
            T previous = this.entities.get(position - 1);

            if (MessageType.UNDEF.equals(previous.getType()) && "TIME_LINE".equals(previous.getContent())) {
                previous = this.entities.get(position - 2);
            }

            if (message.getSendTime() >= previous.getSendTime()) {
                // 判斷String != null || ""
                if (!Strings.isNullOrEmpty(message.getPreviousMessageId()) && !message.getPreviousMessageId().equals(previous.getId())) {
                    if (!MessageType.UNDEF.equals(message.getType()) && !MessageType.UNDEF.equals(previous.getType())) {
                        if (this.onMessageControlEventListener != null) {
                            this.onMessageControlEventListener.makeUpMessages(message, previous);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }


    public ChildMessageAdapter setKeyword(String keyword) {
        this.keyword = keyword;
        return this;
    }


    //    // 設定黑名單
//    public MessageAdapter setBlackList(Set<MessageType> blackList) {
//        this.blackList = blackList;
//        return this;
//    }
    @SuppressLint("NotifyDataSetChanged")
    public void refreshData() {
        sort(this.entities);
        notifyDataSetChanged();
    }

    public void refreshData(int position, T m) {
        sort(this.entities);
        notifyItemChanged(position, m);
    }


    private void sort(List<T> entities) {
        if (this.b != null && ChatRoomType.broadcast.equals(this.b.getType())) {
            Collections.sort(entities, (o1, o2) -> ComparisonChain.start()
                .compare(o1.getBroadcastWeights(), o2.getBroadcastWeights())
                .compare(o1.getBroadcastTime(), o2.getBroadcastTime())
                .result());
        } else {
            Collections.sort(entities);
        }
    }
    // 過濾黑名單
//    private void filter(List<T> entities) {
//        Iterator<T> iterator = entities.iterator();
//        while (iterator.hasNext()) {
//            if (this.blackList.contains(iterator.next().getType())) {
//                iterator.remove();
//            }
//        }
//    }

    public ChildMessageAdapter setOnMessageControlEventListener(OnMainMessageControlEventListener<T> onMessageControlEventListener) {
        this.onMessageControlEventListener = onMessageControlEventListener;
        return this;
    }

    /**
     * 預設，多選，範圍多選模式設定
     */
    public ChildMessageAdapter setMode(MessageAdapterMode mode) {
        this.mode = mode;
        return this;
    }

    public ChildMessageViewBase getHolder(int pos) {
        return holders.get(pos);
    }
}
