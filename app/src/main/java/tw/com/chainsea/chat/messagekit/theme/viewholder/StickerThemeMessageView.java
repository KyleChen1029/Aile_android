package tw.com.chainsea.chat.messagekit.theme.viewholder;

import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import android.view.View;

import pl.droidsonroids.gif.GifImageView;
import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.ce.sdk.bean.msg.content.StickerContent;
import tw.com.chainsea.ce.sdk.http.ce.request.StickerDownloadRequest;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.StickerService;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.messagekit.theme.viewholder.base.ThemeMessageBubbleView;


/**
 * StickerMessageView
 * Created by Andy on 2016/5/10.
 */
public class StickerThemeMessageView extends ThemeMessageBubbleView {

    GifImageView emoticons;

    public StickerThemeMessageView(@NonNull View itemView) {
        super(itemView);
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
    protected void inflateContentView() {
        emoticons = findView(R.id.msg_emoticons);
    }

    @Override
    protected void bindContentView() {
        StickerContent stickerContent = (StickerContent) getMsg().content();
        String packageId = stickerContent.getPackageId();
        String stickerId = stickerContent.getId();
        emoticons.setVisibility(View.VISIBLE);
        StickerService.postSticker(getContext(), packageId, stickerId, StickerDownloadRequest.Type.PICTURE, new ServiceCallBack<Drawable, RefreshSource>() {
            @Override
            public void complete(Drawable drawable, RefreshSource source) {
                emoticons.setImageDrawable(drawable);
            }

            @Override
            public void error(String message) {
                emoticons.setImageResource(isGreenTheme ? R.drawable.image_load_error_green : R.drawable.image_load_error);
            }
        });
//        if (StringHelper.isValidUUID(stickerId) && !StringHelper.isValidUUID(packageId)) {
//            emoticons.setImageResource(R.drawable.image_load_error);
//        } else if (StringHelper.isValidUUID(stickerId) && StringHelper.isValidUUID(packageId)) {
//            StickerService.postSticker(getContext(), packageId, stickerId, StickerDownloadRequest.Type.PICTURE, new ServiceCallBack<Drawable, RefreshSource>() {
//                @Override
//                public void complete(Drawable drawable, RefreshSource source) {
//                    emoticons.setImageDrawable(drawable);
//                }
//
//                @Override
//                public void error(String message) {
//                    emoticons.setImageResource(R.drawable.image_load_error);
//                }
//            });
//        } else if (!TextUtils.isEmpty(stickerId)) {
//            try {
//
//                InputStream stream = getContext().getAssets().open("emoticons/qbi/" + stickerId);
//                GifDrawable gifFromAssets = new GifDrawable(stream);
//                emoticons.setImageDrawable(gifFromAssets);
////                InputStream stream = EmoticonLoader.getInstance(getContext()).getInputStreamByTag(stickerId);
////                GifDrawable gifFromAssets = new GifDrawable(stream);
////                emoticons.setImageDrawable(gifFromAssets);
//            } catch (IOException e) {
//                CELog.e(e.getMessage());
//                emoticons.setImageResource(R.drawable.image_load_error);
//            }
//        }
    }

//    @Override
//    protected boolean onBubbleLongClicked(float pressX, float pressY) {
////        showListDialog();
////        showPopup((int)pressX, (int)pressY);
//        return true;
//    }

//    private void showPopup(int pressX, int pressY) {
//        final MessageEntity msg = getMsg();
//        final List<String> popupMenuItemList = new ArrayList<>();
//        popupMenuItemList.clear();
////        popupMenuItemList.add(getContext().getString(R.string.transpond));
////        if (message.getStatus() == MessageStatus.FAILED) {
////            popupMenuItemList.add(getContext().getString(R.string.retry));
////        }
//        if (MessageStatus.FAILED.equals(msg.getStatus())) {
//            popupMenuItemList.add(getContext().getString(R.string.retry));
//        } else {
//            popupMenuItemList.add(getContext().getString(R.string.transpond));
//        }
//        String userId = TokenPref.getInstance(getContext()).getUserId();
//        if (msg.getSenderId().equals(userId)) {
//            int value = msg.getStatus().getValue();
////            ChatRoomEntity iSession = ChatRoomReference.getInstance().findById(msg.getRoomId());
//            ChatRoomEntity iSession = ChatRoomReference.getInstance().findById2( userId, msg.getRoomId(), false, false, false, false, false);
//            if (value > 0 && value != 2 && !iSession.getType().equals(ChatRoomType.SERVICES) && !iSession.getType().equals(ChatRoomType.SUBSCRIBE)) {
//                //TODO 1.5.3 隐藏撤回
//                popupMenuItemList.add(getContext().getString(R.string.recover));
//            }
//        }
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
//                if (onListDilogItemClickListener != null) {
//                    switch (s) {
//                        case "轉發":
//                            onListDilogItemClickListener.tranSend(msg);
//                            break;
//                        case "重發":
//                            onListDilogItemClickListener.retry(msg);
//                            break;
//                        case "刪除":
//                            onListDilogItemClickListener.delete(msg);
//                            break;
//                        case "撤回":
//                            onListDilogItemClickListener.retractMsg(msg);
//                            break;
//                    }
//                }
//            }
//        });
//    }

//
//    @Override
//    protected void onBubbleClicked() {
//        if (onListDilogItemClickListener != null){
//            onListDilogItemClickListener.locationMsg(msg);
//        }
//    }


    @Override
    public void onClick(View v, MessageEntity message) {
        if (onListDilogItemClickListener != null) {
            onListDilogItemClickListener.locationMsg(msg);
        }
    }

    @Override
    public void onDoubleClick(View v, MessageEntity message) {

    }

    @Override
    public void onLongClick(View v, float x, float y, MessageEntity message) {

    }
}


