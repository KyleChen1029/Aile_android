//package tw.com.chainsea.chat.messagekit.main.viewholder;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.constraintlayout.widget.ConstraintLayout;
//import androidx.viewbinding.ViewBinding;
//
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.load.DataSource;
//import com.bumptech.glide.load.engine.GlideException;
//import com.bumptech.glide.request.RequestListener;
//import com.bumptech.glide.request.RequestOptions;
//import com.bumptech.glide.request.target.Target;
//import com.google.common.base.Strings;
//
//import java.io.File;
//
//import tw.com.chainsea.chat.util.DaVinci;
//import cn.hadcn.davinci.image.base.ImageEntity;
//import tw.com.aile.sdk.bean.message.MessageEntity;
//import tw.com.chainsea.ce.sdk.bean.msg.content.ImageContent;
//import tw.com.chainsea.chat.R;
//import tw.com.chainsea.chat.config.ProgressAppGlideModule;
//import tw.com.chainsea.chat.databinding.MsgkitImageBinding;
//import tw.com.chainsea.chat.messagekit.main.viewholder.base.MessageBubbleView;
//import tw.com.chainsea.chat.widget.LoadingBar;
//import tw.com.chainsea.custom.view.image.RoundImageView;
//
//
///**
// * ImageMessageView
// * Created by Andy on 2016/5/22.
// */
//public class ImageMessageView extends MessageBubbleView<ImageContent> {
//
//
//    private MsgkitImageBinding binding;
//    final static int SMALL_PIC_SIZE = 360;
//
//    public ImageMessageView(@NonNull ViewBinding binding) {
//        super(binding);
//        this.binding = MsgkitImageBinding.inflate(LayoutInflater.from(itemView.getContext()), null, false);
//        getView(this.binding.getRoot());
//    }
//
//    @Override
//    protected boolean showName() {
//        return !isRightMessage();
//    }
//
//    @Override
//    protected int leftBackground() {
//        return R.drawable.msgkit_trans_bg;
//    }
//
//    @Override
//    protected int rightBackground() {
//        return R.drawable.msgkit_trans_bg;
//    }
//
//    @Override
//    protected int getContentResId() {
//        return R.layout.msgkit_image;
//    }
//
//    @Override
//    protected View getChildView() {
//        return binding.getRoot();
//    }
//
//    @Override
//    protected void bindContentView(ImageContent imageContent) {
//        String url = imageContent.getUrl();
//        String thumbnailUrl = url.endsWith(".gif") ? url : imageContent.getThumbnailUrl();
//
//        int fileSize = imageContent.getThumbnailSize();
//
//        int height = imageContent.getHeight() == 0 ? 200 : imageContent.getHeight();
//        int width = imageContent.getWidth() == 0 ? 200 : imageContent.getWidth();
//
//        ViewGroup.LayoutParams params = binding.occupationCL.getLayoutParams();
//        int[] zoomSize = zoomImage(width, height);
//        params.width = zoomSize[0];
//        params.height = zoomSize[1];
//
//        binding.occupationCL.setLayoutParams(params);
//        binding.occupationCL.setBackground(binding.occupationCL.getContext().getResources().getDrawable(R.drawable.file_msg_down_bg));
//
//        if (Strings.isNullOrEmpty(thumbnailUrl)) {
//            imageContent.setFailedToLoad(true);
//            getMessage().setContent(imageContent.toStringContent());
//            return;
//        } else if (thumbnailUrl.startsWith("smallandroid")) {
//            ImageEntity image = DaVinci.with().getImageLoader().getImage(thumbnailUrl);
//            Glide.with(binding.photoRIV)
//                    .load(image.getBitmap())
//                    .apply(new RequestOptions()
//                            .override(SMALL_PIC_SIZE)
//                            .placeholder(R.drawable.file_msg_down_bg)
//                            .error(R.drawable.image_load_error)
//                            .fitCenter())
//                    .listener(new OnRequestListener(imageContent, binding.occupationCL, binding.progressBar))
//                    .into(binding.photoRIV);
//            return;
//        } else if (thumbnailUrl.endsWith(".gif") && !thumbnailUrl.startsWith("http")) {
//            File file = new File(thumbnailUrl);
//            Glide.with(binding.photoRIV)
//                    .asGif()
//                    .load(file)
////                    .apply(option)
//                    .apply(new RequestOptions()
//                            .override(SMALL_PIC_SIZE)
//                            .placeholder(R.drawable.file_msg_down_bg)
//                            .error(R.drawable.image_load_error)
//                            .fitCenter())
//                    .listener(new OnRequestListener(imageContent, binding.occupationCL, binding.progressBar))
//                    .into(binding.photoRIV);
//            return;
//        } else {
//            ProgressAppGlideModule.expect(thumbnailUrl, new ProgressAppGlideModule.UIonProgressListener() {
//                @Override
//                public void onProgress(long bytesRead, long expectedLength, ProgressAppGlideModule.DispatchingProgressListener listener) {
//                    if (binding.progressBar != null) {
//                        if (binding.progressBar.getVisibility() == View.GONE) {
//                            binding.progressBar.setVisibility(View.VISIBLE);
//                        }
//                        try {
//                            int progress = (fileSize != 0) ? (int) ((100 * bytesRead) / fileSize) : 100;
//                            if (progress < 100) {
//                                binding.progressBar.setProgress(progress);
//                            } else {
//                                listener.forget(thumbnailUrl);
//                                binding.progressBar.setProgress(100);
//                                binding.progressBar.setVisibility(View.GONE);
//                                binding.occupationCL.setBackground(null);
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//
//                @Override
//                public float getGranualityPercentage() {
//                    return 1.0f;
//                }
//            });
//
//            Glide.with(binding.photoRIV)
//                    .load(thumbnailUrl)
//                    .apply(new RequestOptions()
//                            .override(SMALL_PIC_SIZE)
//                            .placeholder(R.drawable.file_msg_down_bg)
//                            .error(R.drawable.image_load_error)
//                            .fitCenter())
//                    .listener(new OnRequestListener(imageContent, binding.occupationCL, binding.progressBar))
//                    .into(binding.photoRIV);
//        }
//
//        if (imageContent.isFailedToLoad()) {
//            binding.occupationCL.setBackground(null);
//            Glide.with(binding.photoRIV)
//                    .load(R.drawable.image_load_error)
//                    .apply(new RequestOptions()
//                            .override(100)
//                            .fitCenter())
//                    .into(binding.photoRIV);
//        }
//    }
//
//    class OnRequestListener implements RequestListener {
//        ImageContent imageContent;
//        ConstraintLayout occupationCL;
//        LoadingBar progressBar;
//
//        OnRequestListener(ImageContent imageContent, ConstraintLayout occupationCL, LoadingBar progressBar) {
//            this.imageContent = imageContent;
//            this.occupationCL = occupationCL;
//            this.progressBar = progressBar;
//        }
//
//        @Override
//        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
//            this.occupationCL.setBackground(null);
//            this.progressBar.setVisibility(View.GONE);
//            this.imageContent.setFailedToLoad(true);
//            getMessage().setContent(this.imageContent.toStringContent());
//            return false;
//        }
//
//        @Override
//        public boolean onResourceReady(Object resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {
//            this.occupationCL.setBackground(null);
//            this.progressBar.setVisibility(View.GONE);
//            return false;
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
//    @Override
//    public void onClick(View v, MessageEntity message) {
//        super.onClick(v, message);
//        if (this.onMessageControlEventListener != null) {
//            this.onMessageControlEventListener.onImageClick(getMessage());
//        }
//    }
//
//    @Override
//    public void onDoubleClick(View v, MessageEntity message) {
////        onListDilogItemClickListener.enLarge(getMessage());
//    }
//
//    @Override
//    public void onLongClick(View v, float x, float y, MessageEntity message) {
//        if (this.onMessageControlEventListener != null) {
//            this.onMessageControlEventListener.onLongClick(getMessage(), (int) x, (int) y);
//        }
//    }
//}
//
