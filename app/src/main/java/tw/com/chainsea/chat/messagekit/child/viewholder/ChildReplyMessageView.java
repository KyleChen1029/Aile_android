package tw.com.chainsea.chat.messagekit.child.viewholder;

import static tw.com.chainsea.chat.messagekit.main.viewholder.FileMessageView.formatSizeDisplay;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.common.base.Strings;

import java.io.File;
import java.util.List;
import java.util.Objects;

import tw.com.chainsea.chat.util.DaVinci;
import cn.hadcn.davinci.image.base.ImageEntity;
import pl.droidsonroids.gif.GifImageView;
import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.file.FileHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.text.KeyWordHelper;
import tw.com.chainsea.android.common.voice.VoiceHelper;
import tw.com.chainsea.ce.sdk.SdkLib;
import tw.com.chainsea.ce.sdk.bean.PicSize;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.msg.MessageFlag;
import tw.com.chainsea.ce.sdk.bean.msg.MessageStatus;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;
import tw.com.chainsea.ce.sdk.bean.msg.content.AtContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.FileContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.IMessageContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.ImageContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.MentionContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.StickerContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.TextContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.VideoContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.VoiceContent;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
import tw.com.chainsea.ce.sdk.customview.AvatarIcon;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.http.ce.request.StickerDownloadRequest;
import tw.com.chainsea.ce.sdk.reference.MessageReference;
import tw.com.chainsea.ce.sdk.service.AvatarService;
import tw.com.chainsea.ce.sdk.service.StickerService;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.databinding.MsgkitReplyBinding;
import tw.com.chainsea.chat.keyboard.ChatTextView;
import tw.com.chainsea.chat.lib.AtMatcherHelper;
import tw.com.chainsea.chat.messagekit.enums.FileType;
import tw.com.chainsea.chat.util.DownloadUtil;
import tw.com.chainsea.chat.view.gallery.PhotoGalleryActivity;
import tw.com.chainsea.custom.view.image.CircleImageView;
import tw.com.chainsea.custom.view.image.RoundImageView;


/**
 * text message view
 * Created by 90Chris on 2016/4/20.
 */
public class ChildReplyMessageView extends ChildMessageBubbleView<IMessageContent> {
    private MsgkitReplyBinding binding;

    final static int SMALL_PIC_SIZE = 400;

    enum Type {
        THEME,
        NEAR,
        UNDER

    }

    public ChildReplyMessageView(@NonNull ViewBinding binding) {
        super(binding);
    }

    @Override
    protected int getContentResId() {
        return R.layout.msgkit_reply;
    }

    @Override
    protected void bindView(View itemView) {
        binding = MsgkitReplyBinding.inflate(LayoutInflater.from(itemView.getContext()), null, false);
    }


