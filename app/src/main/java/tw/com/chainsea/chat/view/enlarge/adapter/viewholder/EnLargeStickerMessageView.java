package tw.com.chainsea.chat.view.enlarge.adapter.viewholder;

import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.ce.sdk.bean.msg.content.StickerContent;
import tw.com.chainsea.ce.sdk.http.ce.request.StickerDownloadRequest;
import tw.com.chainsea.ce.sdk.service.StickerService;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ItemEnLargeStickerMessageBinding;
import tw.com.chainsea.chat.view.enlarge.adapter.viewholder.base.EnLargeMessageBaseView;

/**
 * current by evan on 2020-04-15
 *
 * @author Evan Wang
 * @date 2020-04-15
 */
public class EnLargeStickerMessageView extends EnLargeMessageBaseView<StickerContent> {
    ItemEnLargeStickerMessageBinding enLargeStickerMessageBinding;


    public EnLargeStickerMessageView(@NonNull ItemEnLargeStickerMessageBinding binding) {
        super(binding.getRoot());
        enLargeStickerMessageBinding = binding;
    }

    @Override
    public void onBind(MessageEntity entity, StickerContent stickerContent, int position) {
        String packageId = stickerContent.getPackageId();
        String stickerId = stickerContent.getId();
        enLargeStickerMessageBinding.msgEmoticons.setVisibility(View.VISIBLE);
        StickerService.postSticker(context, packageId, stickerId, StickerDownloadRequest.Type.PICTURE, new ServiceCallBack<Drawable, RefreshSource>() {
            @Override
            public void complete(Drawable drawable, RefreshSource source) {
                enLargeStickerMessageBinding.msgEmoticons.setImageDrawable(drawable);
            }

            @Override
            public void error(String message) {
                enLargeStickerMessageBinding.msgEmoticons.setImageResource(isGreenTheme ? R.drawable.image_load_error_green : R.drawable.image_load_error);
            }
        });
    }
}
