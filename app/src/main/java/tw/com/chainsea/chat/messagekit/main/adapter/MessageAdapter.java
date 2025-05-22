package tw.com.chainsea.chat.messagekit.main.adapter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Maps;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.msg.ChannelType;
import tw.com.chainsea.ce.sdk.bean.msg.MessageFlag;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;
import tw.com.chainsea.ce.sdk.bean.msg.SourceType;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.chat.databinding.ItemMsgBubbleBinding;
import tw.com.chainsea.chat.databinding.ItemMsgTipBinding;
import tw.com.chainsea.chat.messagekit.listener.CheckFacebookCommentStatus;
import tw.com.chainsea.chat.messagekit.listener.OnFacebookReplyClick;
import tw.com.chainsea.chat.messagekit.listener.OnMainMessageControlEventListener;
import tw.com.chainsea.chat.messagekit.listener.OnMessageSlideReply;
import tw.com.chainsea.chat.messagekit.listener.OnRobotChatMessageClickListener;
import tw.com.chainsea.chat.messagekit.listener.OnTemplateClickListener;
import tw.com.chainsea.chat.messagekit.main.viewholder.AtMessageView;
import tw.com.chainsea.chat.messagekit.main.viewholder.BroadcastMessageView;
import tw.com.chainsea.chat.messagekit.main.viewholder.BusinessMessageView;
import tw.com.chainsea.chat.messagekit.main.viewholder.CallMessageView;
import tw.com.chainsea.chat.messagekit.main.viewholder.FacebookCommentHolder;
import tw.com.chainsea.chat.messagekit.main.viewholder.FileMessageView;
import tw.com.chainsea.chat.messagekit.main.viewholder.ImageMessageView;
import tw.com.chainsea.chat.messagekit.main.viewholder.NoneMessageView;
import tw.com.chainsea.chat.messagekit.main.viewholder.ReplyMessageView;
import tw.com.chainsea.chat.messagekit.main.viewholder.StickerMessageView;
import tw.com.chainsea.chat.messagekit.main.viewholder.TemplateMessageView;
import tw.com.chainsea.chat.messagekit.main.viewholder.TextMessageView;
import tw.com.chainsea.chat.messagekit.main.viewholder.TipMessageView;
import tw.com.chainsea.chat.messagekit.main.viewholder.TransferMessageView;
import tw.com.chainsea.chat.messagekit.main.viewholder.VideoMessageView;
import tw.com.chainsea.chat.messagekit.main.viewholder.VoiceMessageView;
import tw.com.chainsea.chat.messagekit.main.viewholder.base.MessageViewBase;

/**
 * ThemeMessageAdapter
 * Created by 90Chris on 2016/4/21.
 *
 * @version 1.9.1
 */
// EVAN_REFACTOR: 2020-02-12 (1.9.1) 調整 ApiListener 結構
public class MessageAdapter<T extends MessageEntity> extends RecyclerView.Adapter<MessageViewBase> {
    private static final String TAG = MessageAdapter.class.getSimpleName();

    // data
    private final ChatRoomEntity chatRoomEntity;
    private final UserProfileEntity chatOwner;
    private final List<T> entities;

    // control
    private String keyword = "";

    private MessageAdapterMode mode = MessageAdapterMode.DEFAULT; // 顯示模式
    private boolean isAnonymous = false; //是否啟用匿名

    private final Map<Integer, MessageViewBase> holders = Maps.newHashMap();

    // callback
    private OnMainMessageControlEventListener<T> onMessageControlEventListener;

    private WeakReference<OnRobotChatMessageClickListener> onRobotChatMessageClickListener;

    private OnTemplateClickListener mOnTemplateClickListener;

    private OnMessageSlideReply mOnMessageSlideReply;

    private WeakReference<OnFacebookReplyClick> onFacebookReplyClick;

    private WeakReference<CheckFacebookCommentStatus> checkCommentStatus;

    // verify & makeUp
    private int currentPosition = -1;

    private RecyclerView recyclerView;

    public MessageAdapter(List<T> entities, ChatRoomEntity chatRoomEntity, UserProfileEntity chatOwner) {
        this.entities = entities;
        this.chatOwner = chatOwner;
        this.chatRoomEntity = chatRoomEntity;
        setHasStableIds(true);
    }


    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public long getItemId(int position) {
        return entities.get(position).getId().hashCode();
    }


    @Override
    public int getItemCount() {
        return entities.size();
    }

