package tw.com.chainsea.chat.messagekit.theme.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Longs;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.bean.msg.ChannelType;
import tw.com.chainsea.ce.sdk.bean.msg.MessageFlag;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;
import tw.com.chainsea.ce.sdk.bean.msg.SourceType;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.messagekit.listener.OnMessageControlEventListener;
import tw.com.chainsea.chat.messagekit.theme.viewholder.AtThemeMessageView;
import tw.com.chainsea.chat.messagekit.theme.viewholder.FacebookThemeViewHolder;
import tw.com.chainsea.chat.messagekit.theme.viewholder.FileThemeMessageView;
import tw.com.chainsea.chat.messagekit.theme.viewholder.ImageThemeMessageView;
import tw.com.chainsea.chat.messagekit.theme.viewholder.NoneThemeMessageView;
import tw.com.chainsea.chat.messagekit.theme.viewholder.StickerThemeMessageView;
import tw.com.chainsea.chat.messagekit.theme.viewholder.TemplateThemeMessageView;
import tw.com.chainsea.chat.messagekit.theme.viewholder.TextThemeMessageView;
import tw.com.chainsea.chat.messagekit.theme.viewholder.TipThemeMessageView;
import tw.com.chainsea.chat.messagekit.theme.viewholder.VideoThemeMessageView;
import tw.com.chainsea.chat.messagekit.theme.viewholder.VoiceThemeMessageView;
import tw.com.chainsea.chat.messagekit.theme.viewholder.base.ThemeMessageViewBase;

/**
 * ThemeMessageAdapter
 * Created by 90Chris on 2016/4/21.
 */
public class ThemeMessageAdapter extends RecyclerView.Adapter<ThemeMessageViewBase> {
    private List<MessageEntity> themeMessages = Lists.newArrayList();
    private final LayoutInflater mInflater;
    private OnMessageControlEventListener onMessageControlEventListener;
    private ChatRoomEntity chatRoomEntity;
    private Map<Integer, ThemeMessageViewBase> holders = Maps.newHashMap();
    private String themeId = "";

    public ThemeMessageAdapter(Context context, ChatRoomEntity chatRoomEntity) {
        mInflater = LayoutInflater.from(context);
        this.chatRoomEntity = chatRoomEntity;
    }

    public void setOnMessageControlEventListener(OnMessageControlEventListener listenner) {
        onMessageControlEventListener = listenner;
    }

