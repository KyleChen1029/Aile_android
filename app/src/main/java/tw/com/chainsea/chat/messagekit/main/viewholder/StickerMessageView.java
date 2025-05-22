package tw.com.chainsea.chat.messagekit.main.viewholder;

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
import tw.com.chainsea.chat.databinding.MsgkitStickerBinding;
import tw.com.chainsea.chat.messagekit.main.viewholder.base.MessageBubbleView;


/**
 * StickerMessageView
 * Created by Andy on 2016/5/10.
 */
public class StickerMessageView extends MessageBubbleView<StickerContent> {
//    @Nullable
//    @BindView(R.id.msg_emoticons)
//    GifImageView emoticons;

    private final MsgkitStickerBinding binding;

    public StickerMessageView(@NonNull ViewBinding binding) {
        super(binding);
        this.binding = MsgkitStickerBinding.inflate(LayoutInflater.from(itemView.getContext()), null, false);
        getView(this.binding.getRoot());
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
    protected View getChildView() {
        return binding.getRoot();
    }

    @Override
    protected void bindContentView(StickerContent content) {
        String packageId = content.getPackageId();
        String stickerId = content.getId();
        StickerService.postSticker(binding.getRoot().getContext(), packageId, stickerId, StickerDownloadRequest.Type.PICTURE, new ServiceCallBack<>() {
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


