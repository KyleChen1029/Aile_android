package tw.com.chainsea.chat.messagekit.main.viewholder;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.text.method.LinkMovementMethod;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.text.SimpleDateFormat;
import java.util.Locale;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.text.KeyWordHelper;
import tw.com.chainsea.ce.sdk.SdkLib;
import tw.com.chainsea.ce.sdk.bean.msg.MessageFlag;
import tw.com.chainsea.ce.sdk.bean.msg.SourceType;
import tw.com.chainsea.ce.sdk.bean.msg.content.AtContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.TextContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.UndefContent;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ItemMsgTipBinding;
import tw.com.chainsea.chat.messagekit.main.viewholder.base.MessageViewBase;
import tw.com.chainsea.chat.util.ThemeHelper;


/**
 * 提醒类消息类型，如顯示顶部的时间小方块等
 * Created by 90Chris on 2016/4/20.
 */

public class TipMessageView extends MessageViewBase {
    private static final String TAG = TipMessageView.class.getSimpleName();

    private ItemMsgTipBinding binding;
    boolean isGreenTheme = false;

    public TipMessageView(@NonNull ItemMsgTipBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        getView(this.binding.getRoot());
        isGreenTheme = ThemeHelper.INSTANCE.isGreenTheme();
    }

    @Override
    protected void findViews(View itemView) {
    }

    @Override
    @SuppressLint("SetTextI18n")
    public void refresh(final MessageEntity message, boolean isCache) {
        String userId = TokenPref.getInstance(binding.getRoot().getContext()).getUserId();
        String senderId = message.getSenderId();

        binding.msgkitTipContent.setTextColor(0xFF5E5E5E);
        binding.msgkitTipContent.setBackgroundResource(R.drawable.sys_msg_bg);
        binding.msgkitTipContent.setMovementMethod(null);
        String content = "tip";

        if (SourceType.SYSTEM.equals(message.getSourceType()) && message.content() instanceof UndefContent) {
            UndefContent undefContent = (UndefContent) message.content();
            if ("TIME_LINE".equals(undefContent.getText())) {
//                binding.msgkitTipContent.setTextColor(0xFF76B9CB);
                binding.msgkitTipContent.setTextColor(isGreenTheme ? ResourcesCompat.getColor(binding.getRoot().getResources(), R.color.color_015F57, null) : 0xFF76B9CB);
                binding.msgkitTipContent.setBackgroundResource(isGreenTheme ? R.drawable.time_msg_bg_green : R.drawable.time_msg_bg);
//                binding.msgkitTipContent.setBackgroundResource(R.drawable.time_msg_bg);
                String date = new SimpleDateFormat("MMMdd日(EEE)", Locale.TAIWAN).format(message.getSendTime());
                binding.msgkitTipContent.setText(date);
            } else if ("UNREAD".equals(undefContent.getText())) {
                binding.msgkitTipContent.setText("以下為未讀訊息");
            }
        } else if (SourceType.SYSTEM.equals(message.getSourceType()) && message.content() instanceof TextContent) {
            content = message.content().simpleContent();
            if (content.contains("進線") || content.toUpperCase().contains("END")) {
                binding.msgkitTipContent.setTextColor(0xFFFFFFFF);
                binding.msgkitTipContent.setBackgroundResource(R.drawable.sys_msg_current_state_bg);
                binding.msgkitTipContent.setText(content.replace("-", ""));
            } else if (content.contains("切換")) {
                binding.msgkitTipContent.setTextColor(0xFFFFFFFF);
                binding.msgkitTipContent.setBackgroundResource(R.drawable.sys_msg_switch_bg);
                binding.msgkitTipContent.setText(content.replace("-", ""));
            } else if (content.contains("擁有")) {
                binding.msgkitTipContent.setTextColor(ContextCompat.getColor(binding.msgkitTipContent.getContext(), R.color.item_name));
                binding.msgkitTipContent.setBackgroundResource(R.drawable.sys_msg_has_owner_bg);
                binding.msgkitTipContent.setText(content);
            } else {
                binding.msgkitTipContent.setTextColor(0xFFFFFFFF);
                binding.msgkitTipContent.setBackgroundResource(R.drawable.sys_msg_current_state_bg);
                if (!userId.equals(senderId)) {
                    binding.msgkitTipContent.setText(content);
                } else if (senderId.equals(userId)) {
                    binding.msgkitTipContent.setText(content);
                }
            }
        } else if (MessageFlag.RETRACT.equals(message.getFlag())) {
            binding.msgkitTipContent.setBackgroundResource(R.drawable.sys_msg_bg);
            binding.msgkitTipContent.setTextColor(0xFF5E5E5E);
            if (message.content() instanceof TextContent || message.content() instanceof AtContent) {
                binding.msgkitTipContent.setBackgroundResource(R.drawable.sys_msg_bg);
                if (!userId.equals(senderId) && MessageFlag.RETRACT.equals(message.getFlag())) {
                    if (chatRoomEntity.getRoomType() == ChatRoomType.friend) {
                        binding.msgkitTipContent.setText("對方已收回訊息");
                    } else {
                        binding.msgkitTipContent.setText(message.getSenderName() + "已收回訊息");
                    }
                } else if (senderId.equals(userId) && MessageFlag.RETRACT.equals(message.getFlag())) {
                    binding.msgkitTipContent.setText(KeyWordHelper.matcherKeys(0xFF4A90E2, SdkLib.getAppContext().getString(R.string.text_you_retract_message) + "  " + SdkLib.getAppContext().getString(R.string.text_edit_again), SdkLib.getAppContext().getString(R.string.text_edit_again), view -> {
                        if (this.onMessageControlEventListener != null) {
                            this.onMessageControlEventListener.onTipClick(message);
                        }
                    }));
                    binding.msgkitTipContent.setMovementMethod(LinkMovementMethod.getInstance());
                }
            } else {
                if (!userId.equals(senderId)) {
                    binding.msgkitTipContent.setText(message.getSenderName() + "收回訊息");
                } else {
                    binding.msgkitTipContent.setText(SdkLib.getAppContext().getString(R.string.text_you_retract_message));
                }
            }
        }

        switch (super.mode) {
            case RANGE_SELECTION:
                rangeSelectionMode(message);
                break;
            case SELECTION:
            case DEFAULT:
                defaultMode();
                break;
        }

    }

    private void defaultMode() {
        binding.maskLayer.setVisibility(View.GONE);
        binding.maskLayer.setOnClickListener(null);
        itemView.setBackgroundColor(Color.TRANSPARENT);
    }

    /**
     * Open range selection mode
     */
    public void rangeSelectionMode(MessageEntity message) {
        itemView.setBackgroundColor(Color.WHITE);
        binding.maskLayer.setVisibility(View.VISIBLE);
        binding.maskLayer.setOnClickListener(v -> {
            if (onMessageControlEventListener != null) {
                onMessageControlEventListener.doRangeSelection(message);
            }
        });
        if (message.isShowSelection()) {
            binding.maskLayer.setAlpha(0.0f);
        } else {
            binding.maskLayer.setAlpha(0.6f);
        }
    }
}
