package tw.com.chainsea.chat.messagekit.child.viewholder;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.ce.sdk.bean.msg.content.StickerContent;
import tw.com.chainsea.ce.sdk.http.ce.request.StickerDownloadRequest;
import tw.com.chainsea.ce.sdk.service.StickerService;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.chat.R;


/**
 * StickerMessageView
 * Created by Andy on 2016/5/10.
 */
public class ChildStickerMessageView extends ChildMessageBubbleView<StickerContent> {
    private tw.com.chainsea.chat.databinding.MsgkitStickerBinding binding;

    public ChildStickerMessageView(@NonNull ViewBinding binding) {
        super(binding);
    }

    @Override
    protected boolean showName() {
        return !isRightMessage();
    }

    @Override
    protected int leftBackground() {
        return R.drawable.msgkit_trans_bg;
    }

    @Override
    protected int rightBackground() {
        return R.drawable.msgkit_trans_bg;
    }

    @Override
    protected int getContentResId() {
        return R.layout.msgkit_sticker;
    }

    @Override
    protected void bindView(View itemView) {
        binding = tw.com.chainsea.chat.databinding.MsgkitStickerBinding.inflate(LayoutInflater.from(itemView.getContext()), null, false);
    }

    @Override
    protected void bindContentView(StickerContent content) {
        String packageId = content.getPackageId();
        String stickerId = content.getId();
        StickerService.postSticker(getContext(), packageId, stickerId, StickerDownloadRequest.Type.PICTURE, new ServiceCallBack<>() {
            @Override
            public void complete(Drawable drawable, RefreshSource source) {
                binding.msgEmoticons.setImageDrawable(drawable);
            }

            @Override
            public void error(String message) {
                binding.msgEmoticons.setImageResource(isGreenTheme ? R.drawable.image_load_error_green : R.drawable.image_load_error);
            }
        });
    }

    @Override
    public void onClick(View v, MessageEntity message) {
        super.onClick(v, message);
    }

    @Override
    public void onDoubleClick(View v, MessageEntity message) {
    }

    @Override
    public void onLongClick(View v, float x, float y, MessageEntity message) {
        if (this.onMessageControlEventListener != null) {
            this.onMessageControlEventListener.onLongClick(getMessage(), (int) x, (int) y);
        }
    }

}