    @Override
    protected void bindContentView(IMessageContent iMessageContent) {
        MessageEntity message = getMessage();
        MessageType messageType = message.getType();
        MessageType nearMessageType = message.getNearMessageType();

        View avatarView = null;
        if (isAnonymous) {
            avatarView = LayoutInflater.from(getContext()).inflate(R.layout.view_reply_message_avatar, binding.layoutAvatar, false);
            CircleImageView avatarCIV = avatarView.findViewById(R.id.avatarCIV);
            avatarCIV.setImageResource(getDomino(message.getNearMessageSenderId()).getResId());
        } else {
            if (ChatRoomType.subscribe.equals(b.getType())) {
                avatarView = LayoutInflater.from(getContext()).inflate(R.layout.view_reply_message_avatar, binding.layoutAvatar, false);
                CircleImageView avatarCIV = avatarView.findViewById(R.id.avatarCIV);
                if (!userId.equals(message.getNearMessageSenderId())) {
                    AvatarService.post(getContext(), b.getServiceNumberAvatarId(), PicSize.SMALL, avatarCIV, R.drawable.custom_default_avatar);
                } else {
                    AvatarService.post(getContext(), message.getNearMessageAvatarId(), PicSize.SMALL, avatarCIV, R.drawable.custom_default_avatar);
                }
            } else {
                try {
                    avatarView = LayoutInflater.from(getContext()).inflate(R.layout.view_reply_message_avatar, binding.layoutAvatar, false);
                    AvatarIcon avatarIcon = avatarView.findViewById(R.id.avatarCIV);
                    if (message.getNearMessageSenderId() != null && !message.getNearMessageSenderId().isEmpty()) {
                        UserProfileEntity userProfileEntity = DBManager.getInstance().queryUser(message.getSenderId());
                        avatarIcon.loadAvatarIcon(userProfileEntity.getAvatarId(), userProfileEntity.getNickName(), userProfileEntity.getId());
                    }
                } catch (Exception e) {
                    CELog.e("Exception message" + e.getMessage());
                }
            }
        }

        if (avatarView != null) {
            binding.layoutAvatar.removeAllViews();
            binding.layoutAvatar.addView(avatarView);
        }

        View underView = null;
        if (MessageFlag.RETRACT.equals(message.getFlag())) {
            view = initRetractView(message); // EVAN_FLAG 2020-02-13 (1.9.1) 該訊息被收回
        } else if (messageType == null) {
            underView = assemblyUnDefView();
        } else {
            underView = switch (messageType) {
                case TEXT -> assemblyTextView((TextContent) message.content(), true, Type.UNDER);
                case AT -> assemblyMentionView((AtContent) message.content(), true);
                case IMAGE -> assemblyImageView((ImageContent) message.content());
                case STICKER -> assemblyStickerView((StickerContent) message.content(), Type.NEAR);
                case FILE -> assemblyFileView(message, false);
                case VOICE -> assemblyVoiceView((VoiceContent) message.content());
                case VIDEO -> assemblyVideoView((VideoContent) message.content());
                default -> assemblyUnDefView();
            };
        }

        if (underView != null) {
            binding.underFrameLayout.removeAllViews();
            binding.underFrameLayout.addView(underView);
        } else {
            binding.underFrameLayout.setVisibility(View.INVISIBLE);
        }


        View nearView;

        if (nearMessageType == null) {
            nearView = assemblyUnDefView();
        } else {
            nearView = switch (nearMessageType) {
                case TEXT -> assemblyTextView((TextContent) message.nearMessageContent(), false, Type.NEAR);
                case AT -> assemblyMentionView((AtContent) message.nearMessageContent(), false);
                case IMAGE -> assemblyImageView((ImageContent) message.nearMessageContent());
                case STICKER -> assemblyStickerView((StickerContent) message.nearMessageContent(), Type.NEAR);
                case FILE -> assemblyFileView(message, true);
                case VOICE -> assemblyVoiceView((VoiceContent) message.nearMessageContent());
                case VIDEO -> assemblyVideoView((VideoContent) message.nearMessageContent());
                default -> assemblyUnDefView();
            };
        }


        if (nearView != null) {
            binding.nearFrameLayout.removeAllViews();
            binding.nearFrameLayout.addView(nearView);
        } else {
            binding.nearFrameLayout.setVisibility(View.INVISIBLE);
        }
    }

//    private void initAvatarIcon(String url) {
//        if (URLUtil.isValidUrl(url)) {
//            Glide.with(getContext())
//                    .load(url)
//                    .apply(new RequestOptions()
//                            .placeholder(R.drawable.default_avatar)
//                            .error(R.drawable.default_avatar)
//                            .fitCenter())
//                    .into(avatarCIV);
//        }
//    }


    /**
     * 組裝文字訊息
     */
    @SuppressLint("SetTextI18n")
    private View initRetractView(MessageEntity message) {
        View child = LayoutInflater.from(getContext()).inflate(R.layout.item_msg_tip, binding.underFrameLayout, false);
//        View separationLineV = child.findViewById(R.id.separationLineV);
        TextView tvTip = child.findViewById(R.id.msgkit_tip_content);
        tvTip.setTextColor(0xFF5E5E5E);
        tvTip.setBackgroundResource(R.drawable.sys_msg_bg);
        if (!userId.equals(message.getSenderId())) {
            tvTip.setText(message.getSenderName() + "收回訊息");
        } else {
            tvTip.setText(SdkLib.getAppContext().getString(R.string.text_you_retract_message));
        }
        return child;
    }

