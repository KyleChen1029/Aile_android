package tw.com.chainsea.chat.view.enlarge.adapter.viewholder;

import androidx.annotation.NonNull;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.bean.msg.content.TextContent;
import tw.com.chainsea.chat.databinding.ItemEnLargeTextMessageBinding;
import tw.com.chainsea.chat.view.enlarge.adapter.viewholder.base.EnLargeMessageBaseView;

/**
 * current by evan on 2020-04-14
 *
 * @author Evan Wang
 * date 2020-04-14
 */
public class EnLargeTextMessageView extends EnLargeMessageBaseView<TextContent> {
    ItemEnLargeTextMessageBinding enLargeTextMessageBinding;

    public EnLargeTextMessageView(@NonNull ItemEnLargeTextMessageBinding binding) {
        super(binding.getRoot());
        enLargeTextMessageBinding = binding;
    }

    @Override
    public void onBind(MessageEntity entity, TextContent textContent, int position) {
        CELog.e(civIcon + "");
        enLargeTextMessageBinding.tvText.setText(textContent.getText());
    }

}
