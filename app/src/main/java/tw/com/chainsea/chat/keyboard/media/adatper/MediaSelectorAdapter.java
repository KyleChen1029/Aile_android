package tw.com.chainsea.chat.keyboard.media.adatper;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import tw.com.chainsea.android.common.multimedia.AMediaBean;
import tw.com.chainsea.android.common.multimedia.FileBean;
import tw.com.chainsea.android.common.multimedia.IMediaBean;
import tw.com.chainsea.android.common.multimedia.ImageBean;
import tw.com.chainsea.android.common.multimedia.MultimediaHelper;
import tw.com.chainsea.android.common.multimedia.VideoBean;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.android.common.ui.UiHelper;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ItemKeyboardBottomMediaFileBinding;
import tw.com.chainsea.chat.databinding.ItemKeyboardBottomMediaPhotoBinding;
import tw.com.chainsea.chat.databinding.ItemKeyboardBottomMediaVideoBinding;
import tw.com.chainsea.chat.lib.ToastUtils;
import tw.com.chainsea.chat.messagekit.enums.FileType;
import tw.com.chainsea.chat.style.RoomThemeStyle;
import tw.com.chainsea.chat.util.ThemeHelper;

/**
 * current by evan on 2020-06-05
 * ViewHolder.itemView Event click navigate to MediaSelectorPreview {@link tw.com.chainsea.chat.keyboard.media.MediaSelectorPreviewActivity}
 *
 * @author Evan Wang
 * @date 2020-06-01
 */
public class MediaSelectorAdapter extends RecyclerView.Adapter<MediaSelectorAdapter.IMediaItemViewHolder> {

    List<AMediaBean> mediaBeans = Lists.newArrayList();
    private List<AMediaBean> selectlist = Lists.newArrayList();
    private int maxCount = 9;
    private Context context;
    RoomThemeStyle themeStyle = RoomThemeStyle.UNDEF;
    private OnMediaSelectorListener onMediaSelectorListener;
    ContentResolver contentResolver;
    boolean isGreenTheme = false;

    public ContentResolver getContentResolver(Context context) {
        if (contentResolver == null) {
            contentResolver = context.getContentResolver();
        }
        return contentResolver;
    }

    static BitmapFactory.Options options = new BitmapFactory.Options();

    static {
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
    }


