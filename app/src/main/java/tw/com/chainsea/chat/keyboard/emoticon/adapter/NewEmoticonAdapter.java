package tw.com.chainsea.chat.keyboard.emoticon.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.bean.sticker.EmoticonType;
import tw.com.chainsea.ce.sdk.bean.sticker.StickerItemEntity;
import tw.com.chainsea.ce.sdk.bean.sticker.StickerPackageEntity;
import tw.com.chainsea.ce.sdk.event.EventBusUtils;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.ce.sdk.http.ce.request.StickerDownloadRequest;
import tw.com.chainsea.ce.sdk.service.StickerService;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ItemEmoticon2ViewBinding;
import tw.com.chainsea.chat.databinding.ItemEmoticonNoDataViewBinding;
import tw.com.chainsea.chat.keyboard.emoticon.NewEmoticonLayout;

/**
 * current by evan on 2020-10-05
 *
 * @author Evan Wang
 * date 2020-10-05
 */
public class NewEmoticonAdapter extends RecyclerView.Adapter<NewEmoticonAdapter.BaseEmoticonView> {
    Context context;
    private List<StickerItemEntity> itemEntities;
    private NewEmoticonLayout.OnEmoticonSelectListener<StickerItemEntity> onEmoticonSelectListener;
    public EmoticonType type;
    private final StickerPackageEntity packageEntity;
    private int fileSize;

    public NewEmoticonAdapter(EmoticonType type, int fileSize, StickerPackageEntity packageEntity) {
        this.type = type;
        this.fileSize = fileSize;
        this.packageEntity = packageEntity;
        this.itemEntities = packageEntity.getStickerItems().isEmpty() ? null : packageEntity.getStickerItems();
    }

//    public NewEmoticonAdapter(EmoticonType type, StickerPackageEntity packageEntity, NewEmoticonLayout.OnEmoticonSelectListener<StickerItemEntity> onEmoticonSelectListener) {
//        this.type = type;
//        this.packageEntity = packageEntity;
//        this.itemEntities = packageEntity.getStickerItems();
//        this.onEmoticonSelectListener = onEmoticonSelectListener;
//    }

