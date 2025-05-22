package tw.com.chainsea.chat.view.enlarge.adapter.viewholder;

import androidx.annotation.NonNull;

import java.text.MessageFormat;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.ce.sdk.bean.msg.content.TransferContent;
import tw.com.chainsea.chat.databinding.ItemEnLargeTextMessageBinding;
import tw.com.chainsea.chat.view.enlarge.adapter.viewholder.base.EnLargeMessageBaseView;

public class EnLargeTransferMessageView extends EnLargeMessageBaseView<TransferContent> {
    ItemEnLargeTextMessageBinding enLargeTextMessageBinding;

    public EnLargeTransferMessageView(@NonNull ItemEnLargeTextMessageBinding binding) {
        super(binding.getRoot());
        enLargeTextMessageBinding = binding;
    }

    @Override
    public void onBind(MessageEntity entity, TransferContent transferContent, int position) {
        enLargeTextMessageBinding.tvText.setText(MessageFormat.format("({0})需接續服務", transferContent.getRoomName()));
    }
}
