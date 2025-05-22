package tw.com.chainsea.chat.messagekit.main.viewholder;

import static tw.com.chainsea.chat.messagekit.main.viewholder.FileMessageView.formatSizeDisplay;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.common.base.Strings;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import tw.com.chainsea.chat.util.DaVinci;
import cn.hadcn.davinci.image.base.ImageEntity;
import pl.droidsonroids.gif.GifImageView;
import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.file.FileHelper;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.android.common.text.KeyWordHelper;
import tw.com.chainsea.android.common.video.VideoHelper;
import tw.com.chainsea.android.common.voice.VoiceHelper;
import tw.com.chainsea.ce.sdk.SdkLib;
import tw.com.chainsea.ce.sdk.bean.FacebookContentTypes;
import tw.com.chainsea.ce.sdk.bean.FacebookTag;
import tw.com.chainsea.ce.sdk.bean.PicSize;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.msg.ChannelType;
import tw.com.chainsea.ce.sdk.bean.msg.FacebookCommentStatus;
import tw.com.chainsea.ce.sdk.bean.msg.FacebookPostStatus;
import tw.com.chainsea.ce.sdk.bean.msg.MessageFlag;
import tw.com.chainsea.ce.sdk.bean.msg.MessageStatus;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;
import tw.com.chainsea.ce.sdk.bean.msg.TemplateContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.Action;
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
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.http.ce.request.StickerDownloadRequest;
import tw.com.chainsea.ce.sdk.reference.MessageReference;
import tw.com.chainsea.ce.sdk.service.AvatarService;
import tw.com.chainsea.ce.sdk.service.StickerService;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.databinding.ItemFacebookReplyBinding;
import tw.com.chainsea.chat.databinding.MsgkitReplyBinding;
import tw.com.chainsea.chat.databinding.MsgkitTemplateBinding;
import tw.com.chainsea.chat.keyboard.ChatTextView;
import tw.com.chainsea.chat.lib.AtMatcherHelper;
import tw.com.chainsea.chat.messagekit.enums.FileType;
import tw.com.chainsea.chat.messagekit.listener.TextViewLinkClickListener;
import tw.com.chainsea.chat.messagekit.main.viewholder.base.MessageBubbleView;
import tw.com.chainsea.chat.util.DownloadUtil;
import tw.com.chainsea.chat.util.UrlTextUtil;
import tw.com.chainsea.chat.view.gallery.PhotoGalleryActivity;
import tw.com.chainsea.chat.widget.LoadingBar;
import tw.com.chainsea.custom.view.image.RoundImageView;


/**
 * text message view
 * Created by 90Chris on 2016/4/20.
 */
public class ReplyMessageView extends MessageBubbleView<IMessageContent> {
    String DOWNLOAD_DIR = DownloadUtil.INSTANCE.getDownloadFileDir();
    private final MsgkitReplyBinding binding;
    final static int SMALL_PIC_SIZE = 360;

    enum Type {
        NEAR,
        UNDER
    }

    public ReplyMessageView(@NonNull ViewBinding binding) {
        super(binding);
        this.binding = MsgkitReplyBinding.inflate(LayoutInflater.from(itemView.getContext()), null, false);
        getView(this.binding.getRoot());
    }

    @Override
    protected int getContentResId() {
        return R.layout.msgkit_reply;
    }

    @Override
    protected View getChildView() {
        return binding.getRoot();
    }

