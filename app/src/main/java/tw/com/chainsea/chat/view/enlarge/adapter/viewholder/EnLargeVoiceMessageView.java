package tw.com.chainsea.chat.view.enlarge.adapter.viewholder;

import androidx.annotation.NonNull;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.ce.sdk.bean.msg.content.VoiceContent;
import tw.com.chainsea.chat.databinding.ItemEnLargeVoiceMessageBinding;
import tw.com.chainsea.chat.view.enlarge.adapter.viewholder.base.EnLargeMessageBaseView;

/**
 * current by evan on 2020-04-15
 *
 * @author Evan Wang
 * @date 2020-04-15
 */
public class EnLargeVoiceMessageView extends EnLargeMessageBaseView<VoiceContent> {
    ItemEnLargeVoiceMessageBinding enLargeVoiceMessageBinding;
    public EnLargeVoiceMessageView(@NonNull ItemEnLargeVoiceMessageBinding binding) {
        super(binding.getRoot());
    }

    @Override
    public void onBind(MessageEntity entity, VoiceContent voiceContent, int position) {

    }
}
