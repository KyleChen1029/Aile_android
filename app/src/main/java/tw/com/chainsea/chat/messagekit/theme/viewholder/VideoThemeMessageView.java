package tw.com.chainsea.chat.messagekit.theme.viewholder;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.video.VideoHelper;
import tw.com.chainsea.ce.sdk.bean.msg.content.VideoContent;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.messagekit.theme.viewholder.base.ThemeMessageBubbleView;
import tw.com.chainsea.chat.util.DownloadUtil;

/**
 * current by evan on 2020-01-09
 */
public class VideoThemeMessageView extends ThemeMessageBubbleView {
    final static int SMALL_PIC_SIZE = 400;

    ConstraintLayout occupationCL;
    ShapeableImageView thumbnailRIV;
    //    VideoView reviewVV;
    ImageView playIV;
    ConstraintLayout clPlay;
    TextView tvPeriod;

    public VideoThemeMessageView(@NonNull View itemView) {
        super(itemView);
    }


    @Override
    protected int getContentResId() {
        return R.layout.msgkit_video;
    }

    @Override
    protected boolean showName() {
        return !isRightMessage();
    }


    @Override
    protected void inflateContentView() {
        occupationCL = findView(R.id.occupationCL);
        thumbnailRIV = findView(R.id.thumbnailRIV);
        VideoView reviewVV = findView(R.id.reviewVV);
        if (reviewVV != null) {
            reviewVV.setVisibility(View.GONE);
        }
        playIV = findView(R.id.playIV);
        clPlay = findView(R.id.clPlay);
        tvPeriod = findView(R.id.tvPeriod);
    }

    @Override
    protected void bindContentView() {
        if (getMsg().content() instanceof VideoContent videoContent) {
            ViewGroup.LayoutParams params = occupationCL.getLayoutParams();
            int[] zoomSize = getZoomSize(videoContent);
            // 如果沒高度寬度 給預設
            if (zoomSize[0] <= 0) zoomSize[0] = 300;
            if (zoomSize[1] <= 0) zoomSize[1] = 600;
            params.width = zoomSize[0];
            params.height = zoomSize[1];
            // 如果沒高度寬度 給預設
            thumbnailRIV.getLayoutParams().width = zoomSize[0];
            thumbnailRIV.getLayoutParams().height = zoomSize[1];
            occupationCL.setLayoutParams(params);
            setPreviewImage(videoContent);
        } else {
            playIV.setVisibility(View.GONE);
            Glide.with(getContext())
                    .load(isGreenTheme ? R.drawable.image_load_error_green : R.drawable.image_load_error)
                    .apply(new RequestOptions().override(SMALL_PIC_SIZE))
                    .into(this.thumbnailRIV);
        }
    }


    @Override
    public void onClick(View v, MessageEntity message) {

    }

    @Override
    public void onDoubleClick(View v, MessageEntity message) {

    }

    @Override
    public void onLongClick(View v, float x, float y, MessageEntity message) {

    }

    private void setPreviewImage(VideoContent videoContent) {
        String thumbnailUrl = "";
        if (videoContent.getThumbnailUrl() != null && !videoContent.getThumbnailUrl().isEmpty()) {
            thumbnailUrl = videoContent.getThumbnailUrl();
        }
        File file = getVideoFile(videoContent);
        RequestBuilder<Drawable> requestBuilder;
        if (file != null) {
            clPlay.setVisibility(View.VISIBLE);
            playIV.setImageResource(R.drawable.play);
            tvPeriod.setText(DownloadUtil.INSTANCE.getVideoDuration(file.getAbsolutePath()));
            tvPeriod.setVisibility(View.VISIBLE);
            String videoThumbnail = thumbnailUrl.isEmpty() ? file.getAbsolutePath() : thumbnailUrl;
            requestBuilder = Glide.with(playIV.getContext())
                    .load(videoThumbnail)
                    .apply(new RequestOptions()
                            .frame(1000)
                            .override(SMALL_PIC_SIZE)
                            .fitCenter());
        } else {
            playIV.setImageResource(R.drawable.ic_video_download);
            if (URLUtil.isValidUrl(videoContent.getUrl()) || videoContent.getUrl().endsWith(".mp4")) {
                int thumbnailWidth = (videoContent.getThumbnailWidth() > 0) ? videoContent.getThumbnailWidth() : 300;
                int thumbnailHeight = (videoContent.getThumbnailHeight() > 0) ? videoContent.getThumbnailHeight() : 600;
                requestBuilder = Glide.with(playIV.getContext())
                        .load(thumbnailUrl)
                        .apply(new RequestOptions()
                                .frame(1000)
                                .override(thumbnailWidth, thumbnailHeight)
                                .fitCenter());
            } else {
                clPlay.setVisibility(View.GONE);
                requestBuilder = Glide.with(playIV.getContext())
                        .load(isGreenTheme ? R.drawable.image_load_error_green : R.drawable.image_load_error)
                        .override(SMALL_PIC_SIZE);
            }
        }
        if (requestBuilder != null) {
            requestBuilder.diskCacheStrategy(DiskCacheStrategy.ALL).into(thumbnailRIV);
        }
    }


    private int[] getZoomSize(VideoContent videoContent) {
        if (videoContent.getWidth() != 0 && videoContent.getHeight() != 0) {
            return zoomImage(videoContent.getWidth(), videoContent.getHeight());
        } else if (videoContent.getThumbnailHeight() != 0 && videoContent.getThumbnailWidth() != 0) {
            return zoomImage(videoContent.getThumbnailWidth(), videoContent.getThumbnailHeight());
        } else {
            File file = getVideoFile(videoContent);
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

    private File getVideoFile(VideoContent videoContent) {
        String downloadPath = DownloadUtil.INSTANCE.getDownloadFileDir() + getMsg().getSendTime() + "_" + videoContent.getName();
        if (videoContent.getAndroid_local_path() != null && new File(videoContent.getAndroid_local_path()).exists()) {
            return new File(videoContent.getAndroid_local_path());
        } else if (new File(downloadPath).exists()) {
            return new File(downloadPath);
        } else {
            return null;
        }
    }

    private int[] zoomImage(int inWidth, int inHeight) {
        if (inWidth > inHeight) {
            return new int[]{SMALL_PIC_SIZE, (inHeight * SMALL_PIC_SIZE) / inWidth};
        } else {
            return new int[]{((inWidth * SMALL_PIC_SIZE) / inHeight), SMALL_PIC_SIZE};
        }
    }
}
