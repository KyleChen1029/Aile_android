package tw.com.chainsea.chat.keyboard.media.adatper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.common.collect.Lists;

import java.util.List;

import tw.com.chainsea.android.common.multimedia.AMediaBean;
import tw.com.chainsea.chat.databinding.ItemMediaSelectorThumbnailBinding;
import tw.com.chainsea.chat.util.ThemeHelper;

/**
 * current by evan on 2020-06-10
 *
 * @author Evan Wang
 * @date 2020-06-10
 */
public class MediaSelectorThumbnailPreviewAdapter extends RecyclerView.Adapter<MediaSelectorThumbnailPreviewAdapter.ThumbnailItemView> {

    private Context context;
    private List<AMediaBean> selectList = Lists.newArrayList();

    private OnMediaSelectorThumbnailPreviewListener<AMediaBean> onMediaSelectorThumbnailPreviewListener;
    private AMediaBean currentBean = null;


    public MediaSelectorThumbnailPreviewAdapter(List<AMediaBean> selectList) {
        this.selectList = selectList;
    }

    @NonNull
    @Override
    public ThumbnailItemView onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        this.context = parent.getContext();
        ItemMediaSelectorThumbnailBinding binding = ItemMediaSelectorThumbnailBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ThumbnailItemView(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ThumbnailItemView holder, int position) {
        AMediaBean bean = selectList.get(position);
        if (this.currentBean != null) {
            holder.binding.clBackground.setBackgroundColor(this.currentBean.getPath().equals(bean.getPath()) ? ThemeHelper.INSTANCE.isGreenTheme() ? Color.parseColor("#06B4A5") : 0xFF7EB9FF : 0x00000000);
        } else {
            holder.binding.clBackground.setBackgroundColor(0x00000000);
        }
        try {
            Glide.with(this.context)
                .load(bean.getPath())
                .into(holder.binding.ivThumbnail);
        } catch (Exception ignored) {
        }

        holder.itemView.setOnClickListener(v -> {
            if (onMediaSelectorThumbnailPreviewListener != null) {
                onMediaSelectorThumbnailPreviewListener.onItemClick(bean);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.selectList.size();
    }

    public MediaSelectorThumbnailPreviewAdapter setCurrentBean(List<AMediaBean> selectList, AMediaBean currentBean) {
        this.selectList = selectList;
        this.currentBean = currentBean;
        return this;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refreshData(boolean needHandler) {
        if (needHandler) {
            new Handler().post(this::notifyDataSetChanged);
        } else {
            notifyDataSetChanged();
        }
    }


    public MediaSelectorThumbnailPreviewAdapter setOnMediaSelectorThumbnailPreviewListener(OnMediaSelectorThumbnailPreviewListener<AMediaBean> onMediaSelectorThumbnailPreviewListener) {
        this.onMediaSelectorThumbnailPreviewListener = onMediaSelectorThumbnailPreviewListener;
        return this;
    }

    public interface OnMediaSelectorThumbnailPreviewListener<T> {

        void onItemClick(T t);
    }

    static class ThumbnailItemView extends RecyclerView.ViewHolder {

        private ItemMediaSelectorThumbnailBinding binding;

        public ThumbnailItemView(@NonNull ItemMediaSelectorThumbnailBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

//            int height = UiHelper.dip2px(itemView.getContext(), 54);
//            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(height , height);
//            params.topToTop = 0;
//            params.bottomToBottom = 0;
//            params.startToStart = 0;
//            params.endToEnd = 0;
//            itemView.setLayoutParams(params);
        }
    }
}