    /**
     * 組裝文字訊息
     */
    private View assemblyTextView(TextContent textContent, boolean needKeyword, Type type) {
        String content = textContent.getText();
        if ("%{}%".equals(content)) {
            content = "{}";
        }
        View child = LayoutInflater.from(getContext()).inflate(R.layout.msgkit_text, Type.UNDER.equals(type) ? binding.underFrameLayout : binding.nearFrameLayout, false);
        ((ChatTextView) child.findViewById(R.id.contentCTV)).setMaxLines(Integer.MAX_VALUE);
        if (needKeyword) {
            ((ChatTextView) child.findViewById(R.id.contentCTV)).setText(KeyWordHelper.matcherSearchBackground(0xFFFFF039, content, getKeyword()), TextView.BufferType.NORMAL);
        } else {
            ((ChatTextView) child.findViewById(R.id.contentCTV)).setText(content);
        }
        return child;
    }

    /**
     * 組裝[標註]訊息
     */
    private View assemblyMentionView(AtContent atContent, boolean needKeyword) {
        List<MentionContent> ceMentions = atContent.getMentionContents();
        SpannableStringBuilder builder = AtMatcherHelper.matcherAtUsers("@", ceMentions, getMembersTable());
        View child = LayoutInflater.from(getContext()).inflate(R.layout.msgkit_text, binding.nearFrameLayout, false);
        ((ChatTextView) child.findViewById(R.id.contentCTV)).setText(builder);
        return child;
    }

    /**
     * 組裝[圖片]訊息
     */
    private View assemblyImageView(ImageContent imageContent) {
        try {
            View child = LayoutInflater.from(getContext()).inflate(R.layout.msgkit_image, binding.nearFrameLayout, false);
            ShapeableImageView image = child.findViewById(R.id.photoRIV);
            String thumbnailUrl = imageContent.getThumbnailUrl();

            if (thumbnailUrl.endsWith(".gif") && !thumbnailUrl.startsWith("http")) { // EVAN_FLAG 2019-11-16 如果是本地上傳gif圖片
                File file = new File(thumbnailUrl);
                Glide.with(getContext())
                    .asGif()
                    .load(file)
                    .apply(new RequestOptions()
                        .override(SMALL_PIC_SIZE)
                        .placeholder(R.drawable.file_msg_down_bg)
                        .error(isGreenTheme ? R.drawable.image_load_error_green : R.drawable.image_load_error)
                        .fitCenter())
                    .into(image);
            } else if (thumbnailUrl.startsWith("smallandroid") && !thumbnailUrl.startsWith("http")) { // EVAN_FLAG 2019-11-16 如果是本地已經緩存圖片
                ImageEntity image2 = DaVinci.with().getImageLoader().getImage(thumbnailUrl);
                Glide.with(getContext())
                    .load(image2.getBitmap())
                    .apply(new RequestOptions()
                        .override(SMALL_PIC_SIZE)
                        .placeholder(R.drawable.file_msg_down_bg)
                        .error(isGreenTheme ? R.drawable.image_load_error_green : R.drawable.image_load_error)
                        .fitCenter())
                    .into(image);
            } else if (!URLUtil.isValidUrl(thumbnailUrl) && thumbnailUrl.startsWith("http")) { // EVAN_FLAG 2019-11-16 如果縮略圖url 是空或null
                Glide.with(getContext())
                    .load(isGreenTheme ? R.drawable.image_load_error_green : R.drawable.image_load_error)
                    .apply(new RequestOptions()
                        .override(100)
                        .fitCenter())
                    .into(image);
            } else if (URLUtil.isValidUrl(thumbnailUrl) && thumbnailUrl.startsWith("http")) { // EVAN_FLAG 2019-11-16 如果是讀取遠端url圖片
                Glide.with(getContext())
                    .load(thumbnailUrl)
                    .apply(new RequestOptions()
                        .override(SMALL_PIC_SIZE)
                        .placeholder(R.drawable.file_msg_down_bg)
                        .error(isGreenTheme ? R.drawable.image_load_error_green : R.drawable.image_load_error)
                        .fitCenter())
                    .into(image);
            }

            image.setOnClickListener(v -> {
                MessageEntity msg = getMessage();
                getContext().startActivity(new Intent(getContext(), PhotoGalleryActivity.class)
                    .putExtra(BundleKey.PHOTO_GALLERY_MESSAGE.key(), msg));
            });

            image.setOnClickListener(v -> {
                if (itemMsgBubbleBinding.checkBox.getVisibility() == View.VISIBLE) {
                    boolean isCheck = itemMsgBubbleBinding.checkBox.isChecked();
                    itemMsgBubbleBinding.checkBox.setChecked(!isCheck);
                } else {
                    getContext().startActivity(new Intent(getContext(), PhotoGalleryActivity.class)
                        .putExtra(BundleKey.PHOTO_GALLERY_MESSAGE.key(), getMessage()));
                }
            });
            return child;
        } catch (Exception ignored) {
        }
        return null;
    }


