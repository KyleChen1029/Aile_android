package tw.com.chainsea.chat.messagekit.child.viewholder;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

import android.view.LayoutInflater;
import android.view.View;

import com.google.common.base.Strings;

import tw.com.chainsea.ce.sdk.bean.PicSize;
import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;
import tw.com.chainsea.ce.sdk.bean.msg.content.BusinessContent;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
import tw.com.chainsea.ce.sdk.service.AvatarService;
import tw.com.chainsea.chat.R;

/**
 * current by evan on 2020-03-27
 *
 * @author Evan Wang
 * @date 2020-03-27
 */
public class ChildBusinessMessageView extends ChildMessageBubbleView<BusinessContent> {

//    @Nullable
//    @BindView(R.id.civ_icon)
//    CircleImageView civIcon;
//    @Nullable
//    @BindView(R.id.tv_business_name)
//    TextView tvBusinessName;
//    @Nullable
//    @BindView(R.id.tv_category_name)
//    TextView tvCategoryName;
//    @Nullable
//    @BindView(R.id.tv_primary_name)
//    TextView tvPrimaryName;

    private tw.com.chainsea.chat.databinding.MsgkitBusiness2Binding binding;

    public ChildBusinessMessageView(@NonNull ViewBinding binding) {
        super(binding);
    }

    @Override
    protected int getContentResId() {
        return R.layout.msgkit_business2;
    }

    @Override
    protected void bindView(View itemView) {
        binding = tw.com.chainsea.chat.databinding.MsgkitBusiness2Binding.inflate(LayoutInflater.from(itemView.getContext()), null, false);
//        ButterKnife.bind(this, itemView);
    }

    @Override
    protected void bindContentView(BusinessContent businessContent) {
        if (isAnonymous) {
            if (Strings.isNullOrEmpty(businessContent.getBusinessExecutorId())) {
                binding.civIcon.setImageResource(R.drawable.custom_default_avatar);
            } else {
                binding.civIcon.setImageResource(getDomino(businessContent.getBusinessExecutorId()).getResId());
            }
        } else {
            if (ChatRoomType.subscribe.equals(this.b.getType()) && !isRightMessage()) {
                AvatarService.post(getContext(), this.b.getServiceNumberAvatarId(), PicSize.SMALL, binding.civIcon, R.drawable.custom_default_avatar);
            } else {
                AvatarService.post(getContext(), businessContent.getBusinessExecutorAvatarId(), PicSize.SMALL, binding.civIcon, R.drawable.custom_default_avatar);
            }
        }
        binding.tvCategoryName.setSelected(true);
        binding.tvCategoryName.setText(businessContent.getCode().getName());
        binding.tvBusinessName.setText(businessContent.getName());
        binding.tvPrimaryName.setText(businessContent.getBusinessPrimaryName());
    }

    @Override
    protected boolean showName() {
        return !isRightMessage();
    }


    @Override
    public void onClick(View v, MessageEntity message) {
        super.onClick(v, message);
        if (MessageType.BUSINESS.equals(getMessage().getType())) {
            if (this.onMessageControlEventListener != null) {
                this.onMessageControlEventListener.onImageClick(getMessage());
            }
        }
    }

    @Override
    public void onDoubleClick(View v, MessageEntity message) {
//        if (this.onMessageControlEventListener != null) {
//            this.onMessageControlEventListener.enLarge(getMessage());
//        }
    }

    @Override
    public void onLongClick(View v, float x, float y, MessageEntity message) {
//        final MessageEntity msg = getMessage();
//        if (this.onMessageControlEventListener != null) {
//            this.onMessageControlEventListener.onLongClick(msg, (int) x, (int) y);
//        }
    }
}
