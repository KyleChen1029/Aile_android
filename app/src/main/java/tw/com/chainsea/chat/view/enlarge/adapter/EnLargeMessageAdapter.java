package tw.com.chainsea.chat.view.enlarge.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.base.Strings;

import java.util.Collections;
import java.util.List;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.ce.sdk.bean.msg.MessageFlag;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;
import tw.com.chainsea.ce.sdk.bean.msg.SourceType;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ItemEnLargeAtMessageBinding;
import tw.com.chainsea.chat.databinding.ItemEnLargeBusinessMessageBinding;
import tw.com.chainsea.chat.databinding.ItemEnLargeFileMessageBinding;
import tw.com.chainsea.chat.databinding.ItemEnLargeImageMessageBinding;
import tw.com.chainsea.chat.databinding.ItemEnLargeStickerMessageBinding;
import tw.com.chainsea.chat.databinding.ItemEnLargeTextMessageBinding;
import tw.com.chainsea.chat.databinding.ItemEnLargeVideoMessageBinding;
import tw.com.chainsea.chat.databinding.ItemEnLargeVoiceMessageBinding;
import tw.com.chainsea.chat.view.enlarge.adapter.viewholder.EnLargeAtMessageView;
import tw.com.chainsea.chat.view.enlarge.adapter.viewholder.EnLargeBusinessMessageView;
import tw.com.chainsea.chat.view.enlarge.adapter.viewholder.EnLargeFileMessageView;
import tw.com.chainsea.chat.view.enlarge.adapter.viewholder.EnLargeImageMessageView;
import tw.com.chainsea.chat.view.enlarge.adapter.viewholder.EnLargeStickerMessageView;
import tw.com.chainsea.chat.view.enlarge.adapter.viewholder.EnLargeTextMessageView;
import tw.com.chainsea.chat.view.enlarge.adapter.viewholder.EnLargeTransferMessageView;
import tw.com.chainsea.chat.view.enlarge.adapter.viewholder.EnLargeUndefMessageView;
import tw.com.chainsea.chat.view.enlarge.adapter.viewholder.EnLargeVideoMessageView;
import tw.com.chainsea.chat.view.enlarge.adapter.viewholder.EnLargeVoiceMessageView;
import tw.com.chainsea.chat.view.enlarge.adapter.viewholder.base.EnLargeMessageBaseView;

/**
 * current by evan on 2020-04-14
 *
 * @author Evan Wang
 * date 2020-04-14
 */
public class EnLargeMessageAdapter extends RecyclerView.Adapter<EnLargeMessageBaseView> {
    ChatRoomEntity chatRoomEntity;
    List<MessageEntity> entities;
    OnEnLargeMessageListener onEnLargeMessageListener;

    public EnLargeMessageAdapter(List<MessageEntity> entities, ChatRoomEntity chatRoomEntity) {
        this.entities = entities;
        this.chatRoomEntity = chatRoomEntity;
    }

    @NonNull
    @Override
    public EnLargeMessageBaseView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        EnLargeMessageBaseView baseView = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemEnLargeTextMessageBinding enLargeTextMessageBinding = DataBindingUtil.inflate(inflater, R.layout.item_en_large_text_message, parent, false);
        switch (viewType) {
//            case 1: // system, tip, time
//                return new TipMessageView(LayoutInflater.from(parent.getContext()).inflate(R.layout.msgkit_tip, parent, false));
//            break;
//            case 2: // 主題
//                holder = new ReplyMessageView(v);
//                break;
            case 3: // text OK
                baseView = new EnLargeTextMessageView(enLargeTextMessageBinding);
                break;
            case 4: // At OK
                ItemEnLargeAtMessageBinding enLargeAtMessageBinding = DataBindingUtil.inflate(inflater, R.layout.item_en_large_at_message, parent, false);
                baseView = new EnLargeAtMessageView(enLargeAtMessageBinding);
                break;
            case 5: // Image OK
                ItemEnLargeImageMessageBinding enLargeImageMessageBinding = DataBindingUtil.inflate(inflater, R.layout.item_en_large_image_message, parent, false);
                baseView = new EnLargeImageMessageView(enLargeImageMessageBinding);
                break;
            case 6: // Sticker OK
                ItemEnLargeStickerMessageBinding enLargeStickerMessageBinding = DataBindingUtil.inflate(inflater, R.layout.item_en_large_sticker_message, parent, false);
                baseView = new EnLargeStickerMessageView(enLargeStickerMessageBinding);
                break;
            case 7: // File OK
                ItemEnLargeFileMessageBinding enLargeFileMessageBinding = DataBindingUtil.inflate(inflater, R.layout.item_en_large_file_message, parent, false);
                baseView = new EnLargeFileMessageView(enLargeFileMessageBinding);
                break;
            case 8: // Voice OK
                ItemEnLargeVoiceMessageBinding enLargeVoiceMessageBinding = DataBindingUtil.inflate(inflater, R.layout.item_en_large_voice_message, parent, false);
                baseView = new EnLargeVoiceMessageView(enLargeVoiceMessageBinding);
                break;
            case 9: // Video OK
                ItemEnLargeVideoMessageBinding enLargeVideoMessageBinding = DataBindingUtil.inflate(inflater, R.layout.item_en_large_video_message, parent, false);
                baseView = new EnLargeVideoMessageView(enLargeVideoMessageBinding);
                break;
//            case 10: // Call
//                baseView = new EnLargeCallMessageView(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_en_large_text_message, parent, false));
//                break;
            case 11:  // Business OK
//                item_en_large_business_message
                ItemEnLargeBusinessMessageBinding enLargeBusinessMessageBinding = DataBindingUtil.inflate(inflater, R.layout.item_en_large_business_message, parent, false);
                baseView = new EnLargeBusinessMessageView(enLargeBusinessMessageBinding);
                break;
            case 13:
                baseView = new EnLargeTransferMessageView(enLargeTextMessageBinding);
                break;
            case 0:
            default:
                baseView = new EnLargeUndefMessageView(enLargeTextMessageBinding);
                break;
        }
        baseView.setChatRoomEntity(this.chatRoomEntity);

