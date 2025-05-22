package tw.com.chainsea.chat.view.gallery;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.common.base.Strings;

import java.io.File;
import java.io.Serializable;

import tw.com.chainsea.chat.util.DaVinci;
import cn.hadcn.davinci.image.base.ImageEntity;
import cn.hadcn.davinci.image.base.Util;
import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.image.BitmapHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.bean.msg.content.IMessageContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.ImageContent;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.config.ProgressAppGlideModule;
import tw.com.chainsea.chat.databinding.FragmentPhotoGalleryBinding;
import tw.com.chainsea.chat.util.ThemeHelper;
import tw.com.chainsea.chat.util.VibratorKit;
import tw.com.chainsea.chat.widget.LoadingBar;
import tw.com.chainsea.chat.widget.photoview.PhotoView;
import tw.com.chainsea.chat.widget.photoview.PhotoViewAttacher;

/**
 * A simple {@link Fragment} subclass.
 */
public class PhotoGalleryFragment extends Fragment {
    private static final String TAG = PhotoGalleryFragment.class.getSimpleName();

    MessageEntity message;
//    private Context ctx;

    private boolean isFeedback = true;

    private OnLongClickListener onLongClickListener;
    private OnClickListener onClickListener;
    private FragmentPhotoGalleryBinding binding;
    private boolean isGreenTheme = false;

//    ProgressManager progressManager = ProgressManager.getInstance();

    public PhotoGalleryFragment() {
        // Required empty public constructor
    }


    public static PhotoGalleryFragment newInstance(Context ctx, MessageEntity message) {
        PhotoGalleryFragment fragment = new PhotoGalleryFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(BundleKey.EXTRA_MESSAGE.key(), message);
        fragment.setArguments(bundle);
//        fragment.setMessage(message);
//        fragment.setCtx(ctx);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPhotoGalleryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isGreenTheme = ThemeHelper.INSTANCE.isGreenTheme();
//        photoGZIV.setVisibility(View.VISIBLE);
//        setup();
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.photoView.setVisibility(View.VISIBLE);
        setup();
    }

