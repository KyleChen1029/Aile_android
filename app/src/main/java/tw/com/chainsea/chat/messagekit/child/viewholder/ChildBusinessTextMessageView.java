//package tw.com.chainsea.chat.messagekit.child.viewholder;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import android.view.View;
//import android.widget.TextView;
//
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.request.RequestOptions;
//
//import butterknife.BindView;
//import butterknife.ButterKnife;
//import tw.com.aile.sdk.bean.message.MessageEntity;
//import tw.com.chainsea.ce.sdk.bean.msg.MessageType;
//import tw.com.chainsea.ce.sdk.bean.msg.content.BusinessTextContent;
//import tw.com.chainsea.chat.R;
//import tw.com.chainsea.custom.view.image.RoundImageView;
//
//
///**
// * text message view
// * Created by 90Chris on 2016/4/20.
// */
//public class ChildBusinessTextMessageView extends ChildMessageBubbleView<BusinessTextContent> {
//
//    @Nullable
//    @BindView(R.id.txt_msg_title)
//    TextView tvTitle;
//    @Nullable
//    @BindView(R.id.txt_msg_content)
//    TextView tvContent;
//    @Nullable
//    @BindView(R.id.msg_image)
//    RoundImageView msgImage;
//
//    private long exitTime;
//
//    public ChildBusinessTextMessageView(@NonNull View itemView) {
//        super(itemView);
//    }
//
//    @Override
//    protected int getContentResId() {
//        return R.layout.msgkit_business;
//    }
//
//    @Override
//    protected void bindView(View itemView) {
//        ButterKnife.bind(this, itemView);
//    }
//
//    @Override
//    protected void bindContentView(BusinessTextContent businessTextContent) {
//        String content = businessTextContent.getContent();
//        String title = businessTextContent.getTitle();
//        String pictureUrl = businessTextContent.getPictureUrl();
//        if ("%{}%".equals(content)) {
//            content = "{}";
//        }
//        if ("%{}%".equals(title)) {
//            title = "{}";
//        }
//
//        tvContent.setText(content);
//        tvTitle.setText(title);
//        Glide.with(getContext())
//                .load(pictureUrl)
//                .apply(new RequestOptions()
//                        .override(400)
//                        .placeholder(R.drawable.image_loading)
//                        .error(R.drawable.image_load_error)
//                        .fitCenter())
//                .into(msgImage);
//    }
//
//    @Override
//    protected boolean showName() {
//        return isRightMessage() ? false : true;
//    }
//
//    private void showPopup(int pressX, int pressY) {
//        final MessageEntity msg = getMessage();
//        if (this.onMessageControlEventListener != null) {
//            this.onMessageControlEventListener.onLongClick(msg, pressX, pressY);
//        }
//    }
//
//    @Override
//    public void onClick(View v, MessageEntity message) {
//        super.onClick(v, message);
//        if (MessageType.BUSINESS_TEXT.equals(getMessage().getType())) {
//            if (this.onMessageControlEventListener != null) {
//                this.onMessageControlEventListener.onImageClick(getMessage());
//            }
//        }
//    }
//
//    @Override
//    public void onDoubleClick(View v, MessageEntity message) {
//        if (this.onMessageControlEventListener != null) {
//            this.onMessageControlEventListener.enLarge(getMessage());
//        }
//    }
//
//    @Override
//    public void onLongClick(View v, float x, float y, MessageEntity message) {
//        showPopup((int) x, (int) y);
//    }
//}
