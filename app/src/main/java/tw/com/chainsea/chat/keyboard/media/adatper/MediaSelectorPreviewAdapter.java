package tw.com.chainsea.chat.keyboard.media.adatper;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.List;

import tw.com.chainsea.android.common.multimedia.AMediaBean;
import tw.com.chainsea.android.common.multimedia.VideoBean;
import tw.com.chainsea.chat.databinding.FragmentPhotoGalleryBinding;
import tw.com.chainsea.chat.widget.photoview.PhotoViewAttacher;

/**
 * current by evan on 2020-06-09
 *
 * @author Evan Wang
 * @date 2020-06-09
 */
public class MediaSelectorPreviewAdapter extends RecyclerView.Adapter<MediaSelectorPreviewAdapter.ItemViewHolder> {


    Context context;
    List<AMediaBean> mediaBeans = Lists.newArrayList();

    PhotoViewAttacher.OnPhotoTapListener onPhotoTapListener;

    private final HashMap<Integer, ItemViewHolder> videoViewHolderList = new HashMap<Integer, ItemViewHolder>();

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        FragmentPhotoGalleryBinding binding = FragmentPhotoGalleryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        AMediaBean bean = this.mediaBeans.get(position);

        // 影片
        if (bean instanceof VideoBean) {
            videoViewHolderList.put(position, holder);
            holder.binding.videoView.initPlayer(bean.getPath());
            holder.binding.photoView.setVisibility(View.GONE);
            holder.binding.videoView.setVisibility(View.VISIBLE);
        } else {
            holder.binding.videoView.setVisibility(View.GONE);
            // 圖片
            holder.binding.photoView.setVisibility(View.VISIBLE);
            try {
                Glide.with(holder.itemView.getContext())
                    .load(bean.getPath())
                    .addListener(new RequestListener() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                    @NonNull Target target, boolean isFirstResource) {
                            holder.binding.photoView.setOnPhotoTapListener(onPhotoTapListener);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(@NonNull Object resource, @NonNull Object model, Target target,
                                                       @NonNull DataSource dataSource, boolean isFirstResource) {
                            holder.binding.photoView.setOnPhotoTapListener(onPhotoTapListener);
                            return false;
                        }
                    })
                    .into(holder.binding.photoView);
            } catch (Exception e) {
            }
        }
    }

    @Override
    public int getItemCount() {
        return this.mediaBeans.size();
    }


    @Override
    public void onViewRecycled(@NonNull ItemViewHolder holder) {
        super.onViewRecycled(holder);
        holder.binding.videoView.releasePlayer();
    }

    public MediaSelectorPreviewAdapter setData(List<AMediaBean> mediaBeans) {
        this.mediaBeans = mediaBeans;
        return this;
    }


    public MediaSelectorPreviewAdapter setOnPhotoTapListener(PhotoViewAttacher.OnPhotoTapListener onPhotoTapListener) {
        this.onPhotoTapListener = onPhotoTapListener;
        return this;
    }

    // 自動播放當前影片及暫停其他影片
    public void pauseOtherVideo(int position) {
        videoViewHolderList.forEach((index, holder) -> {
            if (index != position) {
                holder.binding.videoView.pausePlayer();
            } else {
                holder.binding.videoView.startPlayer();
            }
        });
    }

    public void pausePlayer() {
        videoViewHolderList.forEach((index, holder) -> {
            if (holder.binding.videoView.isPlaying()) {
                holder.binding.videoView.pausePlayer();
            }
        });
    }

    public void releasePlayer() {
        videoViewHolderList.forEach((index, holder) -> {
            holder.binding.videoView.releasePlayer();
        });
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        FragmentPhotoGalleryBinding binding;

        public ItemViewHolder(@NonNull FragmentPhotoGalleryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
