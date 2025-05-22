package tw.com.chainsea.chat.messagekit.main.viewholder.base;

import android.graphics.Color;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.random.RandomHelper;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.chat.messagekit.lib.MessageDomino;
import tw.com.chainsea.chat.messagekit.listener.OnFacebookReplyClick;
import tw.com.chainsea.chat.messagekit.listener.OnMainMessageControlEventListener;
import tw.com.chainsea.chat.messagekit.listener.OnMessageSlideReply;
import tw.com.chainsea.chat.messagekit.listener.OnRobotChatMessageClickListener;
import tw.com.chainsea.chat.messagekit.listener.OnTemplateClickListener;
import tw.com.chainsea.chat.messagekit.main.adapter.MessageAdapterMode;

/**
 * base view of message item
 * Created by 90Chris on 2016/4/20.
 */
public abstract class MessageViewBase extends RecyclerView.ViewHolder {
    protected ChatRoomEntity chatRoomEntity;
    protected UserProfileEntity chatOwner;

    private String keyword = "";
    protected MessageAdapterMode mode = MessageAdapterMode.DEFAULT;
    protected boolean isAnonymous;
    protected View view;
    protected OnMainMessageControlEventListener onMessageControlEventListener;

    public OnRobotChatMessageClickListener onRobotChatMessageClickListener;

    public OnTemplateClickListener mOnTemplateClickListener;

    protected WeakReference<OnMessageSlideReply> mOnMessageSlideReply;

    protected WeakReference<OnFacebookReplyClick> onFacebookReplyClick;

    protected boolean isMessageReplyEnable = true;

    public MessageViewBase(@NonNull View itemView) {
        super(itemView);
    }

    protected abstract void findViews(View itemView);

    public abstract void refresh(MessageEntity item, boolean isCache);

    public void releasePlayer() { }

    public void clearObjects() {
        if (onFacebookReplyClick != null && onFacebookReplyClick.get() != null) {
            onFacebookReplyClick.clear();
        }
    }
    public void getView(View itemView) {
        view = itemView;
        findViews(itemView);
    }

    protected MessageDomino getDomino(String sendId) {
        if (MessageDomino.dominos.isEmpty()) {
            MessageDomino.init2(MessageDomino.getBatchNumber());
        }
        if (MessageDomino.dominoData.get(sendId) == null) {
            MessageDomino.Domino d = MessageDomino.dominos.pollFirst();
            MessageDomino.dominoData.put(sendId, new MessageDomino(d.getName(), Color.BLACK, d.getResId(), RandomHelper.randomColor()));
        }
        return MessageDomino.dominoData.get(sendId);
    }

    public MessageViewBase setKeyword(String keyword) {
        this.keyword = keyword;
        return this;
    }

    public String getKeyword() {
        return this.keyword;
    }

    public boolean isClickEventEnable() {
        return MessageAdapterMode.DEFAULT.equals(this.mode);
    }

    public MessageViewBase setMode(MessageAdapterMode mode) {
        this.mode = mode;
        return this;
    }

    public MessageViewBase setAnonymous(boolean isAnonymous) {
        this.isAnonymous = isAnonymous;
        return this;
    }

    public MessageViewBase setChatOwnerEntity(UserProfileEntity chatOwner) {
        this.chatOwner = chatOwner;
        return this;
    }

    public MessageViewBase serChatRoomEntity(ChatRoomEntity chatRoomEntity) {
        this.chatRoomEntity = chatRoomEntity;
        return this;
    }

    protected List<UserProfileEntity> getMembers() {
        if (this.chatRoomEntity == null || this.chatRoomEntity.getMembers() == null || this.chatRoomEntity.getMembers().isEmpty()) {
            return Lists.newArrayList();
        }
        return this.chatRoomEntity.getMembers();
    }

    protected Map<String, String> getMembersTable() {
        if (this.chatRoomEntity == null || this.chatRoomEntity.getMembers() == null || this.chatRoomEntity.getMembers().isEmpty()) {
            return Maps.newHashMap();
        }
        Map<String, String> data = Maps.newHashMap();
        for (UserProfileEntity a : this.chatRoomEntity.getMembers()) {
            data.put(a.getId(), !Strings.isNullOrEmpty(a.getAlias()) ? a.getAlias() : a.getNickName());
        }
        return data;
    }

    protected Map<String, String> getAnonymousMembersTable() {
        if (this.chatRoomEntity == null || this.chatRoomEntity.getMembers() == null || this.chatRoomEntity.getMembers().isEmpty()) {
            return Maps.newHashMap();
        }
        Map<String, String> data = Maps.newHashMap();
        for (UserProfileEntity a : this.chatRoomEntity.getMembers()) {
            data.put(a.getId(), getDomino(a.getId()).getName());
        }
        return data;
    }

    public MessageViewBase setOnMessageControlEventListener(OnMainMessageControlEventListener onMessageControlEventListener) {
        this.onMessageControlEventListener = onMessageControlEventListener;
        return this;
    }

    public MessageViewBase setOnRobotChatMessageClickListener(OnRobotChatMessageClickListener onRobotChatMessageClickListener) {
        this.onRobotChatMessageClickListener = onRobotChatMessageClickListener;
        return this;
    }

    public MessageViewBase setOnTemplateClickListener(OnTemplateClickListener onTemplateClickListener) {
        this.mOnTemplateClickListener = onTemplateClickListener;
        return this;
    }

    public MessageViewBase setOnMessageSlideReply(OnMessageSlideReply onMessageSlideReply) {
        this.mOnMessageSlideReply = new WeakReference<>(onMessageSlideReply);
        return this;
    }

    public MessageViewBase setMessageReplyEnable(boolean enable) {
        this.isMessageReplyEnable = enable;
        return this;
    }

    public MessageViewBase setOnFacebookPublicReplyClick(OnFacebookReplyClick onFacebookReplyClick) {
        this.onFacebookReplyClick = new WeakReference<>(onFacebookReplyClick);
        return this;
    }
    @SuppressWarnings("unchecked")
    protected <T extends View> T findView(int resId) {
        return (T) (view.findViewById(resId));
    }

    public void setTimeLayout(View view, MessageEntity message) {}
}
