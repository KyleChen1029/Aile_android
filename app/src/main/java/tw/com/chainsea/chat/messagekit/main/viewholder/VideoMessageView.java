//package tw.com.chainsea.chat.messagekit.main.viewholder;
//
//import android.os.Environment;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.webkit.URLUtil;
//import android.widget.MediaController;
//
//import androidx.annotation.NonNull;
//import androidx.core.content.res.ResourcesCompat;
//import androidx.viewbinding.ViewBinding;
//
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.load.engine.DiskCacheStrategy;
//import com.bumptech.glide.request.RequestOptions;
//import com.google.common.base.Strings;
//import com.google.common.primitives.Ints;
//import com.luck.picture.lib.utils.ToastUtils;
//
//import java.io.File;
//
//import tw.com.aile.sdk.bean.message.MessageEntity;
//import tw.com.chainsea.android.common.log.CELog;
//import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
//import tw.com.chainsea.android.common.ui.UiHelper;
//import tw.com.chainsea.android.common.video.VideoHelper;
//import tw.com.chainsea.ce.sdk.bean.msg.MessageStatus;
//import tw.com.chainsea.ce.sdk.bean.msg.content.VideoContent;
//import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
//import tw.com.chainsea.chat.R;
//import tw.com.chainsea.chat.databinding.MsgkitVideoBinding;
//import tw.com.chainsea.chat.messagekit.main.viewholder.base.MessageBubbleView;
//import tw.com.chainsea.chat.util.DownloadUtil;
//
///**
// * current by evan on 2020-01-08
// */
//public class VideoMessageView extends MessageBubbleView<VideoContent> {
//    final static int SMALL_PIC_SIZE = 360;
//    static int MARGIN = 0;
//    private final String downloadDir = Environment.getExternalStorageDirectory() + "/download/";
//    private DownloadingStatus newestStatus = DownloadingStatus.UNDEF;
//    private enum DownloadingStatus {
//        UNDEF, SUCCESS, CANCELED, FAILURE
//    }
//
//    private MsgkitVideoBinding binding;
//    public VideoMessageView(@NonNull ViewBinding binding) {
//        super(binding);
//        this.binding = MsgkitVideoBinding.inflate(LayoutInflater.from(itemView.getContext()), null, false);
//        getView(this.binding.getRoot());
//        MARGIN = UiHelper.dip2px(binding.getRoot().getContext(), 8);
//    }
//
//    @Override
//    protected int getContentResId() {
//        return R.layout.msgkit_video;
//    }
//
//    @Override
//    protected int rightBackground() {
//        return R.drawable.msgkit_trans_bg;
//    }
//
//    @Override
//    protected int leftBackground() {
//        return R.drawable.msgkit_trans_bg;
//    }
//
//    @Override
//    protected View getChildView() {
//        return binding.getRoot();
//    }
//
//    @Override
//    protected boolean showName() {
//        return isRightMessage() ? false : true;
//    }
//
//    @Override
//    protected void bindContentView(VideoContent videoContent) {
//        String thumbnailUrl = "";
//        int thumbnailWidth = 300;
//        int thumbnailHeight = 600;
//        ViewGroup.LayoutParams params = binding.occupationCL.getLayoutParams();
//        int[] zoomSize;
//        if (videoContent.getThumbnailHeight() == 0 || videoContent.getThumbnailWidth() == 0) {
//            zoomSize = VideoHelper.size(videoContent.getUrl());
//        } else if (videoContent.getWidth() != 0 || videoContent.getHeight() != 0) {
//            zoomSize = zoomImage(videoContent.getWidth(), videoContent.getHeight());
//        } else {
//            zoomSize = zoomImage(videoContent.getThumbnailWidth(), videoContent.getThumbnailHeight());
//        }
//
//        if(zoomSize[0] > 800){
//            params.width = SMALL_PIC_SIZE;
//            params.height = SMALL_PIC_SIZE;
//        }else {
//            params.width = zoomSize[0];
//            params.height = zoomSize[1];
//        }
//        // 如果沒高度寬度 給預設
//        if (params.width <= 0) params.width = thumbnailWidth;
//        if (params.height <= 0) params.height = thumbnailHeight;
//
//        binding.occupationCL.setLayoutParams(params);
//
//        MediaController mediaController = new MediaController(binding.getRoot().getContext());
//        mediaController.setVisibility(View.GONE);
//        String localPath = videoContent.getAndroid_local_path();
//        String videoUrl = videoContent.getUrl();
//
//        binding.occupationCL.setBackground(ResourcesCompat.getDrawable(binding.getRoot().getContext().getResources(), R.drawable.file_msg_down_bg, null));
////        binding.reviewVV.setVisibility(View.GONE);
//        binding.thumbnailRIV.setVisibility(View.VISIBLE);
//        binding.playIV.setVisibility(View.VISIBLE);
//
//        String progress = videoContent.getProgress();
//        MessageStatus status = getMessage().getStatus();
//        switch (status) {
//            case READ:
//            case RECEIVED:
//            case SUCCESS:
//                if (!Strings.isNullOrEmpty(progress)) {
//                    int pgs = Ints.tryParse(progress);
//                    if (pgs >= 100) {
//                        binding.progressBar.setVisibility(View.GONE);
//                        binding.playIV.setVisibility(View.VISIBLE);
//                    } else {
//                        binding.playIV.setVisibility(View.GONE);
//                        binding.progressBar.setProgress(pgs);
//                    }
//                }
//                break;
//            case SENDING:
//                if (!Strings.isNullOrEmpty(progress)) {
//                    int pgs = Ints.tryParse(progress);
//                    if (pgs >= 100) {
//                        binding.progressBar.setProgress(0);
//                        binding.progressBar.setVisibility(View.GONE);
//                        binding.playIV.setVisibility(View.VISIBLE);
//                    } else {
//                        binding.playIV.setVisibility(View.GONE);
//                        binding.progressBar.setProgress(pgs);
//                    }
//                }
//                break;
//            case FAILED:
//            case ERROR:
//            default:
//                break;
//        }
//
//
//        if (!Strings.isNullOrEmpty(localPath) && new File(localPath).exists()) {
//            binding.playIV.setImageResource(R.drawable.play);
//            Glide.with(binding.thumbnailRIV)
//                    .load(localPath)
//                    .apply(new RequestOptions()
//                            .frame(1000)
//                            .override(SMALL_PIC_SIZE)
//                            .diskCacheStrategy(DiskCacheStrategy.ALL)
//                            .fitCenter())
//                    .into(binding.thumbnailRIV);
//        } else if (URLUtil.isValidUrl(videoUrl) || !videoUrl.endsWith(".mp4")) {
//
//            if(getMessage().getContent()!=null && !getMessage().getContent().isEmpty()) {
//                try {
//                    thumbnailUrl = videoContent.getThumbnailUrl();
//                    if(videoContent.getThumbnailWidth() > 0 || videoContent.getThumbnailHeight() > 0) {
//                        thumbnailWidth = videoContent.getThumbnailWidth();
//                        thumbnailHeight = videoContent.getThumbnailHeight();
//                    }
//                }catch (Exception e) {
//                    CELog.e("VideoMessageView json parse error="+e.getMessage());
//                }
//            }
//
//            if(thumbnailUrl.isEmpty()){
//                Glide.with(binding.thumbnailRIV)
//                        .load(localPath)
//                        .apply(new RequestOptions()
//                                .frame(1000)
//                                .override(SMALL_PIC_SIZE)
//                                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                                .fitCenter())
//                        .into(binding.thumbnailRIV);
//            }else{
//                Glide.with(binding.thumbnailRIV)
//                        .load(thumbnailUrl)
//                        .apply(new RequestOptions()
//                                .frame(1000)
//                                .override(thumbnailWidth, thumbnailHeight)
//                                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                                .fitCenter())
//                        .into(binding.thumbnailRIV);
//            }
//            //判斷是否已下載
//            String path = downloadDir + getMessage().getSendTime() + "_" + videoContent.getName();
//            binding.playIV.setImageResource( new File(path).exists() ? R.drawable.play : R.drawable.ic_video_download);
//
//        } else {
//            binding.playIV.setVisibility(View.GONE);
//            Glide.with(binding.thumbnailRIV)
//                    .load(R.drawable.image_load_error)
//                    .apply(new RequestOptions().override(SMALL_PIC_SIZE))
//                    .into(binding.thumbnailRIV);
//        }
//    }
//
//    public static int[] zoomImage(int inWidth, int inHeight) {
//        int outWidth;
//        int outHeight;
//        if (inWidth > inHeight) {
//            outWidth = SMALL_PIC_SIZE;
//            outHeight = (inHeight * SMALL_PIC_SIZE) / inWidth;
//        } else {
//            outHeight = SMALL_PIC_SIZE;
//            outWidth = (inWidth * SMALL_PIC_SIZE) / inHeight;
//        }
//        int[] sizes = new int[2];
//        sizes[0] = outWidth;
//        sizes[1] = outHeight;
//        return sizes;
//    }
//
//
//    public void updateProgress(int progress) {
//        if (binding != null && binding.progressBar != null) {
//            ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
//                binding.progressBar.setProgress(progress);
//                if (binding.progressBar.getProgress() == 100) {
//                    binding.progressBar.setVisibility(View.GONE);
//                    binding.playIV.setVisibility(View.VISIBLE);
//                } else {
//                    binding.progressBar.setVisibility(View.VISIBLE);
//                    binding.playIV.setVisibility(View.GONE);
//                }
//            });
//        }
//    }
//
//    @Override
//    public void onClick(View v, MessageEntity message) {
//        super.onClick(v, message);
//
//        VideoContent format = (VideoContent) message.content();
//        String downloadPath = downloadDir + message.getSendTime() + "_" + format.getName();
//        File downloadFile = new File(downloadPath);
//        if ((!Strings.isNullOrEmpty(format.getAndroid_local_path()) && new File(format.getAndroid_local_path()).exists()) || downloadFile.exists()) {
//            if (this.onMessageControlEventListener != null) {
//                this.onMessageControlEventListener.onVideoClick(getMessage());
//            }
//            binding.progressBar.setOnFileClickListener(null);
//        } else {
//            binding.progressBar.setOnFileClickListener(() -> {
//                if (!Strings.isNullOrEmpty(format.getProgress())) {
//                    newestStatus = DownloadingStatus.CANCELED;
//                    ToastUtils.showToast(binding.getRoot().getContext(), binding.getRoot().getContext().getString(R.string.canceled_downloading));
//                }
//            });
//                new Thread(() -> {
//                    try {
//                        DownloadUtil downloadUtil = new DownloadUtil();
//                        downloadUtil.doDownloadVideoFile(
//                                format,
//                                downloadPath,
//                                TokenPref.getInstance(binding.getRoot().getContext()).getTokenId(),
//                                progress -> {
//                                    updateProgress(progress);
//                                    format.setProgress("" + progress);
//                                    return null;
//                                },
//                                file -> {
//                                    format.setProgress(null);
//                                    format.setDownload(true);
//                                    binding.progressBar.setProgress(0);
//                                    binding.progressBar.setVisibility(View.GONE);
//                                    format.setProgress("100");
//                                    if (onMessageControlEventListener != null) {
//                                        onMessageControlEventListener.onContentUpdate(getMessage().getId(), format.getClass().getName(), format.toStringContent());
//                                    }
//                                    //handling the behavior of cancel downloading
//                                    binding.progressBar.isCanceledLoading = false;
//                                    if(newestStatus == DownloadingStatus.CANCELED)
//                                        canceledOrFailureHandle(message, format, "", newestStatus);
//                                    else
//                                        newestStatus = DownloadingStatus.SUCCESS;
//                                    return null;
//                                },
//                                errorMsg -> {
//                                    canceledOrFailureHandle(message, format, errorMsg, newestStatus = DownloadingStatus.FAILURE);
//                                    if (onMessageControlEventListener != null) {
//                                        onMessageControlEventListener.onVideoClick(getMessage());
//                                    }
//                                    return null;
//                                }
//                        );
//                        getMessage().setStatus(MessageStatus.RECEIVED);
//                    } catch (Exception e) {
//                        CELog.e("download file failed. reason = " + e.getMessage());
//                    }
//                }).start();
//        }
//    }
//
//    @Override
//    public void onDoubleClick(View v, MessageEntity message) {
//    }
//
//    @Override
//    public void onLongClick(View v, float x, float y, MessageEntity message) {
//        if (this.onMessageControlEventListener != null) {
//            this.onMessageControlEventListener.onLongClick(getMessage(), (int) x, (int) y);
//        }
//    }
//
//    private void canceledOrFailureHandle(MessageEntity message, VideoContent videoContent, String e, DownloadingStatus status) {
//        videoContent.setProgress(null);
//        videoContent.setDownload(false);
//        message.setContent(videoContent.toStringContent());
//        binding.progressBar.setProgress(0);
//        binding.progressBar.setVisibility(View.GONE);
//        if (onMessageControlEventListener != null) {
//            onMessageControlEventListener.onContentUpdate(getMessage().getId(), videoContent.getClass().getName(), videoContent.toStringContent());
//        }
//        try {
//            String downloadPath = downloadDir + message.getSendTime() + "_" + videoContent.getName();
//            File downloadFile = new File(downloadPath);
//            boolean isDelete = downloadFile.delete();
//            CELog.e(isDelete ? "刪除檔案成功" : "刪除檔案失敗");
//        } catch (Exception e1) {
//            CELog.e("刪除檔案失敗", e);
//        }
//    }
//
//}