        return baseView;
    }

    @Override
    public void onBindViewHolder(@NonNull EnLargeMessageBaseView viewHolder, int position) {
        MessageEntity entity = this.entities.get(position);

        viewHolder.setupSendMemberInformation(entity)
            .onBind(entity, entity.content(), position);
    }

    @Override
    public int getItemCount() {
        return entities.size();
    }

    @Override
    public int getItemViewType(int position) {
        MessageEntity message = entities.get(position);

        MessageType type = message.getType();
        // EVAN_FLAG 2020-02-10 (1.9.1) 如果是 (收回、系統訊息、以下未讀提示、時間線)
        if (MessageFlag.RETRACT.equals(message.getFlag()) || SourceType.SYSTEM.equals(message.getSourceType())) return 1;
        // EVAN_FLAG 2020-02-10 (1.9.1) 如果是主題訊息
        if (!Strings.isNullOrEmpty(message.getThemeId())) return 2;

        if (type == null) return 0;

        return switch (type) {
            case TEXT -> 3;
            case AT -> 4;
            case IMAGE -> 5;
            case STICKER -> 6;
            case FILE -> 7;
            case VOICE -> 8;
            case VIDEO -> 9;
            case CALL -> 10;
            case BUSINESS -> 11;
            case BROADCAST -> 12;
            case TRANSFER -> 13;
            case TEMPLATE -> 14;
            default -> 0;
        };
    }

    public EnLargeMessageAdapter setData(List<MessageEntity> list) {
        for (MessageEntity entity : list) {
            this.entities.remove(entity);
            if (isValid(entity)) {
                this.entities.add(entity);
            }
        }
        return this;
    }

    public int indexOf(MessageEntity entity) {
        return this.entities.indexOf(entity);
    }

    private boolean isValid(MessageEntity entity) {
        return !MessageFlag.RETRACT.equals(entity.getFlag()) && !SourceType.SYSTEM.equals(entity.getSourceType()) && !MessageType.CALL.equals(entity.getType());
    }


    public EnLargeMessageAdapter setOnEnLargeMessageListener(OnEnLargeMessageListener onEnLargeMessageListener) {
        this.onEnLargeMessageListener = onEnLargeMessageListener;
        return this;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refreshData() {
        Collections.sort(this.entities);
        notifyDataSetChanged();
    }

    public void handleCurrentPosition(int position) {

        try {
            this.onEnLargeMessageListener.doLastAvatar(this.entities.get(position - 1));
        } catch (IndexOutOfBoundsException e) {
            this.onEnLargeMessageListener.doLastAvatar(null);
        }

        try {
            this.onEnLargeMessageListener.doNextAvatar(this.entities.get(position + 1));
        } catch (IndexOutOfBoundsException e) {
            this.onEnLargeMessageListener.doNextAvatar(null);
        }

    }

    public interface OnEnLargeMessageListener {
        void doLastAvatar(MessageEntity entity);

        void doNextAvatar(MessageEntity entity);
    }
}