    @NonNull
    @Override
    public IMediaItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        this.context = parent.getContext();
        isGreenTheme = ThemeHelper.INSTANCE.isGreenTheme();
        if (contentResolver == null) {
            contentResolver = context.getContentResolver();
        }
        switch (type) {
            case 1:
                ItemKeyboardBottomMediaFileBinding fileBinding =
                    DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_keyboard_bottom_media_file, parent, false);
                return new FileItemViewHolder(fileBinding);
            case 2:
                ItemKeyboardBottomMediaVideoBinding videoBinding =
                    DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_keyboard_bottom_media_video, parent, false);
                return new VideoItemViewHolder(videoBinding);
            case 0:
            default:
                ItemKeyboardBottomMediaPhotoBinding photoBinding =
                    DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_keyboard_bottom_media_photo, parent, false);
                return new PhotoItemViewHolder(photoBinding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull IMediaItemViewHolder holder, int position) {
        AMediaBean bean = mediaBeans.get(position);
        bean.setPosition(position);
        holder.itemView.setTag(position);
        holder.themeStyle(this.themeStyle);
        holder.onBind(bean);
    }

    @Override
    public int getItemViewType(int position) {
        AMediaBean bean = mediaBeans.get(position);
        if (bean instanceof ImageBean) {
            return 0;
        } else if (bean instanceof FileBean) {
            return 1;
        } else if (bean instanceof VideoBean) {
            return 2;
        }
        return super.getItemViewType(position);
    }

    private void notifyImageChanged(ImageView imageView, AMediaBean mediaBean) {
        if (mediaBean.getThumbnailBitmap() != null) {
            //有取得到系統媒體庫縮略圖
            imageView.setImageBitmap(mediaBean.getThumbnailBitmap());
        } else {
            // 沒有取得到系統媒體庫縮略圖
            Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(getContentResolver(context), mediaBean.getId(), MediaStore.Images.Thumbnails.MINI_KIND, options);
            if (bitmap != null)
                imageView.setImageBitmap(bitmap);
        }
    }

    private void notifyVideoChanged(ImageView imageView, AMediaBean mediaBean) {
        if (mediaBean.getThumbnailBitmap() != null) {
            imageView.setImageBitmap(mediaBean.getThumbnailBitmap());
        } else {
            Bitmap bitmap = null;
            Uri videoUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, mediaBean.getId());

            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    // For Android 10 (API 29) and above
                    Size size = new Size(512, 384); // Equivalent to MINI_KIND
                    bitmap = getContentResolver(context).loadThumbnail(videoUri, size, null);
                } else {
                    // For older Android versions, fall back to the deprecated method
                    bitmap = MediaStore.Video.Thumbnails.getThumbnail(
                        getContentResolver(context),
                        mediaBean.getId(),
                        MediaStore.Video.Thumbnails.MINI_KIND,
                        options);
                }
            } catch (Exception ignored) {
            }
            if (bitmap != null)
                imageView.setImageBitmap(bitmap);
        }
    }

    private void notifyIconChanged(FileType fileType, ImageView imageView) {
        switch (fileType) {
            case TEXT:
                imageView.setImageResource(R.drawable.ic_file_icon_word_61dp);
                return;
            case PDF:
                imageView.setImageResource(R.drawable.ic_file_icon_pdf_61dp);
                return;
            case EXCEL:
                imageView.setImageResource(R.drawable.ic_file_icon_excel_61dp);
                return;
            case PPT:
            case AUDIO:
            case FILE:
            case WEB:
            case WORD:
            case NONE:
            default:
                imageView.setImageResource(R.drawable.ic_file_icon_other_61dp);
                return;
        }
    }

    private void nitifyCheckChanged(PhotoItemViewHolder viewHolder, AMediaBean mediaBean) {
        if (selectlist.contains(mediaBean)) {
            viewHolder.photoBinding.tvSelect.setSelected(true);
            viewHolder.photoBinding.tvSelect.setText(String.valueOf(mediaBean.getSelectPosition()));
            viewHolder.photoBinding.ivForgound.setVisibility(View.VISIBLE);
        } else {
            viewHolder.photoBinding.tvSelect.setSelected(false);
            viewHolder.photoBinding.tvSelect.setText("");
            viewHolder.photoBinding.ivForgound.setVisibility(View.GONE);
        }
    }

    private void notifyCheckChanged(VideoItemViewHolder viewHolder, AMediaBean mediaBean) {
        if (selectlist.contains(mediaBean)) {
            viewHolder.videoBinding.tvSelect.setSelected(true);
            viewHolder.videoBinding.tvSelect.setText(String.valueOf(mediaBean.getSelectPosition()));
            viewHolder.videoBinding.ivForgound.setVisibility(View.VISIBLE);
        } else {
            viewHolder.videoBinding.tvSelect.setSelected(false);
            viewHolder.videoBinding.tvSelect.setText("");
            viewHolder.videoBinding.ivForgound.setVisibility(View.GONE);
        }
    }

    private void nitifyCheckChanged(FileItemViewHolder viewHolder, AMediaBean mediaBean) {
        if (selectlist.contains(mediaBean)) {
            viewHolder.fileBinding.tvSelect.setSelected(true);
            viewHolder.fileBinding.tvSelect.setText(String.valueOf(mediaBean.getSelectPosition()));
            viewHolder.fileBinding.ivForgound.setVisibility(View.VISIBLE);
        } else {
            viewHolder.fileBinding.tvSelect.setSelected(false);
            viewHolder.fileBinding.tvSelect.setText("");
            viewHolder.fileBinding.ivForgound.setVisibility(View.GONE);
        }
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public int getMaxCount() {
        return this.maxCount;
    }


    private void setSelectOnClickListener(View view, final AMediaBean mediaBean, final int position) {
        view.setOnClickListener(v -> onSelected(v, mediaBean, position));
    }


    public void onSelected(View v, AMediaBean mediaBean, int position) {
        if (selectlist.contains(mediaBean)) {
            selectlist.remove(mediaBean);
            subSelectPosition();
        } else {
            if (selectlist.size() >= maxCount) {
                Toast.makeText(context, context.getString(R.string.chat_max_send_picture_or_video), Toast.LENGTH_SHORT).show();
                return;
            }
            selectlist.add(mediaBean);
            mediaBean.setSelectPosition(selectlist.size());

            if (mediaBean instanceof VideoBean) {
                if (!((VideoBean) mediaBean).name.endsWith("mp4")) {
                    selectlist.remove(mediaBean);
                    subSelectPosition();
                    ToastUtils.showToast(v.getContext(), v.getContext().getString(R.string.text_video_limit_mp4_format));
                }
            }
        }

        notifyItemChanged(position);

        if (onMediaSelectorListener != null) {
            onMediaSelectorListener.onSelected(selectlist);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void onChangeSelected(TreeMap<String, String> selectData) {
        selectlist.clear();
        for (Map.Entry<String, String> entry : selectData.entrySet()) {
            String key = entry.getKey();
            String path = entry.getValue();
            for (AMediaBean bean : mediaBeans) {
                if (path.equals(bean.getPath())) {
                    if (selectlist.size() >= maxCount) {
                        Toast.makeText(context, context.getString(R.string.chat_max_send_picture_or_video), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    selectlist.add(bean);
                    bean.setSelectPosition(selectlist.size());
                }
            }
        }
        notifyDataSetChanged();
        if (onMediaSelectorListener != null) {
            onMediaSelectorListener.onSelected(selectlist);
        }
    }


    public void unSelectedPic(View v, ImageBean imageBean, int position) {
        if (selectlist.contains(imageBean)) {
            selectlist.remove(imageBean);
            subSelectPosition();
        }

        notifyItemChanged(position);

    }


    private void subSelectPosition() {
        for (int index = 0, len = selectlist.size(); index < len; index++) {
            AMediaBean folderBean = selectlist.get(index);
            folderBean.setSelectPosition(index + 1);
            notifyItemChanged(folderBean.getPosition());
        }
    }

    @Override
    public int getItemCount() {
        return mediaBeans.size();
    }


    public MediaSelectorAdapter setData(List<AMediaBean> mediaBeans) {
        if (mediaBeans == null) {
            mediaBeans = Lists.newArrayList();
        }
        this.mediaBeans = mediaBeans;
        return this;
    }


    public MediaSelectorAdapter setThemeStyle(RoomThemeStyle themeStyle) {
        this.themeStyle = themeStyle;
        return this;
    }

    public MediaSelectorAdapter setOnMediaSelectorListener(OnMediaSelectorListener onMediaSelectorListener) {
        this.onMediaSelectorListener = onMediaSelectorListener;
        return this;
    }

    public MediaSelectorAdapter clearSelect() {
        this.selectlist.clear();
        return this;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refreshData() {
        ThreadExecutorHelper.getMainThreadExecutor().execute(this::notifyDataSetChanged);
    }

    public List<AMediaBean> getSelect() {
        return this.selectlist;
    }

    @SuppressLint("DefaultLocale")
    public TreeMap<String, String> getSelectToMap() {
        TreeMap<String, String> data = Maps.newTreeMap((o1, o2) -> ComparisonChain.start()
            .compare(o1, o2)
            .result());
        if (this.selectlist == null || this.selectlist.isEmpty()) {
            return data;
        }
        for (int i = 0; i < this.selectlist.size(); i++) {
            data.put(String.format("%09d", i), this.selectlist.get(i).getPath());
        }
        return data;
    }

    public AMediaBean getData(int position) {
        return this.mediaBeans.get(position);
    }

    public abstract static class IMediaItemViewHolder<T extends IMediaBean> extends RecyclerView.ViewHolder {

        public IMediaItemViewHolder(@NonNull View itemView) {
            super(itemView);
            int displayWidth = UiHelper.getDisplayWidth(itemView.getContext());
            int height = UiHelper.dip2px(itemView.getContext(), 125);
            // Set the ViewHolder width to be a third of the screen size, and height to wrap content
            itemView.setLayoutParams(new ConstraintLayout.LayoutParams(displayWidth / 4, height));
        }

        abstract void onBind(T t);

        abstract void themeStyle(RoomThemeStyle themeStyle);
    }


    public class PhotoItemViewHolder extends IMediaItemViewHolder<ImageBean> {
        ItemKeyboardBottomMediaPhotoBinding photoBinding;

        PhotoItemViewHolder(@NonNull ItemKeyboardBottomMediaPhotoBinding binding) {
            super(binding.getRoot());
            photoBinding = binding;
        }

        @Override
        void onBind(ImageBean imageBean) {
            notifyImageChanged(photoBinding.ivPic, imageBean);
            nitifyCheckChanged(this, imageBean);
            setSelectOnClickListener(photoBinding.tvSelect, imageBean, getAdapterPosition());
            itemView.setOnClickListener(v -> {
                if (onMediaSelectorListener != null) {
                    onMediaSelectorListener.toMediaSelectorPreview(imageBean, getSelectToMap(), getAbsoluteAdapterPosition());
                }
            });
        }

        @Override
        void themeStyle(RoomThemeStyle themeStyle) {
            @DrawableRes int resId;
            if (isGreenTheme) {
                if (themeStyle == RoomThemeStyle.SERVICES) {
                    resId = R.drawable.media_select_oval_selector_green2;
                } else {
                    resId = R.drawable.media_select_oval_selector_green1;
                }
            } else {
                if (themeStyle == RoomThemeStyle.SERVICES) {
                    resId = R.drawable.media_select_oval_selector_green2;
                } else {
                    resId = R.drawable.media_select_oval_selector;
                }
            }
            photoBinding.tvSelect.setBackgroundResource(resId);
        }
    }

    public class VideoItemViewHolder extends IMediaItemViewHolder<VideoBean> {
        ItemKeyboardBottomMediaVideoBinding videoBinding;

        VideoItemViewHolder(@NonNull ItemKeyboardBottomMediaVideoBinding binding) {
            super(binding.getRoot());
            videoBinding = binding;
        }

        @Override
        @SuppressLint("SetTextI18n")
        void onBind(VideoBean videoBean) {
            notifyVideoChanged(videoBinding.ivPic, videoBean);
            notifyCheckChanged(this, videoBean);
            String videoDuration = getVideoDuration(videoBean.getPath());
            if (Strings.isNullOrEmpty(videoDuration)) {
                videoBinding.tvVideoDuration.setText("00:00");
            } else {
                videoBinding.tvVideoDuration.setText(videoDuration);
            }
            //CELog.d("Kyle2 path="+videoBean.path);
            setSelectOnClickListener(videoBinding.tvSelect, videoBean, getAdapterPosition());
            itemView.setOnClickListener(v -> {
                if (onMediaSelectorListener != null) {
                    onMediaSelectorListener.toMediaSelectorPreview(videoBean, getSelectToMap(), getAbsoluteAdapterPosition());
                }
            });
        }

        /**
         * 若影片長度超過 1 小時, 格式為 h:mm:ss, e.g., 影片時長1小時3分06秒, 顯示為 1:03:06
         * 若影片長度少於 1 小時, 格式為 m:ss, e.g., 影片時長3分06秒, 顯示為 3:06
         * 若影片時長為5秒，則顯示為 0:05
         */
        private String getVideoDuration(String videoPath) {
            try {
                MediaPlayer mediaPlayer = MediaPlayer.create(context, Uri.parse(videoPath));
                int duration = mediaPlayer.getDuration(); // in milliseconds
                mediaPlayer.release();
                return milliSecondsToTimer(duration);
            } catch (Exception e) {
                // Handle Exception
                return "";
            }
        }

        public String milliSecondsToTimer(long milliseconds) {
            String finalTimerString = "";
            String secondsString = "";

            // Convert total duration into time
            int hours = (int) (milliseconds / (1000 * 60 * 60));
            int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
            int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
            // Add hours if there
            if (hours > 0) {
                if (minutes < 10) {
                    finalTimerString = hours + ":0";
                } else {
                    finalTimerString = hours + ":";
                }
            }

            // Prepending 0 to seconds if it is one digit
            if (seconds < 10) {
                secondsString = "0" + seconds;
            } else {
                secondsString = "" + seconds;
            }

            finalTimerString = finalTimerString + minutes + ":" + secondsString;

            // return timer string
            return finalTimerString;
        }

        @Override
        void themeStyle(RoomThemeStyle themeStyle) {
            @DrawableRes int resId;
            if (isGreenTheme) {
                if (themeStyle == RoomThemeStyle.SERVICES) {
                    resId = R.drawable.media_select_oval_selector_green2;
                } else {
                    resId = R.drawable.media_select_oval_selector_green1;
                }
            } else {
                if (themeStyle == RoomThemeStyle.SERVICES) {
                    resId = R.drawable.media_select_oval_selector_green2;
                } else {
                    resId = R.drawable.media_select_oval_selector;
                }
            }
            videoBinding.tvSelect.setBackgroundResource(resId);
        }
    }

    public class FileItemViewHolder extends IMediaItemViewHolder<FileBean> {
        ItemKeyboardBottomMediaFileBinding fileBinding;

        FileItemViewHolder(@NonNull ItemKeyboardBottomMediaFileBinding binding) {
            super(binding.getRoot());
            fileBinding = binding;
        }

        @Override
        void onBind(FileBean fileBean) {
            fileBinding.tvName.setText(fileBean.getName());
            String type = MultimediaHelper.getType(fileBean.getPath());
            FileType fileType = FileType.of(type);
            notifyIconChanged(fileType, fileBinding.ivPic);
            setSelectOnClickListener(fileBinding.getRoot(), fileBean, getAdapterPosition());
            nitifyCheckChanged(this, fileBean);
        }

        @Override
        void themeStyle(RoomThemeStyle themeStyle) {
            @DrawableRes int resId;
            if (isGreenTheme) {
                if (themeStyle == RoomThemeStyle.SERVICES) {
                    resId = R.drawable.media_select_oval_selector_green2;
                } else {
                    resId = R.drawable.media_select_oval_selector_green1;
                }
            } else {
                if (themeStyle == RoomThemeStyle.SERVICES) {
                    resId = R.drawable.media_select_oval_selector_green2;
                } else {
                    resId = R.drawable.media_select_oval_selector;
                }
            }
            fileBinding.tvSelect.setBackgroundResource(resId);
        }
    }


    public interface OnMediaSelectorListener {
        void onSelected(List<AMediaBean> selectList);

        void toMediaSelectorPreview(AMediaBean currenBean, TreeMap<String, String> data, int position);
    }
}
