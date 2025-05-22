package tw.com.chainsea.chat.messagekit.child.viewholder;

import static tw.com.chainsea.chat.messagekit.main.viewholder.FileMessageView.formatSizeDisplay;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewbinding.ViewBinding;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.common.base.Strings;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Objects;

import pl.droidsonroids.gif.GifImageView;
import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.file.FileHelper;
import tw.com.chainsea.android.common.text.KeyWordHelper;
import tw.com.chainsea.android.common.ui.UiHelper;
import tw.com.chainsea.android.common.voice.VoiceHelper;
import tw.com.chainsea.ce.sdk.bean.msg.BroadcastFlag;
import tw.com.chainsea.ce.sdk.bean.msg.content.BroadcastContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.FileContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.IMessageContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.ImageContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.StickerContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.VideoContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.VoiceContent;
import tw.com.chainsea.ce.sdk.http.ce.request.StickerDownloadRequest;
import tw.com.chainsea.ce.sdk.service.PhotoService;
import tw.com.chainsea.ce.sdk.service.StickerService;
import tw.com.chainsea.ce.sdk.service.listener.ProgressServiceCallBack;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.keyboard.ChatTextView;
import tw.com.chainsea.chat.messagekit.enums.FileType;
import tw.com.chainsea.chat.view.service.adapter.ServiceTopicAdapter;
import tw.com.chainsea.custom.view.image.RoundImageView;

/**
 * current by evan on 2020-08-19
 *
 * @author Evan Wang
 * @date 2020-08-19
 */
public class ChildBroadcastMessageView extends ChildMessageBubbleView<BroadcastContent> {
    final static int SMALL_PIC_SIZE = 460;

    private tw.com.chainsea.chat.databinding.MsgkitBroadcastBinding binding;

    public ChildBroadcastMessageView(@NonNull ViewBinding binding) {
        super(binding);
    }

    @Override
    protected int getContentResId() {
        return R.layout.msgkit_broadcast;
    }

    @Override
    protected void bindView(View itemView) {
        binding = tw.com.chainsea.chat.databinding.MsgkitBroadcastBinding.inflate(LayoutInflater.from(itemView.getContext()), null, false);
    }