    @NonNull
    @Override
    public BaseEmoticonView onCreateViewHolder(@NonNull ViewGroup parent, int itemType) {
        this.context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemEmoticon2ViewBinding itemEmoticon2ViewBinding = ItemEmoticon2ViewBinding.inflate(layoutInflater, parent, false);
        if (itemType == 0) {
            ItemEmoticonNoDataViewBinding itemEmoticonNoDataViewBinding = ItemEmoticonNoDataViewBinding.inflate(layoutInflater, parent, false);
            return new NoEmoticonDataViewHolder(itemEmoticonNoDataViewBinding);
        } else if (itemType == 1) {
            return new EmojiViewHolder(itemEmoticon2ViewBinding);
        } else {
            return new StickerViewHolder(itemEmoticon2ViewBinding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseEmoticonView holder, int position) {
        if (this.itemEntities != null) {
            holder.onBind(this.itemEntities.get(position), position);
        } else {
            holder.onBindNoDataBean(this.packageEntity);
        }
    }

    @Override
    public int getItemCount() {
        return this.itemEntities == null ? 1 : this.itemEntities.size();
    }


    /**
     * 0、貼圖未下載
     * 1、表情
     * 2、貼圖
     */
    @Override
    public int getItemViewType(int position) {
        if (EmoticonType.EMOJI.equals(this.packageEntity.getEmoticonType())) {
            return 1;
        } else {
            return fileSize == 0 || fileSize != packageEntity.getCount() ? 0 : 2;
        }
    }

    public void setOnEmoticonSelectListener(NewEmoticonLayout.OnEmoticonSelectListener<StickerItemEntity> onEmoticonSelectListener) {
        this.onEmoticonSelectListener = onEmoticonSelectListener;
    }

    public NewEmoticonAdapter setFileSize(int fileSize) {
        this.fileSize = fileSize;
        return this;
    }

    public NewEmoticonAdapter setData(List<StickerItemEntity> entities) {
        this.itemEntities = entities;
        return this;
    }

    public NewEmoticonAdapter clearData() {
        this.itemEntities = null;
        return this;
    }


    public List<StickerItemEntity> getData() {
        return this.itemEntities;
    }

    abstract static class BaseEmoticonView extends RecyclerView.ViewHolder {

        public BaseEmoticonView(@NonNull View itemView) {
            super(itemView);
        }

        public abstract void onBind(StickerItemEntity itemEntity, int position);

        public void onBindNoDataBean(StickerPackageEntity packageEntity) {

        }
    }

    static class NoEmoticonDataViewHolder extends BaseEmoticonView {

        private final ItemEmoticonNoDataViewBinding binding;

        public NoEmoticonDataViewHolder(@NonNull ItemEmoticonNoDataViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void onBind(StickerItemEntity itemEntity, int position) {
            CELog.e("");
        }


        @Override
        public void onBindNoDataBean(StickerPackageEntity packageEntity) {
            binding.btnDownload.setOnClickListener(v -> EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.DOWNLOAD_STICKER_RESOURCES_BY_PACKAGE_ID, packageEntity.getId())));
        }
    }


    class EmojiViewHolder extends BaseEmoticonView {

        private final ItemEmoticon2ViewBinding binding;

        public EmojiViewHolder(@NonNull ItemEmoticon2ViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void onBind(StickerItemEntity itemEntity, int position) {
            binding.tvEmoticon.setVisibility(View.GONE);
            binding.ivEmoticon.setImageBitmap(getEmoticonResources(context, itemEntity));
            itemView.setOnClickListener(v -> {
                if (onEmoticonSelectListener != null) {
                    onEmoticonSelectListener.onEmoticonSelect(itemEntity);
                }
            });
        }
    }


    class StickerViewHolder extends BaseEmoticonView {

        private final ItemEmoticon2ViewBinding binding;

        public StickerViewHolder(@NonNull ItemEmoticon2ViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void onBind(StickerItemEntity itemEntity, int position) {
            binding.tvEmoticon.setText(itemEntity.getDisplayName());
            binding.tvEmoticon.setVisibility(View.VISIBLE);
            StickerService.postSticker(context, itemEntity.getStickerPackageId(), itemEntity.getId(), StickerDownloadRequest.Type.THUMBNAIL_PICTURE, new ServiceCallBack<>() {
                @Override
                public void complete(Drawable drawable, RefreshSource source) {
                    binding.ivEmoticon.setImageDrawable(drawable);
                }

                @Override
                public void error(String message) {
                    CELog.e(message);
                }
            });
            itemView.setOnClickListener(v -> {
                if (onEmoticonSelectListener != null) {
                    onEmoticonSelectListener.onStickerSelect(itemEntity, binding.ivEmoticon.getDrawable());
                }
            });
        }
    }

    @SuppressLint("DiscouragedApi")
    public Bitmap getEmoticonResources(Context context, StickerItemEntity entity) {
        String strRes = entity.getThumbnailPictureUrl();
        if (strRes.toLowerCase(Locale.US).startsWith("drawable://")) {
            int resID = context.getResources().getIdentifier(strRes.replace("drawable://", "").replace(".png", ""), "drawable", context.getPackageName());
            if (resID > 0) {
                return BitmapFactory.decodeResource(context.getResources(), resID);
            }
        }

        if (strRes.toLowerCase(Locale.US).startsWith("assets://")) {
            try {
                return BitmapFactory.decodeStream(context.getAssets().open(strRes.replace("assets://", "")));
            } catch (Exception ignored) {

            }
        }

        return BitmapFactory.decodeResource(context.getResources(), R.drawable.custom_default_avatar);
    }

}
