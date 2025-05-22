//package tw.com.chainsea.chat.messagekit.main.viewholder;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import android.view.View;
//import android.widget.TextView;
//
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.request.RequestOptions;
//
//import tw.com.aile.sdk.bean.message.MessageEntity;
//import tw.com.chainsea.ce.sdk.bean.msg.MessageType;
//import tw.com.chainsea.ce.sdk.bean.msg.content.BusinessTextContent;
//import tw.com.chainsea.chat.R;
//import tw.com.chainsea.chat.messagekit.main.viewholder.base.MessageBubbleView;
//import tw.com.chainsea.custom.view.image.RoundImageView;
//
//
///**
// * text message view
// * Created by 90Chris on 2016/4/20.
// */
//public class BusinessTextMessageView extends MessageBubbleView<BusinessTextContent> {
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
//    public BusinessTextMessageView(@NonNull View itemView) {
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
//      /*  final List<String> popupMenuItemList = new ArrayList<>();
//        popupMenuItemList.clear();
//        if (message.getStatus() == MessageStatus.FAILED) {
//            popupMenuItemList.add(getContext().getString(R.string.retry));
//        } else {
//            popupMenuItemList.add(getContext().getString(R.string.transpond));
//
//        }
//        if (message.getSenderId().equals(UserPref.getInstance(getContext()).getUserId())) {
//            int value = message.getStatus().getValue();
//            ISession iSession = DBManager.getInstance().querySession(message.getSessionId());
//            if (value > 0 && value != 2 && iSession.getTodoOverviewType() != SessionType.SERVICES
//                    && iSession.getTodoOverviewType() != SessionType.SUBSCRIBE) {
//                //TODO 1.5.3 隐藏撤回
//                popupMenuItemList.add(getContext().getString(R.string.retractMsg));
//            }
//        }
//        popupMenuItemList.add(getContext().getString(R.string.copy));
////        隐藏回复
//        popupMenuItemList.add(getContext().getString(R.string.reply));
//        popupMenuItemList.add(getContext().getString(R.string.share));
//        popupMenuItemList.add(getContext().getString(R.string.item_del));
//        PopupList popupList = new PopupList(view.getContext());
//        popupList.showPopupListWindow(view, pressX, pressY, popupMenuItemList, new PopupList.PopupListListener() {
//            @Override
//            public boolean showPopupList(View adapterView, View contextView) {
//                return true;
//            }
//
//            @Override
//            public void onPopupListClick(View contextView, int position) {
//                String s = popupMenuItemList.get(position);
//                switch (s) {
//                    case "複製":
//                        TextMsgFormat format = (TextMsgFormat) getMessage().getFormat();
//                        onListDilogItemClickListener.copyText(format.getContent());
//                        break;
//                    case "回復":
//                        onListDilogItemClickListener.replyText(message);
//                        break;
//                    case "轉發":
//                        onListDilogItemClickListener.tranSend(message);
//                        break;
//                    case "撤回":
//                        onListDilogItemClickListener.retractMsg(message);
//                        break;
//                    case "重發":
//                        onListDilogItemClickListener.retry(message);
//                        break;
//                    case "分享":
//                        onListDilogItemClickListener.shares(message, tvContent);
//                        break;
//                    case "刪除":
//                        onListDilogItemClickListener.delete(message);
//                        break;
//                    default:
//                        break;
//                }
//            }
//        });*/
//    }
//
////    @Override
////    protected boolean onBubbleLongClicked(float pressX, float pressY) {
////        showPopup((int) pressX, (int) pressY);
////        return true;
////    }
//
//
////    @Override
////    protected void onBubbleClicked() {
////        if (getMessage().getMsgType()== MessageType.BUSINESS_TEXT){
////            onImgClickListener.onClick(super.getMessage());
////        }else {
////            if ((System.currentTimeMillis() - exitTime) > 2000) {
////                exitTime = System.currentTimeMillis();
////            } else {
////                onListDilogItemClickListener.enLarge(super.getMessage());
////            }
////        }
////    }
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
