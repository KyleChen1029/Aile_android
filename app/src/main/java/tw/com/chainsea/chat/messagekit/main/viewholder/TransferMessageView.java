package tw.com.chainsea.chat.messagekit.main.viewholder;

import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

import java.text.MessageFormat;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.bean.PicSize;
import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.ce.sdk.bean.msg.content.TransferContent;
import tw.com.chainsea.ce.sdk.service.AvatarService;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.MsgkitTransferBinding;
import tw.com.chainsea.chat.messagekit.main.viewholder.base.MessageBubbleView;
import tw.com.chainsea.chat.service.ActivityTransitionsControl;
import tw.com.chainsea.chat.util.IntentUtil;


public class TransferMessageView extends MessageBubbleView<TransferContent> {

    private MsgkitTransferBinding binding;

    public TransferMessageView(@NonNull ViewBinding binding) {
        super(binding);
        this.binding = MsgkitTransferBinding.inflate(LayoutInflater.from(itemView.getContext()), null, false);
        getView(this.binding.getRoot());
    }

    @Override
    protected int getContentResId() {
        return R.layout.msgkit_transfer;
    }

    @Override
    protected View getChildView() {
        return binding.getRoot();
    }

    @Override
    protected void bindContentView(TransferContent transferContent) {
        AvatarService.post(binding.imgAvatar.getContext(), transferContent.getRoomId(), PicSize.SMALL, binding.imgAvatar, R.drawable.custom_default_avatar);
        binding.txtName.setText(MessageFormat.format("({0})", transferContent.getRoomName()));
        String reason = transferContent.getTransferReason();
        if (reason != null && !reason.isEmpty()) {
            binding.txtReason.setVisibility(View.VISIBLE);
            binding.txtReason.setText(transferContent.getTransferReason());
        } else {
            binding.txtReason.setVisibility(View.GONE);
        }
    }

    @Override
    protected boolean showName() {
        return !isRightMessage();
    }

    @Override
    public void onClick(View v, MessageEntity message) {
        super.onClick(v, message);
        if (this.onMessageControlEventListener != null) {
            TransferContent transferContent = JsonHelper.getInstance().from(message.getContent(), TransferContent.class);
            if (transferContent != null && transferContent.getRoomId() != null) {
                ActivityTransitionsControl.navigateToChat(binding.getRoot().getContext(), transferContent.getRoomId(), (intent, s) -> {
                    IntentUtil.INSTANCE.start(binding.getRoot().getContext(), intent);
                });
            }
        }
    }

    @Override
    public void onDoubleClick(View v, MessageEntity message) {
        if (this.onMessageControlEventListener != null) {
            this.onMessageControlEventListener.enLarge(getMessage());
        }
    }

    @Override
    public void onLongClick(View v, float x, float y, MessageEntity message) {
        if (this.onMessageControlEventListener != null) {
            this.onMessageControlEventListener.onLongClick(getMessage(), 0, 0);
        }
    }
}