    public void handlerProgress(int total, int progress, boolean stop) {
        itemView.post(() -> {
            binding.pbStatus.setProgress(progress);
            binding.pbStatus.setProgress(progress);
            if (progress < total && !stop) {
                handlerProgress(total, progress + 1, stop);
            } else {
                handlerProgress(total, 1, stop);
            }
        });
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void bindContentView(BroadcastContent broadcastContent) {
        if (BroadcastFlag.DELETED.equals(getMessage().getBroadcastFlag())) {
//            itemView.setAlpha(0.5f);
            itemMsgBubbleBinding.llMsgBubbleRoot.setVisibility(View.GONE);
        } else {
//            itemView.setAlpha(1.0f);
            itemMsgBubbleBinding.llMsgBubbleRoot.setVisibility(View.VISIBLE);

            int displayWidth = UiHelper.getDisplayWidth(getContext());
            ViewGroup.LayoutParams layoutParams = binding.clMessageContent.getLayoutParams();
            layoutParams.width = displayWidth / 3 * 2;

            ServiceTopicAdapter adapter = new ServiceTopicAdapter(ServiceTopicAdapter.Type.MESSAGE);
            binding.rvTopicList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false) {
                @Override
                public boolean canScrollHorizontally() {
                    return false;
                }

                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            });

            binding.rvTopicList.setAdapter(adapter);
            adapter.setData(getMessage().getTopicArray()).refresh();

            binding.pbStatus.setVisibility(View.GONE);
            switch (Objects.requireNonNull(getMessage().getBroadcastFlag())) {
                case DOME:
                    binding.pbStatus.setVisibility(View.VISIBLE);
                    binding.pbStatus.setMax(1);
                    binding.pbStatus.setProgress(1);
                    break;
                case DISPATCHING:
                    binding.pbStatus.setVisibility(View.VISIBLE);
                    handlerProgress(100, 1, false);
                    break;
                case DELETED:
                case UNDEF:
                case BOOKING:
                default:
                    binding.pbStatus.setVisibility(View.GONE);
                    break;
            }
            assert getMessage().getBroadcastTime() != null;
            long broadcastTime = getMessage().getBroadcastTime();
            if (broadcastTime == getMessage().getSendTime()) {
                binding.tvDateTime.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm").format(broadcastTime));
            } else if (broadcastTime > 0) {
                binding.tvDateTime.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm").format(broadcastTime));
            } else {
                binding.tvDateTime.setText("未更新廣播時間");
            }

            if (BroadcastFlag.DOME.equals(getMessage().getBroadcastFlag())) {
                binding.ivCalendar.setImageResource(R.drawable.msg_check);
                binding.ivCalendar.setImageTintList(ColorStateList.valueOf(0xFF34A9E4));
            } else {
                binding.ivCalendar.setImageResource(R.drawable.icon_deta_15dp);
                binding.ivCalendar.setImageTintList(ColorStateList.valueOf(0xFF4A4A4A));
            }

            View underView = switch (broadcastContent.getInsideType()) {
                case TEXT -> assemblyTextView(broadcastContent.content());
                case IMAGE -> assemblyImageView((ImageContent) broadcastContent.content());
                case STICKER -> assemblyStickerView((StickerContent) broadcastContent.content());
                case FILE -> assemblyFileView((FileContent) broadcastContent.content());
                case VIDEO -> assemblyVideoView((VideoContent) broadcastContent.content());
                case VOICE -> assemblyVoiceView((VoiceContent) broadcastContent.content());
                default -> assemblyUnDefView();
            };

            if (underView != null) {
                binding.flContent.removeAllViews();
                binding.flContent.addView(underView);
            } else {
                binding.flContent.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * Assemble text messages
     * version 1.9.1
     */
    private View assemblyTextView(IMessageContent iMessageContent) {
        String content = iMessageContent.simpleContent();
        if ("%{}%".equals(content)) {
            content = "{}";
        }
        View child = LayoutInflater.from(getContext()).inflate(R.layout.msgkit_text, binding.flContent, false);
        ((ChatTextView) child.findViewById(R.id.contentCTV)).setMaxLines(Integer.MAX_VALUE);
        ((ChatTextView) child.findViewById(R.id.contentCTV)).setText(KeyWordHelper.matcherSearchBackground(0xFFFFF039, content, getKeyword()), TextView.BufferType.NORMAL);
        return child;
    }

    /**
     * Assemble [picture] message
     *
     */
    private View assemblyImageView(ImageContent imageContent) {
        View child = LayoutInflater.from(getContext()).inflate(R.layout.msgkit_image, binding.flContent, false);
        ShapeableImageView image = child.findViewById(R.id.photoRIV);

        int width = imageContent.getWidth();
        int height = imageContent.getHeight();

        if (width != 0 && height != 0) {
            int[] zoom = zoomImage(width, height);
            ViewGroup.LayoutParams layoutParams = image.getLayoutParams();
            layoutParams.width = zoom[0];
            layoutParams.height = zoom[1];
        }


        String url = !Strings.isNullOrEmpty(imageContent.getThumbnailUrl()) ? imageContent.getThumbnailUrl() : imageContent.getUrl();
        PhotoService.post(getContext(), url, image, R.drawable.image_load_error, new ProgressServiceCallBack<>() {
            @Override
            public void progress(float progress, long total) {

            }

            @Override
            public void error(String message) {
                ViewGroup.LayoutParams layoutParams = image.getLayoutParams();
                layoutParams.width = 100;
                layoutParams.height = 100;
                image.setImageResource(R.drawable.image_load_error);
            }

            @Override
            public void complete(Drawable drawable, RefreshSource source) {
                if (drawable != null) {
                    ViewGroup.LayoutParams layoutParams = image.getLayoutParams();
                    int width = drawable.getIntrinsicWidth();
                    int height = drawable.getIntrinsicHeight();
                    int[] zoom = zoomImage(width, height);
                    layoutParams.width = zoom[0];
                    layoutParams.height = zoom[1];
                    image.setImageDrawable(drawable);
                }
            }
        });

        // 順便把原圖載進 Cache
        PhotoService.post(getContext(), imageContent.getUrl(), image, R.drawable.image_load_error, new ProgressServiceCallBack<>() {
            @Override
            public void progress(float progress, long total) {

            }

            @Override
            public void error(String message) {
            }

            @Override
            public void complete(Drawable drawable, RefreshSource source) {
            }
        });
        return child;
    }

    /**
     * Assemble [sticker] message
     */
    private View assemblyStickerView(StickerContent stickerContent) {
        View child = LayoutInflater.from(getContext()).inflate(R.layout.msgkit_sticker, binding.flContent, false);
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
                emoticons.setImageResource(R.drawable.image_load_error);
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
//
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
     * Assemble [file] message
     */
    public View assemblyFileView(FileContent fileContent) {
        View child = LayoutInflater.from(getContext()).inflate(R.layout.msgkit_file, binding.flContent, false);
        ImageView iconImage = child.findViewById(R.id.fileIconCIV);
        TextView fileName = child.findViewById(R.id.fileNameTV);
        TextView fileTypeNameTV = child.findViewById(R.id.fileTypeNameTV);
        TextView fileSize = child.findViewById(R.id.fileSizeTV);
        TextView tvSign = child.findViewById(R.id.fileStatusTV);
        String fileTyle = FileHelper.getFileTyle(fileContent.getName());
        FileType fileType = FileType.of(fileTyle);

        fileName.setText(fileContent.getName());
        tvSign.setText("");


        fileTypeNameTV.setText(fileType.getName());
        iconImage.setImageResource(fileType.getDrawable());
//        iconImage.setBorder(0, fileType.getDrawable());

        if (!Strings.isNullOrEmpty(fileContent.getUrl()) && fileContent.getUrl().startsWith("http")) {
            fileSize.setText(formatSizeDisplay(fileContent.getSize()));
        } else if (!Strings.isNullOrEmpty(fileContent.getAndroid_local_path())) {
            File localFile = new File(fileContent.getAndroid_local_path());
            if (localFile.exists()) {
                fileSize.setText(formatSizeDisplay(localFile.length()));
            }
        }
        return child;
    }

    /**
     * Assemble [video] message
     */
    @SuppressLint("MissingInflatedId")
    public View assemblyVideoView(VideoContent videoContent) {
        View child = LayoutInflater.from(getContext()).inflate(R.layout.msgkit_video, binding.flContent, false);
        ConstraintLayout clOccupation = child.findViewById(R.id.occupationCL);
        RoundImageView rivThumbnail = child.findViewById(R.id.thumbnailRIV);
        VideoView vvReview = child.findViewById(R.id.reviewVV);
        ImageView ivPlay = child.findViewById(R.id.playIV);
        vvReview.setVisibility(View.GONE);


        ivPlay.setVisibility(View.GONE);
        rivThumbnail.setImageResource(R.drawable.file_msg_down_bg);
        ViewGroup.LayoutParams layoutParams = clOccupation.getLayoutParams();
        int[] zoom = zoomImage(videoContent.getWidth(), videoContent.getHeight());
        layoutParams.width = zoom[0];
        layoutParams.height = zoom[1];

        String url = !Strings.isNullOrEmpty(videoContent.getUrl()) ? videoContent.getUrl() : videoContent.getAndroid_local_path();
        PhotoService.loadVideoThumbnail(getContext(), url, R.drawable.file_msg_down_bg, new ServiceCallBack<>() {
            @Override
            public void complete(Bitmap bitmap, RefreshSource source) {
                rivThumbnail.setImageBitmap(bitmap);
                ivPlay.setVisibility(View.VISIBLE);
            }

            @Override
            public void error(String message) {
                rivThumbnail.setImageResource(R.drawable.file_msg_down_bg);
                ivPlay.setVisibility(View.GONE);
            }
        });
        return child;
    }

    /**
     * Assemble [video] message
     */
    public View assemblyVoiceView(VoiceContent voiceContent) {
        View child = LayoutInflater.from(getContext()).inflate(R.layout.msgkit_voice, binding.flContent, false);

        child.setBackgroundResource(R.drawable.file_msg_bg);
        child.setPadding(20, 20, 20, 20);
        TextView msg_voice_right_content = child.findViewById(R.id.msg_voice_right_content);
        TextView msg_voice_left_content = child.findViewById(R.id.msg_voice_left_content);
        ImageView message_voice_dot = child.findViewById(R.id.message_voice_dot);
        message_voice_dot.setVisibility(View.GONE);
        msg_voice_right_content.setVisibility(View.GONE);

        //voiceContent.getDuration();
        msg_voice_left_content.setText(VoiceHelper.strDuration(voiceContent.getDuration() * 1000));
        return child;
    }

    /**
     * Assemble [Unknown] Message
     */
    public View assemblyUnDefView() {
        View child = LayoutInflater.from(getContext()).inflate(R.layout.item_msg_tip, binding.flContent, false);
        TextView tvTip = child.findViewById(R.id.msgkit_tip_content);
        tvTip.setBackgroundResource(R.drawable.sys_msg_bg);
        tvTip.setTextColor(Color.WHITE);
        tvTip.setText("暫不支持此消息類型");
        return null;
    }

    public static int[] zoomImage(int inWidth, int inHeight) {
        int outWidth;
        int outHeight;
        int[] sizes = new int[2];
        if (inWidth == 0 || inHeight == 0) {
            sizes[0] = SMALL_PIC_SIZE;
            sizes[1] = SMALL_PIC_SIZE;
            return sizes;
        }

        if (inWidth > inHeight) {
            outWidth = SMALL_PIC_SIZE;
            outHeight = (inHeight * SMALL_PIC_SIZE) / inWidth;
        } else {
            outHeight = SMALL_PIC_SIZE;
            outWidth = (inWidth * SMALL_PIC_SIZE) / inHeight;
        }

        sizes[0] = outWidth;
        sizes[1] = outHeight;

        return sizes;
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

    }

    @Override
    protected boolean showName() {
        return !isRightMessage();
    }

    @Override
    protected boolean isRightMessage() {
        return true;
    }
}