    @Override
    protected void bindContentView(IMessageContent iMessageContent) {
        MessageEntity message = getMessage();
        MessageType messageType = message.getType();
        MessageEntity nearMessage = MessageReference.findById(message.getNearMessageId());
        MessageType nearMessageType;
        IMessageContent content;
        if (nearMessage == null) {
            nearMessageType = message.getNearMessageType();
            assert nearMessageType != null;
            content = nearMessageType.from(message.getNearMessageContent());
        } else {
            nearMessageType = nearMessage.getType();
            content = nearMessage.content();
        }


        View underView = null;
        if (MessageFlag.RETRACT.equals(message.getFlag())) {
            view = initRetractView(message); // EVAN_FLAG 2020-02-13 (1.9.1) 該訊息被收回
        } else if (messageType == null) {
            underView = assemblyUnDefView();
        } else {
            underView = switch (messageType) {
                case TEXT -> assemblyTextView((TextContent) message.content(), true, Type.UNDER, nearMessage != null && nearMessage.getFrom() == ChannelType.FB);
                case AT -> assemblyMentionView((AtContent) message.content(), true, Type.UNDER);
                case IMAGE -> assemblyImageView((ImageContent) message.content(), Type.UNDER, nearMessage != null && nearMessage.getFrom() == ChannelType.FB);
                case STICKER -> assemblyStickerView((StickerContent) message.content(), Type.UNDER);
                case FILE -> assemblyFileView(message, nearMessage, false, Type.UNDER);
                case VOICE -> assemblyVoiceView((VoiceContent) message.content(), Type.UNDER);
                case VIDEO -> assemblyVideoView((VideoContent) message.content(), Type.UNDER);
                default -> assemblyUnDefView();
            };
        }

        if (underView != null) {
            binding.underFrameLayout.setVisibility(View.VISIBLE);
            binding.underFrameLayout.removeAllViews();
            binding.underFrameLayout.addView(underView);
        } else {
            binding.underFrameLayout.setVisibility(View.INVISIBLE);
        }


        View nearView;

        if (nearMessageType == null) {
            nearView = assemblyUnDefView();
        } else {
            switch (nearMessageType) {
                case TEXT:
                    nearView = assemblyTextView((TextContent) content, false, Type.NEAR, false);
                    break;
                case AT:
                    nearView = assemblyMentionView((AtContent) content, false, Type.NEAR);
                    break;
                case IMAGE:
                    nearView = assemblyImageView((ImageContent) content, Type.NEAR, false);
                    break;
                case STICKER:
                    nearView = assemblyStickerView((StickerContent) content, Type.NEAR);
                    break;
                case FILE:
                    nearView = assemblyFileView(message, nearMessage, true, Type.NEAR);
                    break;
                case VOICE:
                    nearView = assemblyVoiceView((VoiceContent) content, Type.NEAR);
                    break;
                case VIDEO:
                    nearView = assemblyVideoView((VideoContent) content, Type.NEAR);
                    break;
                case TEMPLATE:
                    if (nearMessage != null && nearMessage.getFrom() == ChannelType.FB) {
                        nearView = assemblyFacebookCommentView((TemplateContent) content);
                    } else {
                        nearView = assemblyTemplateView((TemplateContent) content);
                    }
                    break;
                default:
                    nearView = assemblyUnDefView();
                    break;
            }
        }


        if (nearView != null) {
            binding.nearFrameLayout.setVisibility(View.VISIBLE);
            binding.nearFrameLayout.removeAllViews();
            binding.nearFrameLayout.addView(nearView);
        } else {
            binding.nearFrameLayout.setVisibility(View.INVISIBLE);
        }

        View avatarView = null;
        if (isAnonymous) {
            avatarView = LayoutInflater.from(binding.getRoot().getContext()).inflate(R.layout.view_reply_message_avatar, binding.layoutAvatar, false);
            AvatarIcon avatarCIV = avatarView.findViewById(R.id.avatarCIV);
            avatarCIV.setImageResource(getDomino(message.getNearMessageSenderId()).getResId());
        } else {
            if (ChatRoomType.subscribe.equals(chatRoomEntity.getType())) {
                avatarView = LayoutInflater.from(binding.getRoot().getContext()).inflate(R.layout.view_reply_message_avatar, binding.layoutAvatar, false);
                AvatarIcon avatarCIV = avatarView.findViewById(R.id.avatarCIV);
                if (!userId.equals(message.getNearMessageSenderId())) {
                    AvatarService.post(avatarCIV.getContext(), chatRoomEntity.getServiceNumberAvatarId(), PicSize.SMALL, avatarCIV, R.drawable.custom_default_avatar);
                } else {
                    AvatarService.post(avatarCIV.getContext(), message.getNearMessageAvatarId(), PicSize.SMALL, avatarCIV, R.drawable.custom_default_avatar);
                }
            } else {
                try {
                    avatarView = LayoutInflater.from(binding.getRoot().getContext()).inflate(R.layout.view_reply_message_avatar, binding.layoutAvatar, false);
                    AvatarIcon avatarIcon = avatarView.findViewById(R.id.avatarCIV);
                    if (message.getNearMessageSenderId() != null && !message.getNearMessageSenderId().isEmpty()) {
                        UserProfileEntity userProfileEntity = DBManager.getInstance().queryUser(message.getNearMessageSenderId());
                        if (userProfileEntity != null) {
                            avatarIcon.loadAvatarIcon(userProfileEntity.getAvatarId(), userProfileEntity.getNickName(), userProfileEntity.getId());
                        }
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
        binding.llNearMessage.setOnClickListener(view -> {
            if (onMessageControlEventListener != null) {
                onMessageControlEventListener.findReplyMessage(getMessage().getNearMessageId());
            }
        });

        binding.ivReply.setOnClickListener(view -> {
            if (onMessageControlEventListener != null) {
                onMessageControlEventListener.showRePlyPanel(getMessage());
            }
        });

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
        View child = LayoutInflater.from(binding.getRoot().getContext()).inflate(R.layout.item_msg_tip, binding.underFrameLayout, false);
//        View separationLineV = child.findViewById(R.id.separationLineV);
        TextView tvTip = child.findViewById(R.id.msgkit_tip_content);
        tvTip.setTextColor(0xFF5E5E5E);
        tvTip.setBackgroundResource(R.drawable.sys_msg_bg);
        if (!userId.equals(message.getSenderId())) {
            tvTip.setText(message.getSenderName() + "已收回訊息");
        } else {
            tvTip.setText(SdkLib.getAppContext().getString(R.string.text_you_retract_message));
        }
        return child;
    }

    /**
     * 組裝文字訊息
     */
    private View assemblyTextView(TextContent textContent, boolean needKeyword, Type type, boolean isFacebookReply) {
        String content = textContent.getText();
        if ("%{}%".equals(content)) {
            content = "{}";
        }
        View child = LayoutInflater.from(binding.getRoot().getContext()).inflate(R.layout.msgkit_text, Type.UNDER.equals(type) ? binding.underFrameLayout : binding.nearFrameLayout, false);
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
    private View assemblyMentionView(AtContent atContent, boolean needKeyword, Type type) {
        List<MentionContent> ceMentions = atContent.getMentionContents();
        SpannableStringBuilder builder = AtMatcherHelper.matcherAtUsers("@", ceMentions, getMembersTable());
        View child = LayoutInflater.from(binding.getRoot().getContext()).inflate(R.layout.msgkit_text, Type.UNDER.equals(type) ? binding.underFrameLayout : binding.nearFrameLayout, false);
        ((ChatTextView) child.findViewById(R.id.contentCTV)).setText(builder);
        return child;
    }

    /**
     * 組裝[圖片]訊息
     */
    private View assemblyImageView(ImageContent imageContent, Type type, boolean isFacebookReply) {
        View child = LayoutInflater.from(binding.getRoot().getContext()).inflate(R.layout.msgkit_image, Type.UNDER.equals(type) ? binding.underFrameLayout : binding.nearFrameLayout, false);
        ShapeableImageView image = child.findViewById(R.id.photoRIV);
        String thumbnailUrl = imageContent.getThumbnailUrl();
        if (thumbnailUrl.endsWith(".gif") && !thumbnailUrl.startsWith("http")) { // EVAN_FLAG 2019-11-16 如果是本地上傳gif圖片
            File file = new File(thumbnailUrl);
            Glide.with(image)
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
            Glide.with(image)
                .load(image2.getBitmap())
                .apply(new RequestOptions()
                    .override(SMALL_PIC_SIZE)
                    .placeholder(R.drawable.file_msg_down_bg)
                    .error(isGreenTheme ? R.drawable.image_load_error_green : R.drawable.image_load_error)
                    .fitCenter())
                .into(image);
        } else if (!URLUtil.isValidUrl(thumbnailUrl) && thumbnailUrl.startsWith("http")) { // EVAN_FLAG 2019-11-16 如果縮略圖url 是空或null
            Glide.with(image)
                .load(isGreenTheme ? R.drawable.image_load_error_green : R.drawable.image_load_error)
                .apply(new RequestOptions()
                    .override(100)
                    .fitCenter())
                .into(image);
        } else {
            Glide.with(image).load(thumbnailUrl).apply(new RequestOptions()
                    .override(SMALL_PIC_SIZE)
                    .placeholder(R.drawable.file_msg_down_bg)
                    .error(isGreenTheme ? R.drawable.image_load_error_green : R.drawable.image_load_error)
                    .fitCenter())
                .into(image);
        }

        image.setOnClickListener(v -> {
            MessageEntity msg = getMessage();
            binding.getRoot().getContext().startActivity(new Intent(binding.getRoot().getContext(), PhotoGalleryActivity.class)
                .putExtra(BundleKey.PHOTO_GALLERY_MESSAGE.key(), msg));
        });

        image.setOnClickListener(v -> {
            if (checkBox.getVisibility() == View.VISIBLE) {
                boolean isCheck = checkBox.isChecked();
                checkBox.setChecked(!isCheck);
            } else {
                binding.getRoot().getContext().startActivity(new Intent(binding.getRoot().getContext(), PhotoGalleryActivity.class)
                    .putExtra(BundleKey.PHOTO_GALLERY_MESSAGE.key(), getMessage()));
            }
        });


        return child;
    }


    /**
     * 組裝[貼圖]訊息
     */
    private View assemblyStickerView(StickerContent stickerContent, Type type) {
        View child = LayoutInflater.from(binding.getRoot().getContext()).inflate(R.layout.msgkit_sticker, Type.UNDER.equals(type) ? binding.underFrameLayout : binding.nearFrameLayout, false);
        GifImageView emoticons = child.findViewById(R.id.msg_emoticons);
        String packageId = stickerContent.getPackageId();
        String stickerId = stickerContent.getId();
        emoticons.setVisibility(View.VISIBLE);
        StickerService.postSticker(binding.getRoot().getContext(), packageId, stickerId, StickerDownloadRequest.Type.PICTURE, new ServiceCallBack<>() {
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
     */
    @SuppressLint("SetTextI18n")
    private View assemblyFileView(MessageEntity message, MessageEntity nearMessage, boolean isNear, Type type) {
        View child = LayoutInflater.from(binding.getRoot().getContext()).inflate(R.layout.msgkit_file, Type.UNDER.equals(type) ? binding.underFrameLayout : binding.nearFrameLayout, false);
        ImageView iconImage = child.findViewById(R.id.fileIconCIV);
        TextView fileName = child.findViewById(R.id.fileNameTV);
        TextView fileTypeNameTV = child.findViewById(R.id.fileTypeNameTV);
        TextView fileSize = child.findViewById(R.id.fileSizeTV);
        TextView tvSign = child.findViewById(R.id.fileStatusTV);

        message = MessageReference.findById(message.getId());
//        FileMsgFormat format = null;
        FileContent fileContent;
        if (isNear) {
            if (nearMessage == null) {
                assert message != null;
                fileContent = (FileContent) MessageType.FILE.from(message.getNearMessageContent());
            } else {
                fileContent = (FileContent) nearMessage.content();
            }
        } else {
            assert message != null;
            fileContent = (FileContent) message.content();
        }


        String fileTyle = FileHelper.getFileTyle(fileContent.getName());
        FileType fileType = FileType.of(fileTyle);

        assert message != null;
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
        String downloadPath = DOWNLOAD_DIR + message.getSendTime() + "_" + fileContent.getName();
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
    private View assemblyVoiceView(VoiceContent voiceContent, Type type) {
        View child = LayoutInflater.from(binding.getRoot().getContext()).inflate(R.layout.msgkit_voice, Type.UNDER.equals(type) ? binding.underFrameLayout : binding.nearFrameLayout, false);
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
    private View assemblyVideoView(VideoContent videoContent, Type type) {
        View child = LayoutInflater.from(binding.getRoot().getContext()).inflate(R.layout.msgkit_video, Type.UNDER.equals(type) ? binding.underFrameLayout : binding.nearFrameLayout, false);
        ConstraintLayout occupationCL = child.findViewById(R.id.occupationCL);
        ConstraintLayout clPlay = child.findViewById(R.id.clPlay);
        ImageView playIV = child.findViewById(R.id.playIV);
        TextView tvPeriod = child.findViewById(R.id.tvPeriod);
        RoundImageView thumbnailRIV = child.findViewById(R.id.thumbnailRIV);
        LoadingBar loadingBar = child.findViewById(R.id.progressBar);
        ViewGroup.LayoutParams params = occupationCL.getLayoutParams();
        VideoView reviewVV = child.findViewById(R.id.reviewVV);
        if (reviewVV != null) {
            reviewVV.setVisibility(View.GONE);
        }
        int[] zoomSize = getZoomSize(videoContent, type);
        // 如果沒高度寬度 給預設
        if (zoomSize[0] <= 0) zoomSize[0] = 300;
        if (zoomSize[1] <= 0) zoomSize[1] = 600;
        params.width = zoomSize[0];
        params.height = zoomSize[1];
        thumbnailRIV.getLayoutParams().width = zoomSize[0];
        thumbnailRIV.getLayoutParams().height = zoomSize[1];
        occupationCL.setLayoutParams(params);
        setPreviewImage(clPlay, playIV, tvPeriod, thumbnailRIV, videoContent, type);

        occupationCL.setOnClickListener(view -> downloadVideo(clPlay, playIV, tvPeriod, thumbnailRIV, videoContent, loadingBar, type));
        return child;
    }

    private void downloadVideo(ConstraintLayout clPlay, ImageView playIV, TextView tvPeriod, RoundImageView thumbnailRIV,
                               VideoContent videoContent, LoadingBar progressBar, Type type) {
        String downloadPath = DownloadUtil.INSTANCE.getDownloadFileDir() + getMessage().getSendTime() + "_" + videoContent.getName();
        File downloadFile = new File(downloadPath);
        File localPath = null;
        if (videoContent.getAndroid_local_path() != null && !videoContent.getAndroid_local_path().isEmpty()) {
            localPath = new File(videoContent.getAndroid_local_path());
        }
        if (downloadFile.exists() || (localPath != null && localPath.exists())) {
            onMessageControlEventListener.onVideoClick(getMessage());
        } else {
            ThreadExecutorHelper.getIoThreadExecutor().execute(() -> {
                try {
                    DownloadUtil.INSTANCE.doDownloadVideoFileForJava(
                        videoContent,
                        downloadPath,
                        TokenPref.getInstance(binding.getRoot().getContext()).getTokenId(),
                        progress -> {
                            ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                                progressBar.setProgress(progress);
                                if (progressBar.getProgress() >= 100) {
                                    progressBar.setVisibility(View.GONE);
                                    clPlay.setVisibility(View.VISIBLE);
                                    tvPeriod.setVisibility(View.VISIBLE);
                                } else {
                                    progressBar.setVisibility(View.VISIBLE);
                                    clPlay.setVisibility(View.GONE);
                                    tvPeriod.setVisibility(View.GONE);
                                }
                            });
                            return null;
                        },
                        file -> {
                            handleDownloadSuccess(videoContent, file, type);
                            setPreviewImage(clPlay, playIV, tvPeriod, thumbnailRIV, videoContent, type);
                            return null;
                        },
                        errorMessage -> {
                            deleteFile(downloadPath);
                            return null;
                        }
                    );
                } catch (Exception e) {
                    CELog.e(e.getMessage());
                }
            });
        }
    }

    private void handleDownloadSuccess(VideoContent videoContent, File file, Type type) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(file));
        binding.getRoot().getContext().sendBroadcast(intent);
        videoContent.setDownload(true);
        videoContent.setProgress("100");
        onMessageControlEventListener.onContentUpdate(getMessage().getId(), videoContent.getClass().getName(), videoContent.toStringContent());
        String messageId;
        if (type == Type.NEAR) {
            messageId = getMessage().getNearMessageId();
        } else {
            messageId = getMessage().getId();
        }
        onMessageControlEventListener.updateReplyMessageWhenVideoDownload(messageId);
    }

    private void deleteFile(String path) {
        try {
            File file = new File(path);
            file.delete();
        } catch (Exception ignored) {
        }
    }

    private int[] getZoomSize(VideoContent videoContent, Type type) {
        if (videoContent.getWidth() != 0 && videoContent.getHeight() != 0) {
            return zoomImage(videoContent.getWidth(), videoContent.getHeight());
        } else if (videoContent.getThumbnailHeight() != 0 && videoContent.getThumbnailWidth() != 0) {
            return zoomImage(videoContent.getThumbnailWidth(), videoContent.getThumbnailHeight());
        } else {
            File file = getVideoFile(videoContent, type);
            int[] size;
            if (file == null) {
                size = VideoHelper.size(videoContent.getUrl());
            } else {
                size = VideoHelper.size(file.getAbsolutePath());
            }
            if (size[0] > 0 && size[1] > 0) {
                return zoomImage(size[0], size[1]);
            } else {
                return size;
            }
        }
    }

    private int[] zoomImage(int inWidth, int inHeight) {
        if (inWidth > inHeight) {
            return new int[]{SMALL_PIC_SIZE, (inHeight * SMALL_PIC_SIZE) / inWidth};
        } else {
            return new int[]{((inWidth * SMALL_PIC_SIZE) / inHeight), SMALL_PIC_SIZE};
        }
    }

    private File getVideoFile(VideoContent videoContent, Type type) {
        String messageId = "";
        if (type == Type.NEAR) {
            messageId = getMessage().getNearMessageId();
        } else if (type == Type.UNDER) {
            messageId = getMessage().getId();
        }
        MessageEntity message = MessageReference.findById(messageId);
        if (message == null) return null;
        VideoContent videoContent1 = (VideoContent) message.content();
        if (videoContent1.getAndroid_local_path() != null && new File(videoContent1.getAndroid_local_path()).exists()) {
            return new File(videoContent1.getAndroid_local_path());
        } else {
            String downloadPath = DownloadUtil.INSTANCE.getDownloadFileDir() + message.getSendTime() + "_" + videoContent.getName();
            File downloadFile = new File(downloadPath);
            if (downloadFile.exists()) {
                return downloadFile;
            }
        }
        return null;
    }

    private void setPreviewImage(ConstraintLayout clPlay, ImageView playIV, TextView tvPeriod,
                                 ImageView thumbnailRIV, VideoContent videoContent, Type type) {
        String thumbnailUrl;
        if (videoContent.getThumbnailUrl() != null && !videoContent.getThumbnailUrl().isEmpty()) {
            thumbnailUrl = videoContent.getThumbnailUrl();
        } else {
            thumbnailUrl = "";
        }
        File file = getVideoFile(videoContent, type);
        AtomicReference<RequestBuilder<Drawable>> requestBuilder = new AtomicReference<>();
        if (file != null) {
            clPlay.setVisibility(View.VISIBLE);
            playIV.setImageResource(R.drawable.play);
            tvPeriod.setText(DownloadUtil.INSTANCE.getVideoDuration(file.getAbsolutePath()));
            tvPeriod.setVisibility(View.VISIBLE);
            String videoThumbnail = thumbnailUrl.isEmpty() ? file.getAbsolutePath() : thumbnailUrl;
            requestBuilder.set(Glide.with(playIV.getContext())
                .load(videoThumbnail)
                .apply(new RequestOptions()
                    .frame(1000)
                    .override(SMALL_PIC_SIZE)
                    .fitCenter()));
        } else {
            playIV.setImageResource(R.drawable.ic_video_download);
            if (URLUtil.isValidUrl(videoContent.getUrl()) || videoContent.getUrl().endsWith(".mp4")) {
                if (!Strings.isNullOrEmpty(thumbnailUrl)) {
                    int thumbnailWidth = (videoContent.getThumbnailWidth() > 0) ? videoContent.getThumbnailWidth() : 300;
                    int thumbnailHeight = (videoContent.getThumbnailHeight() > 0) ? videoContent.getThumbnailHeight() : 600;
                    requestBuilder.set(Glide.with(playIV.getContext())
                        .load(thumbnailUrl)
                        .apply(new RequestOptions()
                            .frame(1000)
                            .override(thumbnailWidth, thumbnailHeight)
                            .fitCenter()));
                } else {
                    ThreadExecutorHelper.getIoThreadExecutor().execute(() -> {
                        try {
                            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                            mediaMetadataRetriever.setDataSource(videoContent.getUrl(), new HashMap<>());
                            Bitmap bmFrame = mediaMetadataRetriever.getFrameAtTime(1000);
                            if (bmFrame != null) {
                                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> thumbnailRIV.setImageBitmap(bmFrame));
                            }
                        } catch (Exception e) {
                            int thumbnailWidth = (videoContent.getThumbnailWidth() > 0) ? videoContent.getThumbnailWidth() : 300;
                            int thumbnailHeight = (videoContent.getThumbnailHeight() > 0) ? videoContent.getThumbnailHeight() : 600;
                            requestBuilder.set(Glide.with(playIV.getContext())
                                .load(thumbnailUrl)
                                .apply(new RequestOptions()
                                    .frame(1000)
                                    .override(thumbnailWidth, thumbnailHeight)
                                    .fitCenter()));
                        }
                    });
                }
            } else {
                clPlay.setVisibility(View.GONE);
                requestBuilder.set(Glide.with(playIV.getContext())
                    .load(isGreenTheme ? R.drawable.image_load_error_green : R.drawable.image_load_error)
                    .override(SMALL_PIC_SIZE));
            }
        }
        if (requestBuilder.get() != null) {
            requestBuilder.get().diskCacheStrategy(DiskCacheStrategy.ALL).into(thumbnailRIV);
        }
    }

    private View assemblyTemplateView(TemplateContent templateContent) {
        MsgkitTemplateBinding templateViewBinding = MsgkitTemplateBinding.inflate(LayoutInflater.from(binding.getRoot().getContext()), binding.nearFrameLayout, false);
        Glide.with(templateViewBinding.img)
            .load(templateContent.getImageUrl())
            .apply(new RequestOptions().centerCrop())
            .into(templateViewBinding.img);
        templateViewBinding.txtTitle.setText(templateContent.getTitle());
        templateViewBinding.txtContent.setText(templateContent.getText());

        Action defaultAction = templateContent.getDefaultAction();
        TemplateMessageView templateMessageView = new TemplateMessageView(templateViewBinding, true);
        TemplateMessageView.ActionClick actionClick;
        if (defaultAction != null) {
            actionClick = templateMessageView.new ActionClick(defaultAction, templateViewBinding.getRoot().getContext());
            templateViewBinding.img.setOnClickListener(actionClick);
            templateViewBinding.txtContent.setOnClickListener(actionClick);
        }

        List<Action> actions = templateContent.getActions();
        if (actions != null && !actions.isEmpty()) {
            templateViewBinding.lineTop.setVisibility(View.VISIBLE);
            String orientation = templateContent.getOrientation();
            if (Constant.Orientation.VERTICAL.equals(orientation)) {
                templateViewBinding.layoutButtonV.setVisibility(View.VISIBLE);
                templateViewBinding.layoutButtonH.setVisibility(View.GONE);
                templateViewBinding.line0V.setVisibility(View.GONE);
                templateViewBinding.line1V.setVisibility(View.GONE);
                templateViewBinding.btn0V.setVisibility(View.GONE);
                templateViewBinding.btn1V.setVisibility(View.GONE);
                templateViewBinding.btn2V.setVisibility(View.GONE);
                switch (actions.size()) {
                    case 3:
                        templateViewBinding.line1V.setVisibility(View.VISIBLE);
                        templateViewBinding.btn2V.setVisibility(View.VISIBLE);
                        templateViewBinding.btn2V.setText(actions.get(2).getLabel());
                        actionClick = templateMessageView.new ActionClick(actions.get(2), templateViewBinding.getRoot().getContext());
                        templateViewBinding.btn2V.setOnClickListener(actionClick);
                    case 2:
                        templateViewBinding.line0V.setVisibility(View.VISIBLE);
                        templateViewBinding.btn1V.setVisibility(View.VISIBLE);
                        templateViewBinding.btn1V.setText(actions.get(1).getLabel());
                        actionClick = templateMessageView.new ActionClick(actions.get(1), templateViewBinding.getRoot().getContext());
                        templateViewBinding.btn1V.setOnClickListener(actionClick);
                    case 1:
                        templateViewBinding.btn0V.setVisibility(View.VISIBLE);
                        templateViewBinding.btn0V.setText(actions.get(0).getLabel());
                        actionClick = templateMessageView.new ActionClick(actions.get(0), templateViewBinding.getRoot().getContext());
                        templateViewBinding.btn0V.setOnClickListener(actionClick);
                }
            } else {
                templateViewBinding.layoutButtonV.setVisibility(View.VISIBLE);
                templateViewBinding.layoutButtonH.setVisibility(View.GONE);
                templateViewBinding.line0H.setVisibility(View.GONE);
                templateViewBinding.line1H.setVisibility(View.GONE);
                templateViewBinding.btn0H.setVisibility(View.GONE);
                templateViewBinding.btn1H.setVisibility(View.GONE);
                templateViewBinding.btn2H.setVisibility(View.GONE);
                switch (actions.size()) {
                    case 3:
                        templateViewBinding.line1H.setVisibility(View.VISIBLE);
                        templateViewBinding.btn2H.setVisibility(View.VISIBLE);
                        templateViewBinding.btn2H.setText(actions.get(2).getLabel());
                        actionClick = templateMessageView.new ActionClick(actions.get(2), templateViewBinding.getRoot().getContext());
                        templateViewBinding.btn2H.setOnClickListener(actionClick);
                    case 2:
                        templateViewBinding.line0H.setVisibility(View.VISIBLE);
                        templateViewBinding.btn1H.setVisibility(View.VISIBLE);
                        templateViewBinding.btn1H.setText(actions.get(1).getLabel());
                        actionClick = templateMessageView.new ActionClick(actions.get(1), templateViewBinding.getRoot().getContext());
                        templateViewBinding.btn1H.setOnClickListener(actionClick);
                    case 1:
                        templateViewBinding.btn0H.setVisibility(View.VISIBLE);
                        templateViewBinding.btn0H.setText(actions.get(0).getLabel());
                        actionClick = templateMessageView.new ActionClick(actions.get(0), templateViewBinding.getRoot().getContext());
                        templateViewBinding.btn0H.setOnClickListener(actionClick);
                }
            }
        } else {
            templateViewBinding.layoutButtonH.setVisibility(View.GONE);
            templateViewBinding.layoutButtonV.setVisibility(View.GONE);
            templateViewBinding.lineTop.setVisibility(View.GONE);
        }


        return templateViewBinding.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    private View assemblyFacebookCommentView(TemplateContent templateContent) {
        ItemFacebookReplyBinding itemFacebookReplyBinding = ItemFacebookReplyBinding.inflate(LayoutInflater.from(binding.getRoot().getContext()), binding.nearFrameLayout, false);
        StringBuilder displayText = new StringBuilder();
        // 內容
        displayText.append(templateContent.getText());
        itemFacebookReplyBinding.tvOriginText.setVisibility(View.VISIBLE);

        MessageEntity nearMessage = MessageReference.findById(getMessage().getNearMessageId());
        if (nearMessage != null) {
            if (nearMessage.getTag() != null) {
                FacebookTag facebookTag = JsonHelper.getInstance().from(nearMessage.getTag(), FacebookTag.class);
                if (facebookTag != null) {
                    boolean isVideo = false;
                    boolean isImage = false;
                    boolean isLink = false;
                    String content = "";
                    String url = "";
                    for (FacebookTag.FacebookTagContent facebookTagContent : Objects.requireNonNull(facebookTag.getData().getContent())) {
                        if (facebookTagContent != null && facebookTagContent.getType() != null) {
                            isVideo = facebookTagContent.getType() == FacebookContentTypes.Video;
                            isImage = facebookTagContent.getType() == FacebookContentTypes.Image;
                            isLink = facebookTagContent.getType() == FacebookContentTypes.Link;
                            if (facebookTagContent.getType() == FacebookContentTypes.Text) {
                                content = facebookTagContent.getContent();
                            }
                            url = facebookTagContent.getUrl();
                        }
                    }
                    if (isVideo) {
                        displayText.insert(0, " ");
                        displayText.insert(0, "[影片]");
                    }
                    if (isImage) {
                        if (content.isEmpty()) {
                            itemFacebookReplyBinding.ivFacebookImage.setVisibility(View.VISIBLE);
                            Glide.with(itemFacebookReplyBinding.ivFacebookImage.getContext())
                                .load(url)
                                .error(isGreenTheme ? R.drawable.image_load_error_green : R.drawable.image_load_error)
                                .into(itemFacebookReplyBinding.ivFacebookImage);
                        } else {
                            displayText.insert(0, " ");
                            displayText.insert(0, "[圖片]");
                        }
                    }
                    if (isLink) {
                        displayText.append(" ").append(url);
                    }
                }
            }


            if (nearMessage.getFacebookPostStatus() == FacebookPostStatus.Delete) {
                itemFacebookReplyBinding.tvStatus.setText(itemFacebookReplyBinding.getRoot().getContext().getString(R.string.facebook_post_status_deleted));
            } else if (nearMessage.getFacebookCommentStatus() == FacebookCommentStatus.Delete) {
                itemFacebookReplyBinding.tvStatus.setText(itemFacebookReplyBinding.getRoot().getContext().getString(R.string.facebook_comment_status_deleted));
            } else if (nearMessage.getFacebookCommentStatus() == FacebookCommentStatus.Update) {
                itemFacebookReplyBinding.tvStatus.setText(itemFacebookReplyBinding.getRoot().getContext().getString(R.string.facebook_comment_status_edited));
            } else {
                itemFacebookReplyBinding.tvStatus.setVisibility(View.GONE);
            }
        } else {
            itemFacebookReplyBinding.tvStatus.setVisibility(View.GONE);
        }

        Spannable stringSpannableString = new UrlTextUtil().getUrlSpannableString(itemFacebookReplyBinding.tvOriginText, displayText);
        itemFacebookReplyBinding.tvOriginText.setText(stringSpannableString);
        itemFacebookReplyBinding.tvOriginText.setOnTouchListener(new TextViewLinkClickListener(stringSpannableString));
        return itemFacebookReplyBinding.getRoot();
    }

    /**
     * 組裝[未知]訊息
     */
    public View assemblyUnDefView() {
        View child = LayoutInflater.from(binding.getRoot().getContext()).inflate(R.layout.item_msg_tip, binding.nearFrameLayout, false);
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
