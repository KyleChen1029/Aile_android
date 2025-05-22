package tw.com.chainsea.chat.view.enlarge.adapter.viewholder;

import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.MediaController;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.common.base.Strings;
import com.google.common.primitives.Ints;

import java.io.File;

import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.ui.UiHelper;
import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.ce.sdk.bean.msg.MessageStatus;
import tw.com.chainsea.ce.sdk.bean.msg.content.VideoContent;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ItemEnLargeVideoMessageBinding;
import tw.com.chainsea.chat.view.enlarge.adapter.viewholder.base.EnLargeMessageBaseView;

/**
 * current by evan on 2020-04-15
 *
 * @author Evan Wang
 * @date 2020-04-15
 */
public class EnLargeVideoMessageView extends EnLargeMessageBaseView<VideoContent> {
    ItemEnLargeVideoMessageBinding enLargeVideoMessageBinding;
    static int SMALL_PIC_SIZE = 400;

    public EnLargeVideoMessageView(@NonNull ItemEnLargeVideoMessageBinding binding) {
        super(binding.getRoot());
        enLargeVideoMessageBinding = binding;
        SMALL_PIC_SIZE = UiHelper.getDisplayWidth(context);
        CELog.w("" + SMALL_PIC_SIZE);
    }

    @Override
    public void onBind(MessageEntity entity, VideoContent videoContent, int position) {
        ViewGroup.LayoutParams params = enLargeVideoMessageBinding.clContent.getLayoutParams();
        int[] zoomSize = zoomImage(videoContent.getWidth(), videoContent.getHeight());
        params.width = zoomSize[0];
        params.height = zoomSize[1];
        enLargeVideoMessageBinding.clContent.setLayoutParams(params);

        MediaController mediaController = new MediaController(context);
        mediaController.setVisibility(View.GONE);
        String localPath = videoContent.getAndroid_local_path();
        String videoUrl = videoContent.getUrl();
        // 先隱藏顯示縮略圖
        enLargeVideoMessageBinding.clContent.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.file_msg_down_bg, null));
        enLargeVideoMessageBinding.reviewVV.setVisibility(View.GONE);
        enLargeVideoMessageBinding.thumbnailRIV.setVisibility(View.VISIBLE);
        enLargeVideoMessageBinding.playIV.setVisibility(View.VISIBLE);
        ConstraintLayout.LayoutParams cllp = (ConstraintLayout.LayoutParams) enLargeVideoMessageBinding.reviewVV.getLayoutParams();

        String progress = videoContent.getProgress();
        enLargeVideoMessageBinding.progressBar.setVisibility((Strings.isNullOrEmpty(progress) || "100".equals(progress)) ? View.GONE : View.VISIBLE);
        enLargeVideoMessageBinding.playIV.setVisibility((Strings.isNullOrEmpty(progress) || "100".equals(progress)) ? View.VISIBLE : View.GONE);
        MessageStatus status = entity.getStatus();
        switch (status) {
            case READ:
            case RECEIVED:
            case SUCCESS:
                if (!Strings.isNullOrEmpty(progress)) {
                    int pgs = Ints.tryParse(progress);
                    if (pgs >= 100) {
                        enLargeVideoMessageBinding.progressBar.setVisibility(View.GONE);
                        enLargeVideoMessageBinding.playIV.setVisibility(View.VISIBLE);
                    } else {
                        enLargeVideoMessageBinding.playIV.setVisibility(View.GONE);
                        enLargeVideoMessageBinding.progressBar.setProgress(pgs);
                    }
                }
                break;
            case SENDING:
                if (!Strings.isNullOrEmpty(progress)) {
                    int pgs = Ints.tryParse(progress);
                    if (pgs >= 100) {
                        enLargeVideoMessageBinding.progressBar.setProgress(0);
                        enLargeVideoMessageBinding.progressBar.setVisibility(View.GONE);
                        enLargeVideoMessageBinding.playIV.setVisibility(View.VISIBLE);
                    } else {
                        enLargeVideoMessageBinding.playIV.setVisibility(View.GONE);
                        enLargeVideoMessageBinding.progressBar.setProgress(pgs);
                    }
                }
                break;
            case FAILED:
            case ERROR:
            default:
                break;
        }


        if (videoContent.isPlaying()) {
            stopPlay(entity, false);
        }

        if (!Strings.isNullOrEmpty(localPath) && new File(localPath).exists()) {
            try {
                Glide.with(context)
                        .load(localPath)
                        .apply(new RequestOptions()
                                .frame(1000)
                                .override(SMALL_PIC_SIZE)
                                .error(isGreenTheme ? R.drawable.image_load_error_green : R.drawable.image_load_error)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .fitCenter())
                        .into(enLargeVideoMessageBinding.thumbnailRIV);
            }catch (Exception ignored) {}

        } else if (URLUtil.isValidUrl(videoUrl) || !videoUrl.endsWith(".mp4")) {
            // 視頻縮略圖
            try {
                Glide.with(context)
                        .load(videoUrl)
                        .apply(new RequestOptions()
                                .frame(1000)
                                .override(SMALL_PIC_SIZE)
                                .error(isGreenTheme ? R.drawable.image_load_error_green : R.drawable.image_load_error)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .fitCenter())
                        .into(enLargeVideoMessageBinding.thumbnailRIV);
            }catch (Exception ignored) {}


            // 播放按鈕
//            playIV.setOnClickListener(v -> {
//                if (checkBox.getVisibility() == View.VISIBLE) {
//                    boolean isCheck = checkBox.isChecked();
//                    checkBox.setChecked(!isCheck);
//                    if (onListDilogItemClickListener != null) {
//                        onListDilogItemClickListener.onStopOtherVideoPlayback(getMessage());
//                    }
//                } else {
//                    reviewVV.setVisibility(View.VISIBLE);
//                    if (zoomSize[0] > zoomSize[1]) {// 比較寬
//                        cllp.setMargins(MARGIN, 0, MARGIN, 0);
//                    } else {// 比較高
//                        cllp.setMargins(0, MARGIN, 0, MARGIN);
//                    }
//                    reviewVV.setLayoutParams(cllp);
//                    reviewVV.setVideoPath(videoUrl);
//                    reviewVV.setMediaController(mediaController);
//                    reviewVV.requestFocus();
//                }
//            });

            // 裝載完成
            enLargeVideoMessageBinding.reviewVV.setOnPreparedListener(mp -> {
//                if (this.onMessageControlEventListener != null) {
//                    this.onMessageControlEventListener.onStopOtherVideoPlayback(getMessage());
//                }
                enLargeVideoMessageBinding.clContent.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.file_msg_down_bg, null));
                enLargeVideoMessageBinding.thumbnailRIV.setVisibility(View.GONE);
                enLargeVideoMessageBinding.playIV.setVisibility(View.GONE);
                enLargeVideoMessageBinding.reviewVV.setVisibility(View.VISIBLE);
                mp.start();
                videoContent.setPlaying(true);
            });

            // 播放完成
            enLargeVideoMessageBinding.reviewVV.setOnCompletionListener(mp -> stopPlay(entity, true));

            // 發生錯誤
            enLargeVideoMessageBinding.reviewVV.setOnErrorListener((mp, what, extra) -> {
                stopPlay(entity, false);
                return false;
            });
        } else {
            enLargeVideoMessageBinding.playIV.setVisibility(View.GONE);
            try {
                Glide.with(context)
                        .load(isGreenTheme ? R.drawable.image_load_error_green : R.drawable.image_load_error)
                        .apply(new RequestOptions().override(SMALL_PIC_SIZE))
                        .into(enLargeVideoMessageBinding.thumbnailRIV);
            }catch (Exception ignored) {}

        }
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

    public void stopPlay(MessageEntity entity, boolean checkPlaying) {
        if (enLargeVideoMessageBinding.reviewVV.isPlaying() || checkPlaying) {
            enLargeVideoMessageBinding.reviewVV.stopPlayback();
            ConstraintLayout.LayoutParams cllp = (ConstraintLayout.LayoutParams) enLargeVideoMessageBinding.reviewVV.getLayoutParams();
            cllp.setMargins(0, 0, 0, 0);
            enLargeVideoMessageBinding.clContent.setBackground(null);
            enLargeVideoMessageBinding.thumbnailRIV.setVisibility(View.VISIBLE);
            enLargeVideoMessageBinding.playIV.setVisibility(View.VISIBLE);
            enLargeVideoMessageBinding.reviewVV.setVisibility(View.GONE);
            VideoContent videoContent = (VideoContent) entity.content();
            videoContent.setPlaying(false);
            // 後續製作全屏幕播放
        }
    }


}