    public void notifyMessage(MessageEntity message) {
        try {
            int index = themeMessages.indexOf(message);
            themeMessages.set(index, message);
            notifyItemChanged(index);
        } catch (Exception e) {
            CELog.e("notifyMessage", e);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return this.themeMessages.size();
    }

    @NonNull
    @Override
    @SuppressLint("InflateParams")
    public ThemeMessageViewBase onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ThemeMessageViewBase holder = null;
        View v = null;
        ChatRoomType type = chatRoomEntity.getType();
        if (type.equals(ChatRoomType.subscribe) || type.equals(ChatRoomType.services)) {
            v = mInflater.inflate(R.layout.item_msg_bubble_need_user, null);
        } else {
            v = mInflater.inflate(R.layout.reply_msg_bubble, null);
        }

        if (viewType == 10) {
            v = mInflater.inflate(R.layout.item_msg_tip, parent, false);
        }


        switch (viewType) {
            case 1:
                holder = new TextThemeMessageView(v);
                break;
            case 2:
                holder = new AtThemeMessageView(v);
                break;
            case 3:
                holder = new ImageThemeMessageView(v);
                break;
            case 4:
                holder = new StickerThemeMessageView(v);
                break;
            case 5:
                holder = new FileThemeMessageView(v);
                break;
            case 6:
                holder = new VoiceThemeMessageView(v);
                break;
            case 7:
                holder = new VideoThemeMessageView(v);
                break;
            case 8:
                holder = new TemplateThemeMessageView(v);
                break;
            case 9:
                holder = new FacebookThemeViewHolder(v);
                break;
            case 10:
                holder = new TipThemeMessageView(v);
                break;
            default:
                holder = new NoneThemeMessageView(v);
                break;
        }


        if (holder != null) {
            holder.setChatRoomEntity(chatRoomEntity)
//                    .setOnAvatarListener(mListener)
                .setOnListDilogItemClickListener(onMessageControlEventListener);
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ThemeMessageViewBase themeMessageViewBase, int position) {
        themeMessageViewBase.refresh(this.themeMessages.get(position));

    }

    @Override
    public int getItemViewType(int position) {
        MessageEntity message = this.themeMessages.get(position);
        if (MessageFlag.RETRACT.equals(message.getFlag())) {
            return -1;
        }
        MessageType type = message.getType();
        // facebook 貼文回覆
        // 卡片形式
        if (message.getFrom() != null && message.getFrom() == ChannelType.FB && message.getType() != null && message.getType() == MessageType.TEMPLATE) {
            return 9;
        }

        if (message.getSourceType() == SourceType.SYSTEM) {
            return 10;
        }

        switch (type) {
            case TEXT:
                return 1;
            case AT:
                return 2;
            case IMAGE:
                return 3;
            case STICKER:
                return 4;
            case FILE:
                return 5;
            case VOICE:
                return 6;
            case VIDEO:
                return 7;
            case TEMPLATE:
                return 8;
            default:
                return -2;
        }
    }

    public MessageEntity getThemeData() {
        if (this.themeMessages.isEmpty()) {
            return null;
        } else {
            for (int i = 0; i < themeMessages.size(); i++) {
                MessageEntity themeMessage = themeMessages.get(i);
                if (themeMessage.getSourceType() != SourceType.SYSTEM) {
                    return themeMessage;
                }
            }
            return this.themeMessages.get(0);
        }
    }

    public MessageEntity getNearData() {
        if (this.themeMessages.isEmpty()) {
            return null;
        } else {
            for (int i = themeMessages.size() - 1; i >= 0; i--) {
                MessageEntity themeMessage = themeMessages.get(i);
                if (themeMessage.getSourceType() != SourceType.SYSTEM) {
                    return themeMessage;
                }
            }
            return this.themeMessages.get(this.themeMessages.size() - 1);
        }
    }

    public ThemeMessageAdapter setThemeId(String themeId) {
        this.themeId = themeId;
        return this;
    }

    public String getThemeId() {
        return this.themeId;
    }
    @SuppressLint("NotifyDataSetChanged")
    public ThemeMessageAdapter refreshData() {
        filter(this.themeId, this.themeMessages);
        sort(this.themeMessages);
        notifyDataSetChanged();
        return this;
    }

    public ThemeMessageAdapter setData(MessageEntity message) {
        if (!this.themeMessages.contains(message)) {
            this.themeMessages.add(message);
        }
        return this;
    }

    public boolean isContains(MessageEntity entity) {
        return this.themeMessages.contains(entity);
    }

    public ThemeMessageAdapter clearData() {
        this.themeMessages = Lists.newArrayList();
        return this;
    }

    private static void filter(String themeId, List<MessageEntity> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        if (Strings.isNullOrEmpty(themeId)) {
            return;
        }

        Iterator<MessageEntity> iterator = messages.iterator();
        while (iterator.hasNext()) {
            MessageEntity message = iterator.next();
            if (!themeId.equals(message.getThemeId()) && !themeId.equals(message.getId())) {
                iterator.remove();
            }
        }
    }

    // Note that JDK7 is easy to throw exceptions
    // java.lang.IllegalArgumentException:Comparison method violates its general contract!。
    private static void sort(List<MessageEntity> messages) {
        Collections.sort(messages, (o1, o2) -> Longs.compare(o1.getSendTime(), o2.getSendTime()));
    }

    public ThemeMessageViewBase getHolder(int pos) {
        return holders.get(pos);
    }

    public List<MessageEntity> getThemeMessagess() {
        return this.themeMessages;
    }
}
