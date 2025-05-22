package tw.com.chainsea.chat.view.enlarge.adapter.viewholder;

import android.view.View;

import androidx.annotation.NonNull;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.ce.sdk.bean.msg.content.IMessageContent;
import tw.com.chainsea.chat.databinding.ItemEnLargeTextMessageBinding;
import tw.com.chainsea.chat.view.enlarge.adapter.viewholder.base.EnLargeMessageBaseView;

/**
 * current by evan on 2020-04-15
 *
 * @author Evan Wang
 * @date 2020-04-15
 */
public class EnLargeUndefMessageView extends EnLargeMessageBaseView<IMessageContent> {
    ItemEnLargeTextMessageBinding enLargeTextMessageBinding;

    public EnLargeUndefMessageView(@NonNull ItemEnLargeTextMessageBinding binding) {
        super(binding.getRoot());
        enLargeTextMessageBinding = binding;
    }

    @Override
    public void onBind(MessageEntity entity, IMessageContent undefContent, int position) {
        civIcon.setVisibility(View.GONE);
        tvName.setVisibility(View.GONE);
        enLargeTextMessageBinding.tvText.setText("暫不支持此消息類型");
    }
}
