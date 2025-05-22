package tw.com.chainsea.chat.view.enlarge.adapter.viewholder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.ce.sdk.bean.msg.content.ImageContent;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.config.ProgressAppGlideModule;
import tw.com.chainsea.chat.databinding.ItemEnLargeImageMessageBinding;
import tw.com.chainsea.chat.view.enlarge.adapter.viewholder.base.EnLargeMessageBaseView;

/**
 * current by evan on 2020-04-15
 *
 * @author Evan Wang
 * @date 2020-04-15
 */
public class EnLargeImageMessageView extends EnLargeMessageBaseView<ImageContent> {
    ItemEnLargeImageMessageBinding enLargeImageMessageBinding;

    public EnLargeImageMessageView(@NonNull ItemEnLargeImageMessageBinding binding) {
        super(binding.getRoot());
        enLargeImageMessageBinding = binding;
    }

    @Override
    public void onBind(MessageEntity entity, ImageContent imageContent, int position) {
        String url = imageContent.getUrl();
        int fileSize = imageContent.getSize();

        ProgressAppGlideModule.expect(url, new ProgressAppGlideModule.UIonProgressListener() {
            @Override
            public void onProgress(long bytesRead, long expectedLength, ProgressAppGlideModule.DispatchingProgressListener listener) {
                if (enLargeImageMessageBinding.progress != null) {
                    if (enLargeImageMessageBinding.progress.getVisibility() == View.GONE) {
                        enLargeImageMessageBinding.progress.setVisibility(View.VISIBLE);
                    }
                    try {
                        int progress = (int) ((100 * bytesRead) / fileSize);
                        if (progress < 100) {
                            enLargeImageMessageBinding.progress.setProgress(progress);
                        } else {
                            listener.forget(url);
                            enLargeImageMessageBinding.progress.setProgress(100);
                            enLargeImageMessageBinding.progress.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {

                    }
                }
            }

            @Override
            public float getGranualityPercentage() {
                return 1.0f;
            }
        });

        try {
            Glide.with(context)
                .load(url)
                .apply(new RequestOptions()
                    .placeholder(R.drawable.file_msg_down_bg)
                    .error(isGreenTheme ? R.drawable.image_load_error_green : R.drawable.image_load_error)
                    .centerInside())
//                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .listener(new OnRequestListener(imageContent))
                .into(enLargeImageMessageBinding.rivPhoto);
        } catch (Exception ignored) {
        }

    }


    class OnRequestListener implements RequestListener {
        ImageContent imageContent;

        OnRequestListener(ImageContent imageContent) {
            this.imageContent = imageContent;
        }

        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target target, boolean isFirstResource) {
            enLargeImageMessageBinding.progress.setVisibility(View.GONE);
            this.imageContent.setFailedToLoad(true);
            return false;
        }

        @Override
        public boolean onResourceReady(@NonNull Object resource, @NonNull Object model, Target target, @NonNull DataSource dataSource, boolean isFirstResource) {
            enLargeImageMessageBinding.progress.setVisibility(View.GONE);
            return false;
        }
    }
}