    @NonNull
    @Override
    public MessageViewBase onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MessageViewBase holder;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ViewBinding viewBinding = ItemMsgBubbleBinding.inflate(layoutInflater, parent, false);
        ItemMsgTipBinding itemMsgTipBinding = ItemMsgTipBinding.inflate(layoutInflater, parent, false);
        switch (viewType) {
            case 1:
                holder = new TipMessageView(itemMsgTipBinding);
                break;
            case 2:
                holder = new ReplyMessageView(viewBinding);
                break;
            case 3:
                holder = new TextMessageView(viewBinding);
                break;
            case 4:
                holder = new AtMessageView(viewBinding);
                break;
            case 5:
                holder = new ImageMessageView(viewBinding);
                break;
            case 6:
                holder = new StickerMessageView(viewBinding);
                break;
            case 7:
                holder = new FileMessageView(viewBinding);
                break;
            case 8:
                holder = new VoiceMessageView(viewBinding);
                break;
            case 9:
                holder = new VideoMessageView(viewBinding);
                break;
            case 10:
                holder = new CallMessageView(viewBinding);
                break;
            case 11:
                holder = new BusinessMessageView(viewBinding);
                break;
            case 12:
                holder = new BroadcastMessageView(viewBinding);
                break;
            case 13:
                holder = new TransferMessageView(viewBinding);
                break;
            case 14:
                holder = new TemplateMessageView(viewBinding);
                break;
            case 15:
                holder = new FacebookCommentHolder(viewBinding);
                break;
            case 0:
            default:
                holder = new NoneMessageView(itemMsgTipBinding);
                Log.i(TAG, viewType + "");
                break;
        }

        holder.setMode(this.mode)
            .setAnonymous(this.isAnonymous)
            .setChatOwnerEntity(this.chatOwner)
            .serChatRoomEntity(this.chatRoomEntity)
            .setKeyword(this.keyword);

        if (mOnMessageSlideReply != null) {
            holder.setOnMessageSlideReply(mOnMessageSlideReply);
        }

        if (onRobotChatMessageClickListener != null && onRobotChatMessageClickListener.get() != null) {
            holder.setOnRobotChatMessageClickListener(onRobotChatMessageClickListener.get());
        }

        if (mOnTemplateClickListener != null) {
            holder.setOnTemplateClickListener(mOnTemplateClickListener);
        }

        if (onFacebookReplyClick != null && onFacebookReplyClick.get() != null) {
            holder.setOnFacebookPublicReplyClick(onFacebookReplyClick.get());
        }

        if(onMessageControlEventListener != null) {
            holder.setOnMessageControlEventListener(this.onMessageControlEventListener);
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewBase holder, int position) {
        holders.put(position, holder);
        T entity = entities.get(position);
        if (MessageType.TEMPLATE.equals(entity.getType())) {
            // 卡片類型 不能轉發、分享、回覆、複製
            entity.setShowChecked(false);
            holder.setMessageReplyEnable(false);
        } else {
            entity.setShowChecked(MessageAdapterMode.SELECTION.equals(this.mode));
        }
        // 主要是為了卡片訊息另外開的 function
        holder.setTimeLayout(holder.itemView, entity);
        if (chatRoomEntity != null) {
            if (chatRoomEntity.getRoomType() == ChatRoomType.serviceONumberAsker
                || chatRoomEntity.getRoomType() == ChatRoomType.serviceONumberStaff
                || chatRoomEntity.getRoomType() == ChatRoomType.consultAi
                || chatRoomEntity.getRoomType() == ChatRoomType.bossServiceNumber
                || chatRoomEntity.getRoomType() == ChatRoomType.bossOwner) {
                holder.setMessageReplyEnable(false);
            }
        }

        if (holder instanceof FacebookCommentHolder) {
            if (checkCommentStatus.get() != null)
                checkCommentStatus.get().checkStatus(entity);
        }

        if (this.onMessageControlEventListener != null) {
            this.onMessageControlEventListener.onItemChange(entity);
        }
        holder.setKeyword(this.keyword)
            .setMode(this.mode)
            .setAnonymous(this.isAnonymous)
            .refresh(entity, false);
    }


    public void onBindViewHolderCache(@NonNull MessageViewBase holder, int position) {
        holders.put(position, holder);
        T entity = entities.get(position);
        holder.refresh(entity, true);
    }


    @Override
    public int getItemViewType(int position) {
        T message = entities.get(position);
        // EVAN_FLAG 2020-05-04 (1.10.0) 當往上滑動，才檢查上一筆訊息
        if (message != null) {
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
            // facebook 貼文回覆
            // 卡片形式
            if (message.getFrom() != null && message.getFrom() == ChannelType.FB && message.getType() != null && message.getType() == MessageType.TEMPLATE) {
                return 15;
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
                case TEMPLATE:
                    return 14;
                default:
                    return 0;
            }
        }
        return position;
    }

