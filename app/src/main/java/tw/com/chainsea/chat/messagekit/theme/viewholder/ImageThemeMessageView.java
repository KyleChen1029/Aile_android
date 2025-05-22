package tw.com.chainsea.chat.messagekit.theme.viewholder;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.common.base.Strings;

import java.io.File;

import tw.com.chainsea.chat.util.DaVinci;
import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.ce.sdk.bean.msg.content.ImageContent;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.messagekit.theme.viewholder.base.ThemeMessageBubbleView;
import tw.com.chainsea.custom.view.image.RoundImageView;


/**
 * ImageMessageView
 * Created by Andy on 2016/5/22.
 */
public class ImageThemeMessageView extends ThemeMessageBubbleView {

    RoundImageView image;
    //        PorterShapeImageView image;
    private int mScreenWidth = 0;
    private ImageContent imageContent;
    //    private ImageMsgFormat mFormat;
    final static int SMALL_PIC_SIZE = 400;

    public ImageThemeMessageView(@NonNull View itemView) {
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
        return R.layout.msgkit_image_reply;
    }

    @Override
    protected void inflateContentView() {
        image = findView(R.id.msg_image);
    }

    @Override
    protected void bindContentView() {
        imageContent = (ImageContent) getMsg().content();
        WindowManager manager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(displayMetrics);
        mScreenWidth = displayMetrics.widthPixels
                - 2 * getContext().getResources().getDimensionPixelSize(R.dimen.image_text_margin)
                - 2 * getContext().getResources().getDimensionPixelSize(R.dimen.image_text_padding);


        String thumbnailUrl = imageContent.getThumbnailUrl();
        if (!Strings.isNullOrEmpty(thumbnailUrl)) {
            if (thumbnailUrl.endsWith(".gif") && !thumbnailUrl.startsWith("http")) {
                File file = new File(thumbnailUrl);
                int[] sizes = zoomImage(imageContent.getWidth(), imageContent.getHeight());
                //解决listView不能滚到最底部
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizes[0], sizes[1]);
                ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                image.setLayoutParams(params);
                RequestOptions option = new RequestOptions()
                        .override(sizes[0], sizes[1]);
                Glide.with(getContext()).asGif().load(file).apply(option).into(image);
            } else {
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            image.setLayoutParams(params);
                ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                image.setLayoutParams(params);
//            Log.d("ImageMessageView", mFormat.getThumbnailUrl());
//                String thumbnailUrl1 = mFormat.getThumbnailUrl();
                if (!Strings.isNullOrEmpty(thumbnailUrl) && thumbnailUrl.startsWith("smallandroid")) {
                    Glide.with(getContext())
                            .load(DaVinci.with().getImageLoader().getImage(thumbnailUrl).getBitmap())
                            .apply(new RequestOptions()
                                    .override(400)
                                    .placeholder(R.drawable.loading_area)
                                    .error(isGreenTheme ? R.drawable.image_load_error_green : R.drawable.image_load_error)
                                    .fitCenter())
                            .into(this.image);
                } else {
                    Glide.with(getContext())
                            .load(thumbnailUrl)
                            .apply(new RequestOptions()
                                    .override(400)
                                    .placeholder(R.drawable.loading_area)
                                    .error(isGreenTheme ? R.drawable.image_load_error_green : R.drawable.image_load_error)
                                    .fitCenter())
                            .into(this.image);
                }
            }
        } else {
            Glide.with(getContext())
                    .load(isGreenTheme ? R.drawable.image_load_error_green : R.drawable.image_load_error)
                    .override(100)
                    .into(this.image);
        }


        /*Glide.with(getContext()).load(mFormat.getThumbnailUrl())
                .placeholder(R.drawable.image_loading)
                .error(R.drawable.image_load_error).into(image);*/

        /*FilterOnView.addTouchColorChange(image, new FilterOnView.ClickAction() {
            @Override
            public void onClickAction() {

            }
        });*/
    }

    public static int[] zoomImage(int inWidth, int inHeight) {
        int outWidth;
        int outHeight;

        if (inWidth > inHeight) {
            outWidth = SMALL_PIC_SIZE;
            outHeight = (inHeight * SMALL_PIC_SIZE) / inWidth;
        } else {
            outHeight = SMALL_PIC_SIZE;
            outWidth = (inWidth * SMALL_PIC_SIZE) / inHeight;
        }

        int[] sizes = new int[2];
        sizes[0] = outWidth;
        sizes[1] = outHeight;

        return sizes;
    }

//    @Override
//    protected boolean onBubbleLongClicked(float pressX, float pressY) {
////        showListDialog();
////        showPopup((int)pressX, (int)pressY);
//        return true;
//    }
//
//    @Override
//    protected void onBubbleClicked() {
//        if (onListDilogItemClickListener != null){
//            onListDilogItemClickListener.locationMsg(msg);
//        }
//    }


//    private void showPopup(int pressX, int pressY) {
//        final MessageEntity msg = getMsg();
//        final List<String> popupMenuItemList = new ArrayList<>();
//        popupMenuItemList.clear();
//        if (MessageStatus.FAILED.equals(msg.getStatus())) {
//            popupMenuItemList.add(getContext().getString(R.string.retry));
//        } else {
//            popupMenuItemList.add(getContext().getString(R.string.transpond));
//        }
//        String userId = TokenPref.getInstance(getContext()).getUserId();
//        if (msg.getSenderId().equals(userId)) {
//            int value = msg.getStatus().getValue();
//            ChatRoomEntity iSession = ChatRoomReference.getInstance().findById2( userId, msg.getRoomId(), false, false, false, false, false);
////            ChatRoomEntity iSession = ChatRoomReference.getInstance().findById(msg.getRoomId());
//            if (value > 0 && value != 2 && !iSession.getType().equals(ChatRoomType.SERVICES) && !iSession.getType().equals(ChatRoomType.SUBSCRIBE)) {
//                //TODO 1.5.3 隐藏撤回
//                popupMenuItemList.add(getContext().getString(R.string.recover));
//            }
//        }
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
//                    case "轉發":
//                        onListDilogItemClickListener.tranSend(msg);
//                        break;
//                    case "重發":
//                        onListDilogItemClickListener.retry(msg);
//                        break;
//                    case "分享":
//                        onListDilogItemClickListener.shares(msg, image);
//                        break;
//                    case "刪除":
//                        onListDilogItemClickListener.delete(msg);
//                        break;
//                    case "撤回":
//                        onListDilogItemClickListener.retractMsg(msg);
//                        break;
//                }
//            }
//        });
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

