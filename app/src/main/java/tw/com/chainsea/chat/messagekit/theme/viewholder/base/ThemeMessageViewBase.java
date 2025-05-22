package tw.com.chainsea.chat.messagekit.theme.viewholder.base;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import java.util.Map;

import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.chat.messagekit.listener.OnMessageControlEventListener;

/**
 * base view of message item
 * Created by 90Chris on 2016/4/20.
 */
public abstract class ThemeMessageViewBase extends RecyclerView.ViewHolder {
    protected View view;
    protected OnMessageControlEventListener onListDilogItemClickListener;
    protected ChatRoomEntity chatRoomEntity;

    public ThemeMessageViewBase(@NonNull View itemView) {
        super(itemView);
        getView(itemView);
    }

    protected abstract int getResId();

    protected abstract void inflate();

    public abstract void refresh(MessageEntity item);

    public void getView(View itemView) {
        view = itemView;
        inflate();
    }

    public ThemeMessageViewBase setChatRoomEntity(ChatRoomEntity chatRoomEntity) {
        this.chatRoomEntity = chatRoomEntity;
        return this;
    }

    public ThemeMessageViewBase setOnListDilogItemClickListener(OnMessageControlEventListener listener) {
        this.onListDilogItemClickListener = listener;
        return this;
    }

    protected Map<String, String> getMembersTable() {
        if (this.chatRoomEntity == null || this.chatRoomEntity.getMembers() == null || this.chatRoomEntity.getMembers().isEmpty()) {
            return Maps.newHashMap();
        }
        Map<String, String> data = Maps.newHashMap();
        for (UserProfileEntity a : this.chatRoomEntity.getMembers()) {
            data.put(a.getId(), !Strings.isNullOrEmpty(a.getNickName()) ? a.getNickName() : a.getLoginName());
        }
        return data;
    }

    final protected Context getContext() {
        return view.getContext();
    }

    @SuppressWarnings("unchecked")
    protected <T extends View> T findView(int resId) {
        return (T) (view.findViewById(resId));
    }
}