    /**
     * 檢查上一筆訊息
     */
    private void verifyPreviousMessage(T message, int position) {
        try {
            if (MessageType.UNDEF.equals(message.getType()) && "TIME_LINE".equals(message.getContent())) {
                message = this.entities.get(position + 1);
            }
            if (position - 1 < 0 || position - 2 < 0) return;
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


    public MessageAdapter setKeyword(String keyword) {
        this.keyword = keyword;
        return this;
    }

    public void notifyMessage(MessageEntity message) {
        try {
            recyclerView.post(() -> {
                int index = entities.indexOf(message);
                if (index > 0) {
                    entities.set(index, (T) message);
                    notifyItemChanged(index);
                }
            });
        } catch (Exception e) {
            CELog.e("notifyMessage", e);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refreshData() {
        sort(this.entities);
        notifyDataSetChanged();
    }

    public void refreshData(int originSize, int size) {
        sort(this.entities);
        notifyItemChanged(size);
    }

    public void refreshData(int position, T m) {
        sort(this.entities);
        notifyItemChanged(position, m);
    }


    private void sort(List<T> entities) {
        if (this.chatRoomEntity != null && ChatRoomType.broadcast.equals(this.chatRoomEntity.getType())) {
            Collections.sort(entities, (o1, o2) -> ComparisonChain.start()
                .compare(o1.getBroadcastWeights(), o2.getBroadcastWeights())
                .compare(o1.getBroadcastTime(), o2.getBroadcastTime())
                .result());
        } else {
            Collections.sort(entities);
        }
    }

    public MessageAdapter setOnMessageControlEventListener(OnMainMessageControlEventListener<T> onMessageControlEventListener) {
        this.onMessageControlEventListener = onMessageControlEventListener;
        return this;
    }

    public void setOnRobotMessageClickListener(OnRobotChatMessageClickListener onRobotChatMessageClickListener) {
        this.onRobotChatMessageClickListener = new WeakReference<>(onRobotChatMessageClickListener);
    }

    public void setOnTemplateClickListener(OnTemplateClickListener onTemplateClickListener) {
        mOnTemplateClickListener = onTemplateClickListener;
    }

    public void setOnMessageSlideReply(OnMessageSlideReply onMessageSlideReply) {
        mOnMessageSlideReply = onMessageSlideReply;
    }

    public void setOnFacebookReplyClick(OnFacebookReplyClick onFacebookReplyClick) {
        this.onFacebookReplyClick = new WeakReference<>(onFacebookReplyClick);
    }

    public void checkCommentStatus(CheckFacebookCommentStatus checkCommentStatus) {
        this.checkCommentStatus = new WeakReference<>(checkCommentStatus);
    }

    public boolean getIsShowCheckBox() {
        return MessageAdapterMode.SELECTION.equals(this.mode);
    }

    /**
     * 預設，多選，範圍多選模式設定
     */
    public MessageAdapter setMode(MessageAdapterMode mode) {
        this.mode = mode;
        return this;
    }

    /**
     * 啟用或禁用匿名
     */
    public MessageAdapter setAnonymous(boolean isAnonymous) {
        this.isAnonymous = isAnonymous;
        return this;
    }


    public MessageViewBase getHolder(int pos) {
        return holders.get(pos);
    }

    public void clearMessage() {
        int count = this.entities.size();
        this.entities.clear();
        this.notifyItemRangeRemoved(0, count);
    }

    public void updateMessageList(String contactId) {
        UserProfileEntity user = DBManager.getInstance().queryFriend(contactId);
        if (user != null) {
            for (MessageEntity entity : this.entities) {
                if (Objects.equals(entity.getSenderId(), contactId)) {
                    entity.setSenderName(user.getAlias());
                }
            }
        }
        int count = this.entities.size();
        this.notifyItemRangeChanged(0, count);
    }

    public void onDestroy() {
        for (int i = 0; i < this.entities.size(); i++) {
            if (entities.get(i).getType() == MessageType.VOICE) {
                Optional<MessageViewBase> optional = Optional.ofNullable(holders.get(i));
                optional.ifPresent(MessageViewBase::releasePlayer);
            }
        }
        if (onFacebookReplyClick.get() != null)
            onFacebookReplyClick.clear();
        if (checkCommentStatus.get() != null)
            checkCommentStatus.clear();
    }

    @Override
    public void onViewRecycled(@NonNull MessageViewBase holder) {
        if (holder instanceof VideoMessageView) {
            ((VideoMessageView) holder).unRegisterEventBus();
        }
        holder.clearObjects();
        super.onViewRecycled(holder);
    }
}