    /**
     * 組裝[貼圖]訊息
     */
    private View assemblyStickerView(StickerContent stickerContent, Type type) {
        View child = LayoutInflater.from(getContext()).inflate(R.layout.msgkit_sticker, Type.UNDER.equals(type) ? binding.underFrameLayout : binding.nearFrameLayout, false);
        GifImageView emoticons = child.findViewById(R.id.msg_emoticons);
        String packageId = stickerContent.getPackageId();
        String stickerId = stickerContent.getId();
        emoticons.setVisibility(View.VISIBLE);
        StickerService.postSticker(getContext(), packageId, stickerId, StickerDownloadRequest.Type.PICTURE, new ServiceCallBack<>() {
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
//        } else if (!Strings.isNullOrEmpty(stickerId)) {
//            try {
//                InputStream stream = getContext().getAssets().open("emoticons/qbi/" + stickerId);
//                GifDrawable gifFromAssets = new GifDrawable(stream);
//                emoticons.setImageDrawable(gifFromAssets);
//            } catch (IOException e) {
//                CELog.e(e.getMessage());
//                emoticons.setImageResource(R.drawable.image_load_error);
//            }
//
//        }
        return child;
    }

    /**
     * 組裝[檔案]訊息
     *
     * @param message
     * @return
     * @version 1.9.1
     */
    @SuppressLint("SetTextI18n")
    private View assemblyFileView(MessageEntity message, boolean isNear) {
        View child = LayoutInflater.from(getContext()).inflate(R.layout.msgkit_file, binding.nearFrameLayout, false);
        ImageView iconImage = child.findViewById(R.id.fileIconCIV);
        TextView fileName = child.findViewById(R.id.fileNameTV);
        TextView fileTypeNameTV = child.findViewById(R.id.fileTypeNameTV);
        TextView fileSize = child.findViewById(R.id.fileSizeTV);
        TextView tvSign = child.findViewById(R.id.fileStatusTV);

        message = MessageReference.findById(message.getId());
//        FileMsgFormat format = null;
        FileContent fileContent;
        if (isNear) {
            assert message != null;
            fileContent = (FileContent) message.nearMessageContent();
        } else {
            assert message != null;
            fileContent = (FileContent) message.content();
        }


        String fileTyle = FileHelper.getFileTyle(fileContent.getName());
        FileType fileType = FileType.of(fileTyle);

        MessageStatus status = message.getStatus();

        fileTypeNameTV.setText(fileType.getName());
        String size = formatSizeDisplay(fileContent.getSize());
        iconImage.setImageResource(fileType.getDrawable());
//        iconImage.setBorder(0, fileType.getDrawable());
        tvSign.setText("");


        fileName.setText(fileContent.getName());
        fileSize.setText(size);

        File file = null;
        if (!Strings.isNullOrEmpty(fileContent.getAndroid_local_path())) {
            file = new File(fileContent.getAndroid_local_path());
        }
        String downloadPath = DownloadUtil.INSTANCE.getDownloadFileDir() + message.getSendTime() + "_" + fileContent.getName();
        File downloadFile = new File(downloadPath);

        if (isNear) {
            if (this.userId.equals(message.getNearMessageSenderId())) {
                switch (Objects.requireNonNull(status)) {
                    case READ:
                    case RECEIVED:
                    case SUCCESS:
                        tvSign.setText("is me 2 已送出");
                        if (file != null && !file.exists() && !downloadFile.exists()) {
                            tvSign.setText("is me 2 未下載");
                        }
                        break;
                    case SENDING:
                        tvSign.setText("is me 2 正在上傳");
                        break;
                    case FAILED:
                    case ERROR:
                    default:
                        tvSign.setText("is me 2 送出失敗");
                        break;
                }
            } else {
                if (downloadFile.exists()) {
                    tvSign.setText("not me 2 已下載");
                } else {
                    tvSign.setText("not me 2 未下載");
                }
            }
        } else {
            if (this.userId.equals(message.getSenderId())) {
                switch (Objects.requireNonNull(status)) {
                    case READ:
                    case RECEIVED:
                    case SUCCESS:
                        tvSign.setText("is me 已送出");
                        if (file != null && !file.exists() && !downloadFile.exists()) {
                            tvSign.setText("is me 未下載");
                        }
                        break;
                    case SENDING:
                        tvSign.setText("is me 正在上傳");
                        break;
                    case FAILED:
                    case ERROR:
                    default:
                        tvSign.setText("is me 送出失敗");
                        break;
                }
            } else {
                if (downloadFile.exists()) {
                    tvSign.setText("not me 已下載");
                } else {
                    tvSign.setText("not me 未下載");
                }
            }
        }

        tvSign.setText("");
        return child;
    }

    /**
     * 組裝[音頻]訊息
     */
    private View assemblyVoiceView(VoiceContent voiceContent) {
        View child = LayoutInflater.from(getContext()).inflate(R.layout.msgkit_voice, binding.nearFrameLayout, false);
        TextView voiceContentRight = child.findViewById(R.id.msg_voice_right_content);
        TextView voiceContentLeft = child.findViewById(R.id.msg_voice_left_content);
        ImageView ivVoiceDot = child.findViewById(R.id.message_voice_dot);
        TextView voiceView;
        if (isRightMessage()) {
            voiceContentRight.setVisibility(View.VISIBLE);
            voiceContentLeft.setVisibility(View.GONE);
            ivVoiceDot.setVisibility(View.INVISIBLE);
            voiceView = voiceContentRight;
            voiceContent.setRead(true);
        } else {
            voiceContentRight.setVisibility(View.GONE);
            voiceContentLeft.setVisibility(View.VISIBLE);
            voiceView = voiceContentLeft;
            if (voiceContent.isRead()) {
                ivVoiceDot.setVisibility(View.INVISIBLE);
            } else {
                ivVoiceDot.setVisibility(View.VISIBLE);
            }
        }
//        voiceView.setText(getVoiceDisplay(isRightMessage(), voiceContent.getDuration()));
        voiceView.setText(VoiceHelper.strDuration(voiceContent.getDuration() * 1000));
        return child;
    }

    /**
     * 組裝[影音]訊息
     */
    @SuppressLint("MissingInflatedId")
    private View assemblyVideoView(VideoContent videoContent) {
        View child = LayoutInflater.from(getContext()).inflate(R.layout.msgkit_video, binding.nearFrameLayout, false);
        RoundImageView thumbnailRIV = child.findViewById(R.id.thumbnailRIV);
        VideoView reviewVV = child.findViewById(R.id.reviewVV);
        reviewVV.setVisibility(View.GONE);
        ImageView playIV = child.findViewById(R.id.playIV);
        String videoUrl = videoContent.getUrl();
        if (URLUtil.isValidUrl(videoUrl) || !videoUrl.endsWith(".mp4")) {
            // 視頻縮略圖
            try {
                Glide.with(getContext())
                    .load(videoUrl)
                    .apply(new RequestOptions()
                        .frame(1000)
                        .override(SMALL_PIC_SIZE)
                        .error(isGreenTheme ? R.drawable.image_load_error_green : R.drawable.image_load_error)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .fitCenter())
                    .into(thumbnailRIV);
            } catch (Exception ignored) {
            }
        } else {
            playIV.setVisibility(View.GONE);
            try {
                Glide.with(getContext())
                    .load(isGreenTheme ? R.drawable.image_load_error_green : R.drawable.image_load_error)
                    .apply(new RequestOptions().override(SMALL_PIC_SIZE))
                    .into(thumbnailRIV);
            } catch (Exception ignored) {
            }
        }
        return child;
    }

    /**
     * 組裝[未知]訊息
     */
    public View assemblyUnDefView() {
        View child = LayoutInflater.from(getContext()).inflate(R.layout.item_msg_tip, binding.nearFrameLayout, false);
        TextView tvTip = child.findViewById(R.id.msgkit_tip_content);
        tvTip.setBackgroundResource(R.drawable.sys_msg_bg);
        tvTip.setTextColor(Color.WHITE);
        tvTip.setText("暫不支持此消息類型");
        return null;
    }

    @Override
    protected boolean showName() {
        return !isRightMessage();
    }

//    @Override
//    protected void onBubbleClicked() {
////        if ((System.currentTimeMillis() - exitTime) > 2000) {
////            exitTime = System.currentTimeMillis();
////        } else {
//        onListDilogItemClickListener.showRePlyPanel(super.message);
////        }
//    }

    private void showPopup(int pressX, int pressY) {
        final MessageEntity msg = getMessage();
        if (this.onMessageControlEventListener != null) {
            this.onMessageControlEventListener.onLongClick(msg, pressX, pressY);
        }

        /*final List<String> popupMenuItemList = new ArrayList<>();
        popupMenuItemList.clear();
        if (message.getStatus() == MessageStatus.FAILED) {
            popupMenuItemList.add(getContext().getString(R.string.retry));
        } else {
            popupMenuItemList.add(getContext().getString(R.string.transpond));

        }
        if (message.getSenderId().equals(UserPref.getInstance(getContext()).getUserId())) {
            int value = message.getStatus().getValue();
            ISession iSession = DBManager.getInstance().querySession(message.getSessionId());
            if (value > 0 && value != 2 && iSession.getTodoOverviewType() != SessionType.SERVICES
                    && iSession.getTodoOverviewType() != SessionType.SUBSCRIBE) {
                //TODO 1.5.3 隐藏撤回
                popupMenuItemList.add(getContext().getString(R.string.retractMsg));
            }
        }
        popupMenuItemList.add(getContext().getString(R.string.copy));
        popupMenuItemList.add(getContext().getString(R.string.share));
        popupMenuItemList.add(getContext().getString(R.string.item_del));
        PopupList popupList = new PopupList(view.getContext());
        popupList.showPopupListWindow(view, pressX, pressY, popupMenuItemList, new PopupList.PopupListListener() {
            @Override
            public boolean showPopupList(View adapterView, View contextView) {
                return true;
            }

            @Override
            public void onPopupListClick(View contextView, int position) {
                String s = popupMenuItemList.get(position);
                switch (s) {
                    case "複製":
                        TextMsgFormat format = (TextMsgFormat) message.getFormat();
                        onListDilogItemClickListener.copyText(format.getContent());
                        break;
                    case "轉發":
                        onListDilogItemClickListener.tranSend(message);
                        break;
                    case "撤回":
                        onListDilogItemClickListener.retractMsg(message);
                        break;
                    case "重發":
                        onListDilogItemClickListener.retry(message);
                        break;
                    case "分享":
//                        onListDilogItemClickListener.shares(message, tvContent);
                        break;
                    case "刪除":
                        onListDilogItemClickListener.delete(message);
                        break;
                }
            }
        });*/
    }
//
//    @Override
//    protected boolean onBubbleLongClicked(float pressX, float pressY) {
//        showPopup((int) pressX, (int) pressY);
//        return true;
//    }


    @Override
    public void onClick(View v, MessageEntity message) {
        super.onClick(v, message);
        if (this.onMessageControlEventListener != null) {
            this.onMessageControlEventListener.showRePlyPanel(super.getMessage());
        }
    }

    @Override
    public void onDoubleClick(View v, MessageEntity message) {
//        onListDilogItemClickListener.enLarge(message);
    }

    @Override
    public void onLongClick(View v, float x, float y, MessageEntity message) {
        showPopup((int) x, (int) y);
    }

}
