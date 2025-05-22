package tw.com.chainsea.chat.messagekit.child.viewholder;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.text.method.LinkMovementMethod;
import android.view.View;

import androidx.annotation.NonNull;
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
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ItemMsgTipBinding;
import tw.com.chainsea.chat.util.ThemeHelper;


/**
 * 提醒类消息类型，如顯示顶部的时间小方块等
 * Created by 90Chris on 2016/4/20.
 */

public class ChildTipMessageView extends ChildMessageViewBase {
    private static final String TAG = ChildTipMessageView.class.getSimpleName();
    ItemMsgTipBinding msgTipBinding;
    boolean isGreenTheme = false;

    public ChildTipMessageView(@NonNull ItemMsgTipBinding binding) {
        super(binding);
        msgTipBinding = binding;
        isGreenTheme = ThemeHelper.INSTANCE.isGreenTheme();
    }

    @Override
    protected void findViews(View itemView) {
//        ButterKnife.bind(this, itemView);
    }

    @Override
    @SuppressLint("SetTextI18n")
    public void refresh(final MessageEntity message, boolean isCache) {
        String userId = TokenPref.getInstance(getContext()).getUserId();
        String senderId = message.getSenderId();

        msgTipBinding.msgkitTipContent.setTextColor(0xFF5E5E5E);
        msgTipBinding.msgkitTipContent.setBackgroundResource(R.drawable.sys_msg_bg);
        msgTipBinding.msgkitTipContent.setMovementMethod(null);
        String content = "tip";

        if (SourceType.SYSTEM.equals(message.getSourceType()) && message.content() instanceof UndefContent) {
            UndefContent undefContent = (UndefContent) message.content();
            if ("TIME_LINE".equals(undefContent.getText())) {
//                msgTipBinding.msgkitTipContent.setTextColor(0xFF76B9CB);
                msgTipBinding.msgkitTipContent.setTextColor(isGreenTheme ? ResourcesCompat.getColor(msgTipBinding.getRoot().getResources(), R.color.color_015F57, null) : 0xFF76B9CB);
                msgTipBinding.msgkitTipContent.setBackgroundResource(isGreenTheme ? R.drawable.time_msg_bg_green : R.drawable.time_msg_bg);
                String date = new SimpleDateFormat("MMMdd日(EEE)", Locale.TAIWAN).format(message.getSendTime());
                msgTipBinding.msgkitTipContent.setText(date);
            } else if ("UNREAD".equals(undefContent.getText())) {
                msgTipBinding.msgkitTipContent.setText("以下為未讀訊息");
            }
        } else if (SourceType.SYSTEM.equals(message.getSourceType()) && message.content() instanceof TextContent) {
            content = message.content().simpleContent();
            if (content.contains("進線") || content.toUpperCase().contains("END")) {
                msgTipBinding.msgkitTipContent.setTextColor(0xFFFFFFFF);
                msgTipBinding.msgkitTipContent.setBackgroundResource(R.drawable.sys_msg_current_state_bg);
                msgTipBinding.msgkitTipContent.setText(content.replace("-", ""));
            } else if (content.contains("切換")) {
                msgTipBinding.msgkitTipContent.setTextColor(0xFFFFFFFF);
                msgTipBinding.msgkitTipContent.setBackgroundResource(R.drawable.sys_msg_switch_bg);
                msgTipBinding.msgkitTipContent.setText(content.replace("-", ""));
            } else {
                msgTipBinding.msgkitTipContent.setTextColor(0xFF5E5E5E);
                if (!userId.equals(senderId)) {
                    msgTipBinding.msgkitTipContent.setText(content);
                } else if (senderId.equals(userId)) {
                    msgTipBinding.msgkitTipContent.setText(content);
                }
            }
        } else if (MessageFlag.RETRACT.equals(message.getFlag())) {
            msgTipBinding.msgkitTipContent.setBackgroundResource(R.drawable.sys_msg_bg);
            msgTipBinding.msgkitTipContent.setTextColor(0xFF5E5E5E);
            if (message.content() instanceof TextContent || message.content() instanceof AtContent) {
                msgTipBinding.msgkitTipContent.setBackgroundResource(R.drawable.sys_msg_bg);
                if (!userId.equals(senderId) && MessageFlag.RETRACT.equals(message.getFlag())) {
                    msgTipBinding.msgkitTipContent.setText(message.getSenderName() + "收回訊息");
                } else if (senderId.equals(userId) && MessageFlag.RETRACT.equals(message.getFlag())) {
                    msgTipBinding.msgkitTipContent.setText(KeyWordHelper.matcherKeys(0xFF4A90E2, SdkLib.getAppContext().getString(R.string.text_you_retract_message) + "  " + SdkLib.getAppContext().getString(R.string.text_edit_again), SdkLib.getAppContext().getString(R.string.text_edit_again), view -> {
                        if (this.onMessageControlEventListener != null) {
                            this.onMessageControlEventListener.onTipClick(message);
                        }
                    }));
                    msgTipBinding.msgkitTipContent.setMovementMethod(LinkMovementMethod.getInstance());
                }
            } else {
                if (!userId.equals(senderId)) {
                    msgTipBinding.msgkitTipContent.setText(message.getSenderName() + "已收回訊息");
                } else {
                    msgTipBinding.msgkitTipContent.setText(SdkLib.getAppContext().getString(R.string.text_you_retract_message));
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
        msgTipBinding.maskLayer.setVisibility(View.GONE);
        msgTipBinding.maskLayer.setOnClickListener(null);
        itemView.setBackgroundColor(Color.TRANSPARENT);
    }

    /**
     * Open range selection mode
     */
    public void rangeSelectionMode(MessageEntity message) {
        itemView.setBackgroundColor(Color.WHITE);
        msgTipBinding.maskLayer.setVisibility(View.VISIBLE);
        msgTipBinding.maskLayer.setOnClickListener(v -> {
            if (onMessageControlEventListener != null) {
                onMessageControlEventListener.doRangeSelection(message);
            }
        });
        if (message.isShowSelection()) {
            msgTipBinding.maskLayer.setAlpha(0.0f);
        } else {
            msgTipBinding.maskLayer.setAlpha(0.6f);
        }
    }
}
