package tw.com.chainsea.chat.view.enlarge.adapter.viewholder;

import androidx.annotation.NonNull;

import tw.com.chainsea.ce.sdk.bean.PicSize;
import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.ce.sdk.bean.msg.content.BusinessContent;
import tw.com.chainsea.ce.sdk.service.AvatarService;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ItemEnLargeBusinessMessageBinding;
import tw.com.chainsea.chat.view.enlarge.adapter.viewholder.base.EnLargeMessageBaseView;

/**
 * current by evan on 2020-04-15
 *
 * @author Evan Wang
 * @date 2020-04-15
 */
public class EnLargeBusinessMessageView extends EnLargeMessageBaseView<BusinessContent> {
    ItemEnLargeBusinessMessageBinding enLargeBusinessMessageBinding;

    public EnLargeBusinessMessageView(@NonNull ItemEnLargeBusinessMessageBinding binding) {
        super(binding.getRoot());
        enLargeBusinessMessageBinding = binding;
    }

    @Override
    public void onBind(MessageEntity entity, BusinessContent businessContent, int position) {
        enLargeBusinessMessageBinding.tvBusinessName.setText(businessContent.getName());
        enLargeBusinessMessageBinding.tvEndDate.setText(businessContent.getEndTime());
        enLargeBusinessMessageBinding.tvBusinessManager.setText(businessContent.getBusinessManagerName());
        enLargeBusinessMessageBinding.tvBusinessExecutor.setText(businessContent.getBusinessExecutorName());
        enLargeBusinessMessageBinding.tvDescription.setText(businessContent.getDescription());


        AvatarService.post(context, businessContent.getBusinessExecutorAvatarId(), PicSize.SMALL,
            enLargeBusinessMessageBinding.civExecutorIcon, R.drawable.custom_default_avatar);
    }
}