    private void setup() {
        Serializable serializable = getArguments().getSerializable(BundleKey.EXTRA_MESSAGE.key());
        if (serializable instanceof MessageEntity) {
            this.message = (MessageEntity) getArguments().getSerializable(BundleKey.EXTRA_MESSAGE.key());
        }

        if (this.message == null || message.content() == null) {
            return;
        }

        IMessageContent iContent = message.content();

        String url = "";
        String tUrl = "";
        int fileSize = 0;

        if (iContent instanceof ImageContent) {
            url = ((ImageContent) iContent).getUrl();
            tUrl = ((ImageContent) iContent).getThumbnailUrl();
            fileSize = ((ImageContent) iContent).getSize();

            try {
                Bitmap bitmap = BitmapHelper.getBitmapFromLocal(url);
                if (bitmap != null) {
                    if (url.endsWith(".gif")) {
                        Glide.with(getContext()).asGif().load(url).into(binding.photoView);
                    } else {
                        binding.photoView.setImageBitmap(bitmap);
                    }
                    binding.photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                        @Override
                        public void onPhotoTap(View view, float x, float y) {
                            if (onClickListener != null) {
                                VibratorKit.longClick();
                                onClickListener.onClick(message, iContent);
                            }
                        }

                        @Override
                        public void onOutsidePhotoTap() {
                        }
                    });
                    return;
                }
            } catch (Exception ignored) {

            }
        }
        if (url.endsWith(".gif") && !url.startsWith("http")) {
            File file = new File(url);
            Glide.with(getContext()).asGif().load(file)
                .listener(new OnRequestListener(binding.pbMainProgressBar, binding.photoView))
                .into(binding.photoView);
        } else if (url.endsWith(".gif") && url.startsWith("https")) {
            try {
                Glide.with(getContext()).asGif().load(url)
                    .listener(new OnRequestListener(binding.pbMainProgressBar, binding.photoView))
                    .into(binding.photoView);
            } catch (Exception ignored) {
            }

        } else {
            if (Strings.isNullOrEmpty(url) || Strings.isNullOrEmpty(tUrl) || url.endsWith("heic") || tUrl.endsWith("heic")) {
                try {
                    Glide.with(getContext())
                        .load(isGreenTheme ? R.drawable.image_load_error_green : R.drawable.image_load_error)
                        .override(100)
                        .dontAnimate()
                        .listener(new OnRequestListener(binding.pbMainProgressBar, binding.photoView))
                        .into(binding.photoView);
                } catch (Exception ignored) {
                }

            } else {
                String key = Util.generateKey(tUrl);
                ImageEntity image = null;
                BitmapDrawable drawable = null;

                if (!tUrl.startsWith("http")) {
                    image = DaVinci.with().getImageLoader().getImage(url);
                } else {
                    image = DaVinci.with().getImageLoader().getImage(key);
                }
                if (image != null) {
                    drawable = new BitmapDrawable(image.getBitmap());
                }

                if (!tUrl.startsWith("http")) {
                    try {
                        Glide.with(getContext())
                            .load(image == null ? null : image.getBitmap())
                            .dontAnimate()
                            .placeholder(drawable)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .listener(new OnRequestListener(binding.pbMainProgressBar, binding.photoView))
                            .into(binding.photoView);
                    } catch (Exception ignored) {
                    }

                } else {
                    final int finalFileSize = fileSize;
                    String finalUrl = url;

                    ProgressAppGlideModule.expect(url, new ProgressAppGlideModule.UIonProgressListener() {
                        @Override
                        public void onProgress(long bytesRead, long expectedLength, ProgressAppGlideModule.DispatchingProgressListener listener) {
                            if (binding.pbMainProgressBar != null) {
                                if (binding.pbMainProgressBar.getVisibility() == View.GONE) {
                                    binding.pbMainProgressBar.setVisibility(View.VISIBLE);
                                }
                                if (finalFileSize >= 0) {
                                    binding.pbMainProgressBar.setProgress(100);
                                    binding.pbMainProgressBar.setVisibility(View.GONE);
                                    return;
                                }

                                int progress = (int) ((100 * bytesRead) / finalFileSize);
                                if (progress < 100) {
                                    binding.pbMainProgressBar.setProgress(progress);
                                } else {
                                    listener.forget(finalUrl);
                                    binding.pbMainProgressBar.setProgress(100);
                                    binding.pbMainProgressBar.setVisibility(View.GONE);
                                }
                            }
                        }

                        @Override
                        public float getGranualityPercentage() {
                            return 1.0f;
                        }

                    });


                    try {
                        Glide.with(getContext())
                            .load(url)
                            .dontTransform()
//                                .thumbnail(Glide
//                                        .with(getContext())
//                                        .load(tUrl)
//                                        .listener(new ThumbnailRequestListener()))
//                                .dontAnimate()
//                                .placeholder(drawable)
//                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .listener(new OnRequestListener(binding.pbMainProgressBar, binding.photoView))
                            .into(binding.photoView);


//                        GlideApp.with(this.ctx)
//                                .load(url)
//                                .thumbnail(GlideApp
//                                        .with(this.ctx)
//                                        .load(tUrl)
//                                        .listener(new ThumbnailRequestListener()))
//                                .dontAnimate()
////                                .placeholder(drawable)
//                                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                                .listener(new OnRequestListener(progressBar, photoGZIV))
//                                .into(photoGZIV);


//                        GlideApp.with(this.ctx)
//                                .load(url)
//                                .thumbnail(GlideApp
//                                        .with(this.ctx)
//                                        .load(tUrl)
//                                        .dontAnimate()
//                                        .listener(new ThumbnailRequestListener())
//                                        .diskCacheStrategy(DiskCacheStrategy.ALL))
//                                .dontAnimate()
//                                .placeholder(drawable)
//                                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                                .listener(new OnRequestListener(progressBar, photoGZIV))
//                                .into(photoGZIV);
                    } catch (Exception e) {
                        CELog.e(TAG, e);
                    }
                }
            }
        }

        binding.photoView.setOnLongClickListener(v -> {
            if (onLongClickListener != null) {
                VibratorKit.longClick();
                onLongClickListener.onLongClick(message, iContent);
            }
            return true;
        });


        binding.photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                if (onClickListener != null) {
                    VibratorKit.longClick();
                    onClickListener.onClick(message, iContent);
                }
            }

            @Override
            public void onOutsidePhotoTap() {

            }
        });
    }


    static class ThumbnailRequestListener implements RequestListener {

        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target target, boolean isFirstResource) {
            return false;
        }

        @Override
        public boolean onResourceReady(@NonNull Object resource, @NonNull Object model, Target target, @NonNull DataSource dataSource, boolean isFirstResource) {
            return false;
        }
    }

    static class OnRequestListener implements RequestListener {
        LoadingBar progressBar;
        PhotoView photoGZIV;

        OnRequestListener(LoadingBar progressBar, PhotoView photoGZIV) {
            this.progressBar = progressBar;
            this.photoGZIV = photoGZIV;
        }

        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target target, boolean isFirstResource) {
            this.progressBar.setVisibility(View.GONE);
            return false;
        }

        @Override
        public boolean onResourceReady(@NonNull Object resource, @NonNull Object model, Target target, @NonNull DataSource dataSource, boolean isFirstResource) {
            this.progressBar.setVisibility(View.GONE);
            return false;
        }
    }


    public PhotoGalleryFragment setOnLongClickListener(OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
        return this;
    }

    public PhotoGalleryFragment setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        return this;
    }

    public MessageEntity getMessage() {
        return message;
    }

    public void setMessage(MessageEntity message) {
        this.message = message;
    }

    public interface OnLongClickListener {
        void onLongClick(MessageEntity message, IMessageContent format);
    }

    public interface OnClickListener {
        void onClick(MessageEntity message, IMessageContent iContent);
    }
}
