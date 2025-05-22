package tw.com.chainsea.chat.messagekit.child.viewholder;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import android.view.View;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

import tw.com.chainsea.android.common.random.RandomHelper;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.chat.messagekit.lib.MessageDomino;
import tw.com.chainsea.chat.messagekit.listener.OnMainMessageControlEventListener;
import tw.com.chainsea.chat.messagekit.main.adapter.MessageAdapterMode;

/**
 * Create by evan on 1/22/21
 *
 * @author Evan Wang
 * @date 1/22/21
 */
public abstract class ChildMessageViewBase<B extends ChatRoomEntity, M extends MessageEntity> extends RecyclerView.ViewHolder {
    protected B b;

    private String keyword = "";
    //    private boolean isClickEventEnable = true;
    protected MessageAdapterMode mode = MessageAdapterMode.DEFAULT;
    protected boolean isAnonymous;


    protected View view;
    protected OnMainMessageControlEventListener onMessageControlEventListener;


    public ChildMessageViewBase(@NonNull ViewBinding binding) {
        super(binding.getRoot());
        getView(itemView);
//        buildDominoData();
    }

//    protected abstract int getResId();

    protected abstract void findViews(View itemView);

    public abstract void refresh(MessageEntity item, boolean isCache);


    public void getView(View itemView) {
        view = itemView;
//        ButterKnife.bind(this, view);
        findViews(itemView);
    }
//    protected MessageDomino.Domino getDominos(String sendId) {
//        if (MessageDomino.dominoDatas.get(sendId) == null) {
//            MessageDomino.dominoDatas.put(sendId, MessageDomino.dominos.pollFirst());
//        }
//        return MessageDomino.dominoDatas.get(sendId);
//    }

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

    public ChildMessageViewBase setKeyword(String keyword) {
        this.keyword = keyword;
        return this;
    }

    public String getKeyword() {
        return this.keyword;
    }

    public boolean isClickEventEnable() {
        return MessageAdapterMode.DEFAULT.equals(this.mode);
//        return this.isClickEventEnable;
    }

//    public ChildMessageViewBase setClickEventEnable(boolean isClickEventEnable) {
//        this.isClickEventEnable = isClickEventEnable;
//        return this;
//    }


    public ChildMessageViewBase setMode(MessageAdapterMode mode) {
        this.mode = mode;
        return this;
    }


    public ChildMessageViewBase setAnonymous(boolean isAnonymous) {
        this.isAnonymous = isAnonymous;
        return this;
    }

    public ChildMessageViewBase serChatRoomEntity(B b) {
        this.b = b;
        return this;
    }

    protected List<UserProfileEntity> getMembers() {
        if (this.b == null || this.b.getMembers() == null || this.b.getMembers().isEmpty()) {
            return Lists.newArrayList();
        }
        return this.b.getMembers();
    }

    protected Map<String, String> getMembersTable() {
        if (this.b == null || this.b.getMembers() == null || this.b.getMembers().isEmpty()) {
            return Maps.newHashMap();
        }
        Map<String, String> data = Maps.newHashMap();
        for (UserProfileEntity a : this.b.getMembers()) {
            data.put(a.getId(), !Strings.isNullOrEmpty(a.getAlias()) ? a.getAlias() : a.getNickName());
        }
        return data;
    }


    protected Map<String, String> getAnonymousMembersTable() {
        if (this.b == null || this.b.getMembers() == null || this.b.getMembers().isEmpty()) {
            return Maps.newHashMap();
        }
        Map<String, String> data = Maps.newHashMap();
        for (UserProfileEntity a : this.b.getMembers()) {
            data.put(a.getId(), getDomino(a.getId()).getName());
//            data.put(a.getId(), !Strings.isNullOrEmpty(a.getAlias()) ? a.getAlias() : a.getNickname());
        }
        return data;
    }


    public ChildMessageViewBase setOnMessageControlEventListener(OnMainMessageControlEventListener onMessageControlEventListener) {
        this.onMessageControlEventListener = onMessageControlEventListener;
        return this;
    }

    final protected Context getContext() {
        return view.getContext();
    }


    @SuppressWarnings("unchecked")
    protected <T extends View> T findView(int resId) {
        return (T) (view.findViewById(resId));
    }
}
