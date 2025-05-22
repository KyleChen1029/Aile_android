package tw.com.chainsea.chat.messagekit.child.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.MediaController;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.viewbinding.ViewBinding;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.common.base.Strings;
import com.google.common.primitives.Ints;

import java.io.File;
import java.util.Objects;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.ui.UiHelper;
import tw.com.chainsea.android.common.video.VideoHelper;
import tw.com.chainsea.ce.sdk.bean.msg.MessageStatus;
import tw.com.chainsea.ce.sdk.bean.msg.content.VideoContent;
import tw.com.chainsea.chat.R;

/**
 * current by evan on 2020-01-08
 */
public class ChildVideoMessageView extends ChildMessageBubbleView<VideoContent> {
    final static int SMALL_PIC_SIZE = 400;
    static int MARGIN = 0;


    private tw.com.chainsea.chat.databinding.MsgkitVideoBinding binding;

    public ChildVideoMessageView(@NonNull ViewBinding binding) {
        super(binding);
    }

    @Override
    protected int getContentResId() {
        return R.layout.msgkit_video;
    }

    @Override
    protected int rightBackground() {
        return R.drawable.msgkit_trans_bg;
    }

    @Override
    protected int leftBackground() {
        return R.drawable.msgkit_trans_bg;
    }

    @Override
    protected void bindView(View itemView) {
        binding = tw.com.chainsea.chat.databinding.MsgkitVideoBinding.inflate(LayoutInflater.from(itemView.getContext()), null, false);
//        ButterKnife.bind(this, itemView);
        MARGIN = UiHelper.dip2px(getContext(), 8);
    }

    @Override
    protected boolean showName() {
        return !isRightMessage();
    }

    @Override
    protected void bindContentView(VideoContent videoContent) {
        CELog.e("");
        ViewGroup.LayoutParams params = binding.occupationCL.getLayoutParams();
        int[] zoomSize = (videoContent.getWidth() == 0 || videoContent.getHeight() == 0) ? VideoHelper.size(videoContent.getUrl()) : zoomImage(videoContent.getWidth(), videoContent.getHeight());

        params.width = zoomSize[0];
        params.height = zoomSize[1];
        binding.occupationCL.setLayoutParams(params);

        MediaController mediaController = new MediaController(getContext());
        mediaController.setVisibility(View.GONE);
        String localPath = videoContent.getAndroid_local_path();
        String videoUrl = videoContent.getUrl();

        binding.occupationCL.setBackground(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.file_msg_down_bg, null));
        binding.thumbnailRIV.setVisibility(View.VISIBLE);
        binding.playIV.setVisibility(View.VISIBLE);

        String progress = videoContent.getProgress();
        binding.progressBar.setVisibility((Strings.isNullOrEmpty(progress) || "100".equals(progress)) ? View.GONE : View.VISIBLE);
        binding.playIV.setVisibility((Strings.isNullOrEmpty(progress) || "100".equals(progress)) ? View.VISIBLE : View.GONE);
        MessageStatus status = getMessage().getStatus();
        switch (Objects.requireNonNull(status)) {
            case READ:
            case RECEIVED:
            case SUCCESS:
                if (!Strings.isNullOrEmpty(progress)) {
                    Integer pgs = Ints.tryParse(progress);
                    if (pgs !=null && pgs >= 100) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.playIV.setVisibility(View.VISIBLE);
                    } else {
                        binding.playIV.setVisibility(View.GONE);
                        binding.progressBar.setProgress(pgs != null ? pgs : 0);
                    }
                }
                break;
            case SENDING:
                if (!Strings.isNullOrEmpty(progress)) {
                    Integer pgs = Ints.tryParse(progress);
                    if (pgs != null && pgs >= 100) {
                        binding.progressBar.setProgress(0);
                        binding.progressBar.setVisibility(View.GONE);
                        binding.playIV.setVisibility(View.VISIBLE);
                    } else {
                        binding.playIV.setVisibility(View.GONE);
                        binding.progressBar.setProgress(pgs!=null ? pgs : 0);
                    }
                }
                break;
            case FAILED:
            case ERROR:
            default:
                break;
        }


        if (!Strings.isNullOrEmpty(localPath) && new File(localPath).exists()) {
            Glide.with(getContext())
                    .load(localPath)
                    .apply(new RequestOptions()
                            .frame(1000)
                            .override(SMALL_PIC_SIZE)
                            .error(isGreenTheme ? R.drawable.image_load_error_green : R.drawable.image_load_error)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .fitCenter())
                    .into(binding.thumbnailRIV);
        } else if (URLUtil.isValidUrl(videoUrl) || !videoUrl.endsWith(".mp4")) {
            Glide.with(getContext())
                    .load(videoUrl)
                    .apply(new RequestOptions()
                            .frame(1000)
                            .override(SMALL_PIC_SIZE)
                            .error(isGreenTheme ? R.drawable.image_load_error_green : R.drawable.image_load_error)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .fitCenter())
                    .into(binding.thumbnailRIV);

//            binding.reviewVV.setOnPreparedListener(mp -> {
//                if (this.onMessageControlEventListener != null) {
//                    this.onMessageControlEventListener.onStopOtherVideoPlayback(getMessage());
//                }
//                binding.occupationCL.setBackground(getContext().getResources().getDrawable(R.drawable.file_msg_down_bg));
//                binding.thumbnailRIV.setVisibility(View.GONE);
//                binding.playIV.setVisibility(View.GONE);
//                binding.reviewVV.setVisibility(View.VISIBLE);
//                mp.start();
//                videoContent.setPlaying(true);
//            });

//            binding.reviewVV.setOnCompletionListener(mp -> stopPlay(true));
//            binding.reviewVV.setOnErrorListener((mp, what, extra) -> {
//                stopPlay(false);
//                return false;
//            });
        } else {
            binding.playIV.setVisibility(View.GONE);
            Glide.with(getContext())
                    .load(isGreenTheme ? R.drawable.image_load_error_green : R.drawable.image_load_error)
                    .apply(new RequestOptions().override(SMALL_PIC_SIZE))
                    .into(binding.thumbnailRIV);
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

    @Override
    public void onClick(View v, MessageEntity message) {
        super.onClick(v, message);
    }

    @Override
    public void onDoubleClick(View v, MessageEntity message) {
    }

    @Override
    public void onLongClick(View v, float x, float y, MessageEntity message) {
        if (this.onMessageControlEventListener != null) {
            this.onMessageControlEventListener.onLongClick(getMessage(), (int) x, (int) y);
        }
    }

}
