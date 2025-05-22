package tw.com.chainsea.chat.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.util.FileUtils;


/**
 * author：luck
 * project：PictureSelector
 * package：com.luck.pictureselector.adapter
 * email：893855882@qq.com
 * data：16/7/27
 */
public class GridImageAdapter extends
    RecyclerView.Adapter<GridImageAdapter.ViewHolder> {
    public static final int TYPE_CAMERA = 1;
    public static final int TYPE_PICTURE = 2;
    private LayoutInflater mInflater;
    private List<LocalMedia> list = new ArrayList<>();
    private int selectMax = 9;
    private Context context;
    private boolean isShow = true;
    /**
     * 点击添加图片跳转
     */
    private onAddPicClickListener mOnAddPicClickListener;

    public interface onAddPicClickListener {
        void onAddPicClick();
    }

    public GridImageAdapter(Context context, onAddPicClickListener mOnAddPicClickListener) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
        this.mOnAddPicClickListener = mOnAddPicClickListener;
    }

    public void setSelectMax(int selectMax) {
        this.selectMax = selectMax;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setList(List<LocalMedia> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView mImg;
        LinearLayout ll_del;
        TextView tv_duration;

        public ViewHolder(View view) {
            super(view);
            mImg = (ImageView) view.findViewById(R.id.fiv);
            ll_del = (LinearLayout) view.findViewById(R.id.ll_del);
            tv_duration = (TextView) view.findViewById(R.id.txt_duration);
        }
    }

    @Override
    public int getItemCount() {
        if (list.size() < selectMax) {
            return list.size() + 1;
        } else {
            return list.size();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isShowAddItem(position)) {
            return TYPE_CAMERA;
        } else {
            return TYPE_PICTURE;
        }
    }

    /**
     * 创建ViewHolder
     */
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = mInflater.inflate(R.layout.gv_filter_image,
            viewGroup, false);
        final ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    private boolean isShowAddItem(int position) {
        int size = list.isEmpty() ? 0 : list.size();
        return position == size;
    }

    /**
     * 设置值
     */
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int position) {
        //少于8张，顯示继续添加的图标
        if (getItemViewType(position) == TYPE_CAMERA) {
            viewHolder.mImg.setImageResource(R.drawable.addimg_1x);
            viewHolder.mImg.setVisibility(View.GONE);
            viewHolder.mImg.setOnClickListener(v -> mOnAddPicClickListener.onAddPicClick());
            viewHolder.ll_del.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.ll_del.setVisibility(View.VISIBLE);
            if (!isShow) {
                viewHolder.ll_del.setVisibility(View.INVISIBLE);
            }
            viewHolder.ll_del.setOnClickListener(view -> mItemClickListener.onItemDeleteClick(position));
            LocalMedia media = list.get(position);
            String mimeType = media.getMimeType();
            String path = "";
            if (media.isCut() && !media.isCompressed()) {
                // 裁剪过
                path = media.getCutPath();
            } else if (media.isCompressed() || (media.isCut() && media.isCompressed())) {
                // 压缩过,或者裁剪同时压缩过,以最终压缩过图片为准
                path = media.getCompressPath();
            } else {
                // 原图
                path = media.getRealPath();
            }
            viewHolder.tv_duration.setVisibility(View.GONE);
            if (Objects.equals(mimeType, PictureMimeType.MIME_TYPE_AUDIO)) {
                viewHolder.tv_duration.setVisibility(View.VISIBLE);
                Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ps_ic_audio);
                FileUtils.modifyTextViewDrawable(viewHolder.tv_duration, drawable, 0);
            } else {
                Drawable drawable = ContextCompat.getDrawable(context, R.drawable.video_icon);
                FileUtils.modifyTextViewDrawable(viewHolder.tv_duration, drawable, 0);
            }
//            viewHolder.tv_duration.setText(DateUtils.timeParse(duration));
            if (Objects.equals(mimeType, PictureMimeType.MIME_TYPE_AUDIO)) {
                viewHolder.mImg.setImageResource(R.drawable.ps_audio_placeholder);
            } else {
                try {
                    RequestOptions options = new RequestOptions()
                        .centerCrop()
                        .placeholder(R.color.color_f6)
                        .diskCacheStrategy(DiskCacheStrategy.ALL);
                    Glide.with(viewHolder.itemView.getContext())
                        .load(path)
                        .apply(options)
                        .into(viewHolder.mImg);
                } catch (Exception ignored) {
                }

            }
            //itemView 的点击事件
            if (mItemClickListener != null) {
                viewHolder.itemView.setOnClickListener(v -> {
                    int adapterPosition = viewHolder.getAdapterPosition();
                    mItemClickListener.onItemClick(adapterPosition, v);
                });
            }
        }
    }

    protected OnItemClickListener mItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position, View v);

        void onItemDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mItemClickListener = listener;
    }
}
